# flight-booking-app-microservice

1) Built using Java Spring boot, MYSQL
2) Microservices style of implementation is followed
3) Flight service and Booking Service are connected through Service Registry
4) API gateway is added to enable sending HTTP request using a common port
5) Config server implemented for storing configuration details of all the services
6) Used Eureka for service registry
7) Used Open Feign for inter service communication
8) The app contains 3 Post, 2 Get and 1 Delete endpoint
9) POST - Add flight, Search flights, Book Ticket
10) GET - View Ticket, View history of bookings
11) DELETE - Cancel Ticket
12) Used Sonar Cloud Quality analysis and implemented its suggestions
13) Used Jmeter for Stress testing the APIs
14) Sonar cloud analysis link - https://sonarcloud.io/project/overview?id=test-org-project_microservices-flight-app
