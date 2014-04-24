package co.uk.escape.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;

import co.uk.escape.domain.RegisteredUser;
import co.uk.escape.domain.RegistrationRequest;

@Controller
public class ReceiverNewUserRegistration {
	
	@Autowired
	RegisteredUserRepository registeredUserRepository;
	
	public void saveNewUser(RegistrationRequest newUserRegistrationRequest) {
		
		RegisteredUser registeredUser = new RegisteredUser(null, 
				newUserRegistrationRequest.getFirstname(),
				newUserRegistrationRequest.getLastname(),
				newUserRegistrationRequest.getEmailAddress(), 
				newUserRegistrationRequest.getPassword());		
	
		try {
			registeredUserRepository.save(registeredUser);
		} catch (DuplicateKeyException e){
			System.out.println("opps problems: " + e.getMessage());
		}

        System.out.println("ReceiverNewUserRegistration <" + registeredUser + ">");
    }
	

	
}
