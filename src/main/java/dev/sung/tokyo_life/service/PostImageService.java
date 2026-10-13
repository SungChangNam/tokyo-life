package dev.sung.tokyo_life.service;

import dev.sung.tokyo_life.model.PostImage;
import dev.sung.tokyo_life.model.StoredImage;
import dev.sung.tokyo_life.repository.PostImageRepository;
import dev.sung.tokyo_life.repository.PostRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import dev.sung.tokyo_life.form.ImageDetailsForm;

import java.util.List;

@Service
public class PostImageService {

    private static final Logger log =
            LoggerFactory.getLogger(PostImageService.class);

    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;
    private final ImageStorageService imageStorageService;

    public PostImageService(
            PostRepository postRepository,
            PostImageRepository postImageRepository,
            ImageStorageService imageStorageService
    ) {
        this.postRepository = postRepository;
        this.postImageRepository = postImageRepository;
        this.imageStorageService = imageStorageService;
    }

    @Transactional
    public void upload(
            Long postId,
            MultipartFile file,
            String altText
    ) {

        // 게시글 존재 확인과 잠금
        if (postRepository.lockById(postId) == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "게시글을 찾을 수 없습니다."
            );
        }

        String normalizedAltText = altText == null
                ? null
                : altText.strip();

        if (normalizedAltText != null
                && normalizedAltText.length() > 255) {
            throw new IllegalArgumentException(
                    "이미지 설명은 255자 이내로 입력해주세요."
            );
        }

        List<PostImage> images =
                postImageRepository.findByPostId(postId);

        int maxOrder = images.stream()
                .mapToInt(image -> image.displayOrder())
                .max()
                .orElse(-1);

        int displayOrder = Math.addExact(maxOrder, 1);

        // 대표 이미지가 없으면 새 이미지를 대표로 지정
        boolean isCover = images.stream()
                .noneMatch(image -> Boolean.TRUE.equals(image.isCover()));

        StoredImage stored = imageStorageService.store(file);

        // DB 트랜잭션이 롤백되면 저장한 파일도 삭제
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCompletion(int status) {

                        if (status != STATUS_ROLLED_BACK) {
                            return;
                        }

                        try {
                            imageStorageService.delete(stored.storageKey());
                        } catch (RuntimeException e) {
                            log.error(
                                    "롤백 후 이미지 파일 정리 실패: {}",
                                    stored.storageKey(),
                                    e
                            );
                        }
                    }
                }
        );

        int insertedRows = postImageRepository.insert(
                postId,
                stored.storageKey(),
                stored.originalFilename(),
                stored.contentType(),
                stored.fileSize(),
                normalizedAltText,
                displayOrder,
                isCover
        );

        if (insertedRows != 1) {
            throw new IllegalStateException(
                    "이미지 등록 결과가 올바르지 않습니다."
            );
        }
    }

    @Transactional(readOnly = true)
    public List<PostImage> findByPostId(Long postId) {

        if (postRepository.findByIdForAdmin(postId) == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "게시글을 찾을 수 없습니다."
            );
        }

        return postImageRepository.findByPostId(postId);
    }

    @Transactional(readOnly = true)
    public List<PostImage> findByPublishedPostId(Long postId) {

        // 기존 findById는 공개 글만 조회
        if (postRepository.findById(postId) == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "게시글을 찾을 수 없습니다."
            );
        }

        return postImageRepository.findByPostId(postId);
    }

    @Transactional
    public void delete(Long postId, Long imageId) {

        // 같은 게시글의 이미지 변경 작업을 순서대로 처리
        if (postRepository.lockById(postId) == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "게시글을 찾을 수 없습니다."
            );
        }

        PostImage image = postImageRepository.findByPostId(postId)
                .stream()
                .filter(item -> item.id().equals(imageId))
                .findFirst()
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "이미지를 찾을 수 없습니다."
                        )
                );

        int deletedRows = postImageRepository.deleteByIdAndPostId(
                imageId,
                postId
        );

        if (deletedRows != 1) {
            throw new IllegalStateException(
                    "이미지 삭제 결과가 올바르지 않습니다."
            );
        }

        // DB 삭제가 커밋된 뒤 실제 파일 삭제
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        try {
                            imageStorageService.delete(image.storageKey());
                        } catch (RuntimeException e) {
                            log.error(
                                    "DB 삭제 후 이미지 파일 정리 실패: {}",
                                    image.storageKey(),
                                    e
                            );
                        }
                    }
                }
        );
    }


    @Transactional
    public void changeCover(Long postId, Long imageId) {

        // 업로드·삭제와 같은 잠금 사용
        if (postRepository.lockById(postId) == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "게시글을 찾을 수 없습니다."
            );
        }

        PostImage selected = postImageRepository.findByPostId(postId)
                .stream()
                .filter(image -> image.id().equals(imageId))
                .findFirst()
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "이미지를 찾을 수 없습니다."
                        )
                );

        // 이미 대표 이미지면 변경할 필요 없음
        if (Boolean.TRUE.equals(selected.isCover())) {
            return;
        }

        postImageRepository.clearCover(postId);

        int updatedRows = postImageRepository.setCover(imageId, postId);

        if (updatedRows != 1) {
            throw new IllegalStateException(
                    "대표 이미지 변경 결과가 올바르지 않습니다."
            );
        }
    }

    @Transactional
    public void updateDetails(
            Long postId,
            Long imageId,
            ImageDetailsForm form
    ) {
        if (postRepository.lockById(postId) == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "게시글을 찾을 수 없습니다."
            );
        }

        String altText = form.getAltText() == null
                ? null
                : form.getAltText().strip();

        if (altText != null && altText.length() > 255) {
            throw new IllegalArgumentException(
                    "이미지 설명은 255자 이내로 입력해주세요."
            );
        }

        Integer displayOrder = form.getDisplayOrder();

        if (displayOrder == null || displayOrder < 0) {
            throw new IllegalArgumentException(
                    "표시 순서는 0 이상이어야 합니다."
            );
        }

        int updatedRows = postImageRepository.updateDetails(
                imageId,
                postId,
                altText,
                displayOrder
        );

        if (updatedRows == 0) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "이미지를 찾을 수 없습니다."
            );
        }

        if (updatedRows != 1) {
            throw new IllegalStateException(
                    "이미지 정보 수정 결과가 올바르지 않습니다."
            );
        }
    }
}