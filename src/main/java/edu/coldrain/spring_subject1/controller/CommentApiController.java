package edu.coldrain.spring_subject1.controller;

import edu.coldrain.spring_subject1.domain.Board;
import edu.coldrain.spring_subject1.domain.Comment;
import edu.coldrain.spring_subject1.service.BoardService;
import edu.coldrain.spring_subject1.service.CommentService;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class CommentApiController {

    private final CommentService commentService;
    private final BoardService boardService;

    @GetMapping("/api/boards/{id}/comments")
    public ResponseEntity<List<ListCommentResponseDTO>> viewComments(@PathVariable Long id) {
        // TODO: 2022-05-29 로그인 토큰을 전달하지 않아도 댓글 목록 조회가 가능하도록 하기
        List<ListCommentResponseDTO> collect = commentService.findAll(id)
                .stream()
                .map(comment ->
                        ListCommentResponseDTO.builder()
                                .id(comment.getId())
                                .author(comment.getAuthor())
                                .content(comment.getContent())
                                .build()
                ).collect(Collectors.toList());
        return ResponseEntity.ok(collect);
    }

    // 댓글 작성
    @PostMapping("/api/boards/{id}/comments")
    public ResponseEntity<String> createComment(@PathVariable Long id, @RequestBody CreateCommentRequestDTO requestDTO) {
        // TODO: 2022-05-29 로그인 토큰을 전달했을 때에만 댓글 작성이 가능하도록 하기
        // TODO: 2022-05-29 로그인 토큰을 전달하지 않은 채로 댓글 작성란을 누르면 "로그인이 필요한 기능입니다." 라는 에러 메세지를 응답에 포함하기
        // TODO: 2022-05-29 댓글 내용란을 비워둔 채 API 를 호출하면 "댓글 내용을 입력해주세요" 라는 에러 메세지를 응답에 포함하기
        Board board = boardService.findOne(id) // 예외를 터트려도 되나?
                .orElseThrow(() -> new IllegalArgumentException("id is null"));
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
    public ResponseEntity<String> modifyComment(@PathVariable Long id, @RequestBody ModifyCommentRequestDTO request) {
        // TODO: 2022-05-29 로그인 토큰에 해당하는 사용자가 작성한 댓글만 수정 가능하도록 하기
        Comment comment = commentService.findById(id) // 1차 캐시에 있음
                .orElseThrow(() -> new IllegalArgumentException("id is null"));
        comment.modify(request.getContent());
        return ResponseEntity.ok("success");
    }

    // 댓글 삭제
    @DeleteMapping("/api/comments/{id}")
    public ResponseEntity<String> removeComment(@PathVariable Long id) {
        // TODO: 2022-05-29 로그인 토큰에 해당하는 사용자가 작성한 댓글만 삭제 가능하도록 하기
        commentService.remove(id);
        return ResponseEntity.ok("success");
    }

    @Data
    static class CreateCommentRequestDTO {
        private String author;
        private String content;
    }

    @Data
    static class ModifyCommentRequestDTO {
        private String content;
    }

    @Data
    @Builder
    static class ListCommentResponseDTO {
        private Long id;
        private String author;
        private String content;
    }
}
