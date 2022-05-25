package edu.coldrain.spring_subject1.repository;

import edu.coldrain.spring_subject1.domain.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

public interface BoardRepository extends JpaRepository<Board, Long> {

}
