package edu.coldrain.spring_subject1.exhandler;

import lombok.AllArgsConstructor;
import lombok.Data;

// TODO: 2022-05-31 API 예외 처리 연습용 
@Data
@AllArgsConstructor
public class ErrorResult {
    
    private String code;
    private String message;
}
