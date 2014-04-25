package co.uk.escape;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Binding.DestinationType;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.rabbitmq.client.Channel;

import co.uk.escape.domain.TemporaryQueue;
import co.uk.escape.service.ReceiverNewUserRegistration;

@Configuration
@EnableAutoConfiguration
@ComponentScan
public class UserRegistrationConfiguration {
	
//	final static String queueName = "user-registration";
	
	@Bean
	TemporaryQueue temporaryQueue(ConnectionFactory connectionFactory) throws IOException {
		Channel channel=connectionFactory.createConnection().createChannel(false); //String name, boolean durable, boolean exclusive, boolean autoDelete)
		return new TemporaryQueue(channel.queueDeclare().getQueue());
	}
	
	
	@Bean
	FanoutExchange exchange() {
		return new FanoutExchange("user-registrations-exchange");
	}
	
	@Bean
	Binding binding(TemporaryQueue temporaryQueue, FanoutExchange exchange) {
		return new Binding(temporaryQueue.getName(), DestinationType.QUEUE, exchange.getName(), "", null);
	}
	
	@Bean
	RabbitTemplate template(ConnectionFactory connectionFactory){
		Jackson2JsonMessageConverter jsonConverter = new Jackson2JsonMessageConverter();
		RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);	
		rabbitTemplate.setMessageConverter(jsonConverter);
		return rabbitTemplate;
	}

	@Bean
	ReceiverNewUserRegistration receiver() {
		return new ReceiverNewUserRegistration();
	}
	
	@Bean
	MessageListenerAdapter listenerAdapter(ReceiverNewUserRegistration receiver) {
		MessageListenerAdapter messageListenerAdapter = new MessageListenerAdapter(receiver, "saveNewUser");	
		Jackson2JsonMessageConverter jsonConverter = new Jackson2JsonMessageConverter();
		messageListenerAdapter.setMessageConverter(jsonConverter);
		return messageListenerAdapter;
	}

	
	@Bean
	SimpleMessageListenerContainer container(TemporaryQueue temporaryQueue, ConnectionFactory connectionFactory, MessageListenerAdapter listenerAdapter) throws IOException {			
		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
		container.setConcurrentConsumers(10);
		container.setConnectionFactory(connectionFactory);
		container.setQueueNames(temporaryQueue.getName());
		container.setMessageListener(listenerAdapter);
		System.out.println(container.getQueueNames());
		return container;
	}

}
