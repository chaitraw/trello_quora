package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.QuestionDao;
import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.entity.UserAuthEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class QuestionService {

    @Autowired
    private QuestionDao questionDao;
    @Autowired
    private UserDao userDao;

    public QuestionEntity createQuestion(String accessToken, QuestionEntity questionEntity) throws AuthorizationFailedException {
        UserAuthEntity userAuthEntity = userDao.getUserAuthToken(accessToken);
        if(userAuthEntity == null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        }
        if(userAuthEntity.getLoginAt().isBefore(userAuthEntity.getLogoutAt())) {
            throw new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to post a question");
        }
        questionEntity.setUser(userDao.getUser(String.valueOf(userAuthEntity.getUuid())));
        return questionDao.createQuestion(questionEntity);
    }
}
