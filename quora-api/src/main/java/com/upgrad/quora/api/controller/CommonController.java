package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.SignoutResponse;
import com.upgrad.quora.api.model.UserDetailsResponse;
import com.upgrad.quora.service.business.CommonService;
import com.upgrad.quora.service.entity.UserAuthEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthenticationFailedException;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.SignOutRestrictedException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class CommonController {

    @Autowired
    private CommonService commonService;

    /**
     * This endpoint is used to get the details of any user in the Quora Application. This endpoint can be accessed by any user in the application.
     *
     * @param userId
     * @param authorization
     * @return
     * @throws AuthorizationFailedException
     * @throws UserNotFoundException
     */
    @RequestMapping(method = RequestMethod.GET, path = "/userprofile/{userId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<UserDetailsResponse> getUserProfile(@PathVariable("userId") String userId, @RequestHeader("authorization") final String authorization) throws AuthorizationFailedException, UserNotFoundException {
        String accessToken = authorization.split("Bearer ")[0];

        UserEntity userEntity = commonService.getUserProfile(userId, accessToken);

        UserDetailsResponse userDetailsResponse = new UserDetailsResponse().firstName(userEntity.getFirstName())
            .lastName(userEntity.getLastName()).userName(userEntity.getUserName()).emailAddress(userEntity.getEmail())
            .country(userEntity.getCountry()).aboutMe(userEntity.getAboutme())
            .dob(userEntity.getDob()).contactNumber(userEntity.getContactNumber());

        return new ResponseEntity<UserDetailsResponse>(userDetailsResponse, HttpStatus.OK);
    }
}
