package com.pearadmin.modules.chat.domain;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 接收 AI 智能体发来的JSON 报文的数据
 */
@Data
public class AiQueryRequestDTO {

    /** 账期：M:月，D：日 */
    private String cycleType;

    /** 账期类型：S:时点，D：时段 */
    private String acctType;

    /** 是否合计 */
    private Boolean sumFlag;

    /** 账期开始 */
    private String dateBegin;

    /** 账期结束 */
    private String dateEnd;

    /** 指标 (是一个一维数组 [[{...}]] ) */
    private List<IndDTO> ind;

    /** 行维度 */
    private List<RowDTO> row;

    /** 列维度 */
    private List<ColDTO> col;

    /** 条件维度 */
    private List<CondDTO> cond;

    @Data
    public static class IndDTO {
        /** 指标编号 */
        private String id;
        /** 指标条件 */
        private String cond;
    }

    @Data
    public static class RowDTO {
        /** 行维度编号 */
        private String id;
        /** 行-维度条件 */
        private String cond;
    }

    @Data
    public static class ColDTO {
        /** 维度编号 */
        private String id;

        private List<Map<String, String>> cond;
    }

    @Data
    public static class CondDTO {
        /** 维度编号 */
        private String id;
        /** 维度值 */
        private String cond;
    }
}