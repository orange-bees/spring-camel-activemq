# spring-camel-activemq

This is an example application for demonstrating Spring Boot and Camel with Apache MQ.

The example is highly configurable by modifying the `application.properties` file to change any of its parameters.

## Integration Tests

The application includes an integration test which uses the `docker-maven-plugin` from fabric8 to build an image, start it, and connect to another 
Docker image for Apache MQ to demonstrate the functionality.

## Running application 

This requires an Apache MQ Server running separately. You may choose to use a pre-built Docker image as shown below:

```bash
docker run -p 61616:61616 -p 8161:8161 rmohr/activemq:5.15.9     
```
