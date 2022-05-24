package edu.coldrain.spring_subject1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class SpringSubject1Application {

    public static void main(String[] args) {
        SpringApplication.run(SpringSubject1Application.class, args);
    }

}
