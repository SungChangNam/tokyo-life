package dev.sung.tokyo_life.mapper;


import dev.sung.tokyo_life.model.AdminPostSummary;
import org.apache.ibatis.annotations.Arg;
import org.apache.ibatis.annotations.ConstructorArgs;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import dev.sung.tokyo_life.model.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import dev.sung.tokyo_life.model.AdminPostDetail;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Delete;
import dev.sung.tokyo_life.model.PostListItem;

import java.time.OffsetDateTime;
import java.util.List;

@Mapper
public interface PostMapper {

    @Select("""
            SELECT id, title, content
            FROM posts
            WHERE status = 'PUBLISHED'
            ORDER BY published_at DESC, id DESC
            """)
    List<Post> findAll();

    @Select("""
            SELECT id, title, content
            FROM posts
            WHERE id = #{id}
              AND status = 'PUBLISHED'
            """)
    Post findById(Long id);

    @Insert("""
        INSERT INTO posts (
            author_id,
            category_id,
            title,
            summary,
            content,
            status,
            published_at
        )
        VALUES (
            #{authorId},
            #{categoryId},
            #{title},
            #{summary},
            #{content},
            #{status},
            CASE
                WHEN #{status} = 'PUBLISHED' THEN CURRENT_TIMESTAMP
                ELSE NULL
            END
        )
        """)
    int insert(
            @Param("authorId") Long authorId,
            @Param("categoryId") Long categoryId,
            @Param("title") String title,
            @Param("summary") String summary,
            @Param("content") String content,
            @Param("status") String status
    );

    @Select("""
        SELECT EXISTS (
            SELECT 1
            FROM categories
            WHERE id = #{id}
        )
        """)
    boolean existsById(@Param("id") Long id);

    @Select("""
        SELECT id, title, status, created_at
        FROM posts
        ORDER BY created_at DESC, id DESC
        """)
    @ConstructorArgs({
            @Arg(column = "id", javaType = Long.class, id = true),
            @Arg(column = "title", javaType = String.class),
            @Arg(column = "status", javaType = String.class),
            @Arg(column = "created_at", javaType = OffsetDateTime.class)
    })
    List<AdminPostSummary> findAllForAdmin();

    @Select("""
        SELECT id, title, summary, category_id, content, status
        FROM posts
        WHERE id = #{id}
        """)
    @ConstructorArgs({
            @Arg(column = "id", javaType = Long.class, id = true),
            @Arg(column = "title", javaType = String.class),
            @Arg(column = "summary", javaType = String.class),
            @Arg(column = "category_id", javaType = Long.class),
            @Arg(column = "content", javaType = String.class),
            @Arg(column = "status", javaType = String.class)
    })
    AdminPostDetail findByIdForAdmin(@Param("id") Long id);

    @Update("""
        UPDATE posts
        SET title = #{title},
            summary = #{summary},
            category_id = #{categoryId},
            content = #{content},
            status = #{status},
            published_at = CASE
                WHEN #{status} = 'PUBLISHED'
                    THEN COALESCE(published_at, CURRENT_TIMESTAMP)
                ELSE NULL
            END,
            updated_at = CURRENT_TIMESTAMP
        WHERE id = #{id}
        """)
    int update(
            @Param("id") Long id,
            @Param("categoryId") Long categoryId,
            @Param("title") String title,
            @Param("summary") String summary,
            @Param("content") String content,
            @Param("status") String status
    );

    @Delete("""
        DELETE FROM posts
        WHERE id = #{id}
        """)
    int deleteById(@Param("id") Long id);

    @Select("""
        SELECT id
        FROM posts
        WHERE id = #{id}
        FOR UPDATE
        """)
    Long lockById(@Param("id") Long id);

    @Select("""
        SELECT p.id,
               p.title,
               p.summary,
               i.id AS cover_image_id,
               i.alt_text AS cover_alt_text
        FROM posts p
        LEFT JOIN post_images i
            ON i.post_id = p.id
           AND i.is_cover = true
        WHERE p.status = 'PUBLISHED'
        ORDER BY p.published_at DESC, p.id DESC
        """)
    @ConstructorArgs({
            @Arg(column = "id", javaType = Long.class, id = true),
            @Arg(column = "title", javaType = String.class),
            @Arg(column = "summary", javaType = String.class),
            @Arg(column = "cover_image_id", javaType = Long.class),
            @Arg(column = "cover_alt_text", javaType = String.class)
    })
    List<PostListItem> findPublishedList();

}
