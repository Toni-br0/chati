package com.pearadmin.modules.chat.mapper;

import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import com.pearadmin.modules.chat.domain.TChatSession;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * AI智能会话主Mapper接口
 *
 * @author jmys
 * @date 2026-03-16
 */
@Mapper
public interface TChatSessionMapper extends BaseMapper<TChatSession> {
    /**
     * 查询AI智能会话主列表
     *
     * @param tChatSession AI智能会话主
     * @return AI智能会话主集合
     */
    List<TChatSession> selectTChatSessionList(TChatSession tChatSession);

    /**
     * 根据会话ID删除历史对话
     *
     * @param sessionId 会话ID
     * @return 影响的行数
     */
    int deleteTChatSessionById(String sessionId);
}
