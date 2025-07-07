package com.hushunjian.jooq.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class AnswerInfo {

    @JsonProperty("Data")
    private List<AnswerInfo1> Data;

    @Data
    public static class AnswerInfo1 {

        @JsonProperty("tempId")
        private String tempId;

        @JsonProperty("SubjectiveAnswer")
        private String subjectiveAnswer;

        @JsonProperty("CompanyId")
        private String companyId;

        @JsonProperty("ListQuestionAnswerList")
        private List<AnswerInfo2> ListQuestionAnswerList;


        @JsonProperty("CreateTime")
        private Date createTime;

        @JsonProperty("TestTime")
        private Date testTime;

        @JsonProperty("QuestionBankId")
        private String questionBankId;

        @JsonProperty("QuestionType")
        private Integer questionType;

        @JsonProperty("TotalCount")
        private Integer totalCount;

        @JsonProperty("BorderLine")
        private Integer borderLine;

        @JsonProperty("CreateUserId")
        private String createUserId;

        @JsonProperty("Answer")
        private String answer;

        @JsonProperty("UserAnswer")
        private String userAnswer;

        @JsonProperty("Score")
        private Integer score;

        @JsonProperty("EmployeeCoursePlanId")
        private String employeeCoursePlanId;

        @JsonProperty("CourseId")
        private String courseId;

        @JsonProperty("AnswerChoice")
        private String answerChoice;

        @JsonProperty("QuestionId")
        private String questionId;

        @JsonProperty("Content")
        private String content;

        @JsonProperty("Analysis")
        private String analysis;


        @JsonProperty("ChoiceAnswer")
        private String choiceAnswer;


        @JsonProperty("UpdateTime")
        private Date updateTime;

        @JsonProperty("UpdateUserId")
        private String updateUserId;


        @JsonProperty("Id")
        private String id;


        @JsonProperty("SortAnswer")
        private String sortAnswer;

        @JsonProperty("IsTest")
        private Integer isTest;


        @JsonProperty("KnowledgepointId")
        private String knowledgepointId;


        @JsonProperty("IsCorrect")
        private String isCorrect;

    }

    @Data
    public static class AnswerInfo2 {

        @JsonProperty("AnswerChoice")
        private String AnswerChoice;

        @JsonProperty("SubjectiveAnswer")
        private String subjectiveAnswer;

        @JsonProperty("CompanyId")
        private String companyId;

        @JsonProperty("QuestionID")
        private String questionID;

        @JsonProperty("IsAnswer")
        private Integer IsAnswer;

        @JsonProperty("SortID")
        private String sortID;

        @JsonProperty("Id")
        private String id;
    }

    @Data
    public static class SaveDTO {

        @JsonProperty("OpenType")
        private Integer OpenType = 0;

        @JsonProperty("QuestionDtos")
        private List<AnswerInfo1> questionDtos;

        @JsonProperty("TestType")
        private Integer TestType = 1;

    }
}
