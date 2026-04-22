package com.pearadmin.modules.chat.service.impl;

import java.util.List;
import java.util.ArrayList;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pearadmin.common.web.domain.request.PageDomain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.pearadmin.modules.chat.mapper.TChatMessageMapper;
import com.pearadmin.modules.chat.domain.TChatMessage;
import com.pearadmin.modules.chat.service.ITChatMessageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import java.util.Arrays;
/**
 * AI智能会话消息明细Service业务层处理
 *
 * @author jmys
 * @date 2026-03-16
 */
@Service
public class TChatMessageServiceImpl extends ServiceImpl<TChatMessageMapper,TChatMessage> implements ITChatMessageService {


    /**
     * 查询AI智能会话消息明细
     * @param tChatMessage AI智能会话消息明细
     * @param pageDomain
     * @return AI智能会话消息明细 分页集合
     * */
    @Override
    public PageInfo<TChatMessage> selectTChatMessagePage(TChatMessage tChatMessage, PageDomain pageDomain) {
        PageHelper.startPage(pageDomain.getPage(), pageDomain.getLimit());
        List<TChatMessage> data = baseMapper.selectTChatMessageList(tChatMessage);
        return new PageInfo<>(data);
    }

}
