package dev.sung.tokyo_life.service;

import dev.sung.tokyo_life.form.PostCreateForm;
import dev.sung.tokyo_life.mapper.UserMapper;
import dev.sung.tokyo_life.model.AdminUser;
import dev.sung.tokyo_life.model.Post;
import dev.sung.tokyo_life.repository.CategoryRepository;
import dev.sung.tokyo_life.repository.PostRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import dev.sung.tokyo_life.model.AdminPostSummary;
import dev.sung.tokyo_life.model.AdminPostDetail;

import java.util.List;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final UserMapper userMapper;

    public PostService(
            PostRepository postRepository,
            CategoryRepository categoryRepository,
            UserMapper userMapper
    ) {
        this.postRepository = postRepository;
        this.categoryRepository = categoryRepository;
        this.userMapper = userMapper;
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
        int deletedRows = postRepository.deleteById(id);

        if (deletedRows == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다.");
        }
        if (deletedRows != 1) {
            throw new IllegalStateException("게시글 삭제 결과 올바르지 않습니다.");
        }
    }
}