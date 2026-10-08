package dev.sung.tokyo_life.repository;

import dev.sung.tokyo_life.mapper.PostMapper;
import dev.sung.tokyo_life.model.Post;
import org.springframework.stereotype.Repository;

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
}