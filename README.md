# 🛒 E-Commerce Backend (Spring Boot)

## 📌 Project Overview
This is a backend system for an e-commerce platform built using Spring Boot. It supports user authentication, product management, cart, order processing, and payment integration using Stripe.

---

## 🚀 Features

### 🔐 Authentication
- User Registration & Login
- JWT-based authentication
- Role-based authorization

### 📦 Product Management
- Create, Update, Delete products (Admin)
- View all products

### 🛒 Cart Management
- Add items to cart
- Update quantity
- Remove items
- View cart

### 📦 Order Management
- Checkout cart → Order
- Manage shipping details
- View user orders

### 💳 Payment Integration
- Stripe Payment Intent
- Payment Success & Failure handling

---

## 🛠️ Tech Stack

- Java 17
- Spring Boot
- Spring Security (JWT)
- Spring Data JPA
- MySQL
- Stripe API

---

## 🔗 API Endpoints

### Auth
- POST /api/v1/auth/register
- POST /api/v1/auth/login

### Products
- GET /api/v1/products
- POST /api/v1/products
- PUT /api/v1/products/{id}
- DELETE /api/v1/products/{id}

### Cart
- POST /api/v1/cart/add
- GET /api/v1/cart
- PUT /api/v1/cart/update
- DELETE /api/v1/cart/remove/{id}

### Orders
- POST /api/v1/orders
- GET /api/v1/orders
- GET /api/v1/orders/{id}

### Payments
- POST /api/v1/payments/intent
- POST /api/v1/payments/success/{paymentIntentId}
- POST /api/v1/payments/failure/{paymentIntentId}

---

## 🧪 Testing

Use Postman to test APIs:
- Add JWT token in Authorization header
- Follow flow: Auth → Cart → Order → Payment

---

## ⚙️ Setup Instructions

1. Clone the repository
2. Configure MySQL in application.yml
3. Add Stripe API key
4. Run the project

---

## 👩‍💻 Author
Mayuri Gore