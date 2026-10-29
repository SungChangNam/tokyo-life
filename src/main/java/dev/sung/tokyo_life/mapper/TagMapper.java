package dev.sung.tokyo_life.mapper;

import dev.sung.tokyo_life.model.AdminTagSummary;
import dev.sung.tokyo_life.model.Tag;
import org.apache.ibatis.annotations.Arg;
import org.apache.ibatis.annotations.ConstructorArgs;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Delete;

import java.time.OffsetDateTime;
import java.util.List;

@Mapper
public interface TagMapper {

    @Select("""
            SELECT id, name
            FROM tags
            ORDER BY name, id
            """)
    @ConstructorArgs({
            @Arg(column = "id", javaType = Long.class, id = true),
            @Arg(column = "name", javaType = String.class)
    })
    List<Tag> findAll();

    @Select("""
            SELECT t.id,
                   t.name,
                   COUNT(pt.post_id) AS post_count,
                   t.created_at,
                   t.updated_at
            FROM tags t
            LEFT JOIN post_tags pt
                ON pt.tag_id = t.id
            GROUP BY t.id,
                     t.name,
                     t.created_at,
                     t.updated_at
            ORDER BY t.name, t.id
            """)
    @ConstructorArgs({
            @Arg(column = "id", javaType = Long.class, id = true),
            @Arg(column = "name", javaType = String.class),
            @Arg(column = "post_count", javaType = Long.class),
            @Arg(column = "created_at", javaType = OffsetDateTime.class),
            @Arg(column = "updated_at", javaType = OffsetDateTime.class)
    })
    List<AdminTagSummary> findAllForAdmin();

    @Select("""
        SELECT EXISTS (
            SELECT 1
            FROM tags
            WHERE name = #{name}
        )
        """)
    boolean existsByName(@Param("name") String name);

    @Insert("""
        INSERT INTO tags (name)
        VALUES (#{name})
        """)
    int insert(@Param("name") String name);

    @Select("""
        SELECT id, name
        FROM tags
        WHERE id = #{id}
        """)
    @ConstructorArgs({
            @Arg(column = "id", javaType = Long.class, id = true),
            @Arg(column = "name", javaType = String.class)
    })
    Tag findById(@Param("id") Long id);

    @Select("""
        SELECT EXISTS (
            SELECT 1
            FROM tags
            WHERE name = #{name}
              AND id <> #{id}
        )
        """)
    boolean existsByNameExcludingId(
            @Param("name") String name,
            @Param("id") Long id
    );

    @Update("""
        UPDATE tags
        SET name = #{name},
            updated_at = CURRENT_TIMESTAMP
        WHERE id = #{id}
        """)
    int update(
            @Param("id") Long id,
            @Param("name") String name
    );

    @Delete("""
        DELETE FROM tags
        WHERE id = #{id}
        """)
    int deleteById(@Param("id") Long id);

    @Select("""
        SELECT EXISTS (
            SELECT 1
            FROM tags
            WHERE id = #{id}
        )
        """)
    boolean existsById(@Param("id") Long id);

    @Select("""
        SELECT t.id, t.name
        FROM tags t
        JOIN post_tags pt
            ON pt.tag_id = t.id
        WHERE pt.post_id = #{postId}
        ORDER BY t.name, t.id
        """)
    @ConstructorArgs({
            @Arg(column = "id", javaType = Long.class, id = true),
            @Arg(column = "name", javaType = String.class)
    })
    List<Tag> findByPostId(@Param("postId") Long postId);
}