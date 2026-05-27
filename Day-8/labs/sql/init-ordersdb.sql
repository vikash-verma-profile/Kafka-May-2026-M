-- Day 8 Lab: MySQL setup for JDBC source connector
-- Run: mysql -u root -p < init-ordersdb.sql

CREATE DATABASE IF NOT EXISTS ordersdb;
USE ordersdb;

CREATE TABLE IF NOT EXISTS orders (
    id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    order_total DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS customers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(120)
);

INSERT INTO orders (customer_id, order_total) VALUES
    (1, 99.50),
    (2, 250.00),
    (1, 1200.00);

INSERT INTO customers (name, email) VALUES
    ('Alice', 'alice@example.com'),
    ('Bob', 'bob@example.com');

CREATE DATABASE IF NOT EXISTS analytics;
