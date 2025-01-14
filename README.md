# Backend - Online Learning Platform

## Overview

The backend of the Edumart platform is a Spring Boot application designed to handle the server-side logic of an online learning platform. It provides RESTful APIs for managing courses, orders, users, and more.

## Features

- User authentication and authorization
- Product management
- Order processing
- Wishlist functionality
- Cart management
- Image upload and management
- Email notifications

## Technologies Used

- Java 17
- Spring Boot 3.3.2
- Hibernate/JPA
- MySQL
- Docker
- Maven

## Getting Started

### Prerequisites

- Java 17
- Maven
- Docker
- MySQL

### Installation

1. **Clone the repository:**

   ```bash
   git clone https://github.com/yourusername/dream-shops-backend.git
   cd dream-shops-backend
   ```

2. **Set up the database:**

   Ensure MySQL is running and create a database named `e-commerce`. Update the `application.properties` file with your database credentials.

3. **Build the application:**

   ```bash
   mvn clean package
   ```

4. **Run the application:**

   ```bash
   docker-compose up
   ```

   This will start the application along with a MySQL database in Docker containers.

## API Documentation

The API documentation is available at `/swagger-ui.html` once the application is running.


## Contributing

Contributions are welcome!

## License

This project is licensed under the MIT License. 