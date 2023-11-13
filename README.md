# Simple-microservice-project
This project simply offer three main service called Inventory service, Order service and Product service
Inventory service, Order service uses mysql database and product service use mongodb database
All the service are access through a gateway called API gateway.
Also implement an event driven Notification service using kafka.
All the services are registerd as a client to a server, for this netflix eureka server is used.
There load balancing and distributed tracing is implemented using Resilience4j and micrometer-tracing + zipkin-reporter
