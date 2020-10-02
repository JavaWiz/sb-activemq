# Event-Driven Microservices With Spring Boot and ActiveMQ

Most communications between microservices are either via HTTP request-response APIs or asynchronous messaging. While these two mechanisms are most commonly used, yet they’re quite different. It is important to know when to use which mechanism.

Event-driven communication is important when propagating changes across several microservices and their related domain models. This means that when changes occur, we need some way to coordinate changes across the different models. This ensures reliable communication as well as loose coupling between microservices.

There are multiple patterns to achieve event-driven architecture. One of the common and popular patterns is the messaging pattern. It is extremely scalable, flexible, and guarantees delivery of messages. There are several tools that can be used for messaging pattern such as RabbitMQ, ActiveMQ, Apache Kafka and so on.

We are going to build microservices using Spring Boot and we will set up the ActiveMQ message broker to communicate between microservices asynchronously.

## Building Microservices
Here we'll create two Spring Boot projects `activemq-publisher` and `activemq-subscriber`. Let's add maven dependency `spring-boot-starter-activemq` to enable ActiveMQ.

## Configuring Publisher
In the project `activemq-publisher`, we will first configure a queue. Create a PublisherConfigurer class as follows.
```
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.jms.Queue;

@Configuration
public class PublisherConfigurer {
    @Bean
    public Queue queue(){
        return new ActiveMQQueue("sample-queue");
    }
}
```
In above class we declare a bean Queue and our queue name would be `sample-queue`.

Now, let’s create a REST API which will be used to publish the message to the queue.
```
import com.javawiz.service.Publisher;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PublisherController {
    
    private final Publisher publisher;

    @GetMapping("message/{message}")
    public ResponseEntity<String> publish(@PathVariable("message") final String message) {
        return new ResponseEntity<>(publisher.publish(message), HttpStatus.OK);
    }
}
```
Our publisher class
```
import lombok.RequiredArgsConstructor;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import javax.jms.Queue;

@Service
@RequiredArgsConstructor
public class Publisher {
    private final Queue queue;
    private final JmsTemplate jmsTemplate;

    public String publish(String message){
        jmsTemplate.convertAndSend(queue, message);
        return message;
    }
}
```
In above class, we will inject the bean Queue which we declared before and we will also inject  JmsTemplate. 

To send or receive messages through JMS, we need to establish a connection to the JMS provider and obtain a session. JmsTemplate is a helper class that simplifies sending and receiving of messages through JMS and gets rid of boilerplate code. 

We have now created a simple API endpoint that will accept a string as a parameter and puts it in the queue.

## Configuring Subscriber
In `activemq-subscriber` project, create a component class as follows:
```
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
@EnableJms
public class Subscriber {
    private final Logger logger = LoggerFactory.getLogger(Subscriber.class);

    @JmsListener(destination = "sample-queue")
    public void listener(String message) {
        logger.info("Message received {} ", message);
    }
}
```
In this class, we have an annotated method with  `@JmsListener` and we have passed the queue name `sample-queue` which we configured in the publisher. `@JmsListener` is used for listening to any messages that are put on the queue `sample-queue`.

Notice that we have an annotated class with `@EnableJms`. As Spring documentation says “@EnableJms enables detection of JmsListener annotations on any Spring-managed bean in the container.”

## Configuring ActiveMQ
In both the projects, in `application.properties` file and add the following properties.
```
spring.activemq.broker-url=tcp://localhost:61616
spring.activemq.user=admin
spring.activemq.password=admin
```
Now we are ready to start our apps but before that we need to set up activemq.

## Installing ActiveMQ
I have installed ActiveMQ by downloading [here](http://activemq.apache.org/components/classic/download/). We can also use Spring Boot’s embedded ActiveMQ for testing purposes. 
Set it to your path of environment variable then we can start `ActiveMQ` server using command `activemq start`.
The ActiveMQ server should be available at [http://localhost:8161/admin](http://localhost:8161/admin).

## Testing ActiveMQ
Before running the applications, make sure to change the server port for one of the projects. The embedded tomcat server runs on the port 8080 by default.

Run both the applications and run the URL `http://localhost:8080/api/message/Welcome to activemq` in browser or any REST API testing tool.

In the consumer application, we will see the following log in console.
```
2020-10-02 18:13:09.329  INFO 13364 --- [enerContainer-1] com.javawiz.service.Subscriber           : Message received Welcome to activemq
```
What just happened is that the message was put on the queue. The consumer application that was listening to the queue read the message from the queue.

In the ActiveMQ dashboard, navigate to the Queue tab. We can see the details such as a number of consumers to a queue, the number is messages pending, queued and dequeued.

![ActiveMQ Dashboard](https://github.com/JavaWiz/sb-activemq/blob/master/dashboard.png)

ActiveMq message brokers guarantee delivery of messages. Imagine that the consumer service is down, and the message was put on the queue by publisher service.

Stop the application  `activemq-subscriber` . Run the URL again with messages in browser.

Navigate to the ActiveMQ dashboard and notice the queue state.
![Queue State](https://github.com/JavaWiz/sb-activemq/blob/master/queue_state.png)

We can see that 3 messages are pending and enqueued. Start the application activemq-receiver again.

As soon as the application is started, we will the following message in console.
```
2020-10-02 18:36:06.484  INFO 22180 --- [enerContainer-1] com.javawiz.service.Subscriber           : Message received Welcome to activemq 
2020-10-02 18:36:06.503  INFO 22180 --- [enerContainer-1] com.javawiz.service.Subscriber           : Message received Welcome to activemq 
2020-10-02 18:36:06.506  INFO 22180 --- [enerContainer-1] com.javawiz.service.Subscriber           : Message received test
```
The number of pending messages are now set to zero and the number of dequeued messages is set to 4. The message broker guarantees the delivery of messages.
![Queue State](https://github.com/JavaWiz/sb-activemq/blob/master/queue_state_after.png) 

### Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/2.3.4.RELEASE/maven-plugin/reference/html/)
* [Create an OCI image](https://docs.spring.io/spring-boot/docs/2.3.4.RELEASE/maven-plugin/reference/html/#build-image)
* [Spring Web](https://docs.spring.io/spring-boot/docs/2.3.4.RELEASE/reference/htmlsingle/#boot-features-developing-web-applications)
* [Spring for Apache ActiveMQ 5](https://docs.spring.io/spring-boot/docs/2.3.4.RELEASE/reference/htmlsingle/#boot-features-activemq)

### Guides
The following guides illustrate how to use some features concretely:

* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/bookmarks/)
* [Java Message Service API via Apache ActiveMQ Classic.](https://spring.io/guides/gs/messaging-jms/)

