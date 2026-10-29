package dev.sung.tokyo_life.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PostTagMapper {

    @Select("""
            SELECT tag_id
            FROM post_tags
            WHERE post_id = #{postId}
            ORDER BY tag_id
            """)
    List<Long> findTagIdsByPostId(@Param("postId") Long postId);

    @Insert("""
            INSERT INTO post_tags (post_id, tag_id)
            VALUES (#{postId}, #{tagId})
            """)
    int insert(
            @Param("postId") Long postId,
            @Param("tagId") Long tagId
    );

    @Delete("""
            DELETE FROM post_tags
            WHERE post_id = #{postId}
            """)
    int deleteByPostId(@Param("postId") Long postId);
}