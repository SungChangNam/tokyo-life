package dev.sung.tokyo_life.mapper;

import dev.sung.tokyo_life.model.AdminUser;
import org.apache.ibatis.annotations.Arg;
import org.apache.ibatis.annotations.ConstructorArgs;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {

    @Select("""
            SELECT id, username, password_hash
            FROM users
            WHERE username = #{username}
            """)
    @ConstructorArgs({
            @Arg(column = "id", javaType = Long.class, id = true),
            @Arg(column = "username", javaType = String.class),
            @Arg(column = "password_hash", javaType = String.class)
    })
    AdminUser findByUsername(@Param("username") String username);
}