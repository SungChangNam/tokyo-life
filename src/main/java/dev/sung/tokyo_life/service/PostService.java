package dev.sung.tokyo_life.service;

import dev.sung.tokyo_life.form.PostCreateForm;
import dev.sung.tokyo_life.mapper.UserMapper;
import dev.sung.tokyo_life.model.AdminPostDetail;
import dev.sung.tokyo_life.model.AdminPostSummary;
import dev.sung.tokyo_life.model.AdminUser;
import dev.sung.tokyo_life.model.Post;
import dev.sung.tokyo_life.model.PostImage;
import dev.sung.tokyo_life.model.PostListItem;
import dev.sung.tokyo_life.model.PostPage;
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
    private final PostTagService postTagService;

    public PostService(
            PostRepository postRepository,
            CategoryRepository categoryRepository,
            UserMapper userMapper,
            PostImageRepository postImageRepository,
            ImageStorageService imageStorageService,
            PostTagService postTagService
    ) {
        this.postRepository = postRepository;
        this.categoryRepository = categoryRepository;
        this.userMapper = userMapper;
        this.postImageRepository = postImageRepository;
        this.imageStorageService = imageStorageService;
        this.postTagService = postTagService;
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

        // 3. 공개 상태 확인
        String status = form.getStatus();

        if (!"DRAFT".equals(status) && !"PUBLISHED".equals(status)) {
            throw new IllegalArgumentException(
                    "올바른 공개 상태를 선택해주세요."
            );
        }

        // 4. 선택한 카테고리 존재 여부 확인
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

        // 6. 게시글 저장 후 생성된 번호 받기
        Long postId = postRepository.insert(
                author.getId(),
                categoryId,
                form.getTitle().strip(),
                form.getSummary(),
                form.getContent(),
                status
        );

        if (postId == null) {
            throw new IllegalStateException(
                    "생성된 게시글 번호를 확인할 수 없습니다."
            );
        }

        // 7. 선택한 태그 연결
        postTagService.replaceTags(postId, form.getTagIds());
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

        // 기존에 연결된 태그 번호를 폼에 담기
        form.setTagIds(postTagService.findTagIdsByPostId(id));

        return form;
    }

    @Transactional
    public void update(Long id, PostCreateForm form) {

        // 1. 수정할 게시글 존재 확인 및 잠금
        if (postRepository.lockById(id) == null) {
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

        // 4. 선택한 카테고리 존재 여부 확인
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

        // 7. 선택한 태그로 연결 갱신
        postTagService.replaceTags(id, form.getTagIds());
    }

    @Transactional
    public void delete(Long id) {

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

        // DB 커밋 성공 후 실제 이미지 파일 정리
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

    public List<PostListItem> searchPublishedList(
            Long categoryId,
            String keyword
    ) {
        if (categoryId != null
                && (categoryId <= 0
                || !categoryRepository.existsById(categoryId))) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "카테고리를 찾을 수 없습니다."
            );
        }

        String normalizedKeyword = keyword == null
                ? ""
                : keyword.strip();

        if (normalizedKeyword.length() > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "검색어는 100자 이내로 입력해주세요."
            );
        }

        return postRepository.searchPublishedList(
                categoryId,
                normalizedKeyword
        );
    }

    @Transactional(readOnly = true)
    public PostPage findPublishedPage(
            Long categoryId,
            String keyword,
            int page
    ) {
        if (page < 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "페이지 번호는 1 이상이어야 합니다."
            );
        }

        if (categoryId != null
                && (categoryId <= 0
                || !categoryRepository.existsById(categoryId))) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "카테고리를 찾을 수 없습니다."
            );
        }

        String normalizedKeyword = keyword == null
                ? ""
                : keyword.strip();

        if (normalizedKeyword.length() > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "검색어는 100자 이내로 입력해주세요."
            );
        }

        int pageSize = 10;

        long totalCount = postRepository.countPublished(
                categoryId,
                normalizedKeyword
        );

        long totalPages = totalCount == 0
                ? 1
                : (totalCount - 1) / pageSize + 1;

        if (page > totalPages) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "존재하지 않는 페이지입니다."
            );
        }

        long offset = ((long) page - 1) * pageSize;

        List<PostListItem> posts = postRepository.findPublishedPage(
                categoryId,
                normalizedKeyword,
                pageSize,
                offset
        );

        return new PostPage(
                posts,
                page,
                pageSize,
                totalCount,
                totalPages
        );
    }
}