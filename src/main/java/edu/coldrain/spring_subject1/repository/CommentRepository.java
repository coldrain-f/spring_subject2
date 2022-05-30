package edu.coldrain.spring_subject1.repository;

import edu.coldrain.spring_subject1.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("select c from Comment c join fetch Board b where b.id = :boardId order by c.createdAt desc")
    List<Comment> findAllByBoardId(@Param("boardId") Long boardId);
}
