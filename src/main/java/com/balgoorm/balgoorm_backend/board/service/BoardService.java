package com.balgoorm.balgoorm_backend.board.service;

import com.balgoorm.balgoorm_backend.board.model.dto.request.BoardWriteRequestDTO;
import com.balgoorm.balgoorm_backend.board.model.dto.request.BoardEditRequest;
import com.balgoorm.balgoorm_backend.board.model.dto.response.BoardResponseDTO;
import com.balgoorm.balgoorm_backend.board.model.entity.Board;
import com.balgoorm.balgoorm_backend.board.model.entity.BoardImage;
import com.balgoorm.balgoorm_backend.board.model.entity.Likes;
import com.balgoorm.balgoorm_backend.board.model.entity.View;
import com.balgoorm.balgoorm_backend.board.repository.BoardRepository;
import com.balgoorm.balgoorm_backend.board.repository.BoardImageRepository;
import com.balgoorm.balgoorm_backend.board.repository.LikesRepository;
import com.balgoorm.balgoorm_backend.board.repository.ViewRepository;
import com.balgoorm.balgoorm_backend.user.model.entity.User;
import com.balgoorm.balgoorm_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final BoardImageRepository boardImageRepository;
    private final UserRepository userRepository;
    private final LikesRepository likesRepository;
    private final ViewRepository viewRepository;

    @Value("${file.boardImagePath}")
    private String uploadFolder;

    // 게시글 조회(엔티티 반환, 내부용)
    @Transactional(readOnly = true)
    public Board getBoardById(Long boardId) {
        return boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));
    }

    // 게시글 상세(응답DTO로 변환)
    @Transactional(readOnly = true)
    public BoardResponseDTO searchBoard(Long boardId) {
        return new BoardResponseDTO(getBoardById(boardId));
    }

    // 게시글 목록(페이징, 정렬)
    @Transactional(readOnly = true)
    public List<BoardResponseDTO> searchBoardList(int page, int pageSize, String direction, String sortBy) {
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.fromString(direction),
                "likes".equals(sortBy) ? "likesCount" :
                        "views".equals(sortBy) ? "viewCount" : "boardCreateDate"
        ));
        return boardRepository.findAll(pageable)
                .stream().map(BoardResponseDTO::new)
                .collect(Collectors.toList());
    }

    // 게시글 등록
    @Transactional
    public Long saveBoard(BoardWriteRequestDTO dto, String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 ID가 존재하지 않습니다."));

        Board board = Board.builder()
                .boardTitle(dto.getBoardTitle())
                .boardContent(dto.getBoardContent())
                .user(user)
                .build();

        boardRepository.save(board);
        return board.getBoardId();
    }

    // 게시글 이미지 저장 (setter 최소화, 컬렉션 초기화는 엔티티에서)
    private void saveBoardImages(List<MultipartFile> files, Board board) {
        for (MultipartFile file : files) {
            String imageFileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            File destinationFile = new File(uploadFolder + File.separator + imageFileName);

            try {
                file.transferTo(destinationFile);
            } catch (IOException e) {
                log.error("파일 업로드 중 오류 발생: {}", e.getMessage());
                throw new RuntimeException("파일 업로드 중 오류가 발생했습니다.", e);
            }

            BoardImage image = BoardImage.builder()
                    .url("/uploads/" + imageFileName)
                    .board(board)
                    .build();

            boardImageRepository.save(image);
            board.addBoardImage(image); // 양방향 연관관계 편의 메서드 활용 (엔티티에서 구현)
        }
    }

    // 게시글 수정 (Dirty Checking 활용)
    @Transactional
    public BoardResponseDTO editBoard(Long boardId, BoardEditRequest request, String userId) {
        Board board = getBoardById(boardId);
        if (!board.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("다른 사용자의 게시글은 수정할 수 없습니다.");
        }
        board.setBoardTitle(request.getBoardTitle());
        board.setBoardContent(request.getBoardContent());
        return new BoardResponseDTO(board);
    }

    // 게시글 삭제
    @Transactional
    public BoardResponseDTO deleteBoard(Long boardId, String userId) {
        Board board = getBoardById(boardId);
        if (!board.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("다른 사용자의 게시글은 삭제할 수 없습니다.");
        }
        boardRepository.delete(board);
        return new BoardResponseDTO(board);
    }

    // 게시글 좋아요
    @Transactional
    public void likeBoard(Long boardId, Long userId) {
        Board board = getBoardById(boardId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));

        if (likesRepository.existsByUserIdAndBoardBoardId(userId, boardId)) {
            throw new IllegalArgumentException("이미 좋아요를 누르셨습니다.");
        }
        Likes like = new Likes(user, board);
        likesRepository.save(like);
        board.incrementLikes();
    }

    // 게시글 좋아요 취소
    @Transactional
    public void unlikeBoard(Long boardId, Long userId) {
        Likes like = likesRepository.findByUserIdAndBoardBoardId(userId, boardId)
                .orElseThrow(() -> new IllegalArgumentException("Like not found for user and board"));
        Board board = like.getBoard();
        likesRepository.delete(like);
        board.decrementLikes();
    }

    // 게시글 조회수 (View)
    @Transactional
    public void viewBoard(Long boardId, Long userId) {
        Board board = getBoardById(boardId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));

        if (!viewRepository.existsByUserIdAndBoardBoardId(userId, boardId)) {
            View view = new View(user, board);
            viewRepository.save(view);
            board.incrementViews();
        }
    }
}
