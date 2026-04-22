package com.pearadmin.modules.chat.service;

import java.util.List;
import com.github.pagehelper.PageInfo;
import com.pearadmin.common.web.domain.request.PageDomain;
import com.pearadmin.modules.chat.domain.TChatSession;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * AI智能会话主Service接口
 *
 * @author jmys
 * @date 2026-03-16
 */
public interface ITChatSessionService extends IService<TChatSession> {

    /**
     * 查询AI智能会话主
     * @param tChatSession AI智能会话主
     * @param pageDomain
     * @return AI智能会话主 分页集合
     * */
    PageInfo<TChatSession> selectTChatSessionPage(TChatSession tChatSession, PageDomain pageDomain);

    /**
     * 根据会话ID删除历史对话
     *
     * @param sessionId 会话ID
     * @return 结果
     */
    int deleteTChatSessionById(String sessionId);
}
