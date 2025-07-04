package com.balgoorm.balgoorm_backend.board.controller;

import com.balgoorm.balgoorm_backend.board.model.dto.request.CommentRequestDTO;
import com.balgoorm.balgoorm_backend.board.model.dto.response.CommentResponseDTO;
import com.balgoorm.balgoorm_backend.board.service.CommentService;
import com.balgoorm.balgoorm_backend.user.auth.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/board")
public class CommentController {

    private final CommentService commentService;

    // 댓글 작성
    @PostMapping("/{id}/comment")
    public ResponseEntity<CommentResponseDTO> writeComment(
            @PathVariable Long id,
            @RequestBody CommentRequestDTO dto,
            Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();
        CommentResponseDTO result = commentService.writeComment(dto, id, userId);
        return ResponseEntity.ok(result);
    }

    // 댓글 수정
    @PutMapping("/{id}/comment/{commentId}")
    public ResponseEntity<CommentResponseDTO> updateComment(
            @PathVariable Long id,
            @PathVariable Long commentId,
            @RequestBody CommentRequestDTO dto,
            Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();
        CommentResponseDTO result = commentService.updateComment(dto, commentId, userId);
        return ResponseEntity.ok(result);
    }

    // 댓글 삭제
    @DeleteMapping("/{id}/comment/{commentId}")
    public ResponseEntity<String> deleteComment(
            @PathVariable Long id,
            @PathVariable Long commentId,
            Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();
        commentService.deleteComment(commentId, userId);
        return ResponseEntity.ok("댓글이 성공적으로 삭제되었습니다.");
    }

    // 댓글 좋아요
    @PostMapping("/{id}/comment/{commentId}/like")
    public ResponseEntity<String> likeComment(
            @PathVariable Long id,
            @PathVariable Long commentId,
            Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();
        commentService.likeComment(commentId, userId);
        return ResponseEntity.ok("댓글 좋아요 완료");
    }

    // 댓글 좋아요 취소
    @PostMapping("/{id}/comment/{commentId}/unlike")
    public ResponseEntity<String> unlikeComment(
            @PathVariable Long id,
            @PathVariable Long commentId,
            Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();
        commentService.unlikeComment(commentId, userId);
        return ResponseEntity.ok("댓글 좋아요 취소 완료");
    }

    // 게시글별 댓글 목록
    @GetMapping("/{id}/comments")
    public ResponseEntity<List<CommentResponseDTO>> getComments(@PathVariable Long id) {
        List<CommentResponseDTO> result = commentService.getCommentsByBoardId(id);
        return ResponseEntity.ok(result);
    }
}
