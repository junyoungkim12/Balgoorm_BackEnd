package com.balgoorm.balgoorm_backend.user.model.entity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "USERS")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String userId;

    @Setter
    @Column(nullable = false)
    private String userPassword;

    @Setter
    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(updatable = false)
    private LocalDateTime createDate;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole role;

    // 생성일 자동 세팅
    @PrePersist
    public void prePersist() {
        this.createDate = LocalDateTime.now();
    }

    @Builder
    public User(Long id, String userId, String userPassword, String nickname, String email, UserRole role) {
        this.id = id;
        this.userId = userId;
        this.userPassword = userPassword;
        this.nickname = nickname;
        this.email = email;
        this.role = role;
    }

    // 연관관계는 주석 처리(필요 시 활성화)
    // @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    // private List<Board> boards = new ArrayList<>();
}
