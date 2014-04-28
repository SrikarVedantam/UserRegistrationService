package co.uk.escape.service;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
public class ReceiverNewUserRegistration {
	
	@Autowired
	RabbitTemplate rabbitTemplate;
	
	@Autowired
	DirectExchange exchange;
	
	@Autowired
	FanoutExchange fanoutExchange;	
	
	@Autowired
	RegisteredUserRepository registeredUserRepository;
	
	public RegisteredUser saveNewUser(RegistrationRequest newUserRegistrationRequest) {
		
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
		
		
		rabbitTemplate.convertAndSend(fanoutExchange.getName(), registeredUser);
		

        System.out.println("[1] ReceiverNewUserRegistration <" + registeredUser + ">");
        return registeredUser;
    }
	

	
}
