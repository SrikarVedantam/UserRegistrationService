package co.uk.escape;

import static co.uk.escape.RMQExchange.Type.*;
import static co.uk.escape.RMQQueue.Type.*;

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
	@Bean @RMQQueue(LOGIN_RESPONSE)
	Queue loginResponseQueue() {
		return new Queue("LoginResponseQueue", true);
	}

	@Bean @RMQQueue(LOGIN_REQUEST)
	Queue loginRequestQueue() {
		return new Queue("LoginRequestQueue", true);
	}	

	@Bean @RMQQueue(REGISTRATION_REQUEST)
	Queue registrationRequestQueue() {
		return new Queue("RegistrationRequestQueue", true);
	}
	
	@Bean @RMQQueue(REGISTRATION_RESPONSE)
	Queue registrationResponseQueue() {
		return new Queue("RegistrationResponseQueue", true);
	}
	
	@Bean @RMQQueue(USER_SERVICE)
	Queue userServcieQueue() {
		return new Queue("UserServiceQueue", true);
	}
	

	// EXCHANGE //
	@Bean @RMQExchange(AUTHORISATION)
	TopicExchange authorisationExchange() {
		return new TopicExchange("AuthorisationExchange");
	}
	
	@Bean @RMQExchange(RESPONSE)
	TopicExchange responseExchange() {
		return new TopicExchange("ResponseExchange");
	}
	
	@Bean @RMQExchange(MESSAGE)
	TopicExchange messageExchange() {
		return new TopicExchange("MessageExchange");
	}
	
	
	// BINDINGS //	
	// request queue bindings //
    @Bean
	Binding registrationRequestBind(){
		return BindingBuilder.bind(registrationRequestQueue()).to(authorisationExchange()).with("RegistrationRoutingKey");
	}

	// service bindings //
	@Bean
	Binding userServiceBind(){
		return BindingBuilder.bind(userServcieQueue()).to(messageExchange()).with("RegistrationRoutingKey");
	}	
	
    // response queue bindings //
	@Bean
	Binding registrationResponseBind(){
		return BindingBuilder.bind(registrationResponseQueue()).to(responseExchange()).with("ResponseRoutingKey");
	}	 
	
    /////////////////////////////////////////
	// register user : Receiver and Sender //
    /////////////////////////////////////////
    @Bean
    RabbitTemplate template(ConnectionFactory connectionFactory,
    		@RMQExchange(RESPONSE) TopicExchange responseExchange,
    		@RMQQueue(REGISTRATION_RESPONSE) Queue registrationResponseQueue){
        Jackson2JsonMessageConverter jsonConverter = new Jackson2JsonMessageConverter();
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonConverter);
        rabbitTemplate.setExchange(responseExchange.getName());
        rabbitTemplate.setQueue(registrationResponseQueue.getName());
        rabbitTemplate.setRoutingKey("ResponseRoutingKey");
        return rabbitTemplate;
    }
    
    //////////////////////////////
	// register user : Listener //
    ////////////////////////////// 
	@Bean
	SimpleMessageListenerContainer container(ConnectionFactory connectionFactory, RegisterUserService receiver,
    		@RMQQueue(USER_SERVICE) Queue userServiceQueue) throws IOException {			
		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.setQueues(userServiceQueue);
		container.setMessageListener(receiver);
		return container;
	}

	@Bean
	RegisterUserService receiver() {
		return new RegisterUserService();
	} 
	
}
