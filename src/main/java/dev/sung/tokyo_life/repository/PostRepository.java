package dev.sung.tokyo_life.repository;

import dev.sung.tokyo_life.mapper.PostMapper;
import dev.sung.tokyo_life.model.Post;
import org.springframework.stereotype.Repository;
import dev.sung.tokyo_life.model.AdminPostSummary;
import dev.sung.tokyo_life.model.AdminPostDetail;
import dev.sung.tokyo_life.model.PostListItem;

import java.util.List;

@Repository
public class PostRepository {

    private final PostMapper postMapper;

    public PostRepository(PostMapper postMapper) {
        this.postMapper = postMapper;
    }

    public List<Post> findAll() {
        return postMapper.findAll();
    }

    public Post findById(Long id) {
        return postMapper.findById(id);
    }
    public int insert(
            Long authorId,
            Long categoryId,
            String title,
            String summary,
            String content,
            String status
    ) {
        return postMapper.insert(
                authorId,
                categoryId,
                title,
                summary,
                content,
                status
        );
    }
    public List<AdminPostSummary> findAllForAdmin() {
        return postMapper.findAllForAdmin();
    }

    public AdminPostDetail findByIdForAdmin(Long id) {
        return postMapper.findByIdForAdmin(id);
    }

    public int update(
            Long id,
            Long categoryId,
            String title,
            String summary,
            String content,
            String status
    ) {
        return postMapper.update(
                id,
                categoryId,
                title,
                summary,
                content,
                status
        );
    }

    public int deleteById(Long id) {
        return postMapper.deleteById(id);
    }

    public Long lockById(Long id) {
        return postMapper.lockById(id);
    }

    public List<PostListItem> findPublishedList() {
        return postMapper.findPublishedList();
    }

    public List<PostListItem> findPublishedListByCategoryId(Long categoryId) {
        return postMapper.findPublishedListByCategoryId(categoryId);
    }

    public List<PostListItem> searchPublishedList(
            Long categoryId,
            String keyword
    ) {
        return postMapper.searchPublishedList(categoryId, keyword);
    }
}