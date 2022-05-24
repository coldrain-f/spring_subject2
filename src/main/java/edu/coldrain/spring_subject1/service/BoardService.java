package edu.coldrain.spring_subject1.service;

import edu.coldrain.spring_subject1.domain.Board;
import edu.coldrain.spring_subject1.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardService {

    private final BoardRepository boardRepository;

    /**
     * 모든 게시글 조회 기능
     */
    public List<Board> findAll() {
        return boardRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    /**
     * 특정 게시글 조회 기능
     */
    public Optional<Board> findOne(Long id) {
        return boardRepository.findById(id);
    }

    /**
     * 게시글 등록 기능
     */
    @Transactional
    public Long registerBoard(Board board) {
        boardRepository.save(board);
        return board.getId();
    }

    /**
     * 게시글 수정 기능
     */
    @Transactional
    public boolean modifyBoard(Long id, Board request) {
        Optional<Board> board = boardRepository.findById(id);
        AtomicBoolean state = new AtomicBoolean(false);
        board.ifPresent(b -> {
            if (isSamePassword(b.getPassword(), request.getPassword())) {
                if (request.getTitle() != null) {
                    b.setTitle(request.getTitle());
                }
                if (request.getAuthor() != null) {
                    b.setAuthor(request.getAuthor());
                }
                if (request.getContents() != null) {
                    b.setContents(request.getContents());
                }
                state.set(true);
                return;
            }
            state.set(false);
        });
        return state.get();
    }

    /**
     * 게시글 삭제 기능
     */
    @Transactional
    public boolean removeBoard(Long id, Board request) throws IllegalArgumentException {
        Optional<Board> board = boardRepository.findById(id);
        if (board.isPresent()) {
            if (isSamePassword(board.get().getPassword(), request.getPassword())) {
                boardRepository.delete(board.get());
                return true;
            }
        }
        return false;
    }

    /**
     * 같은 비밀번호인지 확인
     */
    private boolean isSamePassword(String password1, String password2) {
        return password1.equals(password2);
    }

}
