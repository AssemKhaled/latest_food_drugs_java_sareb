package com.example.food_drugs.rest;

import org.springframework.beans.factory.annotation.Autowired;	
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import com.example.examplequerydslspringdatajpamaven.service.LoginServiceImpl;

/**
 * Services Login and Logout Component
 * @author fuinco
 *
 */
@CrossOrigin
@RestController
public class LoginRestControllerSFDA {
	
	@Autowired
	private LoginServiceImpl loginServiceImpl;


	@GetMapping(path = "/loginSFDA")
	public 	ResponseEntity<?> login(@RequestHeader(value = "Authorization", defaultValue = "")String authtorization ){

		
		return loginServiceImpl.login(authtorization);
	}
	
	@GetMapping(path = "/logoutSFDA")
	public ResponseEntity<?> logout(@RequestHeader(value = "TOKEN", defaultValue = "")String TOKEN ){
		
		return loginServiceImpl.logout(TOKEN);
	}
	
		

		
}
