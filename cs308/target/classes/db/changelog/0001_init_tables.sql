CREATE TABLE role (
                      roleId INT PRIMARY KEY,
                      roleName VARCHAR(20) UNIQUE
);

CREATE TABLE users (
                       userId BIGINT PRIMARY KEY,
                       firstName VARCHAR(50),
                       lastName VARCHAR(50),
                       password VARCHAR(255),
                       email VARCHAR(100) UNIQUE,
                       phoneNumber VARCHAR(15)
);

CREATE TABLE category (
                          categoryId INT PRIMARY KEY,
                          name VARCHAR(100)
);

CREATE TABLE discount (
                          discountId INT PRIMARY KEY,
                          discountValue DECIMAL(5, 2),
                          expiryDate DATE,
                          isActive BOOLEAN,
                          discountCode VARCHAR(50) UNIQUE,
                          startDate DATE
);

CREATE TABLE shipping (
                          shippingId BIGINT PRIMARY KEY,
                          shippingAddress TEXT,
                          shippingStatus VARCHAR(20)
);

CREATE TABLE product (
                         productId BIGINT PRIMARY KEY,
                         productName VARCHAR(100),
                         productDescription TEXT,
                         productPrice DECIMAL(10, 2),
                         productQuantity INT,
                         productStock INT,
                         productModel VARCHAR(50),
                         serialNumber VARCHAR(50),
                         productWarranty INT,
                         distributor VARCHAR(100),
                         categoryId INT,
                         discountId INT,
                         FOREIGN KEY (categoryId) REFERENCES category(categoryId),
                         FOREIGN KEY (discountId) REFERENCES discount(discountId)
);

CREATE TABLE admin (
                       adminId BIGINT PRIMARY KEY,
                       username VARCHAR(50) UNIQUE,
                       password VARCHAR(255),
                       roleId INT,
                       FOREIGN KEY (roleId) REFERENCES role(roleId)
);


CREATE TABLE orders (
                        orderId BIGINT PRIMARY KEY,
                        orderStatus VARCHAR(20),
                        userId BIGINT,
                        totalPrice DECIMAL(10, 2),
                        shippingId BIGINT,
                        FOREIGN KEY (userId) REFERENCES users(userId) ON DELETE CASCADE,
                        FOREIGN KEY (shippingId) REFERENCES shipping(shippingId) ON DELETE SET NULL
);

CREATE TABLE payment (
                         paymentId BIGINT PRIMARY KEY,
                         paymentType VARCHAR(20),
                         paymentStatus VARCHAR(20),
                         paymentRefund VARCHAR(20),
                         orderId BIGINT,
                         paymentDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         FOREIGN KEY (orderId) REFERENCES orders(orderId) ON DELETE CASCADE
);



CREATE TABLE review (
                        reviewId INT PRIMARY KEY,
                        rating INT,
                        comment TEXT,
                        adminApproval BOOLEAN,
                        userId BIGINT,
                        productId BIGINT,
                        FOREIGN KEY (userId) REFERENCES users(userId) ON DELETE CASCADE,
                        FOREIGN KEY (productId) REFERENCES product(productId) ON DELETE CASCADE
);

CREATE TABLE wishlist (
                          wishlistId INT PRIMARY KEY,
                          userId BIGINT,
                          productId BIGINT,
                          FOREIGN KEY (userId) REFERENCES users(userId) ON DELETE CASCADE,
                          FOREIGN KEY (productId) REFERENCES product(productId) ON DELETE CASCADE
);

CREATE TABLE shoppingCart (
                              cartId BIGINT PRIMARY KEY,
                              userId BIGINT,
                              productId BIGINT,
                              totalPrice DECIMAL(10, 2),
                              FOREIGN KEY (userId) REFERENCES users(userId) ON DELETE CASCADE,
                              FOREIGN KEY (productId) REFERENCES product(productId) ON DELETE CASCADE
);




CREATE TABLE refund (
                        refundId BIGINT PRIMARY KEY,
                        userId BIGINT,
                        productId BIGINT,
                        amount DECIMAL(10, 2),
                        FOREIGN KEY (userId) REFERENCES users(userId) ON DELETE CASCADE,
                        FOREIGN KEY (productId) REFERENCES product(productId) ON DELETE CASCADE
);
