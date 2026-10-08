package dev.sung.tokyo_life.mapper;

import org.apache.ibatis.annotations.Param;
import dev.sung.tokyo_life.model.Category;
import org.apache.ibatis.annotations.Arg;
import org.apache.ibatis.annotations.ConstructorArgs;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

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

}