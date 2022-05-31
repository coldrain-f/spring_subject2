package edu.coldrain.spring_subject1.controller;

import edu.coldrain.spring_subject1.exhandler.ErrorResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// TODO: 2022-05-31 API 예외 처리 연습용
@Slf4j
@RestController
public class ApiExceptionController {
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

    @GetMapping("/api/members/{id}")
    public MemberDTO getMember(@PathVariable("id") String id) {
        // 아무런 설정이 없으면 스프링 부트는 BasicErrorController 를 사용해서 응답한다.

        if (id.equals("user")) {
            throw new RuntimeException("RuntimeException");
        }
        if (id.equals("bad")) {
            throw new IllegalArgumentException("IllegalArgumentException");
        }

        return new MemberDTO(id, "hello " + id);
    }

    @Data
    @AllArgsConstructor
    static class MemberDTO {
        private String id;
        private String name;
    }
    
}