package dev.sung.tokyo_life.repository;

import dev.sung.tokyo_life.mapper.CategoryMapper;
import dev.sung.tokyo_life.model.Category;
import org.springframework.stereotype.Repository;
import dev.sung.tokyo_life.model.AdminCategorySummary;
import dev.sung.tokyo_life.form.CategoryForm;

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

    public List<AdminCategorySummary> findAllForAdmin() {
        return categoryMapper.findAllForAdmin();
    }

    public int insert(String name, Integer displayOrder) {
        return categoryMapper.insert(name, displayOrder);
    }

    public boolean existsByName(String name) {
        return categoryMapper.existsByName(name);
    }

    public CategoryForm findEditFormById(Long id) {
        return categoryMapper.findEditFormById(id);
    }

    public boolean existsByNameExcludingId(String name, Long id) {
        return categoryMapper.existsByNameExcludingId(name, id);
    }

    public int update(Long id, String name, Integer displayOrder) {
        return categoryMapper.update(id, name, displayOrder);
    }

    public boolean isUsedByPosts(Long id) {
        return categoryMapper.isUsedByPosts(id);
    }

    public int deleteById(Long id) {
        return categoryMapper.deleteById(id);
    }
}