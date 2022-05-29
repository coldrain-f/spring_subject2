package edu.coldrain.spring_subject1.domain;

import lombok.*;

import javax.persistence.*;

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
