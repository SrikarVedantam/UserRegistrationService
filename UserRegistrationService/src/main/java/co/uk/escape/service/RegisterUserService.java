package co.uk.escape.service;

import java.util.List;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Controller;
import co.uk.escape.domain.MessageBundle;
import co.uk.escape.domain.RegisteredUser;
import co.uk.escape.domain.RegistrationRequest;
import co.uk.escape.domain.RegistrationRequestMessageBundle;
import co.uk.escape.domain.RegistrationResponse;
import co.uk.escape.domain.RegistrationResponseMessageBundle;

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
	     
		RegistrationRequestMessageBundle registrationRequestMessageBundle = (RegistrationRequestMessageBundle) messageObject;
		
		RegistrationRequest registrationRequest = registrationRequestMessageBundle.getPayload();
				
		RegisteredUser registeredUser = new RegisteredUser(null, 
											registrationRequest.getFirstname(),
											registrationRequest.getLastname(),
											registrationRequest.getEmailAddress(), 
											registrationRequest.getPassword());		
		
		 try {
			 registeredUser = registeredUserRepository.save(registeredUser);
		} catch (DuplicateKeyException e){
			System.out.println("opps problems: " + e.getMessage());
		}
		 
		RegistrationResponse registrationResponse = new RegistrationResponse(registeredUser);
		
 		// Transform message payload into message bundle
		List<String> permissions = registrationRequestMessageBundle.getPermissions();
		MessageBundle messageBundle = bundleMessage(registrationResponse, permissions);
	
		
		
		// Get and set correlationID
		final byte[] correlationId = message.getMessageProperties().getCorrelationId();	
		MessagePostProcessor messagePostProcessor = new MessagePostProcessor() 
				{
			   		public Message postProcessMessage(Message message) throws AmqpException {
			   			message.getMessageProperties().setCorrelationId(correlationId);;
			   			return message;  
			   } 
		};		
		
		rabbitTemplate.convertAndSend(messageBundle, messagePostProcessor);
		
        System.out.println("[1] ReceiverNewUserRegistration <" + messageBundle + ">");
    }
	
	
	
	// Bundle message
	private MessageBundle bundleMessage(RegistrationResponse payload, List<String> permissions) {
		MessageBundle messageBundle = new RegistrationResponseMessageBundle(payload);
		//messageBundle.setPermissions(permissions);
		return messageBundle;		
	}

	
}
