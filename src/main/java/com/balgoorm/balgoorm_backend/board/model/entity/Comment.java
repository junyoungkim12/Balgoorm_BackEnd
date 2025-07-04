package com.balgoorm.balgoorm_backend.board.model.entity;

import com.balgoorm.balgoorm_backend.user.model.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commentId;

    @Setter
    private String commentContent;
    private LocalDateTime commentCreateDate;
    private int likesCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BOARD_ID", nullable = false)
    private Board board;

    @OneToMany(mappedBy = "comment", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private List<Likes> likes = new ArrayList<>();

    @Builder
    public Comment(String commentContent, User user, Board board) {
        this.commentContent = commentContent;
        this.user = user;
        this.board = board;
        this.likesCount = 0;
    }

    @PrePersist
    protected void onCreate() {
        this.commentCreateDate = LocalDateTime.now();
    }

    public void incrementLikes() { this.likesCount++; }
    public void decrementLikes() { this.likesCount--; }

}

