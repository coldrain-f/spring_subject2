package edu.coldrain.spring_subject1.controller;

import edu.coldrain.spring_subject1.dto.UserDto;
import edu.coldrain.spring_subject1.repository.UserRepository;
import edu.coldrain.spring_subject1.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    @PostMapping("/test-redirect")
    public void testRedirect(HttpServletResponse response) throws IOException {
        response.sendRedirect("/api/user");
    }

    // TODO: 2022-05-30 UserDTO 와 에러 리스트를 응답으로 보내주도록 변경하기
    @PostMapping("/signup") // 원래 String -> UserDto 였음.
    public ResponseEntity<String> signup(@Valid @RequestBody UserDto userDto, BindingResult bindingResult) {
        String password = userDto.getPassword();
        String confirmPassword = userDto.getConfirmPassword();
        String username = userDto.getUsername();
        String objectName = "userDto";

        // TODO: 2022-05-30 Validation 을 따로 빼는 방법은? -> 알아보기
        // TODO: 2022-05-30 비밀번호는 닉네임과 같은 값이 포함된 경우 회원가입 실패
        if (password.contains(username)) {
            String errorMessage = "비밀번호에 닉네임과 같은 값이 포함되어 있습니다.";
            bindingResult.addError(new FieldError(objectName, "password", errorMessage));
        }
        // TODO: 2022-05-30 비밀번호 확인은 비밀번호와 정확하게 일치해야 한다.
        if (!password.equals(confirmPassword)) {
            String errorMessage = "비밀번호와 비밀번호 확인이 일치하지 않습니다.";
            bindingResult.addError(new FieldError(objectName, "confirmPassword", errorMessage));
        }

        // TODO: 2022-05-30 데이터베이스에 존재하는 닉네임이라면  "중복된 닉네임입니다." 메시지 응답에 포함하기
        boolean present = userRepository.findByUsername(username).isPresent();
        if (present) {
            String errorMessage = "중복된 닉네임입니다.";
            bindingResult.addError(new FieldError(objectName, "username", errorMessage));
            return ResponseEntity.badRequest().body("중복된 닉네임입니다.");
        }

        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body("fail");
        }

        return ResponseEntity.ok("success");
    }

    @GetMapping("/user")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<UserDto> getMyUserInfo(HttpServletRequest request) {
        return ResponseEntity.ok(userService.getMyUserWithAuthorities());
    }

    @GetMapping("/user/{username}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<UserDto> getUserInfo(@PathVariable String username) {
        return ResponseEntity.ok(userService.getUserWithAuthorities(username));
    }
}
