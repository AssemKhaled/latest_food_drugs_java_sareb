package com.example.food_drugs.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import com.example.examplequerydslspringdatajpamaven.entity.User;
import com.example.examplequerydslspringdatajpamaven.responses.GetObjectResponse;
import com.example.examplequerydslspringdatajpamaven.service.UserServiceImpl;
import com.example.food_drugs.entity.UserSFDA;

/**
 * Services of user component
 * @author fuinco
 *
 */
@RestController
@RequestMapping(path = "/usersSFDA")
@CrossOrigin
public class UserRestControllerSFDA {

	@Autowired
	private UserServiceImpl userServiceImpl;

	@GetMapping("/usersList")
	public ResponseEntity<?> usersList(@RequestHeader(value = "TOKEN", defaultValue = "")String TOKEN,
							           @RequestParam (value = "exportData", defaultValue = "") String exportData,                           
									   @RequestParam (value = "loggedUserId", defaultValue = "0") Long loggedUserId,
									   @RequestParam (value = "userId", defaultValue = "0") Long userId,
									   @RequestParam(value = "offset", defaultValue = "0") int offset,
							           @RequestParam(value = "search", defaultValue = "") String search,
							           @RequestParam(value = "active",defaultValue = "1") int active) {
		return userServiceImpl.usersOfUser(TOKEN,userId,loggedUserId,offset,search,active,exportData);
	}
	
	@GetMapping("/getUserById")
	public ResponseEntity<?> getUserById(@RequestHeader(value = "TOKEN", defaultValue = "")String TOKEN,
			                             @RequestParam (value = "loggedUserId", defaultValue = "0") Long loggedUserId,
			                             @RequestParam (value = "userId", defaultValue = "0") Long userId
			                             
			                             ) {
		return userServiceImpl.findUserById(TOKEN,userId,loggedUserId);
	}
	
	@PostMapping(path ="/createUser")
	public ResponseEntity<?> createUser(@RequestHeader(value = "TOKEN", defaultValue = "")String TOKEN,
			                              @RequestParam (value = "userId", defaultValue = "0") Long userId,
			                              @RequestBody(required = false) UserSFDA user) {

		return userServiceImpl.createUser(TOKEN,user,userId);
				
	}
	
	@PostMapping(path ="/editUser")
	public ResponseEntity<?> editUser(@RequestHeader(value = "TOKEN", defaultValue = "")String TOKEN,
			                            @RequestParam (value = "userId", defaultValue = "0") Long userId,
			                            @RequestBody(required = false) UserSFDA user) {
		

	    
		 return  userServiceImpl.editUser(TOKEN,user,userId);
		
				
	}
	
	@GetMapping(path ="/deleteUser")
	public ResponseEntity<?> deleteUser(@RequestHeader(value = "TOKEN", defaultValue = "")String TOKEN,
			                              @RequestParam (value = "userId", defaultValue = "0") Long userId,
			                              @RequestParam (value = "deleteUserId", defaultValue = "0") Long deleteUserId) {
		
		return userServiceImpl.deleteUser(TOKEN,userId,deleteUserId);
				
	}
	@GetMapping(path ="/activeUser")
	public ResponseEntity<?> activeUser(@RequestHeader(value = "TOKEN", defaultValue = "")String TOKEN,
			                              @RequestParam (value = "userId", defaultValue = "0") Long userId,
			                              @RequestParam (value = "activeUserId", defaultValue = "0") Long deleteUserId) {
		
		return userServiceImpl.activeUser(TOKEN,userId,deleteUserId);
				
	}
	
	@GetMapping(path="/getUserRole")
	public ResponseEntity<?> getUserRole(@RequestParam (value = "userId", defaultValue = "0") Long userId){
		
		return userServiceImpl.getUserRole(userId);
	}

	@RequestMapping(value = "/getUserSelect", method = RequestMethod.GET)
	public @ResponseBody ResponseEntity<?> getUserSelect(@RequestHeader(value = "TOKEN", defaultValue = "")String TOKEN,
			                                               @RequestParam (value = "userId", defaultValue = "0") Long userId) {
		
    	return userServiceImpl.getUserSelect(TOKEN,userId);
		
	}
	
	@RequestMapping(value = "/getVendorSelect", method = RequestMethod.GET)
	public @ResponseBody ResponseEntity<?> getVendorSelect(@RequestHeader(value = "TOKEN", defaultValue = "")String TOKEN,
			                                               @RequestParam (value = "userId", defaultValue = "0") Long userId) {
		
    	return userServiceImpl.getVendorSelect(TOKEN,userId);
		
	}
	
	@RequestMapping(value = "/getClientSelect", method = RequestMethod.GET)
	public @ResponseBody ResponseEntity<?> getClientSelect(@RequestHeader(value = "TOKEN", defaultValue = "")String TOKEN,
			                                               @RequestParam (value = "vendorId", defaultValue = "0") Long vendorId) {
		
    	return userServiceImpl.getClientSelect(TOKEN,vendorId);
		
	}
	
	@RequestMapping(value = "/getUserSelectWithChild", method = RequestMethod.GET)
	public @ResponseBody ResponseEntity<?> getUserSelectWithChild(@RequestHeader(value = "TOKEN", defaultValue = "")String TOKEN,
			                                               @RequestParam (value = "userId", defaultValue = "0") Long userId) {
		
    	return userServiceImpl.getUserSelectWithChild(TOKEN,userId);
		
	}
	


}
