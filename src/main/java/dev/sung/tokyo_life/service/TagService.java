package dev.sung.tokyo_life.service;

import dev.sung.tokyo_life.model.AdminTagSummary;
import dev.sung.tokyo_life.model.Tag;
import dev.sung.tokyo_life.repository.TagRepository;
import org.springframework.stereotype.Service;
import dev.sung.tokyo_life.form.TagForm;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class TagService {

    private final TagRepository tagRepository;

    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    public List<Tag> findAll() {
        return tagRepository.findAll();
    }

    public List<AdminTagSummary> findAllForAdmin() {
        return tagRepository.findAllForAdmin();
    }

    @Transactional
    public void create(TagForm form) {

        if (form.getName() == null || form.getName().isBlank()) {
            throw new IllegalArgumentException(
                    "태그 이름을 입력해주세요."
            );
        }

        String name = form.getName().strip();

        if (name.length() > 50) {
            throw new IllegalArgumentException(
                    "태그 이름은 50자 이내로 입력해주세요."
            );
        }

        if (tagRepository.existsByName(name)) {
            throw new IllegalArgumentException(
                    "이미 등록된 태그 이름입니다."
            );
        }

        int insertedRows = tagRepository.insert(name);

        if (insertedRows != 1) {
            throw new IllegalStateException(
                    "태그 등록 결과가 올바르지 않습니다."
            );
        }
    }

    public TagForm getEditForm(Long id) {

        Tag tag = tagRepository.findById(id);

        if (tag == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "태그를 찾을 수 없습니다."
            );
        }

        TagForm form = new TagForm();
        form.setName(tag.name());

        return form;
    }

    @Transactional
    public void update(Long id, TagForm form) {

        if (tagRepository.findById(id) == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "태그를 찾을 수 없습니다."
            );
        }

        if (form.getName() == null || form.getName().isBlank()) {
            throw new IllegalArgumentException(
                    "태그 이름을 입력해주세요."
            );
        }

        String name = form.getName().strip();

        if (name.length() > 50) {
            throw new IllegalArgumentException(
                    "태그 이름은 50자 이내로 입력해주세요."
            );
        }

        if (tagRepository.existsByNameExcludingId(name, id)) {
            throw new IllegalArgumentException(
                    "이미 등록된 태그 이름입니다."
            );
        }

        int updatedRows = tagRepository.update(id, name);

        if (updatedRows == 0) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "태그를 찾을 수 없습니다."
            );
        }

        if (updatedRows != 1) {
            throw new IllegalStateException(
                    "태그 수정 결과가 올바르지 않습니다."
            );
        }
    }
    @Transactional
    public void delete(Long id) {

        int deletedRows = tagRepository.deleteById(id);

        if (deletedRows == 0) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "태그를 찾을 수 없습니다."
            );
        }

        if (deletedRows != 1) {
            throw new IllegalStateException(
                    "태그 삭제 결과가 올바르지 않습니다."
            );
        }
    }

    public List<Tag> findByPostId(Long postId) {
        return tagRepository.findByPostId(postId);
    }
}