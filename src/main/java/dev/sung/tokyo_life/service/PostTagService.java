package dev.sung.tokyo_life.service;

import dev.sung.tokyo_life.repository.PostRepository;
import dev.sung.tokyo_life.repository.PostTagRepository;
import dev.sung.tokyo_life.repository.TagRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class PostTagService {

    private final PostRepository postRepository;
    private final PostTagRepository postTagRepository;
    private final TagRepository tagRepository;

    public PostTagService(
            PostRepository postRepository,
            PostTagRepository postTagRepository,
            TagRepository tagRepository
    ) {
        this.postRepository = postRepository;
        this.postTagRepository = postTagRepository;
        this.tagRepository = tagRepository;
    }

    public List<Long> findTagIdsByPostId(Long postId) {
        return postTagRepository.findTagIdsByPostId(postId);
    }

    @Transactional
    public void replaceTags(Long postId, List<Long> tagIds) {

        // 같은 게시글의 변경 작업을 순서대로 처리
        if (postRepository.lockById(postId) == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "게시글을 찾을 수 없습니다."
            );
        }

        List<Long> requestedIds = tagIds == null
                ? List.of()
                : tagIds;

        if (requestedIds.size() > 20) {
            throw new IllegalArgumentException(
                    "태그는 최대 20개까지 선택해주세요."
            );
        }

        // 중복된 태그 번호 제거
        Set<Long> uniqueIds = new LinkedHashSet<>(requestedIds);

        // 기존 연결을 삭제하기 전에 모든 태그 검증
        for (Long tagId : uniqueIds) {

            if (tagId == null || tagId <= 0) {
                throw new IllegalArgumentException(
                        "올바른 태그를 선택해주세요."
                );
            }

            if (!tagRepository.existsById(tagId)) {
                throw new IllegalArgumentException(
                        "존재하지 않는 태그가 포함되어 있습니다."
                );
            }
        }

        postTagRepository.deleteByPostId(postId);

        for (Long tagId : uniqueIds) {
            int insertedRows = postTagRepository.insert(postId, tagId);

            if (insertedRows != 1) {
                throw new IllegalStateException(
                        "게시글 태그 저장 결과가 올바르지 않습니다."
                );
            }
        }
    }
}