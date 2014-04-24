package co.uk.escape.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

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

		registeredUserRepository.save(registeredUser);
        System.out.println("ReceiverNewUserRegistration <" + registeredUser + ">");
    }
	
}
