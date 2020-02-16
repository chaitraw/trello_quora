package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.UserAuthEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthenticationFailedException;
import com.upgrad.quora.service.exception.SignOutRestrictedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

/**
 * Service class which deals with business logic related to User entity
 *
 * @author chaitraw
 */
public class UserService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private PasswordCryptographyProvider cryptographyProvider;

    @Transactional(propagation = Propagation.REQUIRED)
    public UserEntity createUser(UserEntity userEntity) {
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
    public String signOut(final String username, final String password, final String accessToken) throws SignOutRestrictedException, AuthenticationFailedException {
        UserAuthEntity userAuthEntity = userDao.getUserAuthToken(accessToken);
        if (userAuthEntity == null) {
            throw new AuthenticationFailedException("SGR-001", "User is not Signed in");
        }

        final LocalDateTime now = LocalDateTime.now();
        UserEntity userEntity = userDao.getUserByUserName(username);
        if (userEntity == null) {
            throw new AuthenticationFailedException("ATH-001", "User with email not found");
        }

        userAuthEntity.setLogoutAt(now);
        userDao.updateAuthToken(userAuthEntity);
        return userEntity.getUuid();
    }
}
