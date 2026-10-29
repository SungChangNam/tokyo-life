package dev.sung.tokyo_life.repository;

import dev.sung.tokyo_life.mapper.TagMapper;
import dev.sung.tokyo_life.model.AdminTagSummary;
import dev.sung.tokyo_life.model.Tag;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class TagRepository {

    private final TagMapper tagMapper;

    public TagRepository(TagMapper tagMapper) {
        this.tagMapper = tagMapper;
    }

    public List<Tag> findAll() {
        return tagMapper.findAll();
    }

    public List<AdminTagSummary> findAllForAdmin() {
        return tagMapper.findAllForAdmin();
    }

    public boolean existsByName(String name) {
        return tagMapper.existsByName(name);
    }

    public int insert(String name) {
        return tagMapper.insert(name);
    }

    public Tag findById(Long id) {
        return tagMapper.findById(id);
    }

    public boolean existsByNameExcludingId(String name, Long id) {
        return tagMapper.existsByNameExcludingId(name, id);
    }

    public int update(Long id, String name) {
        return tagMapper.update(id, name);
    }

    public int deleteById(Long id) {
        return tagMapper.deleteById(id);
    }

    public boolean existsById(Long id) {
        return tagMapper.existsById(id);
    }
    public List<Tag> findByPostId(Long postId) {
        return tagMapper.findByPostId(postId);
    }
}