package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.*;
import com.upgrad.quora.service.business.AnswerService;
import com.upgrad.quora.service.entity.AnswerEntity;
import com.upgrad.quora.service.exception.AnswerNotFoundException;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
public class AnswerController {

    @Autowired
    private AnswerService answerService;

    /**
     * This endpoint is used to create an answer to a particular question. Any user can access this endpoint.
     *
     * @param questionId
     * @param authorization
     * @param answerRequest
     * @return
     * @throws AuthorizationFailedException
     * @throws InvalidQuestionException
     */
    @RequestMapping(method = RequestMethod.POST, path = "/question/{questionId}/answer/create", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<AnswerResponse> createAnswer(@PathVariable("questionId") String questionId, @RequestHeader("authorization") final String authorization, final AnswerRequest answerRequest) throws AuthorizationFailedException, InvalidQuestionException {
        String accessToken = authorization.split("Bearer ")[0];

        AnswerEntity answerEntity = new AnswerEntity();
        answerEntity.setAns(answerRequest.getAnswer());
        answerEntity.setUuid(UUID.randomUUID().toString());
        AnswerEntity answerEntityResponse = answerService.createAnswer(questionId, answerEntity, accessToken);

        AnswerResponse answerResponse = new AnswerResponse();
        answerResponse.setId(answerEntityResponse.getUuid());
        answerResponse.setStatus("ANSWER CREATED");

        return new ResponseEntity<AnswerResponse>(answerResponse, HttpStatus.CREATED);
    }

    /**
     * This endpoint is used to edit an answer. Only the owner of the answer can edit the answer.
     *
     * @param answerId
     * @param authorization
     * @param answerEditRequest
     * @return
     * @throws AuthorizationFailedException
     * @throws AnswerNotFoundException
     */
    @RequestMapping(method = RequestMethod.PUT, path = "/answer/edit/{answerId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<AnswerEditResponse> editAnswer(@PathVariable("answerId") String answerId, @RequestHeader("authorization") final String authorization, final AnswerEditRequest answerEditRequest)
        throws AuthorizationFailedException, AnswerNotFoundException {
        String accessToken = authorization.split("Bearer ")[0];

        AnswerEntity answerEntity = new AnswerEntity();
        answerEntity.setAns(answerEditRequest.getContent());
        AnswerEntity answerEntityResponse = answerService.editAnswer(answerId, answerEntity, accessToken);

        AnswerEditResponse answerResponse = new AnswerEditResponse();
        answerResponse.setId(answerEntityResponse.getUuid());
        answerResponse.setStatus("ANSWER EDITED");

        return new ResponseEntity<AnswerEditResponse>(answerResponse, HttpStatus.OK);
    }

    /**
     * This endpoint is used to delete an answer. Only the owner of the answer or admin can delete an answer.
     *
     * @param answerId
     * @param authorization
     * @return
     * @throws AuthorizationFailedException
     * @throws AnswerNotFoundException
     */
    @RequestMapping(method = RequestMethod.DELETE, path = "/answer/delete/{answerId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<AnswerDeleteResponse> deleteAnswer(@PathVariable("answerId") final String answerId,
        @RequestHeader("authorization") final String authorization) throws AuthorizationFailedException, AnswerNotFoundException {
        String accessToken = authorization.split("Bearer ")[0];
        final AnswerEntity answerEntity = answerService.deleteAnswer(answerId, accessToken);

        AnswerDeleteResponse answerDeleteResponse = new AnswerDeleteResponse().id(answerEntity.getUuid()).status("ANSWER DELETED");
        return new ResponseEntity<AnswerDeleteResponse>(answerDeleteResponse, HttpStatus.OK);
    }

    /**
     * This endpoint is used to get all answers to a particular question. Any user can access this endpoint.
     *
     * @param questionId
     * @param authorization
     * @return
     * @throws AuthorizationFailedException
     * @throws InvalidQuestionException
     */
    @RequestMapping(method = RequestMethod.GET, path = "answer/all/{questionId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<List<AnswerDetailsResponse>> getAllAnswersByQuestionId(@PathVariable("questionId")String questionId, @RequestHeader("authorization") final String authorization)
        throws AuthorizationFailedException, InvalidQuestionException {
        String accessToken = authorization.split("Bearer ")[0];

        final List<AnswerEntity> answerEntities = answerService.getAllAnswersByQuestionId(accessToken, questionId);

        List<AnswerDetailsResponse> answerDetailsResponseList = null;

        if(!answerEntities.isEmpty()) {
            answerDetailsResponseList = new ArrayList<>();
            for (AnswerEntity answerEntity : answerEntities) {
                AnswerDetailsResponse answerDetailsResponse = new AnswerDetailsResponse();
                answerDetailsResponse.setId(answerEntity.getUuid());
                answerDetailsResponse.setAnswerContent(answerEntity.getAns());
                answerDetailsResponse.setQuestionContent(answerEntity.getQuestion().getContent());
                answerDetailsResponseList.add(answerDetailsResponse);
            }
        }
        return new ResponseEntity<List<AnswerDetailsResponse>>(answerDetailsResponseList, HttpStatus.OK);
    }
}
