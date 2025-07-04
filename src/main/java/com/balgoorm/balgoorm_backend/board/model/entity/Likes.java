package com.balgoorm.balgoorm_backend.board.model.entity;

import com.balgoorm.balgoorm_backend.user.model.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Likes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "BOARD_ID", nullable = true)
    private Board board;

    @ManyToOne
    @JoinColumn(name = "COMMENT_ID", nullable = true)
    private Comment comment;

    public Likes(User user, Board board) {
        this.user = user;
        this.board = board;
    }

    public Likes(User user, Comment comment) {
        this.user = user;
        this.comment = comment;
    }
}
