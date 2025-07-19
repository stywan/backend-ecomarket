package com.ecomarket.backend.cart_order.faker;

import com.ecomarket.backend.cart_order.DTO.request.OrderItemRequestDTO;
import com.ecomarket.backend.cart_order.DTO.request.OrderRequestDTO;
import com.ecomarket.backend.cart_order.service.OrderService;
import net.datafaker.Faker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@Slf4j
@Component
@Profile({"test"})
@RequiredArgsConstructor
public class DataFakerConfig implements CommandLineRunner {

    private final OrderService orderService;
    private final Faker faker = new Faker();

    private static final int MIN_USER_ID = 1;
    private static final int MAX_USER_ID = 10;

    private static final int MIN_PRODUCT_ID = 1;
    private static final int MAX_PRODUCT_ID = 10;

    @Override
    public void run(String... args) {
        log.info("--- Starting fake order data generation ---");

        log.warn("This OrderDataSeeder assumes User and Product services have already seeded their databases with IDs from {} to {} (users) and {} to {} (products).",
                MIN_USER_ID, MAX_USER_ID, MIN_PRODUCT_ID, MAX_PRODUCT_ID);

        int numberOfOrdersToGenerate = 10;

        for (int i = 0; i < numberOfOrdersToGenerate; i++) {
            try {
                Long fakeUserId = (long) faker.number().numberBetween(MIN_USER_ID, MAX_USER_ID + 1);

                // Generar ítems de orden falsos
                List<OrderItemRequestDTO> items = new ArrayList<>();
                int numItemsPerOrder = faker.number().numberBetween(1, 4);

                for (int j = 0; j < numItemsPerOrder; j++) {
                    Long fakeProductId = (long) faker.number().numberBetween(MIN_PRODUCT_ID, MAX_PRODUCT_ID + 1);
                    int quantity = faker.number().numberBetween(1, 5);

                    items.add(OrderItemRequestDTO.builder()
                            .productId(fakeProductId)
                            .quantity(quantity)
                            .build());
                }

                OrderRequestDTO orderRequest = OrderRequestDTO.builder()
                        .userId(fakeUserId)
                        .items(items)
                        .build();

                orderService.createOrder(orderRequest);
                log.info("Generated fake order #{} for user {}", (i + 1), fakeUserId);

            } catch (Exception e) {
                log.error("Error generating fake order {}: {}", (i + 1), e.getMessage(), e);
            }
        }
        log.info("--- Fake order data generation completed ---");
    }
}
