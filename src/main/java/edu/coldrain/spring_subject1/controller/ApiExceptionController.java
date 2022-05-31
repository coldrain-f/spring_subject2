package edu.coldrain.spring_subject1.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

// TODO: 2022-05-31 API 예외 처리 연습용
@Slf4j
@RestController
public class ApiExceptionController {

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