package com.pearadmin.modules.chat.service.impl;

import java.util.List;
import java.util.ArrayList;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pearadmin.common.web.domain.request.PageDomain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.pearadmin.modules.chat.mapper.TChatSessionMapper;
import com.pearadmin.modules.chat.domain.TChatSession;
import com.pearadmin.modules.chat.service.ITChatSessionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import java.util.Arrays;
/**
 * AI智能会话主Service业务层处理
 *
 * @author jmys
 * @date 2026-03-16
 */
@Service
public class TChatSessionServiceImpl extends ServiceImpl<TChatSessionMapper,TChatSession> implements ITChatSessionService {

    @Autowired
    private TChatSessionMapper tChatSessionMapper;

    /**
     * 查询AI智能会话主
     * @param tChatSession AI智能会话主
     * @param pageDomain
     * @return AI智能会话主 分页集合
     * */
    @Override
    public PageInfo<TChatSession> selectTChatSessionPage(TChatSession tChatSession, PageDomain pageDomain) {
        PageHelper.startPage(pageDomain.getPage(), pageDomain.getLimit());
        List<TChatSession> data = baseMapper.selectTChatSessionList(tChatSession);
        return new PageInfo<>(data);
    }

    /**
     * 根据会话ID删除历史对话
     *
     * @param sessionId 会话ID
     * @return 结果
     */
    @Override
    public int deleteTChatSessionById(String sessionId) {
        return tChatSessionMapper.deleteTChatSessionById(sessionId);
    }

}
