package com.balgoorm.balgoorm_backend.board.model.entity;

import com.balgoorm.balgoorm_backend.user.model.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long boardId;

    @Setter
    private String boardTitle;
    @Setter
    private String boardContent;

    private LocalDateTime boardCreateDate;

    private int likesCount;
    private int viewCount;

    // 작성자(유저)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    // 이미지 리스트
    @Setter
    @OneToMany(mappedBy = "board", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @OrderBy("id asc")
    private List<BoardImage> boardImages = new ArrayList<>();

    // 댓글 리스트
    @OneToMany(mappedBy = "board", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @OrderBy("commentCreateDate asc")
    private List<Comment> comments = new ArrayList<>();

    // 조회수 기록(뷰)
    @OneToMany(mappedBy = "board", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private List<View> views = new ArrayList<>();

    @Builder
    public Board(String boardTitle, String boardContent, User user) {
        this.boardTitle = boardTitle;
        this.boardContent = boardContent;
        this.user = user;
        this.likesCount = 0;
        this.viewCount = 0;
    }

    @PrePersist
    protected void onCreate() {
        this.boardCreateDate = LocalDateTime.now();
    }

    // 좋아요/조회수 증가/감소 메서드
    public void incrementLikes() {
        this.likesCount++;
    }

    public void decrementLikes() {
        this.likesCount--;
    }

    public void incrementViews() {
        this.viewCount++;
    }
}

