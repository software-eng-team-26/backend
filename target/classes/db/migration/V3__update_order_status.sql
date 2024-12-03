-- Update any PAID orders to DELIVERED for digital products
UPDATE orders SET order_status = 'DELIVERED' WHERE order_status = 'PAID';

-- Update any IN_TRANSIT orders to PROCESSING
UPDATE orders SET order_status = 'PROCESSING' WHERE order_status = 'IN_TRANSIT';

-- Update any null or invalid statuses to PENDING
UPDATE orders SET order_status = 'PENDING' WHERE order_status IS NULL 
    OR order_status NOT IN ('PENDING', 'PAID', 'PROCESSING', 'PROVISIONING', 'DELIVERED', 'CANCELLED', 'REFUNDED', 'FAILED'); 