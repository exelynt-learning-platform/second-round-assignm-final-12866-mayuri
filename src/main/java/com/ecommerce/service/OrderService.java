package com.ecommerce.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ecommerce.dto.OrderDTO;
import com.ecommerce.dto.CartItemDTO;
import com.ecommerce.entity.*;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.exception.ValidationException;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartService cartService;

    public OrderDTO createOrder(Long userId, OrderDTO orderDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Cart cart = cartService.getCartByUserId(userId);

        if (cart.getItems() == null || cart.getItems().isEmpty()){
            throw new ValidationException("Cart is empty");
        }

        Order order = Order.builder()
                .user(user)
                .status(Order.OrderStatus.PENDING)
                .shippingAddress(orderDTO.getShippingAddress())
                .shippingCity(orderDTO.getShippingCity())
                .shippingPostalCode(orderDTO.getShippingPostalCode())
                .shippingCountry(orderDTO.getShippingCountry())
                .build();

        // Safety initialization
        if (order.getItems() == null) {
            order.setItems(new ArrayList<>());
        }
        if (order.getProducts() == null) {
            order.setProducts(new HashSet<>());
        }

        BigDecimal totalPrice = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();

            // Reduce stock
            if (!product.hasStock(cartItem.getQuantity())) {
                throw new ValidationException("Insufficient stock for product: " + product.getName());
            }
            product.reduceStock(cartItem.getQuantity());


            // Create order item
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(cartItem.getQuantity())
                    .priceAtPurchase(product.getPrice())
                    .build();
            order.getItems().add(orderItem);
            order.getProducts().add(product);

            totalPrice = totalPrice.add(cartItem.getTotalPrice());
        }

        order.setTotalPrice(totalPrice);
        order = orderRepository.save(order);

        // Clear cart
        cartService.clearCart(userId);

        return mapToDTO(order);
    }

    public OrderDTO getOrderById(Long orderId, Long userId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        return mapToDTO(order);
    }

    public List<OrderDTO> getUserOrders(Long userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public void updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        try {
            order.setStatus(Order.OrderStatus.valueOf(status.toUpperCase()));
            orderRepository.save(order);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid order status: " + status);
        }
    }

    private OrderDTO mapToDTO(Order order) {
        return new OrderDTO(
                order.getId(),
                order.getItems().stream()
                        .map(item -> new CartItemDTO(
                                item.getId(),
                                item.getProduct().getId(),
                                item.getQuantity(),
                                item.getProduct().getName(),
                                item.getProduct().getPrice(),
                                item.getTotalPrice()
                        ))
                        .collect(Collectors.toList()),
                order.getTotalPrice(),
                order.getStatus().toString(),
                order.getShippingAddress(),
                order.getShippingCity(),
                order.getShippingPostalCode(),
                order.getShippingCountry(),
                order.getCreatedAt()
        );
    }
}