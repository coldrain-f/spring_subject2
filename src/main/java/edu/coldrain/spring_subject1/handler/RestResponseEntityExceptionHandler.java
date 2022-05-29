package edu.coldrain.spring_subject1.handler;

import static org.springframework.http.HttpStatus.CONFLICT;

import edu.coldrain.spring_subject1.dto.ErrorDTO;
import edu.coldrain.spring_subject1.exception.DuplicateMemberException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    @ResponseStatus(CONFLICT)
    @ExceptionHandler(value = { DuplicateMemberException.class })
    @ResponseBody
    protected ErrorDTO badRequest(RuntimeException ex, WebRequest request) {
        return new ErrorDTO(CONFLICT.value(), ex.getMessage());
    }
}
