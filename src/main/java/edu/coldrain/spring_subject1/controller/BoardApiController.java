package edu.coldrain.spring_subject1.controller;

import edu.coldrain.spring_subject1.domain.Board;
import edu.coldrain.spring_subject1.service.BoardService;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
public class BoardApiController {

    private final BoardService boardService;

    /**
     * 전체 게시글 목록 조회 API
     * 작성 날짜 기준으로 내림차순 정렬하기
     */
    @GetMapping("/api/boards")
    public List<SelectBoardListResponse> findAll() {
        List<Board> boards = boardService.findAll();
        List<SelectBoardListResponse> responses = new ArrayList<>();
        for (Board board : boards) {
            SelectBoardListResponse response =
                    new SelectBoardListResponse(board.getTitle(), board.getAuthor(), board.getCreatedAt());
            responses.add(response);
        }
        return responses;
    }

    /**
     * 게시글 조회 API
     * 제목, 작성자명, 작성 날짜, 작성 내용을 조회하기
     */
    @GetMapping("/api/boards/{id}")
    public ResponseEntity<SelectBoardResponse> searchBoard(@PathVariable Long id) {
        Optional<Board> boardOptional = boardService.findOne(id);
        if (boardOptional.isPresent()) {
            Board board = boardOptional.get();
            SelectBoardResponse response =
                    new SelectBoardResponse(board.getTitle(), board.getAuthor(), board.getCreatedAt(), board.getContents());
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().build();
    }

    /**
     * 게시글 등록 API - 개발 완료
     * 제목, 작성자명, 비밀번호, 작성 내용을 입력하기
     */
    @PostMapping("/api/boards")
    public ResponseEntity<CreateBoardResponse> createBoard(@RequestBody CreateBoardRequest request) {
        Board board = new Board(request.getTitle(), request.getAuthor(), request.password, request.contents);
        Long id = boardService.registerBoard(board);
        return ResponseEntity.ok(new CreateBoardResponse(id));
    }

    /**
     * 게시글 수정 API
     * API 를 호출할 때 입력된 비밀번호를 비교하여 동일할 때만 글이 수정되게 하기
     */
    @PatchMapping("/api/boards/{id}")
    public ResponseEntity<String> modifyBoard(
            @PathVariable Long id,
            @RequestBody UpdateBoardRequest request) {

        Board board = new Board(request.getTitle(), request.getAuthor(), request.getPassword(), request.getContents());
        boolean isUpdate = boardService.modifyBoard(id, board);
        if (isUpdate) {
            return ResponseEntity.ok("success");
        } else {
            return ResponseEntity.badRequest().body("bad request");
        }
    }

    /**
     * 게시글 삭제 API
     * API 를 호출할 때 입력된 비밀번호를 비교하여 동일할 때만 글이 삭제되게 하기
     */
    @DeleteMapping("/api/boards/{id}")
    public ResponseEntity<String> removeBoard(
            @PathVariable Long id,
            @RequestBody DeleteBoardRequest request) {

        Board board = new Board(null, null, request.getPassword(), null);
        boolean isRemove = boardService.removeBoard(id, board);
        if (isRemove) {
            return ResponseEntity.ok("success");
        } else {
            return ResponseEntity.badRequest().body("bad request");
        }
    }

    @Data
    static class DeleteBoardRequest {
        private String password;
    }

    @Data
    static class CreateBoardRequest {
        private String title;
        private String author;
        private String password;
        private String contents;
    }

    @Data
    static class CreateBoardResponse {
        private final Long id;
    }

    @Data
    static class UpdateBoardRequest {
        private String title;
        private String author;
        private String password;
        private String contents;
    }

    @Data
    static class SelectBoardListResponse {
        private final String title;
        private final String author;
        private final LocalDateTime createdAt;
    }

    @Data
    static class SelectBoardResponse {
        private final String title;
        private final String author;
        private final LocalDateTime createdAt;
        private final String contents;
    }
}