package com.pearadmin.modules.chat.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.pearadmin.modules.chat.mapper.DynamicSqlMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class DynamicSqlService {

    @Autowired
    private DynamicSqlMapper dynamicSqlMapper;

    @DS("second")
    public List<Map<String, Object>> executeDorisSql(String sql) {
        return dynamicSqlMapper.executeSelectSql(sql);
    }
}