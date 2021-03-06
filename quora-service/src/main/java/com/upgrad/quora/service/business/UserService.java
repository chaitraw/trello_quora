package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.UserAuthEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

/**
 * Service class which deals with business logic related to User entity
 *
 * @author chaitraw
 */
@Service
public class UserService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private PasswordCryptographyProvider cryptographyProvider;

    @Transactional(propagation = Propagation.REQUIRED)
    public UserEntity createUser(UserEntity userEntity) throws SignUpRestrictedException {
        UserEntity userByUserName = userDao.getUserByUserName(userEntity.getUserName());
        if(userByUserName != null) {
            throw new SignUpRestrictedException("SGR-001", "Try any other Username, this Username has already been taken");
        }
        UserEntity userByEmail = userDao.getUserByEmail(userEntity.getEmail());
        if(userByEmail != null && userByEmail.getEmail().equals(userEntity.getEmail())) {
            throw new SignUpRestrictedException("SGR-002", "This user has already been registered, try with any other emailId");
        }
        String password = userEntity.getPassword();
        if (password == null) {
            userEntity.setPassword("quora@123");
        }
        String[] encryptedText = cryptographyProvider.encrypt(userEntity.getPassword());
        userEntity.setSalt(encryptedText[0]);
        userEntity.setPassword(encryptedText[1]);
        return userDao.createUser(userEntity);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public UserAuthEntity signIn(final String username, final String password) throws AuthenticationFailedException {
        UserEntity userEntity = userDao.getUserByUserName(username);
        if (userEntity == null) {
            throw new AuthenticationFailedException("ATH-001", "User with email not found");
        }

        final String encryptedPassword = cryptographyProvider.encrypt(password, userEntity.getSalt());
        if (encryptedPassword.equals(userEntity.getPassword())) {
            JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(encryptedPassword);
            UserAuthEntity userAuthToken = new UserAuthEntity();
            userAuthToken.setUser(userEntity);
            final ZonedDateTime now = ZonedDateTime.now();
            final ZonedDateTime expiresAt = now.plusHours(8);
            userAuthToken.setAccessToken(jwtTokenProvider.generateToken(userEntity.getUuid(), now, expiresAt));
            userAuthToken.setLoginAt(LocalDateTime.now());
            userAuthToken.setExpiresAt(LocalDateTime.now().plusHours(8));

            userDao.createAuthToken(userAuthToken);
            userDao.updateUser(userEntity);

            return userAuthToken;
        } else {
            throw new AuthenticationFailedException("ATH-002", "Password Failed");
        }

    }

    @Transactional(propagation = Propagation.REQUIRED)
    public UserAuthEntity signOut(final String accessToken) throws SignOutRestrictedException {

        UserAuthEntity userAuthEntity = userDao.getUserAuthToken(accessToken);
      
        if (userAuthEntity == null) {
            throw new SignOutRestrictedException("SGR-001", "User is not Signed in");
        }
      
        final LocalDateTime now = LocalDateTime.now();
        userAuthEntity.setLogoutAt(now);
        userDao.updateAuthToken(userAuthEntity);
        return userAuthEntity;
    }

    public UserEntity deleteUser(String userUuid, String accessToken) throws AuthorizationFailedException, UserNotFoundException {
        UserAuthEntity userAuthEntity = userDao.getUserAuthToken(accessToken);
        if (userAuthEntity == null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        }

        UserEntity userEntity = userDao.getUserById(userUuid);

        if(userEntity == null) {
            throw new UserNotFoundException("USR-001", "User with entered uuid to be deleted does not exist");
        }
        if(userAuthEntity.getLogoutAt() != null && userAuthEntity.getLoginAt().isBefore(userAuthEntity.getLogoutAt())) {
            throw new AuthorizationFailedException("ATHR-002", "User is signed out");
        }
        if(!(userEntity.getRole().equalsIgnoreCase("ADMIN"))) {
            throw new AuthorizationFailedException("ATHR-003", "Unauthorized Access, Entered user is not an admin");
        }
        userEntity = userDao.deleteUser(userEntity);
        return userEntity;
    }
}
