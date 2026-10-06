package dev.sung.tokyo_life.mapper;

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
}