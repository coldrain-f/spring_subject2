package edu.coldrain.spring_subject1.domain;

import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Board extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "BOARD_ID")
    private Long id;

    private String title;

    private String author;

    private String password;

    @Lob
    private String contents;

    @OneToMany(mappedBy = "board", cascade = CascadeType.REMOVE)
    private List<Comment> comments = new ArrayList<>();

    @Builder
    public Board(String title, String author, String password, String contents) {
        super();
        this.title = title;
        this.author = author;
        this.password = password;
        this.contents = contents;
    }

    public void modify(String title, String contents) {
        this.title = title;
        this.contents = contents;
    }

}
