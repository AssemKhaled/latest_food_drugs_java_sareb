package com.example.food_drugs.rest.mobileRestSFDA;

import com.example.examplequerydslspringdatajpamaven.service.LoginServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@Component
@RestController
@RequestMapping(path = "/mobile/auth")
public class MobileLoginControllerSFDA {

    private final LoginServiceImpl loginServiceImpl;

    public MobileLoginControllerSFDA(LoginServiceImpl loginServiceImpl) {
        this.loginServiceImpl = loginServiceImpl;
    }


    @GetMapping(path = "/loginSFDA")
    public ResponseEntity<?> login(@RequestHeader(value = "Authorization", defaultValue = "")String authtorization ){


        return loginServiceImpl.login(authtorization);
    }

    @GetMapping(path = "/logoutSFDA")
    public ResponseEntity<?> logout(@RequestHeader(value ="TOKEN", defaultValue = "")String TOKEN ){

        return loginServiceImpl.logout(TOKEN);
    }

}
