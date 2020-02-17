package com.upgrad.quora.service.dao;

import com.upgrad.quora.service.entity.QuestionEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class QuestionDao {

    @PersistenceContext
    private EntityManager entityManager;

    public QuestionEntity createQuestion(QuestionEntity questionEntity) {
        entityManager.persist(questionEntity);
        return questionEntity;
    }

    public QuestionEntity getQuestionById(final String questionUuid) {
        try {
            return entityManager.createNamedQuery("questionByUuid", QuestionEntity.class).setParameter("uuid", questionUuid)
                    .getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    public List<QuestionEntity> getAllQuestions() {
        try {
            return entityManager.createNamedQuery("getAllQuestion", QuestionEntity.class)
                    .getResultList();
        } catch (NoResultException nre) {
            return null;
        }
    }

    public QuestionEntity updateQuestion(QuestionEntity questionEntity) {
        entityManager.merge(questionEntity);
        return  questionEntity;
    }

    public QuestionEntity deleteQuestion(QuestionEntity questionEntityFromDB) {
        entityManager.remove(questionEntityFromDB);
        return questionEntityFromDB;
    }

    public List<QuestionEntity> getAllQuestionsByUser(String userId) {
        try {
            return entityManager.createNamedQuery("getAllQuestionByUser", QuestionEntity.class).setParameter("uuid", userId)
                    .getResultList();
        } catch (NoResultException nre) {
            return null;
        }
    }
}
