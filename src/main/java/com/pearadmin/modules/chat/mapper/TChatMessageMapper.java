package com.pearadmin.modules.chat.mapper;

import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import com.pearadmin.modules.chat.domain.TChatMessage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * AI智能会话消息明细Mapper接口
 *
 * @author jmys
 * @date 2026-03-16
 */
@Mapper
public interface TChatMessageMapper extends BaseMapper<TChatMessage> {
    /**
     * 查询AI智能会话消息明细列表
     *
     * @param tChatMessage AI智能会话消息明细
     * @return AI智能会话消息明细集合
     */
    List<TChatMessage> selectTChatMessageList(TChatMessage tChatMessage);

}
