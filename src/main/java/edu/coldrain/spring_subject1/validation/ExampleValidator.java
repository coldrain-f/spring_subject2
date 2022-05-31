package edu.coldrain.spring_subject1.validation;


import edu.coldrain.spring_subject1.domain.Comment;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.ResponseBody;

// TODO: 2022-05-31 스프링 Validator 연습
// 컨트롤러에서 @InitBinder 를 통해서 사용하면 된다.
/*
    private final ExampleValidator exampleValidator;

    @InitBinder
    public void init(WebDataBinder dataBinder) {
        dataBinder.addValidators(exampleValidator);
    }
 */
// 컨트롤러 요청이 올 때마다 항상 호출한다. ( 핸들러가 호출되기 전에 @InitBinder 가 먼저 호출 )
// 핸들러에서 @Validated Comment comment 형태로 사용해야 한다. 꼭 @Validated 나 @Valid 필요
// @Validated 가 스프링에서 제공 @Valid 는 자바에서 제공
// @Validated 는 Bean validation 사용시 groups 라는 기능을 추가로 사용 할 수 있다.
@Component
public class ExampleValidator implements Validator {
    
    @Override
    public boolean supports(Class<?> clazz) {
        // 검증대상 체크
        return Comment.class.isAssignableFrom(clazz);
        // comment == clazz 인지 체크
        // comment.class == comment.class
        // comment.class == comment 의 자식 클래스도 통과
    }

    @Override // 실제 검증 부분
    public void validate(Object target, Errors errors) {
        Comment comment = (Comment) target;

        if (!comment.getAuthor().equals("admin")) {
            // 필드와 에러코드를 순서대로...
            errors.rejectValue("author", "is not admin");
        }
    }
}
