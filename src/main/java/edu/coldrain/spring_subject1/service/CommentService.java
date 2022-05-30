package edu.coldrain.spring_subject1.service;

import edu.coldrain.spring_subject1.domain.Comment;
import edu.coldrain.spring_subject1.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;

    // 특정 게시글에 댓글 등록
    public void write(Comment comment) {
        commentRepository.save(comment);
    }

    // 특정 게시글에 소속된 모든 댓글 목록
    public List<Comment> findAll(Long boardId) {
        // TODO: 2022-05-29 에러 발생
        return commentRepository.findAllByBoardId(boardId);
    }

    // 댓글 삭제
    public void remove(Long commentId) {
        commentRepository.deleteById(commentId);
    }

    // 댓글 한 개 조회
    public Optional<Comment> findById(Long commentId) {
        return commentRepository.findById(commentId);
    }
}
