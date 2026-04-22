package com.pearadmin.modules.chat.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.pearadmin.common.web.base.BaseDomain;

/**
 * AI智能会话消息明细实体
 *
 * @author jmys
 * @date 2026-03-16
 */
@Data
@TableName("t_chat_message")
public class TChatMessage extends BaseDomain{

    /** 消息ID(主键) */
    @TableId
    private String msgId;

    /** 关联会话ID */
    private String sessionId;

    /** 角色(user-用户, system-系统) */
    private String role;

    /** 消息类型(text-文本, table-表格, file-附件) */
    private String msgType;

    /** 消息内容(存文本、JSON或URL) */
    private String content;


}
