package dev.sung.tokyo_life.repository;

import dev.sung.tokyo_life.mapper.PostImageMapper;
import dev.sung.tokyo_life.model.PostImage;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PostImageRepository {

    private final PostImageMapper postImageMapper;

    public PostImageRepository(PostImageMapper postImageMapper) {
        this.postImageMapper = postImageMapper;
    }

    public List<PostImage> findByPostId(Long postId) {
        return postImageMapper.findByPostId(postId);
    }

    public int insert(
            Long postId,
            String storageKey,
            String originalFilename,
            String contentType,
            Long fileSize,
            String altText,
            Integer displayOrder,
            Boolean isCover
    ) {
        return postImageMapper.insert(
                postId,
                storageKey,
                originalFilename,
                contentType,
                fileSize,
                altText,
                displayOrder,
                isCover
        );
    }

    public int deleteByIdAndPostId(Long imageId, Long postId) {
        return postImageMapper.deleteByIdAndPostId(imageId, postId);
    }

    public int clearCover(Long postId) {
        return postImageMapper.clearCover(postId);
    }

    public int setCover(Long imageId, Long postId) {
        return postImageMapper.setCover(imageId, postId);
    }

    public int updateDetails(
            Long imageId,
            Long postId,
            String altText,
            Integer displayOrder
    ) {
        return postImageMapper.updateDetails(
                imageId,
                postId,
                altText,
                displayOrder
        );
    }
}