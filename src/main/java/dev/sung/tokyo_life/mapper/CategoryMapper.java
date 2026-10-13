package dev.sung.tokyo_life.mapper;

import org.apache.ibatis.annotations.Param;
import dev.sung.tokyo_life.model.Category;
import org.apache.ibatis.annotations.Arg;
import org.apache.ibatis.annotations.ConstructorArgs;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import dev.sung.tokyo_life.model.AdminCategorySummary;
import java.time.OffsetDateTime;
import org.apache.ibatis.annotations.Insert;
import dev.sung.tokyo_life.form.CategoryForm;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Delete;

import java.util.List;

@Mapper
public interface CategoryMapper {

    @Select("""
            SELECT id, name
            FROM categories
            ORDER BY display_order, id
            """)
    @ConstructorArgs({
            @Arg(column = "id", javaType = Long.class, id = true),
            @Arg(column = "name", javaType = String.class)
    })
    List<Category> findAll();

    @Select("""
        SELECT EXISTS (
            SELECT 1
            FROM categories
            WHERE id = #{id}
        )
        """)
    boolean existsById(@Param("id") Long id);

    @Select("""
        SELECT c.id,
               c.name,
               c.display_order,
               COUNT(p.id) AS post_count,
               c.created_at,
               c.updated_at
        FROM categories c
        LEFT JOIN posts p
            ON p.category_id = c.id
        GROUP BY c.id,
                 c.name,
                 c.display_order,
                 c.created_at,
                 c.updated_at
        ORDER BY c.display_order, c.id
        """)
    @ConstructorArgs({
            @Arg(column = "id", javaType = Long.class, id = true),
            @Arg(column = "name", javaType = String.class),
            @Arg(column = "display_order", javaType = Integer.class),
            @Arg(column = "post_count", javaType = Long.class),
            @Arg(column = "created_at", javaType = OffsetDateTime.class),
            @Arg(column = "updated_at", javaType = OffsetDateTime.class)
    })
    List<AdminCategorySummary> findAllForAdmin();

    @Insert("""
        INSERT INTO categories (name, display_order)
        VALUES (#{name}, #{displayOrder})
        """)
    int insert(
            @Param("name") String name,
            @Param("displayOrder") Integer displayOrder
    );

    @Select("""
        SELECT EXISTS (
            SELECT 1
            FROM categories
            WHERE name = #{name}
        )
        """)
    boolean existsByName(@Param("name") String name);

    @Select("""
        SELECT name, display_order
        FROM categories
        WHERE id = #{id}
        """)
    @Results({
            @Result(column = "name", property = "name"),
            @Result(column = "display_order", property = "displayOrder")
    })
    CategoryForm findEditFormById(@Param("id") Long id);

    @Select("""
        SELECT EXISTS (
            SELECT 1
            FROM categories
            WHERE name = #{name}
              AND id <> #{id}
        )
        """)
    boolean existsByNameExcludingId(
            @Param("name") String name,
            @Param("id") Long id
    );

    @Update("""
        UPDATE categories
        SET name = #{name},
            display_order = #{displayOrder},
            updated_at = CURRENT_TIMESTAMP
        WHERE id = #{id}
        """)
    int update(
            @Param("id") Long id,
            @Param("name") String name,
            @Param("displayOrder") Integer displayOrder
    );

    @Select("""
        SELECT EXISTS (
            SELECT 1
            FROM posts
            WHERE category_id = #{id}
        )
        """)
    boolean isUsedByPosts(@Param("id") Long id);

    @Delete("""
        DELETE FROM categories
        WHERE id = #{id}
        """)
    int deleteById(@Param("id") Long id);
}


