package com.pearadmin.modules.chat.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.pearadmin.common.web.base.BaseDomain;

/**
 * AI智能会话主实体
 *
 * @author jmys
 * @date 2026-03-16
 */
@Data
@TableName("t_chat_session")
public class TChatSession extends BaseDomain{

    /** 会话ID(主键) */
    @TableId
    private String sessionId;

    /** 创建人ID(关联系统用户) */
    private String userId;

    /** 会话类型(1-智能问数, 2-智能分析) */
    private Long sessionType;

    /** 会话标题 */
    private String sessionTitle;

    /** 是否置顶 **/
    private Integer isTop;

    public Integer getIsTop() {
        return isTop;
    }

    public void setIsTop(Integer isTop) {
        this.isTop = isTop;
    }

    /** 置顶时间 **/
    private java.time.LocalDateTime topTime;

    public java.time.LocalDateTime getTopTime() {
        return topTime;
    }

    public void setTopTime(java.time.LocalDateTime topTime) {
        this.topTime = topTime;
    }

}
