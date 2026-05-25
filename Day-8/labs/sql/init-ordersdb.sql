-- Day 8 Lab: PostgreSQL setup for JDBC source connector
CREATE DATABASE ordersdb;

\c ordersdb

CREATE TABLE IF NOT EXISTS orders (
    id SERIAL PRIMARY KEY,
    customer_id INT NOT NULL,
    order_total NUMERIC(10, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS customers (
    id SERIAL PRIMARY KEY,
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

-- Analytics DB for JDBC sink lab
CREATE DATABASE analytics;
