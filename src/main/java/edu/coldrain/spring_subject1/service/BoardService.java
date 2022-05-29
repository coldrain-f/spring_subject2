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
    public Long write(Board board) {
        boardRepository.save(board);
        return board.getId();
    }

    /**
     * 게시글 삭제 기능
     */
    @Transactional
    public boolean remove(Long id, String requestPassword) throws IllegalArgumentException {
        Optional<Board> board = boardRepository.findById(id);
        if (board.isPresent() && isSamePassword(board.get().getPassword(), requestPassword)) {
            // TODO: 2022-05-29 비밀번호가 같지 않다면 예외를 터트려야 하나?
            boardRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * 같은 비밀번호인지 확인
     */
    public boolean isSamePassword(String password1, String password2) {
        return password1.equals(password2);
    }

}
