-- Truck Booking Management System Database Schema
-- MySQL 8.0+

CREATE DATABASE IF NOT EXISTS truck_booking_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE truck_booking_db;

-- Users table (admin and customer accounts)
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'CUSTOMER') NOT NULL DEFAULT 'CUSTOMER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Customers table
CREATE TABLE IF NOT EXISTS customers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(100) NOT NULL,
    user_id BIGINT UNIQUE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- Trucks table
CREATE TABLE IF NOT EXISTS trucks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    truck_number VARCHAR(50) NOT NULL,
    registration VARCHAR(20) NOT NULL UNIQUE,
    capacity VARCHAR(50) NOT NULL,
    description TEXT,
    status ENUM('AVAILABLE', 'BOOKED', 'MAINTENANCE') NOT NULL DEFAULT 'AVAILABLE',
    image VARCHAR(500)
);

-- Bookings table
CREATE TABLE IF NOT EXISTS bookings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    truck_id BIGINT NOT NULL,
    pickup_date DATE NOT NULL,
    return_date DATE NOT NULL,
    pickup_address VARCHAR(500) NOT NULL,
    delivery_address VARCHAR(500) NOT NULL,
    cargo_description TEXT,
    weight DECIMAL(10,2),
    special_instructions TEXT,
    status ENUM('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED', 'COMPLETED') NOT NULL DEFAULT 'PENDING',
    total_amount DECIMAL(12,2),
    booking_reference VARCHAR(20) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(id),
    FOREIGN KEY (truck_id) REFERENCES trucks(id),
    INDEX idx_booking_dates (pickup_date, return_date),
    INDEX idx_booking_status (status),
    INDEX idx_booking_reference (booking_reference)
);

-- Seed default admin (password: admin123 - BCrypt hashed)
-- Note: Spring Boot DataInitializer handles seeding on first run

-- Seed trucks
INSERT INTO trucks (truck_number, registration, capacity, description, status, image) VALUES
('Truck 1', 'CA 123-456 GP', '34 Tonnes', 'Mercedes-Benz Actros Code 14 — ideal for long-haul freight and heavy cargo transport.', 'AVAILABLE', 'images/truck1.jpg'),
('Truck 2', 'CA 789-012 GP', '34 Tonnes', 'Volvo FH16 Code 14 — reliable heavy-duty truck for construction materials and bulk deliveries.', 'AVAILABLE', 'images/truck2.jpg')
ON DUPLICATE KEY UPDATE truck_number = truck_number;
