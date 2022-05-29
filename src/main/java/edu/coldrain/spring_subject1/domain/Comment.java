package edu.coldrain.spring_subject1.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends Timestamped {

    @Id @GeneratedValue
    @Column(name = "COMMENT_ID")
    private Long id;

    private String author;

    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BOARD_ID")
    private Board board;

    @Builder
    public Comment(String author, String content, Board board) {
        this.author = author;
        this.content = content;
        this.board = board;
    }

    public void modify(String author, String content) {
        this.author = author;
        this.content = content;
    }
}
