package edu.coldrain.spring_subject1.controller;

import edu.coldrain.spring_subject1.domain.Board;
import edu.coldrain.spring_subject1.domain.Comment;
import edu.coldrain.spring_subject1.exception.AuthenticationException;
import edu.coldrain.spring_subject1.service.BoardService;
import edu.coldrain.spring_subject1.service.CommentService;
import edu.coldrain.spring_subject1.util.SecurityUtil;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
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

    private final CommentService commentService;
    private final BoardService boardService;

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/api/boards/{id}/comments")
    public Result viewComments(@PathVariable Long id) {
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
        return new Result(responses);
    }

    @PostMapping("/api/boards/{id}/comments")
    public ResponseEntity<RestResponse> createComment(@PathVariable Long id, @RequestBody CommentCreateRequestDTO requestDTO) {
        Optional<String> currentUsername = SecurityUtil.getCurrentUsername();
        currentUsername.ifPresent(c -> log.info("currentUsername = {}", c));
        if (currentUsername.isPresent() && currentUsername.get().equals("anonymousUser")) {
            throw new AuthenticationException("로그인이 필요한 기능입니다.");
        }

        // TODO: 2022-05-29 댓글 내용란을 비워둔 채 API 를 호출하면 "댓글 내용을 입력해주세요" 라는 에러 메세지를 응답에 포함하기
        // TODO: 2022-06-01 -> Bean Validation 을 사용하여 ControllerAdvice 가 처리하도록 변경하기
        if (!StringUtils.hasText(requestDTO.getContent())) {
            throw new IllegalArgumentException("댓글 내용을 입력해주세요.");
        }

        // 등록하려는 게시글의 id 값이 없으면 예외가 터진다.
        Board board = boardService.findOne(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글 번호입니다."));

        Comment comment = Comment.builder()
                .author(requestDTO.getAuthor())
                .content(requestDTO.getContent())
                .board(board)
                .build();

        commentService.write(comment);

        // TODO: 2022-06-01 특정 게시글의 특정 댓글 조회 API 는 현재 존재하지 않음
        String location = "/api/boards/" + id + "/comments/" + comment.getId();

        return ResponseEntity.created(URI.create(location))
                .body(new RestResponse(true, "댓글 등록에 성공했습니다.", location));
    }

    @Transactional
    @PatchMapping("/api/comments/{id}")
    public ResponseEntity<RestResponse> modifyComment(@PathVariable Long id, @RequestBody CommentModifyRequestDTO requestDTO) {
        Comment comment = commentService.findById(id) // 1차 캐시에 있음
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글 번호입니다."));

        // TODO: 2022-05-29 로그인 토큰에 해당하는 사용자가 작성한 댓글만 수정 가능하도록 하기
        Optional<String> currentUsername = SecurityUtil.getCurrentUsername();
        if (currentUsername.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        if (!currentUsername.get().equals(comment.getAuthor())) {
            return ResponseEntity.badRequest()
                    .body(new RestResponse(false, "본인의 댓글만 수정할 수 있습니다."));
        }

        comment.modify(requestDTO.getContent());

        return ResponseEntity.ok(new RestResponse(true, "댓글 수정에 성공했습니다."));
    }

    // 댓글 삭제
    @DeleteMapping("/api/comments/{id}")
    public ResponseEntity<RestResponse> removeComment(@PathVariable Long id) {
        Comment comment = commentService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글 번호입니다."));

        // TODO: 2022-05-29 로그인 토큰에 해당하는 사용자가 작성한 댓글만 삭제 가능하도록 하기
        Optional<String> currentUsername = SecurityUtil.getCurrentUsername();
        if (currentUsername.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        if (!currentUsername.get().equals(comment.getAuthor())) {
            return ResponseEntity.badRequest()
                    .body(new RestResponse(false, "본인의 댓글만 삭제할 수 있습니다."));
        }

        commentService.remove(id);
        return ResponseEntity.ok(new RestResponse(true, "댓글 삭제에 성공했습니다."));
    }

    // 이런식으로 List 를 한 번 감싸서 응답하면 확장하기 쉽다.
    @AllArgsConstructor
    @Getter // 없는 상태로 JSON 반환시 406 에러 발생.
    static class Result {
        private List<CommentListResponseDTO> data;
    }

    @Getter
    @AllArgsConstructor
    static class RestResponse {
        private boolean success;
        private String message;
        private Object data;

        public RestResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
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