package com.example.food_drugs.service;

import com.example.examplequerydslspringdatajpamaven.entity.User;
import com.example.examplequerydslspringdatajpamaven.responses.GetObjectResponse;
import com.example.examplequerydslspringdatajpamaven.service.UserRoleService;
import com.example.examplequerydslspringdatajpamaven.service.UserServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.example.examplequerydslspringdatajpamaven.rest.RestServiceController;

/**
 * services functionality related to user SFDA
 * @author fuinco
 *
 */
@Component
@Service
public class UserServiceImplSFDA extends RestServiceController implements UserServiceSFDA{

    private GetObjectResponse getObjectResponse;

    private final UserServiceImpl userServiceImpl;

    private final UserRoleService userRoleService;

    public UserServiceImplSFDA(UserServiceImpl userServiceImpl, UserRoleService userRoleService) {

        this.userServiceImpl = userServiceImpl;
        this.userRoleService = userRoleService;
    }

    public ResponseEntity userAndTokenErrorCheckerForElm(String TOKEN , Long userId){

        if(TOKEN.equals("")) {
            getObjectResponse = new GetObjectResponse(HttpStatus.BAD_REQUEST.value(), "TOKEN id is required",null);
            return  ResponseEntity.badRequest().body(getObjectResponse);
        }

        if(super.checkActive(TOKEN)!= null)
        {
            return super.checkActive(TOKEN);
        }

        if(userId == 0) {
            getObjectResponse = new GetObjectResponse(HttpStatus.BAD_REQUEST.value(), "No loggedId",null);
            return  ResponseEntity.badRequest().body(getObjectResponse);
        }


        User user = userServiceImpl.findById(userId);
        if(user == null) {
            getObjectResponse = new GetObjectResponse(HttpStatus.NOT_FOUND.value(), "this user not found",null);
            return  ResponseEntity.status(404).body(getObjectResponse);
        }

        if(user.getAccountType()!= 1) {
            if(!userRoleService.checkUserHasPermission(userId, "WAREHOUSE", "updateInElm")) {
                getObjectResponse = new GetObjectResponse(HttpStatus.BAD_REQUEST.value(), "this user does not has permission to connectToElm",null);
                return  ResponseEntity.badRequest().body(getObjectResponse);
            }
        }
        return ResponseEntity.ok(user);
    }

    public ResponseEntity userAndTokenErrorChecker(String TOKEN , Long userId){

        if(TOKEN.equals("")) {
            getObjectResponse = new GetObjectResponse(HttpStatus.BAD_REQUEST.value(), "TOKEN id is required",null);
            return  ResponseEntity.badRequest().body(getObjectResponse);
        }

        if(super.checkActive(TOKEN)!= null)
        {
            return super.checkActive(TOKEN);
        }

        if(userId == 0) {
            getObjectResponse = new GetObjectResponse(HttpStatus.BAD_REQUEST.value(), "No loggedId",null);
            return  ResponseEntity.badRequest().body(getObjectResponse);
        }


        User user = userServiceImpl.findById(userId);
        if(user == null) {
            getObjectResponse = new GetObjectResponse(HttpStatus.NOT_FOUND.value(), "this user not found",null);
            return  ResponseEntity.status(404).body(getObjectResponse);
        }

        return ResponseEntity.ok(user);
    }


    public ResponseEntity<?> userErrorChecker(Long userId){

        if(userId == 0) {
            getObjectResponse = new GetObjectResponse(HttpStatus.BAD_REQUEST.value(), "No loggedId",null);
            return  ResponseEntity.badRequest().body(getObjectResponse);
        }

        User user = userServiceImpl.findById(userId);
        if(user == null) {
            getObjectResponse = new GetObjectResponse(HttpStatus.NOT_FOUND.value(), "this user not found",null);
            return  ResponseEntity.status(404).body(getObjectResponse);
        }

        return ResponseEntity.ok(user);
    }

}
