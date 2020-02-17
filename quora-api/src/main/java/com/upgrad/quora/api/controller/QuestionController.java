package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.*;
import com.upgrad.quora.service.business.QuestionService;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @RequestMapping(method = RequestMethod.POST, path = "question/create", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<QuestionResponse> createQuestion(@RequestHeader("authorization") final String authorization, final QuestionRequest questionRequest) throws AuthorizationFailedException {
        String accessToken = authorization.split("Bearer ")[0];

        QuestionEntity questionEntity = new QuestionEntity();
        questionEntity.setUuid(UUID.randomUUID().toString());
        questionEntity.setContent(questionRequest.getContent());
        questionEntity.setDate(LocalDateTime.now());

        final QuestionEntity createdQuestionEntity = questionService.createQuestion(accessToken, questionEntity);

        QuestionResponse questionResponse = new QuestionResponse().id(createdQuestionEntity.getUuid()).status("QUESTION CREATED");
        return new ResponseEntity<QuestionResponse>(questionResponse, HttpStatus.CREATED);
    }

    @RequestMapping(method = RequestMethod.GET, path = "question/all", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<List<QuestionDetailsResponse>> getAllQuestion(@RequestHeader("authorization") final String authorization) throws AuthorizationFailedException {
        String accessToken = authorization.split("Bearer ")[0];

        final List<QuestionEntity> questionEntities = questionService.getAllQuestions(accessToken);

        List<QuestionDetailsResponse> questionDetailsResponses = null;

        if(!questionEntities.isEmpty()) {
            questionDetailsResponses = new ArrayList<>();
            for (QuestionEntity questionEntity : questionEntities) {
                QuestionDetailsResponse questionDetailsResponse = new QuestionDetailsResponse();
                questionDetailsResponse.setId(questionEntity.getUuid());
                questionDetailsResponse.setContent(questionEntity.getContent());
                questionDetailsResponses.add(questionDetailsResponse);
            }
        }
        return new ResponseEntity<List<QuestionDetailsResponse>>(questionDetailsResponses, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.PUT, path = "/question/edit/{questionId}", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<QuestionEditResponse> editQuestion(@PathVariable("questionId") final String questionId, @RequestHeader("authorization") final String authorization, final QuestionEditRequest questionEditRequest) throws AuthorizationFailedException, InvalidQuestionException {
        String accessToken = authorization.split("Bearer ")[0];

        QuestionEntity questionEntity = new QuestionEntity();
        questionEntity.setContent(questionEditRequest.getContent());

        final QuestionEntity editedQuestionEntity = questionService.editQuestion(accessToken, questionId, questionEntity);

        QuestionEditResponse questionEditResponse = new QuestionEditResponse().id(editedQuestionEntity.getUuid()).status("QUESTION EDITED");
        return new ResponseEntity<QuestionEditResponse>(questionEditResponse, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.DELETE, path = "/question/delete/{questionId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<QuestionDeleteResponse> deleteQuestion(@PathVariable("questionId") final String questionId,
                                                         @RequestHeader("authorization") final String authorization) throws AuthorizationFailedException, InvalidQuestionException {
        String [] bearerToken = authorization.split("Bearer ");
        final QuestionEntity questionEntity = questionService.deleteQuestion(questionId, bearerToken[0]);
        QuestionDeleteResponse questionDeleteResponse = new QuestionDeleteResponse().id(questionEntity.getUuid()).status("QUESTION DELETED");
        return new ResponseEntity<QuestionDeleteResponse>(questionDeleteResponse, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, path = "question/all/{userId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<List<QuestionDetailsResponse>> getAllQuestionByUser(@PathVariable("userId")String userId, @RequestHeader("authorization") final String authorization) throws AuthorizationFailedException, UserNotFoundException {
        String accessToken = authorization.split("Bearer ")[0];

        final List<QuestionEntity> questionEntities = questionService.getAllQuestionsByUser(accessToken, userId);

        List<QuestionDetailsResponse> questionDetailsResponses = null;

        if(!questionEntities.isEmpty()) {
            questionDetailsResponses = new ArrayList<>();
            for (QuestionEntity questionEntity : questionEntities) {
                QuestionDetailsResponse questionDetailsResponse = new QuestionDetailsResponse();
                questionDetailsResponse.setId(questionEntity.getUuid());
                questionDetailsResponse.setContent(questionEntity.getContent());
                questionDetailsResponses.add(questionDetailsResponse);
            }
        }
        return new ResponseEntity<List<QuestionDetailsResponse>>(questionDetailsResponses, HttpStatus.OK);
    }
}
