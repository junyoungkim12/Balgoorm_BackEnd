package com.balgoorm.balgoorm_backend.board.controller;

import com.balgoorm.balgoorm_backend.board.model.dto.request.BoardEditRequest;
import com.balgoorm.balgoorm_backend.board.model.dto.request.BoardImageUploadDTO;
import com.balgoorm.balgoorm_backend.board.model.dto.request.BoardWriteRequestDTO;
import com.balgoorm.balgoorm_backend.board.model.dto.response.BoardResponseDTO;
import com.balgoorm.balgoorm_backend.board.service.BoardService;
import com.balgoorm.balgoorm_backend.user.auth.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/boards")
public class BoardController {

    private final BoardService boardService;

    @Value("${board.list.size}")
    private int pageSize;

    // 게시글 상세조회
    @GetMapping("/{id}")
    public ResponseEntity<BoardResponseDTO> searchBoard(@PathVariable Long id) {
        BoardResponseDTO dto = boardService.searchBoard(id);
        return ResponseEntity.ok(dto);
    }

    // 게시글 목록조회(페이징/정렬)
    @GetMapping
    public ResponseEntity<List<BoardResponseDTO>> searchBoardList(
            @RequestParam int page,
            @RequestParam Sort.Direction direction,
            @RequestParam String sortBy) {
        List<BoardResponseDTO> list = boardService.searchBoardList(page, pageSize, direction.name(), sortBy);
        return ResponseEntity.ok(list);
    }

    // 게시글 등록
    @PostMapping
    public ResponseEntity<BoardResponseDTO> createBoard(
            @RequestBody BoardWriteRequestDTO boardWriteRequestDTO,
            Authentication authentication) throws IOException {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long boardId = boardService.saveBoard(boardWriteRequestDTO, userDetails.getUsername());
        return ResponseEntity.ok(boardService.searchBoard(boardId));
    }

    // 게시글 수정
    @PutMapping("/{id}")
    public ResponseEntity<BoardResponseDTO> editBoard(
            @PathVariable Long id,
            @RequestBody BoardEditRequest boardEditRequest,
            Authentication authentication) throws IOException {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        BoardResponseDTO dto = boardService.editBoard(id, boardEditRequest, userDetails.getUsername());
        return ResponseEntity.ok(dto);
    }

    // 게시글 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBoard(
            @PathVariable Long id,
            Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        boardService.deleteBoard(id, userDetails.getUsername());
        return ResponseEntity.ok("게시글이 성공적으로 삭제되었습니다.");
    }

    // 게시글 좋아요
    @PostMapping("/{id}/like")
    public ResponseEntity<String> likeBoard(
            @PathVariable Long id,
            Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        try {
            boardService.likeBoard(id, userDetails.getUserId());
            return ResponseEntity.ok("Liked the board");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 게시글 좋아요 취소
    @PostMapping("/{id}/unlike")
    public ResponseEntity<String> unlikeBoard(
            @PathVariable Long id,
            Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        boardService.unlikeBoard(id, userDetails.getUserId());
        return ResponseEntity.ok("Unliked the board");
    }
}
