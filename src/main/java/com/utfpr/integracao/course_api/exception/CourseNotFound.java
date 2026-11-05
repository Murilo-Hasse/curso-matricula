package com.utfpr.integracao.course_api.exception;

public class CourseNotFound extends RuntimeException{

    public CourseNotFound(String message) {
        super(message);
    }
}
