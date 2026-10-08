package dev.sung.tokyo_life.repository;

import dev.sung.tokyo_life.mapper.CategoryMapper;
import dev.sung.tokyo_life.model.Category;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CategoryRepository {

    private final CategoryMapper categoryMapper;

    public CategoryRepository(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    public List<Category> findAll() {
        return categoryMapper.findAll();
    }

    public boolean existsById(Long id) {
        return categoryMapper.existsById(id);
    }
}