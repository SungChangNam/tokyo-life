package dev.sung.tokyo_life.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import dev.sung.tokyo_life.model.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

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
}