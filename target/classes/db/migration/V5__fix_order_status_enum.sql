-- First, update any PAID orders to DELIVERED (since they're digital products)
UPDATE orders SET order_status = 'DELIVERED' WHERE order_status = 'PAID';

-- Update any IN_TRANSIT orders to PROCESSING
UPDATE orders SET order_status = 'PROCESSING' WHERE order_status = 'IN_TRANSIT';

-- Update any CANCELLED orders to PENDING
UPDATE orders SET order_status = 'PENDING' WHERE order_status = 'CANCELLED';

-- Update any REFUNDED orders to PENDING
UPDATE orders SET order_status = 'PENDING' WHERE order_status = 'REFUNDED';

-- Update any FAILED orders to PENDING
UPDATE orders SET order_status = 'PENDING' WHERE order_status = 'FAILED';

-- Set any remaining invalid or null statuses to PENDING
UPDATE orders SET order_status = 'PENDING' 
WHERE order_status IS NULL 
   OR order_status NOT IN ('PENDING', 'PROCESSING', 'PROVISIONING', 'DELIVERED'); 