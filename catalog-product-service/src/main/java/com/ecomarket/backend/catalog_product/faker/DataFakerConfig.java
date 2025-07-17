package com.ecomarket.backend.catalog_product.faker;

import com.ecomarket.backend.catalog_product.model.Brand;
import com.ecomarket.backend.catalog_product.model.Category;
import com.ecomarket.backend.catalog_product.repository.BrandRepository;
import com.ecomarket.backend.catalog_product.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile({"dev", "test"})
@RequiredArgsConstructor
public class DataFakerConfig implements CommandLineRunner {

    private final BrandRepository brandRepo;
    private final CategoryRepository categoryRepo;

    private final Faker faker = new Faker();

    @Override
    public void run(String... args) {
        log.info("🔹 Seeding data for Brands & Categories ");

        // Insert Brands
        for (int i = 0; i < 5; i++) {
            String name = faker.company().name();
            String desc = faker.company().catchPhrase();
            if (brandRepo.findByName(name).isEmpty()) {
                brandRepo.save(new Brand(null, name, desc));
            }
        }

        //  Insert Categories
        for (int i = 0; i < 5; i++) {
            String name = faker.commerce().department();
            String desc = faker.lorem().sentence();
            if (categoryRepo.findByName(name).isEmpty()) {
                categoryRepo.save(new Category(null, name, desc));
            }
        }

        log.info(" Seeding completed!");
    }
}
