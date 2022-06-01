package edu.coldrain.spring_subject1.controller;

import edu.coldrain.spring_subject1.domain.User;
import edu.coldrain.spring_subject1.dto.LoginDto;
import edu.coldrain.spring_subject1.dto.TokenDto;
import edu.coldrain.spring_subject1.exception.AuthenticationException;
import edu.coldrain.spring_subject1.exhandler.ErrorResult;
import edu.coldrain.spring_subject1.jwt.JwtFilter;
import edu.coldrain.spring_subject1.jwt.TokenProvider;
import edu.coldrain.spring_subject1.service.UserService;
import edu.coldrain.spring_subject1.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final TokenProvider tokenProvider;

    private final UserService userService;

    private final PasswordEncoder passwordEncoder;

    // TODO: 2022-05-30  AuthenticationManagerBuilder 가 무슨 역할을 하는지 자세히 알아보기
    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    @ExceptionHandler
    public ErrorResult illegalArgumentException(IllegalArgumentException e) {
        return new ErrorResult(HttpStatus.BAD_REQUEST.getReasonPhrase(), e.getMessage());
    }

    @PostMapping("/authenticate")
    public ResponseEntity<TokenDto> authorize(@RequestBody @Valid LoginDto loginDto) {
        // TODO: 2022-06-01 Validator 로 검증로직 분리 예정
        //====================================================================
        String username = loginDto.getUsername();
        String password = loginDto.getPassword();

        Optional<String> currentUsername = SecurityUtil.getCurrentUsername();
        if (currentUsername.isPresent() && !currentUsername.get().equals("anonymousUser")) {
            throw new IllegalArgumentException("이미 로그인이 되어있습니다.");
        }

        Optional<User> found = userService.findByUsername(username);
        if (found.isEmpty()) {
            throw new IllegalArgumentException("닉네임 또는 패스워드를 확인해주세요.");
        }

        // 꼭! matches 로 비교해야 함! ( 직접 암호화하여 비교 X )
        if (!passwordEncoder.matches(password, found.get().getPassword())) {
            throw new IllegalArgumentException("닉네임 또는 패스워드를 확인해주세요.");
        }
        //====================================================================

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword());

        // authenticationToken 을 이용해서 authenticate() 메소드를 호출하면
        // UserDetailsService 의 loadUserByUsername() 이 호출되고
        // 그 결과를 가지고 authentication 객체를 생성한다.
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        // 시큐리티 컨텍스트에 저장
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 인증 정보를 가지고 JWT Token 을 생성한다.
        String jwt = tokenProvider.createToken(authentication);

        // 토큰을 헤더에 저장
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(JwtFilter.AUTHORIZATION_HEADER, "Bearer " + jwt);

        return new ResponseEntity<>(new TokenDto(jwt), httpHeaders, HttpStatus.OK);
    }
}