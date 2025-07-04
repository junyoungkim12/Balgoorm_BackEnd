package com.balgoorm.balgoorm_backend.board.service;

import com.balgoorm.balgoorm_backend.board.model.dto.request.CommentRequestDTO;
import com.balgoorm.balgoorm_backend.board.model.dto.response.CommentResponseDTO;
import com.balgoorm.balgoorm_backend.board.model.entity.Board;
import com.balgoorm.balgoorm_backend.board.model.entity.Comment;
import com.balgoorm.balgoorm_backend.board.model.entity.Likes;
import com.balgoorm.balgoorm_backend.board.repository.BoardRepository;
import com.balgoorm.balgoorm_backend.board.repository.CommentRepository;
import com.balgoorm.balgoorm_backend.board.repository.LikesRepository;
import com.balgoorm.balgoorm_backend.user.model.entity.User;
import com.balgoorm.balgoorm_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final LikesRepository likesRepository;

    // 댓글 작성
    @Transactional
    public CommentResponseDTO writeComment(CommentRequestDTO dto, Long boardId, Long userId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        Comment comment = Comment.builder()
                .commentContent(dto.getCommentContent())
                .board(board)
                .user(user)
                .build();

        commentRepository.save(comment);
        return new CommentResponseDTO(comment);
    }

    // 댓글 수정 (Dirty Checking)
    @Transactional
    public CommentResponseDTO updateComment(CommentRequestDTO dto, Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));
        if (!comment.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("다른 사용자의 댓글은 수정할 수 없습니다.");
        }
        comment.setCommentContent(dto.getCommentContent());
        return new CommentResponseDTO(comment);
    }

    // 댓글 삭제
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));
        if (!comment.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("다른 사용자의 댓글은 삭제할 수 없습니다.");
        }
        commentRepository.delete(comment);
    }

    // 댓글 좋아요
    @Transactional
    public void likeComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 회원입니다."));

        if (likesRepository.existsByUserIdAndCommentCommentId(userId, commentId)) {
            throw new IllegalArgumentException("이미 좋아요를 누르셨습니다.");
        }

        Likes like = new Likes(user, comment);
        likesRepository.save(like);
        comment.incrementLikes();
    }

    // 댓글 좋아요 취소
    @Transactional
    public void unlikeComment(Long commentId, Long userId) {
        Likes like = likesRepository.findByUserIdAndCommentCommentId(userId, commentId)
                .orElseThrow(() -> new IllegalArgumentException("좋아요 정보가 없습니다."));
        Comment comment = like.getComment();
        likesRepository.delete(like);
        comment.decrementLikes();
    }

    // 게시글별 댓글 조회
    @Transactional(readOnly = true)
    public List<CommentResponseDTO> getCommentsByBoardId(Long boardId) {
        return commentRepository.findByBoardBoardId(boardId).stream()
                .map(CommentResponseDTO::new)
                .collect(Collectors.toList());
    }
}
