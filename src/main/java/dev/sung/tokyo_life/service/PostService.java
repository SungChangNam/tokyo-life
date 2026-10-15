package dev.sung.tokyo_life.service;

import dev.sung.tokyo_life.form.PostCreateForm;
import dev.sung.tokyo_life.mapper.UserMapper;
import dev.sung.tokyo_life.model.AdminPostDetail;
import dev.sung.tokyo_life.model.AdminPostSummary;
import dev.sung.tokyo_life.model.AdminUser;
import dev.sung.tokyo_life.model.Post;
import dev.sung.tokyo_life.model.PostImage;
import dev.sung.tokyo_life.repository.CategoryRepository;
import dev.sung.tokyo_life.repository.PostImageRepository;
import dev.sung.tokyo_life.repository.PostRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;
import dev.sung.tokyo_life.model.PostListItem;

import java.util.List;

@Service
public class PostService {

    private static final Logger log =
            LoggerFactory.getLogger(PostService.class);

    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final UserMapper userMapper;
    private final PostImageRepository postImageRepository;
    private final ImageStorageService imageStorageService;

    public PostService(
            PostRepository postRepository,
            CategoryRepository categoryRepository,
            UserMapper userMapper,
            PostImageRepository postImageRepository,
            ImageStorageService imageStorageService
    ) {
        this.postRepository = postRepository;
        this.categoryRepository = categoryRepository;
        this.userMapper = userMapper;
        this.postImageRepository = postImageRepository;
        this.imageStorageService = imageStorageService;
    }

    public List<Post> findAll() {
        return postRepository.findAll();
    }

    public Post findById(Long id) {
        Post post = postRepository.findById(id);

        if (post == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        return post;
    }

    @Transactional
    public void create(PostCreateForm form, String username) {

        // 1. 로그인한 아이디로 작성자 조회
        AdminUser author = userMapper.findByUsername(username);

        if (author == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "로그인한 사용자 정보를 찾을 수 없습니다."
            );
        }

        // 2. 제목 확인
        if (form.getTitle() == null || form.getTitle().isBlank()) {
            throw new IllegalArgumentException("제목을 입력해주세요.");
        }

        // 3. 허용된 공개 상태인지 확인
        String status = form.getStatus();

        if (!"DRAFT".equals(status) && !"PUBLISHED".equals(status)) {
            throw new IllegalArgumentException(
                    "올바른 공개 상태를 선택해주세요."
            );
        }

        // 4. 선택한 카테고리가 실제로 존재하는지 확인
        Long categoryId = form.getCategoryId();

        if (categoryId != null
                && !categoryRepository.existsById(categoryId)) {
            throw new IllegalArgumentException(
                    "존재하지 않는 카테고리입니다."
            );
        }

        // 5. 공개에 필요한 조건 확인
        if ("PUBLISHED".equals(status)
                && (categoryId == null
                || form.getContent() == null
                || form.getContent().isBlank())) {
            throw new IllegalArgumentException(
                    "공개하려면 카테고리와 본문을 입력해주세요."
            );
        }

        // 6. 게시글 저장
        int insertedRows = postRepository.insert(
                author.getId(),
                categoryId,
                form.getTitle().strip(),
                form.getSummary(),
                form.getContent(),
                status
        );

        if (insertedRows != 1) {
            throw new IllegalStateException(
                    "게시글 저장 결과가 올바르지 않습니다."
            );
        }
    }

    public List<AdminPostSummary> findAllForAdmin() {
        return postRepository.findAllForAdmin();
    }

    public PostCreateForm getEditForm(Long id) {

        AdminPostDetail post = postRepository.findByIdForAdmin(id);

        if (post == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "게시글을 찾을 수 없습니다."
            );
        }

        PostCreateForm form = new PostCreateForm();
        form.setTitle(post.title());
        form.setSummary(post.summary());
        form.setCategoryId(post.categoryId());
        form.setContent(post.content());
        form.setStatus(post.status());

        return form;
    }

    @Transactional
    public void update(Long id, PostCreateForm form) {

        // 1. 수정할 게시글이 존재하는지 확인
        if (postRepository.findByIdForAdmin(id) == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "게시글을 찾을 수 없습니다."
            );
        }

        // 2. 제목 확인
        if (form.getTitle() == null || form.getTitle().isBlank()) {
            throw new IllegalArgumentException("제목을 입력해주세요.");
        }

        // 3. 공개 상태 확인
        String status = form.getStatus();

        if (!"DRAFT".equals(status) && !"PUBLISHED".equals(status)) {
            throw new IllegalArgumentException(
                    "올바른 공개 상태를 선택해주세요."
            );
        }

        // 4. 선택한 카테고리가 실제로 존재하는지 확인
        Long categoryId = form.getCategoryId();

        if (categoryId != null
                && !categoryRepository.existsById(categoryId)) {
            throw new IllegalArgumentException(
                    "존재하지 않는 카테고리입니다."
            );
        }

        // 5. 공개에 필요한 조건 확인
        if ("PUBLISHED".equals(status)
                && (categoryId == null
                || form.getContent() == null
                || form.getContent().isBlank())) {
            throw new IllegalArgumentException(
                    "공개하려면 카테고리와 본문을 입력해주세요."
            );
        }

        // 6. 게시글 수정
        int updatedRows = postRepository.update(
                id,
                categoryId,
                form.getTitle().strip(),
                form.getSummary(),
                form.getContent(),
                status
        );

        if (updatedRows == 0) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "게시글을 찾을 수 없습니다."
            );
        }

        if (updatedRows != 1) {
            throw new IllegalStateException(
                    "게시글 수정 결과가 올바르지 않습니다."
            );
        }
    }

    @Transactional
    public void delete(Long id) {

        // 이미지 업로드·삭제와 동일한 게시글 잠금 사용
        if (postRepository.lockById(id) == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "게시글을 찾을 수 없습니다."
            );
        }

        // CASCADE로 기록이 삭제되기 전에 이미지 정보 확보
        List<PostImage> images = postImageRepository.findByPostId(id);

        int deletedRows = postRepository.deleteById(id);

        if (deletedRows != 1) {
            throw new IllegalStateException(
                    "게시글 삭제 결과가 올바르지 않습니다."
            );
        }

        // DB 삭제가 커밋된 뒤 실제 이미지 파일 정리
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {

                        for (PostImage image : images) {
                            try {
                                imageStorageService.delete(
                                        image.storageKey()
                                );
                            } catch (RuntimeException e) {
                                log.error(
                                        "게시글 삭제 후 이미지 파일 정리 실패: {}",
                                        image.storageKey(),
                                        e
                                );
                            }
                        }
                    }
                }
        );
    }

    public List<PostListItem> findPublishedList() {
        return postRepository.findPublishedList();
    }

    public List<PostListItem> findPublishedList(Long categoryId) {

        // 카테고리를 선택하지 않으면 전체 공개 글 조회
        if (categoryId == null) {
            return postRepository.findPublishedList();
        }

        if (categoryId <= 0
                || !categoryRepository.existsById(categoryId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "카테고리를 찾을 수 없습니다."
            );
        }

        return postRepository.findPublishedListByCategoryId(categoryId);
    }
}