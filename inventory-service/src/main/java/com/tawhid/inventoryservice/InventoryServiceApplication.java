package com.tawhid.inventoryservice;

import com.tawhid.inventoryservice.model.Inventory;
import com.tawhid.inventoryservice.repository.InventoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication

public class InventoryServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(InventoryServiceApplication.class, args);
	}

	@Bean
	public CommandLineRunner loadData(InventoryRepository inventoryRepository){
		return args -> {
			Inventory inventory= new Inventory();
			inventory.setSkuCode("Iphone 13");
			inventory.setQuantity(200);
			Inventory inventory1= new Inventory();
			inventory1.setSkuCode("Iphone 13 promax Red");
			inventory1.setQuantity(0);
			inventoryRepository.save(inventory);
			inventoryRepository.save(inventory1);

		};
	}
}
