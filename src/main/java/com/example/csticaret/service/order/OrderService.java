package com.example.csticaret.service.order;

import com.example.csticaret.enums.OrderStatus;
import com.example.csticaret.enums.RefundStatus;
import com.example.csticaret.exceptions.ResourceNotFoundException;
import com.example.csticaret.model.*;
import com.example.csticaret.repository.OrderItemRepository;
import com.example.csticaret.repository.OrderRepository;
import com.example.csticaret.repository.ProductRepository;
import com.example.csticaret.request.PaymentRequest;
import com.example.csticaret.request.ShippingDetailsRequest;
import com.example.csticaret.service.cart.ICartService;
import com.example.csticaret.service.email.EmailService;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.annotation.PostConstruct;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderService implements IOrderService {
    private final OrderRepository orderRepository;
    private final ICartService cartService;
    private final ProductRepository productRepository;
    private final EmailService emailService;
    private static final String INVOICE_DIR = "invoices";
    private final OrderItemRepository orderItemRepository;

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Path.of(INVOICE_DIR));
            log.info("Invoice directory created/verified at: {}", INVOICE_DIR);
        } catch (IOException e) {
            log.error("Failed to create invoice directory", e);
        }
    }


    @Override
    @Transactional
    public Order createOrderFromCart(Cart cart, ShippingDetailsRequest shippingDetails) {
        // Create new order
        Order order = new Order();
        order.setUser(cart.getUser());
        order.setOrderDate(LocalDateTime.now());
        order.setOrderStatus(OrderStatus.PENDING);
        order.setTotalAmount(cart.getTotalAmount());
        order.setShippingAddress(shippingDetails.getAddress());
        order.setShippingEmail(shippingDetails.getEmail());
        order.setShippingPhone(shippingDetails.getPhone());
        
        // Save the order first to get an ID
        order = orderRepository.save(order);
        
        // Create and add order items
        final Order savedOrder = order; // Create final reference for lambda
        cart.getItems().forEach(cartItem -> {
            OrderItem orderItem = new OrderItem(
                savedOrder,
                cartItem.getProduct(),
                cartItem.getQuantity(),
                cartItem.getUnitPrice()
            );
            savedOrder.addItem(orderItem);
        });

        // Save again with items
        return orderRepository.save(savedOrder);
    }
    @Transactional
    public OrderItem requestRefund(Long orderId, Long itemId, Long userId) {
        // Siparişi bul
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        // Siparişin kullanıcıya ait olduğunu kontrol et
        if (!order.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("You do not have permission to request a refund for this order");
        }

        // İlgili sipariş kalemini bul
        OrderItem orderItem = order.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Order item not found with id: " + itemId));

        // İade durumu kontrolü
        if (orderItem.getRefundStatus() != RefundStatus.NONE) {
            throw new IllegalStateException("Refund has already been requested for this item");
        }

        // Refund talebini işaretle
        orderItem.setRefundStatus(RefundStatus.REQUESTED);
        return orderItemRepository.save(orderItem);
    }
    @Transactional
    public OrderItem approveRefund(Long orderId, Long itemId, boolean approved) {
        // OrderItem'ı bul
        OrderItem orderItem = orderItemRepository.findByOrderIdAndId(orderId, itemId)
                .orElseThrow(() -> new IllegalArgumentException("Order item not found"));

        // Refund durumunu kontrol et
        if (orderItem.getRefundStatus() != RefundStatus.REQUESTED) {
            throw new IllegalStateException("Refund is not in a requested state");
        }

        // Refund durumunu güncelle
        orderItem.setRefundStatus(approved ? RefundStatus.APPROVED : RefundStatus.REJECTED);

        // İlgili işlemleri yap (örneğin, geri ödeme sürecini başlat)
        if (approved) {
            processRefund(orderItem);
        }

        return orderItemRepository.save(orderItem);
    }
    private void processRefund(OrderItem orderItem) {
        // Stok iadesi yapılabilir
        Product product = orderItem.getProduct();
        product.setInventory(product.getInventory() + orderItem.getQuantity());
        productRepository.save(product);

        // Kullanıcıya bilgilendirme e-postası gönderilebilir
        String email = orderItem.getOrder().getUser().getEmail();
        String message = "Your refund request for product " + product.getName() + " has been approved.";

    }





    @Override
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
    }

    @Override
    public Order processPayment(Long orderId) {
        Order order = getOrderById(orderId);
        order.setOrderStatus(OrderStatus.PROCESSING);
        return orderRepository.save(order);
    }

    @Override
    public String generateInvoicePdf(Order order) {
        try {
            Files.createDirectories(Path.of(INVOICE_DIR));
            String fileName = String.format("%s/invoice_%d.pdf", INVOICE_DIR, order.getId());
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(fileName));

            document.open();
            addInvoiceHeader(document, order);
            addInvoiceItems(document, order);
            addInvoiceTotal(document, order);
            document.close();

            return fileName;
        } catch (DocumentException | IOException e) {
            throw new RuntimeException("Failed to generate invoice", e);
        }
    }

    @Override
    public Resource getInvoicePdf(Order order) {
        String fileName = String.format("%s/invoice_%d.pdf", INVOICE_DIR, order.getId());
        Resource resource = new FileSystemResource(fileName);
        
        if (!resource.exists()) {
            generateInvoicePdf(order);
            resource = new FileSystemResource(fileName);
        }
        
        return resource;
    }

    private void addInvoiceHeader(Document document, Order order) throws DocumentException {
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
        Font normalFont = new Font(Font.FontFamily.HELVETICA, 12);

        Paragraph title = new Paragraph("INVOICE", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(Chunk.NEWLINE);

        document.add(new Paragraph("Order #: " + order.getId(), normalFont));
        document.add(new Paragraph("Date: " + order.getOrderDate().format(DateTimeFormatter.ISO_DATE), normalFont));
        document.add(new Paragraph("Customer: " + order.getUser().getFirstName() + " " + order.getUser().getLastName(), normalFont));
        document.add(Chunk.NEWLINE);
    }

    private void addInvoiceItems(Document document, Order order) throws DocumentException {
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{2, 1, 1, 1});

        // Add header row
        Stream.of("Product", "Quantity", "Price", "Total")
            .forEach(columnTitle -> {
                PdfPCell header = new PdfPCell();
                header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                header.setBorderWidth(2);
                header.setPhrase(new Phrase(columnTitle));
                table.addCell(header);
            });

        // Add items
        for (OrderItem item : order.getItems()) {
            table.addCell(item.getProduct().getName());
            table.addCell(String.valueOf(item.getQuantity()));
            table.addCell(String.format("$%.2f", item.getPrice()));
            table.addCell(String.format("$%.2f", item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))));
        }

        document.add(table);
    }
    @Transactional
    public Order cancelOrder(Long orderId, Long userId) {
        // Siparişi al
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        // Kullanıcı doğrulama
        if (!order.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("You do not have permission to cancel this order");
        }

        // Durum kontrolü
        if (order.getOrderStatus() == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Order cannot be cancelled as it is already delivered");
        }

        // Siparişi iptal et
        order.setOrderStatus(OrderStatus.CANCELLED);
        return orderRepository.save(order);
    }


    private void addInvoiceTotal(Document document, Order order) throws DocumentException {
        document.add(Chunk.NEWLINE);
        Paragraph total = new Paragraph(
            String.format("Total Amount: $%.2f", order.getTotalAmount()),
            new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD)
        );
        total.setAlignment(Element.ALIGN_RIGHT);
        document.add(total);
    }



    public List<Order> getUserOrders(Long userId) {
        List<Order> orders = orderRepository.findByUserId(userId);
        // Sort by order date descending
        orders.sort((o1, o2) -> o2.getOrderDate().compareTo(o1.getOrderDate()));
        return orders;
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public Order updateOrderStatus(Long id, OrderStatus status) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        
        order.setOrderStatus(status);
        
        // If order is cancelled, restore product inventory
        if (status == OrderStatus.CANCELLED) {
            restoreInventory(order);
        }
        
        return orderRepository.save(order);
    }

    private void restoreInventory(Order order) {
        order.getItems().forEach(item -> {
            Product product = item.getProduct();
            product.setInventory(product.getInventory() + item.getQuantity());
            productRepository.save(product);
        });
    }
}

