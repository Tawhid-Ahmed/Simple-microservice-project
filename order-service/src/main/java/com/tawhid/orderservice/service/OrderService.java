package com.tawhid.orderservice.service;

import com.tawhid.orderservice.config.WebClientConfig;
import com.tawhid.orderservice.dto.InventoryResponse;
import com.tawhid.orderservice.dto.OrderLineItemsDto;
import com.tawhid.orderservice.dto.OrderRequest;
import com.tawhid.orderservice.event.OrderPlacedEvent;
import com.tawhid.orderservice.model.Order;
import com.tawhid.orderservice.model.OrderLineItems;
import com.tawhid.orderservice.repository.OrderRepository;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService  {

    private final WebClient.Builder webClientBuilder;
    private final OrderRepository orderRepository;
    private final ObservationRegistry observationRegistry;
    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;


    public String placeOrder(OrderRequest orderRequest){
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
      List <OrderLineItems> orderLineItems= orderRequest.getOrderLineItemsDtoList()
                .stream()
                .map(this::mapToDto)
                .toList();
      order.setOrderLineItemsList(orderLineItems);
      List<String> skuCode = order.getOrderLineItemsList().stream().map(OrderLineItems::getSkuCode).toList();


        Observation inventoryServiceObservation = Observation.createNotStarted("inventory-service-lookup",this.observationRegistry);
        inventoryServiceObservation.lowCardinalityKeyValue("call","inventory-service");

        return inventoryServiceObservation.observe(() -> {
            InventoryResponse[] inventoryResponsesArray = webClientBuilder.build().get()
                    .uri("http://inventory-service/api/inventory", uriBuilder -> uriBuilder.queryParam("skuCode", skuCode).build())
                    .retrieve()
                    .bodyToMono(InventoryResponse[].class)
                    .block();


            assert inventoryResponsesArray != null;
            boolean allProductsInStock = Arrays.stream(inventoryResponsesArray).allMatch(InventoryResponse::isInStock);


            if (allProductsInStock) {
                orderRepository.save(order);
                kafkaTemplate.send("notificationTopic",new OrderPlacedEvent(order.getOrderNumber()));
                return "Order Placed Successfully!!";
            } else {
                throw new IllegalArgumentException("Product is not in stock, please try again");
            }

        });

    }

    private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto) {
       OrderLineItems orderLineItems = new OrderLineItems();
       orderLineItems.setPrice(orderLineItemsDto.getPrice());
       orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
       orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());
       return orderLineItems;
    }

}
