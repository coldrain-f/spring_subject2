package edu.coldrain.spring_subject1.config;

import edu.coldrain.spring_subject1.jwt.JwtSecurityConfig;
import edu.coldrain.spring_subject1.jwt.JwtAccessDeniedHandler;
import edu.coldrain.spring_subject1.jwt.JwtAuthenticationEntryPoint;
import edu.coldrain.spring_subject1.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.CorsFilter;

//===================================================
// @EnableWebSecurity: 기본적인 Web 보안을 활성화 하는 애노테이션이다.
// 추가적인 설정을 하려면 WebSecurityConfigurer 를 implements 하거나
// WebSecurityConfigurerAdapter 를 extends 하여 사용해야 한다.
// debug = true 로 설정하면 시큐리티 디버깅이 가능하다. ( chain 확인 가능 ) -> 확인해보기
@EnableWebSecurity
//===================================================

//===================================================
// @PreAuthorize 애노테이션을 메소드 단위로 추가하기 위햐서 사용
@EnableGlobalMethodSecurity(prePostEnabled = true)
//===================================================
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    // 토큰을 생성하고 유효성 검사하는 객체
    private final TokenProvider tokenProvider;
    private final CorsFilter corsFilter;
    // 로그인 안 하고 접근하면 401 에러를 반환하는 객체
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    // 권한이 없는데 접근하면 403 에러를 반환하는 객체
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    public void configure(WebSecurity web) {

        //===================================================
        // ignoring(): 여기서 설정한 요청들은 스프링 시큐리티 로직을 수행하지 않음
        // Security filter chain 을 적용할 필요가 없을 때 사용한다. -> 알아보기
        web.ignoring()
                .antMatchers("/h2-console/**"
                        , "/favicon.ico"
                        , "/error"
                );
        //===================================================
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http    // token 을 사용하는 방식이기 때문에 csrf 를 disable 합니다.
                .csrf().disable()

                .addFilterBefore(corsFilter, UsernamePasswordAuthenticationFilter.class)

                //=============================================================
                // 예외를 핸들링 할 떄 사용할 클래스들을 추가해 준다.
                .exceptionHandling()
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(jwtAccessDeniedHandler)
                //=============================================================

                // enable h2-console
                .and()
                .headers()
                .frameOptions()
                .sameOrigin()

                //===================================================
                // 세션을 사용하지 않기 때문에 STATELESS 로 설정
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                //===================================================

                //===================================================
                // authorizeRequests: HttpServletRequest 를 사용하는 요청들에 대한 접근제한 설정
                // permitAll(): 인증(로그인)을 받지 않아도 접근 가능하도록 설정
                .and()
                .authorizeRequests()
                .antMatchers("/api/authenticate").permitAll() // 로그인 API 요청 허용
                .antMatchers("/api/signup").permitAll() // 회원가입 API 요청 허용

                // TODO: 2022-05-29 테스트를 위해서 임시적으로 모든 API 권한을 풀어둠. ( 삭제 예정 )
                .antMatchers("/api/**").permitAll()
                //===================================================

                //===================================================
                // anyRequest(): 이외의 모든 요청
                // authenticated(): 인증(로그인)을 받아야 접근할 수 있도록 설정
                .anyRequest().authenticated()
                //===================================================

                //===================================================
                // JwtFilter 를 addFilterBefore 로 등록했던 JwtSecurityConfig 클래스 적용
                .and()
                .apply(new JwtSecurityConfig(tokenProvider));
                //===================================================
    }
}
