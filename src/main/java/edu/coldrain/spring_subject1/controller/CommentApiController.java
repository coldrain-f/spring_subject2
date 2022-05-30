package edu.coldrain.spring_subject1.controller;

import edu.coldrain.spring_subject1.domain.Board;
import edu.coldrain.spring_subject1.domain.Comment;
import edu.coldrain.spring_subject1.service.BoardService;
import edu.coldrain.spring_subject1.service.CommentService;
import edu.coldrain.spring_subject1.util.SecurityUtil;
import io.jsonwebtoken.Jwts;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CommentApiController {

    private static final String ID_IS_NULL = "id is null";

    private final CommentService commentService;
    private final BoardService boardService;

    // TODO: 2022-05-30 헤더로 토큰 가져오기 테스트
    @GetMapping("/api/header")
    public ResponseEntity<String> header(@RequestHeader HttpHeaders httpHeaders) {
        String authorization = httpHeaders.getFirst(HttpHeaders.AUTHORIZATION);
        log.info("jwtToken = {}", authorization);
        // TODO: 2022-05-30 헤더에 있는 JWT Token 을 복호화하여 payload 가져와 보기
        // TODO: 2022-05-30 authentication 로 로그인 여부를 확인해야 하는지, 토큰의 payload 로 확인해야 하는지?
//        Jwts.parserBuilder()
//                .setSigningKey(key)
//                .build()
//                .parseClaimsJws(token)
//                .getBody();
        return ResponseEntity.ok(authorization);
    }

    @GetMapping("/api/boards/{id}/comments")
    public ResponseEntity<Result> viewComments(@PathVariable Long id) {
        // TODO: 2022-05-29 로그인 토큰을 전달하지 않아도 댓글 목록 조회가 가능하도록 하기
        List<CommentListResponseDTO> responses = commentService.findAll(id)
                .stream()
                .map(comment ->
                        CommentListResponseDTO.builder()
                                .id(comment.getId())
                                .author(comment.getAuthor())
                                .content(comment.getContent())
                                .build()
                ).collect(Collectors.toList());
        return ResponseEntity.ok(new Result(responses));
    }

    // 댓글 작성
    @PostMapping("/api/boards/{id}/comments")
    public ResponseEntity<String> createComment(@PathVariable Long id, @RequestBody CommentCreateRequestDTO requestDTO) {
        // TODO: 2022-05-29 로그인 토큰을 전달했을 때에만 댓글 작성이 가능하도록 하기
        // TODO: 2022-05-29 로그인 토큰을 전달하지 않은 채로 댓글 작성란을 누르면 "로그인이 필요한 기능입니다." 라는 에러 메세지를 응답에 포함하기
        Optional<String> currentUsername = SecurityUtil.getCurrentUsername();
        if (currentUsername.isPresent() && currentUsername.get().equals("anonymousUser")) {
            return ResponseEntity.badRequest().body("로그인이 필요한 기능입니다.");
        }
        // TODO: 2022-05-30 삭제 예정 테스트 로그
        currentUsername.ifPresent(c -> log.info("currentUsername = {}", c));

        // TODO: 2022-05-29 댓글 내용란을 비워둔 채 API 를 호출하면 "댓글 내용을 입력해주세요" 라는 에러 메세지를 응답에 포함하기
        if (!StringUtils.hasText(requestDTO.getContent())) {
            return ResponseEntity.badRequest().body("댓글 내용을 입력해주세요.");
        }

        Board board = boardService.findOne(id) // 예외를 터트려도 되나?
                .orElseThrow(() -> new IllegalArgumentException(ID_IS_NULL));
        Comment comment = Comment.builder()
                .author(requestDTO.getAuthor())
                .content(requestDTO.getContent())
                .board(board)
                .build();
        commentService.write(comment);

        String location = "/api/boards" + id + "/comments/" + comment.getId();
        return ResponseEntity.created(URI.create(location)).build();
    }

    // 댓글 수정
    @PatchMapping("/api/comments/{id}")
    public ResponseEntity<String> modifyComment(@PathVariable Long id, @RequestBody CommentModifyRequestDTO requestDTO) {
        Comment comment = commentService.findById(id) // 1차 캐시에 있음
                .orElseThrow(() -> new IllegalArgumentException(ID_IS_NULL));
        // TODO: 2022-05-29 로그인 토큰에 해당하는 사용자가 작성한 댓글만 수정 가능하도록 하기
        Optional<String> currentUsername = SecurityUtil.getCurrentUsername();
        if (currentUsername.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        if (!currentUsername.get().equals(comment.getAuthor())) {
            return ResponseEntity.badRequest().build();
        }
        comment.modify(requestDTO.getContent());
        return ResponseEntity.ok("success");
    }

    // 댓글 삭제
    @DeleteMapping("/api/comments/{id}")
    public ResponseEntity<String> removeComment(@PathVariable Long id) {
        // TODO: 2022-05-29 로그인 토큰에 해당하는 사용자가 작성한 댓글만 삭제 가능하도록 하기
        Optional<String> currentUsername = SecurityUtil.getCurrentUsername();
        if (currentUsername.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        Comment comment = commentService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(ID_IS_NULL));
        if (!currentUsername.get().equals(comment.getAuthor())) {
            return ResponseEntity.badRequest().build();
        }
        commentService.remove(id);
        return ResponseEntity.ok("success");
    }


    // 이런식으로 List 를 한 번 감싸서 응답하면 확장하기 쉽다.
    @AllArgsConstructor
    @Getter // 없으면 406..
    static class Result {
        private List<CommentListResponseDTO> data;
    }

    @Data
    static class CommentCreateRequestDTO {
        private String author;
        private String content;
    }

    @Data
    static class CommentModifyRequestDTO {
        private String content;
    }

    @Data
    @Builder
    static class CommentListResponseDTO {
        private Long id;
        private String author;
        private String content;
    }
}
