package edu.coldrain.spring_subject1.repository;

import edu.coldrain.spring_subject1.domain.Authority;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorityRepository extends JpaRepository<Authority, String> {

}
