package com.pearadmin.modules.chat.service;

import java.util.List;
import com.github.pagehelper.PageInfo;
import com.pearadmin.common.web.domain.request.PageDomain;
import com.pearadmin.modules.chat.domain.TChatMessage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * AI智能会话消息明细Service接口
 *
 * @author jmys
 * @date 2026-03-16
 */
public interface ITChatMessageService extends IService<TChatMessage> {

    /**
     * 查询AI智能会话消息明细
     * @param tChatMessage AI智能会话消息明细
     * @param pageDomain
     * @return AI智能会话消息明细 分页集合
     * */
    PageInfo<TChatMessage> selectTChatMessagePage(TChatMessage tChatMessage, PageDomain pageDomain);

}
