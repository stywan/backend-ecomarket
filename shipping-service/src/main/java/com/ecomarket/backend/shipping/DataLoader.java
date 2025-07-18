package com.ecomarket.backend.shipping;

import com.ecomarket.backend.shipping.DTO.request.ShipmentRequestDTO;
import com.ecomarket.backend.shipping.DTO.response.OrderResponseDTO;
import com.ecomarket.backend.shipping.client.OrderServiceClient;
import com.ecomarket.backend.shipping.model.Supplier;
import com.ecomarket.backend.shipping.repository.SupplierRepository;
import com.ecomarket.backend.shipping.service.ShipmentService;
import net.datafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Component
@Profile("test")
public class DataLoader implements CommandLineRunner {

    private final SupplierRepository supplierRepository;
    private final ShipmentService shipmentService;
    private final OrderServiceClient orderServiceClient;
    private final Faker faker;

    private static final List<Long> ORDER_IDS_TO_CONSUME = Arrays.asList(1L, 2L, 3L, 4L, 5L);

    public DataLoader(SupplierRepository supplierRepository,
                      ShipmentService shipmentService,
                      OrderServiceClient orderServiceClient) {
        this.supplierRepository = supplierRepository;
        this.shipmentService = shipmentService;
        this.orderServiceClient = orderServiceClient;
        this.faker = new Faker(new Locale("es", "CL"));
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        System.out.println("Iniciando carga de datos de prueba con DataFaker...");

        List<Supplier> createdSuppliers = createMockSuppliers(5);
        System.out.println("Creados " + createdSuppliers.size() + " proveedores.");

        List<Long> existingOrderIdsForShipments = new ArrayList<>();
        System.out.println("Intentando obtener órdenes desde el servicio externo...");
        for (Long orderId : ORDER_IDS_TO_CONSUME) {
            try {
                OrderResponseDTO order = orderServiceClient.getOrderById(orderId);
                if (order != null && order.getId() != null) {
                    existingOrderIdsForShipments.add(order.getId());
                    System.out.println("Order " + order.getId() + " retrieved successfully (Status: " + order.getOrderStatus() + ", Total: " + order.getTotalAmount() + ")");
                } else {
                    System.out.println("Order " + orderId + " not found or incomplete. Skipping for shipment creation.");
                }
            } catch (Exception e) {
                System.err.println("Error fetching order " + orderId + ": " + e.getMessage());

            }
        }

        if (existingOrderIdsForShipments.isEmpty()) {
            System.err.println("No se pudieron obtener órdenes válidas del servicio externo. No se crearán envíos.");
            return;
        }

        System.out.println("Órdenes disponibles para crear envíos: " + existingOrderIdsForShipments);

        createMockShipments(20, createdSuppliers, existingOrderIdsForShipments);
        System.out.println("Creados 20 envíos de prueba.");

        System.out.println("Carga de datos de prueba finalizada.");
    }

    private List<Supplier> createMockSuppliers(int count) {
        List<Supplier> suppliers = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Supplier supplier = new Supplier();
            supplier.setName(faker.company().name());
            supplier.setContactPerson(faker.name().fullName());
            supplier.setPhone(faker.phoneNumber().phoneNumber());
            supplier.setEmail(faker.internet().emailAddress());
            suppliers.add(supplier);
        }
        return supplierRepository.saveAll(suppliers);
    }

    private void createMockShipments(int count, List<Supplier> suppliers, List<Long> availableOrderIds) {
        if (suppliers.isEmpty() || availableOrderIds.isEmpty()) {
            System.err.println("No hay proveedores u órdenes disponibles para crear envíos.");
            return;
        }

        for (int i = 0; i < count; i++) {
            try {
                Supplier randomSupplier = suppliers.get(faker.random().nextInt(0, suppliers.size() - 1));

                Long randomOrderId = availableOrderIds.get(faker.random().nextInt(0, availableOrderIds.size() - 1));

                ShipmentRequestDTO shipmentRequestDTO = new ShipmentRequestDTO();
                shipmentRequestDTO.setOrderId(randomOrderId);
                shipmentRequestDTO.setTrackingNumber(faker.code().asin().toUpperCase());
                shipmentRequestDTO.setSupplierId(randomSupplier.getSupplierId());

                shipmentService.createShipment(shipmentRequestDTO);

            } catch (Exception e) {
                System.err.println("Error al crear envío mock #" + (i + 1) + ": " + e.getMessage());
            }
        }
    }
}
