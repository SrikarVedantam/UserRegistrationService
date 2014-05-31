package co.uk.escape.service;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.amqp.core.Queue;

import co.uk.escape.domain.RegisteredUser;
import co.uk.escape.domain.RegistrationRequest;

@Controller
public class RegisterUserService implements MessageListener {
	
	@Autowired
	RabbitTemplate rabbitTemplate;
	
	@Autowired
	RegisteredUserRepository registeredUserRepository;

	@Override
    public void onMessage(Message message){
 
		System.out.println("Message: " + message);      
		System.out.println("Message: " + message.getMessageProperties().getCorrelationId());
	               
		Jackson2JsonMessageConverter jmc = new Jackson2JsonMessageConverter();
		Object messageObject = jmc.fromMessage(message);
	     
		RegistrationRequest newUserRegistrationRequest = (RegistrationRequest) messageObject;
		
		RegisteredUser registeredUser = new RegisteredUser(null, 
							newUserRegistrationRequest.getFirstname(),
							newUserRegistrationRequest.getLastname(),
							newUserRegistrationRequest.getEmailAddress(), 
							newUserRegistrationRequest.getPassword());		
	
		try {
			registeredUser = registeredUserRepository.save(registeredUser);
		} catch (DuplicateKeyException e){
			System.out.println("opps problems: " + e.getMessage());
		}
		
		// Get and set correlationID
		final byte[] correlationId = message.getMessageProperties().getCorrelationId();	
		MessagePostProcessor messagePostProcessor = new MessagePostProcessor() 
				{
			   		public Message postProcessMessage(Message message) throws AmqpException {
			   			message.getMessageProperties().setCorrelationId(correlationId);;
			   			return message;  
			   } 
		};		
		
		rabbitTemplate.convertAndSend(registeredUser, messagePostProcessor);
		
        System.out.println("[1] ReceiverNewUserRegistration <" + registeredUser + ">");
    }
	

	
}
