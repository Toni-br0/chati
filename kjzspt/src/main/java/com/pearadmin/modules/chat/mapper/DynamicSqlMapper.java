package com.pearadmin.modules.chat.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Map;

/**
 * 专门用于执行 AI 动态生成的原生 SQL 的 Mapper
 */
@Mapper
@DS("second")
public interface DynamicSqlMapper {

    @Select("${sql}")
    List<Map<String, Object>> executeSelectSql(@Param("sql") String sql);

}