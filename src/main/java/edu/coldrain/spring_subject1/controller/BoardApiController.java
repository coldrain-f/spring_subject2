package edu.coldrain.spring_subject1.controller;

import edu.coldrain.spring_subject1.domain.Board;
import edu.coldrain.spring_subject1.service.BoardService;
import edu.coldrain.spring_subject1.util.SecurityUtil;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    public ResponseEntity<Result> viewAll() {
        List<BoardDetailListResponseDTO> responses = boardService.findAll()
                .stream()
                .map(b ->
                        BoardDetailListResponseDTO.builder()
                                .title(b.getTitle())
                                .author(b.getAuthor())
                                .createdAt(b.getCreatedAt())
                                .build()
                ).collect(Collectors.toList());
        return ResponseEntity.ok(new Result(responses));
    }

    /**
     * 게시글 조회 API
     * 제목, 작성자명, 작성 날짜, 작성 내용을 조회하기
     */
    @GetMapping("/api/boards/{id}")
    public ResponseEntity<BoardDetailResponseDTO> viewDetail(@PathVariable Long id) {
        Board board;
        try {
            board = boardService.findOne(id)
                    .orElseThrow(() -> new IllegalArgumentException("id is null"));
        } catch (IllegalArgumentException e) {
            // TODO: 2022-05-29 해당하는 글이 없으면 404 Not Found ? Bad Request?
            return ResponseEntity.badRequest().build();
        }
        BoardDetailResponseDTO response = BoardDetailResponseDTO.builder()
                .title(board.getTitle())
                .author(board.getAuthor())
                .createdAt(board.getCreatedAt())
                .contents(board.getContents())
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * 게시글 등록 API
     * 제목, 작성자명, 비밀번호, 작성 내용을 입력하기
     */
    @PostMapping("/api/boards")
    public ResponseEntity<String> create(@RequestBody BoardCreateRequestDTO requestDTO) {
        Board board = Board.builder()
                .title(requestDTO.getTitle())
                .author(requestDTO.getAuthor())
                .password(requestDTO.getPassword())
                .contents(requestDTO.getContents())
                .build();
        boardService.write(board);

        String location = "/api/boards/" + board.getId();
        return ResponseEntity.created(URI.create(location)).build();
    }

    /**
     * 게시글 수정 API
     * API 를 호출할 때 입력된 비밀번호를 비교하여 동일할 때만 글이 수정되게 하기
     */
    @PatchMapping("/api/boards/{id}")
    public ResponseEntity<String> modify(@PathVariable Long id, @RequestBody BoardModifyRequestDTO requestDTO) {
        Board newBoard = Board.builder()
                .title(requestDTO.getTitle())
                .author(requestDTO.getAuthor())
                .password(requestDTO.getPassword())
                .contents(requestDTO.getContents())
                .build();

        Board board = boardService.findOne(id) // 1차 캐시에 있음.
                .orElseThrow(() -> new IllegalArgumentException("id is null"));

        if (!boardService.isSamePassword(board.getPassword(), newBoard.getPassword())) {
            return ResponseEntity.badRequest().body("The password is different.");
        }

        board.modify(newBoard.getTitle(), newBoard.getContents());
        return ResponseEntity.ok("success");
    }

    /**
     * 게시글 삭제 API
     * API 를 호출할 때 입력된 비밀번호를 비교하여 동일할 때만 글이 삭제되게 하기
     */
    @DeleteMapping("/api/boards/{id}")
    public ResponseEntity<String> remove(@PathVariable Long id, @RequestBody String password) {
        // id 값이 NULL 인 경우에는 IllegalArgumentException 발생
        // Exception 이 발생하지 않도록 하는 방법은 없나? -> BindingResult 사용?
        // 애초에 요청시 null 을 보낼 수 있기는 한가?
        boolean isRemove = boardService.remove(id, password);
        if (isRemove) {
            return ResponseEntity.ok("success");
        }
        // id 값이 NULL 은 아니지만 데이터베이스에 없는 id 값일 경우 bad request
        return ResponseEntity.badRequest().body("The id does not exist.");
    }

    // 이런식으로 List 를 한 번 감싸서 응답하면 확장하기 쉽다.
    @AllArgsConstructor
    @Data
    static class Result {
        // Getter 가 없으면 406 Status code 발생...
        private List<BoardDetailListResponseDTO> data;
    }

    @Data
    static class BoardCreateRequestDTO {
        private String title;
        private String author;
        private String password;
        private String contents;
    }

    @Data
    static class BoardModifyRequestDTO {
        private String title;
        private String author;
        private String password;
        private String contents;
    }

    @Data
    @Builder
    static class BoardDetailListResponseDTO {
        private final String title;
        private final String author;
        private final LocalDateTime createdAt;
    }

    @Data
    @Builder
    static class BoardDetailResponseDTO {
        private final String title;
        private final String author;
        private final LocalDateTime createdAt;
        private final String contents;
    }
}