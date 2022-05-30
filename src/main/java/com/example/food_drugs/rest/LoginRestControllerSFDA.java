package com.example.food_drugs.rest;

import com.example.examplequerydslspringdatajpamaven.service.LoginService;
import com.example.examplequerydslspringdatajpamaven.service.LoginServiceImpl;
import com.example.food_drugs.dto.ApiResponse;
import com.example.food_drugs.dto.responses.LoginResponse;
import com.example.food_drugs.exception.ApiGetException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

/**
 * Services Login and Logout Component
 * @author fuinco
 *
 */
@CrossOrigin
@RestController
public class LoginRestControllerSFDA {

	private final LoginService loginService;

	public LoginRestControllerSFDA(LoginService loginService) {
		this.loginService = loginService;
	}


	@GetMapping(path = "/loginSFDA")
	public 	ResponseEntity<?> login(@RequestHeader(value = "Authorization", defaultValue = "")String authtorization ){
		return loginService.login(authtorization);
//		try{
//			return ResponseEntity.ok(loginServiceImpl.login(authtorization));
//
//		}catch (Exception | Error e){
//			throw new ApiGetException(e.getLocalizedMessage());
//		}
	}
	
	@GetMapping(path = "/logoutSFDA")
	public ResponseEntity<?> logout(@RequestHeader(value = "TOKEN", defaultValue = "")String TOKEN ){
		
		return loginService.logout(TOKEN);
	}
	
		

		
}
