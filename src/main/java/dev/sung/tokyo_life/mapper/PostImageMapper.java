package dev.sung.tokyo_life.mapper;

import dev.sung.tokyo_life.model.PostImage;
import org.apache.ibatis.annotations.Arg;
import org.apache.ibatis.annotations.ConstructorArgs;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Update;

import java.time.OffsetDateTime;
import java.util.List;

@Mapper
public interface PostImageMapper {

    @Select("""
            SELECT id,
                   post_id,
                   storage_key,
                   original_filename,
                   content_type,
                   file_size,
                   alt_text,
                   display_order,
                   is_cover,
                   created_at,
                   updated_at
            FROM post_images
            WHERE post_id = #{postId}
            ORDER BY display_order ASC, id ASC
            """)
    @ConstructorArgs({
            @Arg(column = "id", javaType = Long.class, id = true),
            @Arg(column = "post_id", javaType = Long.class),
            @Arg(column = "storage_key", javaType = String.class),
            @Arg(column = "original_filename", javaType = String.class),
            @Arg(column = "content_type", javaType = String.class),
            @Arg(column = "file_size", javaType = Long.class),
            @Arg(column = "alt_text", javaType = String.class),
            @Arg(column = "display_order", javaType = Integer.class),
            @Arg(column = "is_cover", javaType = Boolean.class),
            @Arg(column = "created_at", javaType = OffsetDateTime.class),
            @Arg(column = "updated_at", javaType = OffsetDateTime.class)
    })
    List<PostImage> findByPostId(@Param("postId") Long postId);

    @Insert("""
            INSERT INTO post_images (
                post_id,
                storage_key,
                original_filename,
                content_type,
                file_size,
                alt_text,
                display_order,
                is_cover
            )
            VALUES (
                #{postId},
                #{storageKey},
                #{originalFilename},
                #{contentType},
                #{fileSize},
                #{altText},
                #{displayOrder},
                #{isCover}
            )
            """)
    int insert(
            @Param("postId") Long postId,
            @Param("storageKey") String storageKey,
            @Param("originalFilename") String originalFilename,
            @Param("contentType") String contentType,
            @Param("fileSize") Long fileSize,
            @Param("altText") String altText,
            @Param("displayOrder") Integer displayOrder,
            @Param("isCover") Boolean isCover
    );

    @Delete("""
        DELETE FROM post_images
        WHERE id = #{imageId}
          AND post_id = #{postId}
        """)
    int deleteByIdAndPostId(
            @Param("imageId") Long imageId,
            @Param("postId") Long postId
    );

    @Update("""
        UPDATE post_images
        SET is_cover = false,
            updated_at = CURRENT_TIMESTAMP
        WHERE post_id = #{postId}
          AND is_cover = true
        """)
    int clearCover(@Param("postId") Long postId);

    @Update("""
        UPDATE post_images
        SET is_cover = true,
            updated_at = CURRENT_TIMESTAMP
        WHERE id = #{imageId}
          AND post_id = #{postId}
        """)
    int setCover(
            @Param("imageId") Long imageId,
            @Param("postId") Long postId
    );
}