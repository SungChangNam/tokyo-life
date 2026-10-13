package dev.sung.tokyo_life.service;

import dev.sung.tokyo_life.model.Category;
import dev.sung.tokyo_life.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import dev.sung.tokyo_life.model.AdminCategorySummary;
import dev.sung.tokyo_life.form.CategoryForm;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    public List<AdminCategorySummary> findAllForAdmin() {
        return categoryRepository.findAllForAdmin();
    }

    @Transactional
    public void create(CategoryForm form) {

        if (form.getName() == null || form.getName().isBlank()) {
            throw new IllegalArgumentException(
                    "카테고리 이름을 입력해주세요."
            );
        }

        String name = form.getName().strip();

        if (name.length() > 50) {
            throw new IllegalArgumentException(
                    "이름은 50자 이내로 입력해주세요."
            );
        }

        Integer displayOrder = form.getDisplayOrder();

        if (displayOrder == null || displayOrder < 0) {
            throw new IllegalArgumentException(
                    "표시 순서는 0 이상이어야 합니다."
            );
        }

        if (categoryRepository.existsByName(name)) {
            throw new IllegalArgumentException(
                    "이미 등록된 카테고리 이름입니다."
            );
        }

        int insertedRows = categoryRepository.insert(name, displayOrder);

        if (insertedRows != 1) {
            throw new IllegalStateException(
                    "카테고리 등록 결과가 올바르지 않습니다."
            );
        }
    }

    public CategoryForm getEditForm(Long id) {

        CategoryForm form = categoryRepository.findEditFormById(id);

        if (form == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "카테고리를 찾을 수 없습니다."
            );
        }

        return form;
    }

    @Transactional
    public void update(Long id, CategoryForm form) {

        if (!categoryRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "카테고리를 찾을 수 없습니다."
            );
        }

        if (form.getName() == null || form.getName().isBlank()) {
            throw new IllegalArgumentException(
                    "카테고리 이름을 입력해주세요."
            );
        }

        String name = form.getName().strip();

        if (name.length() > 50) {
            throw new IllegalArgumentException(
                    "이름은 50자 이내로 입력해주세요."
            );
        }

        Integer displayOrder = form.getDisplayOrder();

        if (displayOrder == null || displayOrder < 0) {
            throw new IllegalArgumentException(
                    "표시 순서는 0 이상이어야 합니다."
            );
        }

        if (categoryRepository.existsByNameExcludingId(name, id)) {
            throw new IllegalArgumentException(
                    "이미 등록된 카테고리 이름입니다."
            );
        }

        int updatedRows = categoryRepository.update(
                id,
                name,
                displayOrder
        );

        if (updatedRows == 0) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "카테고리를 찾을 수 없습니다."
            );
        }

        if (updatedRows != 1) {
            throw new IllegalStateException(
                    "카테고리 수정 결과가 올바르지 않습니다."
            );
        }
    }

    @Transactional
    public void delete(Long id) {

        if (!categoryRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "카테고리를 찾을 수 없습니다."
            );
        }

        if (categoryRepository.isUsedByPosts(id)) {
            throw new IllegalArgumentException(
                    "게시글에서 사용 중인 카테고리는 삭제할 수 없습니다."
            );
        }

        int deletedRows = categoryRepository.deleteById(id);

        if (deletedRows == 0) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "카테고리를 찾을 수 없습니다."
            );
        }

        if (deletedRows != 1) {
            throw new IllegalStateException(
                    "카테고리 삭제 결과가 올바르지 않습니다."
            );
        }
    }
}