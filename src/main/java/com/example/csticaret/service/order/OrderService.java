package com.example.csticaret.service.order;

import com.example.csticaret.enums.OrderStatus;
import com.example.csticaret.exceptions.ResourceNotFoundException;
import com.example.csticaret.model.*;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
        Order order = new Order();
        order.setUser(cart.getUser());
        order.setOrderDate(LocalDate.now());
        order.setTotalAmount(cart.getTotalAmount());
        order.setOrderStatus(OrderStatus.PENDING);

        // Set shipping details
        order.setShippingAddress(String.format("%s\n%s\n%s, %s %s\n%s",
                shippingDetails.getAddress(),
                shippingDetails.getCity(),
                shippingDetails.getState(),
                shippingDetails.getZipCode(),
                shippingDetails.getCountry(),
                shippingDetails.getPhone()
        ));

        // Process each cart item: convert to order item and update stock
        cart.getItems().forEach(cartItem -> {
            Product product = cartItem.getProduct();

            // Decrease stock
            product.decreaseStock(cartItem.getQuantity());
            productRepository.save(product); // Save updated stock to the database

            // Convert cart item to order item
            OrderItem orderItem = new OrderItem(
                    order,
                    product,
                    cartItem.getQuantity(),
                    cartItem.getUnitPrice()
            );
            order.getOrderItems().add(orderItem);
        });

        // Save the order
        Order savedOrder = orderRepository.save(order);

        // Generate invoice and send email
        try {
            log.info("Attempting to generate invoice and send email for order: {}", savedOrder.getOrderId());
            String invoicePath = generateInvoicePdf(savedOrder);
            log.info("Invoice generated at path: {}", invoicePath);
            
            emailService.sendOrderConfirmation(
                shippingDetails.getEmail(),
                savedOrder,
                invoicePath
            );
            log.info("Email sent successfully for order: {}", savedOrder.getOrderId());
        } catch (Exception e) {
            log.error("Failed to send order confirmation email for order: {}", savedOrder.getOrderId(), e);
        }

        // Clear the cart after successful order creation
        cartService.clearCart(cart.getId());

        return savedOrder;
    }


    @Override
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
    }

    @Override
    public Order processPayment(Long orderId) {
        Order order = getOrderById(orderId);
        order.setOrderStatus(OrderStatus.DELIVERED);
        return orderRepository.save(order);
    }

    @Override
    public String generateInvoicePdf(Order order) {
        try {
            Files.createDirectories(Path.of(INVOICE_DIR));
            String fileName = String.format("%s/invoice_%d.pdf", INVOICE_DIR, order.getOrderId());
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
        String fileName = String.format("%s/invoice_%d.pdf", INVOICE_DIR, order.getOrderId());
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

        document.add(new Paragraph("Order #: " + order.getOrderId(), normalFont));
        document.add(new Paragraph("Date: " + order.getOrderDate().format(DateTimeFormatter.ISO_DATE), normalFont));
        document.add(new Paragraph("Customer: " + order.getUser().getFirstName() + " " + order.getUser().getLastName(), normalFont));
        document.add(Chunk.NEWLINE);
    }

    private void addInvoiceItems(Document document, Order order) throws DocumentException {
        PdfPTable table = new PdfPTable(4); // 4 columns
        table.setWidthPercentage(100);
        table.setWidths(new float[]{2, 1, 1, 1}); // Relative column widths

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
        for (OrderItem item : order.getOrderItems()) {
            table.addCell(item.getProduct().getName());
            table.addCell(String.valueOf(item.getQuantity()));
            table.addCell(String.format("$%.2f", item.getPrice()));
            table.addCell(String.format("$%.2f", item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))));
        }

        document.add(table);
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
        return orderRepository.findByUserIdOrderByOrderDateDesc(userId);
    }
}
