package com.ecommerce.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CartService cartService;

    @InjectMocks
    private OrderService orderService;

    private User user;
    private Product product;
    private Cart cart;
    private CartItem cartItem;
    private OrderDTO orderDTO;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        user = User.builder()
                .id(1L)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .build();

        product = Product.builder()
                .id(1L)
                .name("Test Product")
                .price(new BigDecimal("99.99"))
                .stockQuantity(10)
                .isActive(true)
                .build();

        cartItem = CartItem.builder()
                .id(1L)
                .product(product)
                .quantity(2)
                .build();

        cart = Cart.builder()
                .id(1L)
                .user(user)
                .items(new ArrayList<>())
                .build();
        cart.getItems().add(cartItem);

        orderDTO = new OrderDTO();
        orderDTO.setShippingAddress("123 Main St");
        orderDTO.setShippingCity("New York");
        orderDTO.setShippingPostalCode("10001");
        orderDTO.setShippingCountry("USA");
    }

    @Test
    public void testCreateOrderSuccess() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartService.getCartByUserId(1L)).thenReturn(cart);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(1L);
            return order;
        });

        OrderDTO result = orderService.createOrder(1L, orderDTO);

        assertNotNull(result);
        assertEquals(new BigDecimal("199.98"), result.getTotalPrice());
        assertEquals(8, product.getStockQuantity());
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(cartService, times(1)).clearCart(1L);
    }

    @Test
    public void testCreateOrderEmptyCart() {
        cart.getItems().clear();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartService.getCartByUserId(1L)).thenReturn(cart);

        assertThrows(ValidationException.class, () -> orderService.createOrder(1L, orderDTO));
    }

    @Test
    public void testGetOrderByIdSuccess() {
        Order order = Order.builder()
                .id(1L)
                .user(user)
                .status(Order.OrderStatus.PENDING)
                .totalPrice(new BigDecimal("199.98"))
                .build();

        when(orderRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(order));

        OrderDTO result = orderService.getOrderById(1L, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("PENDING", result.getStatus());
    }

    @Test
    public void testGetOrderByIdNotFound() {
        when(orderRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.getOrderById(1L, 1L));
    }
}