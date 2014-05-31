package co.uk.escape;

import java.io.IOException;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import co.uk.escape.service.RegisterUserService;

@Configuration
@EnableAutoConfiguration
@ComponentScan
public class UserRegistrationConfiguration {
	
	// QUEUES //	
	@Bean
	Queue replyQueue() {
		return new Queue("replyqueue", true);
	}
	
	@Bean
	Queue registrationQueue() {
		return new Queue("registrationqueue", true);
	}

	
	// EXCHANGE //
	@Bean
	TopicExchange messageexchange() {
		return new TopicExchange("messageexchange");
	}
		
	@Bean
	TopicExchange replyexchange() {
		return new TopicExchange("replyexchange");
	}
	
	
	// BINDINGS //	
	@Bean
	Binding replyBind(){
		return BindingBuilder.bind(replyQueue()).to(replyexchange()).with("replyroutingkey");
	}
	
	@Bean
	Binding registrationBind(){
		return BindingBuilder.bind(registrationQueue()).to(messageexchange()).with("registrationroutingkey");
	}
	
    @Bean
    RabbitTemplate template(TopicExchange messageexchange, ConnectionFactory connectionFactory){
        Jackson2JsonMessageConverter jsonConverter = new Jackson2JsonMessageConverter();
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);    
        rabbitTemplate.setMessageConverter(jsonConverter);
        rabbitTemplate.setExchange("replyexchange");
        rabbitTemplate.setQueue("replyqueue");
        rabbitTemplate.setRoutingKey("replyroutingkey");
        return rabbitTemplate;
    }
    
    
	@Bean
	SimpleMessageListenerContainer container(ConnectionFactory connectionFactory, RegisterUserService receiver) throws IOException {			
		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.setQueues(new Queue("registrationqueue", false));
		container.setMessageListener(receiver);
		return container;
	}

	@Bean
	RegisterUserService receiver() {
		return new RegisterUserService();
	} 
	
//	@Bean
//	RabbitTemplate template(FanoutExchange exchange, ConnectionFactory connectionFactory){
//		Jackson2JsonMessageConverter jsonConverter = new Jackson2JsonMessageConverter();
//		RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);	
//		rabbitTemplate.setMessageConverter(jsonConverter);
//		rabbitTemplate.setExchange(exchange.getName());
//		return rabbitTemplate;
//	}
//
//	@Bean
//	ReceiverNewUserRegistration receiver() {
//		return new ReceiverNewUserRegistration();
//	}
//	
//	@Bean
//	MessageListenerAdapter listenerAdapter(ReceiverNewUserRegistration receiver) {
//		MessageListenerAdapter messageListenerAdapter = new MessageListenerAdapter(receiver, "saveNewUser");	
//		Jackson2JsonMessageConverter jsonConverter = new Jackson2JsonMessageConverter();
//		messageListenerAdapter.setMessageConverter(jsonConverter);
//		return messageListenerAdapter;
//	}
//
//	
//	@Bean
//	SimpleMessageListenerContainer container(ConnectionFactory connectionFactory, MessageListenerAdapter listenerAdapter, Queue requestQueue) throws IOException {			
//		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
//		container.setConnectionFactory(connectionFactory);
//		container.setQueues(requestQueue);
//		container.setMessageListener(listenerAdapter);
//		return container;
//	}	
	
}
