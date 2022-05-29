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
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class CommentApiController {

    private final CommentService commentService;
    private final BoardService boardService;

    // 댓글 목록 조회
    @GetMapping("/api/boards/{id}/comments")
    public ResponseEntity<List<CommentResponseDTO>> viewComments(@PathVariable Long id) {
        // TODO: 2022-05-29 로그인 토큰을 전달하지 않아도 댓글 목록 조회가 가능하도록 하기
        List<CommentResponseDTO> collect = commentService.findAll(id)
                .stream()
                .map(comment ->
                        CommentResponseDTO.builder()
                                .id(comment.getId())
                                .author(comment.getAuthor())
                                .content(comment.getContent())
                                .build()
                ).collect(Collectors.toList());
        return ResponseEntity.ok(collect);
    }

    // 댓글 작성
    @PostMapping("/api/boards/{id}/comments")
    public ResponseEntity<String> createComment(@PathVariable Long id, @RequestBody CommentRequestDTO commentRequestDTO) {
        // TODO: 2022-05-29 로그인 토큰을 전달했을 때에만 댓글 작성이 가능하도록 하기
        // TODO: 2022-05-29 로그인 토큰을 전달하지 않은 채로 댓글 작성란을 누르면 "로그인이 필요한 기능입니다." 라는 에러 메세지를 응답에 포함하기
        // TODO: 2022-05-29 댓글 내용란을 비워둔 채 API 를 호출하면 "댓글 내용을 입력해주세요" 라는 에러 메세지를 응답에 포함하기
        Optional<Board> found = boardService.findOne(id);
        if (found.isPresent()) {
            Comment comment = Comment.builder()
                    .author(commentRequestDTO.getAuthor())
                    .content(commentRequestDTO.getContent())
                    .board(found.get())
                    .build();
            commentService.write(comment);
            return ResponseEntity.created(URI.create("/")).build();
        }
        return ResponseEntity.badRequest().build();
    }

    // 댓글 수정
    @PatchMapping("/api/comments/{id}")
    public ResponseEntity<String> modifyComment(@PathVariable Long id, @RequestBody CommentRequestDTO request) {
        // TODO: 2022-05-29 로그인 토큰에 해당하는 사용자가 작성한 댓글만 수정 가능하도록 하기
        Optional<Comment> found = commentService.findById(id);
        if (found.isPresent()) {
            Comment comment = found.get(); // 1차 캐시에 있음
            comment.modify(request.getAuthor(), request.getContent());
            return ResponseEntity.created(URI.create("/")).build();
        }
        return ResponseEntity.badRequest().build();
    }

    // 댓글 삭제
    @DeleteMapping("/api/comments/{id}")
    public ResponseEntity<String> removeComment(@PathVariable Long id) {
        // TODO: 2022-05-29 로그인 토큰에 해당하는 사용자가 작성한 댓글만 삭제 가능하도록 하기
        try {
            commentService.remove(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Data
    static class CommentRequestDTO {
        private String author;
        private String content;
    }

    @Data
    @Builder
    static class CommentResponseDTO {
        private Long id;
        private String author;
        private String content;
    }
}
