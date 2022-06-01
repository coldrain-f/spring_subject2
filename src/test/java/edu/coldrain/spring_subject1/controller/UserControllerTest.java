package edu.coldrain.spring_subject1.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void 테스트() throws Exception {
        mockMvc.perform(get("/api/boards"))
                .andExpect(status().is2xxSuccessful())
                .andDo(print());
    }

    @Test
    public void 회원가입_사용자_이름_최소_3자이상_실패_테스트() throws Exception {
        // given
        Map<String, String> content = new LinkedHashMap<>();
        content.put("username", "us");
        content.put("password", "password");
        content.put("confirmPassword", "password");

        // Map 을 JSON 형태의 문자열로 변환
        String stringify = objectMapper.writeValueAsString(content);

        // when then
        mockMvc.perform(post("/api/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(stringify))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    public void 회원가입_사용자_이름_최소_3자이상_성공_테스트() throws Exception {
        // given
        Map<String, String> content = new LinkedHashMap<>();
        content.put("username", "username");
        content.put("password", "password");
        content.put("confirmPassword", "password");

        // Map 을 JSON 형태의 문자열로 변환
        String stringify = objectMapper.writeValueAsString(content);

        // when then
        mockMvc.perform(post("/api/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(stringify))
                .andExpect(status().is2xxSuccessful())
                .andDo(print());
    }
}