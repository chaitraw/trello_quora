package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.AnswerDao;
import com.upgrad.quora.service.dao.QuestionDao;
import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.AnswerEntity;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.entity.UserAuthEntity;
import com.upgrad.quora.service.exception.AnswerNotFoundException;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AnswerService {

    @Autowired
    private AnswerDao answerDao;

    @Autowired
    private QuestionDao questionDao;

    @Autowired
    private UserDao userDao;

    @Transactional(propagation = Propagation.REQUIRED)
    public AnswerEntity createAnswer(String questionId, AnswerEntity answerEntity, String accessToken) throws AuthorizationFailedException, InvalidQuestionException {
        UserAuthEntity userAuthEntity = userDao.getUserAuthToken(accessToken);
        if (userAuthEntity == null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        }

        QuestionEntity questionEntity = questionDao.getQuestionById(questionId);
        if(questionEntity == null) {
            throw new InvalidQuestionException("QUES-001", "The question entered is invalid");
        }

        if(userAuthEntity.getLoginAt().isBefore(userAuthEntity.getLogoutAt())) {
            throw new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to post an answer");
        }

        answerEntity.setDate(LocalDateTime.now());
        answerEntity.setUser(userAuthEntity.getUser());
        answerEntity.setQuestion(questionEntity);
        return answerDao.createAnswer(answerEntity);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public AnswerEntity editAnswer(String answerId, AnswerEntity answerEntity, String accessToken) throws AuthorizationFailedException, AnswerNotFoundException {
        UserAuthEntity userAuthEntity = userDao.getUserAuthToken(accessToken);
        if (userAuthEntity == null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        }

        AnswerEntity answerEntityById = answerDao.getAnswerById(answerId);
        if(answerEntityById == null) {
            throw new AnswerNotFoundException("ANS-001", "Entered answer uuid does not exist");
        }

        if(userAuthEntity.getLoginAt().isBefore(userAuthEntity.getLogoutAt())) {
            throw new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to edit an answer");
        }

        if (!answerEntity.getUuid().equals(userAuthEntity.getUuid())) {
            throw new AuthorizationFailedException("ATHR-003", "Only the answer owner can edit the answer");
        }

        answerEntityById.setAns(answerEntity.getAns());
        answerEntityById.setDate(LocalDateTime.now());
        return answerDao.updateAnswer(answerEntityById);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public AnswerEntity deleteAnswer(String answerId, String accessToken) throws AuthorizationFailedException, AnswerNotFoundException {
        UserAuthEntity userAuthEntity = userDao.getUserAuthToken(accessToken);

        if(userAuthEntity == null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        }

        if(userAuthEntity.getLoginAt().isBefore(userAuthEntity.getLogoutAt())) {
            throw new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to delete an answer");
        }

        AnswerEntity answerEntity = answerDao.getAnswerById(answerId);
        if(answerEntity == null) {
            throw new AnswerNotFoundException("ANS-001", "Entered answer uuid does not exist");
        }

        if(!(answerEntity.getUser().getUuid().equals(userAuthEntity.getUuid())) && !(answerEntity.getUser().getRole().equals("ADMIN"))) {
            throw new AuthorizationFailedException("ATHR-003", "Only the answer owner or admin can delete the answer");
        }
        return answerDao.deleteAnswer(answerEntity);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public List<AnswerEntity> getAllAnswersByQuestionId(String accessToken, String questionId) throws AuthorizationFailedException, InvalidQuestionException {
        UserAuthEntity userAuthEntity = userDao.getUserAuthToken(accessToken);
        if(userAuthEntity == null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        }
        if(userAuthEntity.getLoginAt().isBefore(userAuthEntity.getLogoutAt())) {
            throw new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to get the answers");
        }
        QuestionEntity questionEntity = questionDao.getQuestionById(questionId);
        if(questionEntity == null) {
            throw new InvalidQuestionException("QUES-001", "The question with entered uuid whose details are to be seen does not exist");
        }
        return answerDao.getAllAnswersByQuestionId(questionId);
    }
}
