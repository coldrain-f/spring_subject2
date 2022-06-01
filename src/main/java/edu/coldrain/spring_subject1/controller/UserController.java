package edu.coldrain.spring_subject1.controller;

import edu.coldrain.spring_subject1.dto.UserDto;
import edu.coldrain.spring_subject1.exhandler.ErrorResult;
import edu.coldrain.spring_subject1.repository.UserRepository;
import edu.coldrain.spring_subject1.service.UserService;
import edu.coldrain.spring_subject1.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @ExceptionHandler
    public ErrorResult methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e) {
        return new ErrorResult(HttpStatus.BAD_REQUEST.getReasonPhrase(),
                e.getBindingResult().getAllErrors().get(0).getDefaultMessage());
    }

    @ExceptionHandler
    public ErrorResult illegalArgumentExceptionHandler(IllegalArgumentException e) {
        return new ErrorResult(HttpStatus.BAD_REQUEST.getReasonPhrase(), e.getMessage());
    }

    @PostMapping("/signup")
    public ResponseEntity<UserDto> signup(@Valid @RequestBody UserDto userDto) {
        // TODO: 2022-06-01 Validator 로 검증 로직 분리 예정
        //===========================================================================
        String username = userDto.getUsername();
        String password = userDto.getPassword();
        String confirmPassword = userDto.getConfirmPassword();

        Optional<String> currentUsername = SecurityUtil.getCurrentUsername();
        if (currentUsername.isPresent() && !currentUsername.get().equals("anonymousUser")) {
            throw new IllegalArgumentException("이미 로그인이 되어있습니다.");
        }

        if (password.contains(username)) {
            throw new IllegalArgumentException("비밀번호는 닉네임이 포함될 수 없습니다.");
        }

        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("비밀번호와 비밀번호 확인은 정확히 일치해야 합니다.");
        }

        if (userService.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("중복된 닉네임입니다.");
        }
        //===========================================================================

        UserDto user = userService.signup(userDto);
        return ResponseEntity.ok(user);
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
