package edu.coldrain.spring_subject1.exhandler.advice;

import edu.coldrain.spring_subject1.controller.CommentApiController;
import edu.coldrain.spring_subject1.exception.AuthenticationException;
import edu.coldrain.spring_subject1.exhandler.ErrorResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(assignableTypes = {CommentApiController.class})
public class CommentApiControllerAdvice {

    @ExceptionHandler
    public ErrorResult illegalArgumentExceptionHandler(IllegalArgumentException e) {
        return new ErrorResult(HttpStatus.BAD_REQUEST.getReasonPhrase(), e.getMessage());
    }

    @ExceptionHandler
    public ErrorResult authenticationException(AuthenticationException e) {
        return new ErrorResult(HttpStatus.UNAUTHORIZED.getReasonPhrase(), e.getMessage());
    }
}
