package co.uk.escape.service;

import org.springframework.data.mongodb.repository.MongoRepository;

import co.uk.escape.domain.RegisteredUser;

public interface  RegisteredUserRepository extends MongoRepository<RegisteredUser, String>{

}
