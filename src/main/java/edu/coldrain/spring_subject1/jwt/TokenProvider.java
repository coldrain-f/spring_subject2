package edu.coldrain.spring_subject1.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

//===================================================
// TokenProvider 토큰의 생성과 토큰의 유효성 검증을 담당
//===================================================
@Slf4j
@Component
public class TokenProvider implements InitializingBean {

    private static final String AUTHORITIES_KEY = "auth";

    private final String secret;
    private final long tokenValidityInMilliseconds;

    private Key key;

    public TokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.token-validity-in-seconds}") long tokenValidityInSeconds) {
        this.secret = secret;
        this.tokenValidityInMilliseconds = tokenValidityInSeconds * 1000;
    }

    //===================================================
    // afterPropertiesSet() 메서드는
    // InitializingBean 인터페이스의 추상 메서드이다.
    // BeanFactory 에 의해 모든 property 가 설정되고 난 뒤에 실행되는 메서드이다.
    //===================================================
    @Override
    public void afterPropertiesSet() {
        // 빈이 생성되고 생성자 주입을 받은 후에 시크릿 값을 BASE64 decode 해서 key 변수에 할당
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }


    //===================================================
    // Authentication 객체의 권한정보를 이용해서 토큰을 생성하는 메서드이다.
    // 스프링 시큐리티에서 한 유저의 인증 정보를 가지고 있는 객체,
    // 사용자가 인증 과정을 성공적으로 마치면, 스프링 시큐리티는 사용자의 정보 및 인증 성공여부를 가지고
    // Authentication 객체를 생성한 후 보관한다.
    //===================================================
    public String createToken(Authentication authentication) {
        // 권한 설정
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        // 토큰의 만료시간 설정
        long now = new Date().getTime();
        Date validity = new Date(now + this.tokenValidityInMilliseconds);

        // 토큰 생성
        return Jwts.builder()
                .setSubject(authentication.getName())
                .claim(AUTHORITIES_KEY, authorities)
                .signWith(key, SignatureAlgorithm.HS512)
                .setExpiration(validity)
                .compact();
    }

    //===================================================
    // 토큰에 담겨있는 정보를 이용해서 Authentication 객체를 반환하는 메소드
    //===================================================
    public Authentication getAuthentication(String token) {
        Claims claims = Jwts
                .parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        User principal = new User(claims.getSubject(), "", authorities);

        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    //===================================================
    // 토큰의 유효성 검증을 하는 메서드
    //===================================================
    public boolean validateToken(String token) {
        try {
            // TODO: 2022-05-30 각각 무엇을 의미하는지 알아보기
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.info("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.info("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.info("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }
}