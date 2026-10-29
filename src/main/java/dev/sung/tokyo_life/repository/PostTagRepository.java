package dev.sung.tokyo_life.repository;

import dev.sung.tokyo_life.mapper.PostTagMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PostTagRepository {

    private final PostTagMapper postTagMapper;

    public PostTagRepository(PostTagMapper postTagMapper) {
        this.postTagMapper = postTagMapper;
    }

    public List<Long> findTagIdsByPostId(Long postId) {
        return postTagMapper.findTagIdsByPostId(postId);
    }

    public int insert(Long postId, Long tagId) {
        return postTagMapper.insert(postId, tagId);
    }

    public int deleteByPostId(Long postId) {
        return postTagMapper.deleteByPostId(postId);
    }
}