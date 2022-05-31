package edu.coldrain.spring_subject1.exhandler.advice;

import edu.coldrain.spring_subject1.controller.ApiExceptionController;
import edu.coldrain.spring_subject1.exhandler.ErrorResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// TODO: 2022-05-31 API 예외 처리 연습용 컨트롤 어드바이스
// @ControllerAdvice 는 대상으로 지정한 컨트롤러에 @ExceptionHandler, @InitBinder 기능을 부여해준다.
// 대상을 지정하지 않으면 모든 컨트롤러에 적용된다.
// @RestControllerAdvice 는 @ControllerAdvice + @ResponseBody 이다.
// @RestControllerAdvice(annotations = RestController.class) -> @RestController 이 붙은 컨트롤러 모두 적용
// @RestControllerAdvice("org.example.controllers") -> 패키지와 패키지의 하위 패키지 컨트롤러에 모두 적용
// @RestControllerAdvice(assignableTypes = {ApiExceptionController.class}) -> 적용 컨트롤러 직접 지정
// 부모 클래스를 지정하면 자식 클래스도 모두 적용된다.
@Slf4j
@RestControllerAdvice(assignableTypes = {ApiExceptionController.class})
public class ExceptionControllerAdvice {
    // @ExceptionHandler 를 사용하면
    // 1. 스프링의 ExceptionHandlerExceptionResolver 가 동작한다.
    // ExceptionHandlerExceptionResolver 가 @ExceptionHandler 애노테이션이 있는지 스캔해 본다.
    // 2. 있으면 @ExceptionHandler 애노테이션이 붙은 핸들러를 호출한다.
    // 제공해주는 예외 처리중 우선순위가 가장 높다.
    // 실무에서는 주로 @ExceptionHandler + @ControllerAdvice 로 API 예외를 처리한다.

    // ApiExceptionController 에서 발생한 IllegalArgumentException 이랑 그의 자식 예외들이 처리된다.

    private static final String EXCEPTION_HANDLER_LOG_PREFIX = "[exceptionHandler] ex";

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public ErrorResult illegalArgumentExceptionHandler(IllegalArgumentException e) {
        log.error(EXCEPTION_HANDLER_LOG_PREFIX, e);
        return new ErrorResult("BAD", e.getMessage()); // JSON 응답
    }

    @ExceptionHandler // 예외 클래스를 제외해도 된다. ( 매개변수로 확인함 )
    public ResponseEntity<ErrorResult> runtimeExceptionHandler(RuntimeException e) {
        log.error(EXCEPTION_HANDLER_LOG_PREFIX, e);
        ErrorResult errorResult = new ErrorResult("USER", e.getMessage());
        return new ResponseEntity<>(errorResult, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler  // 이외의 모든 예외는 500번으로 처리
    public ErrorResult exHandler(Exception e) {
        log.error(EXCEPTION_HANDLER_LOG_PREFIX, e);
        return new ErrorResult("EX", e.getMessage());
    }
}