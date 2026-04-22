package com.pearadmin.modules.chat.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pearadmin.common.aop.annotation.Log;
import com.pearadmin.common.context.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import com.pearadmin.common.web.domain.response.Result;
import com.pearadmin.modules.chat.service.ITChatSessionService;
import com.pearadmin.modules.chat.service.ITChatMessageService;
import com.pearadmin.modules.chat.domain.TChatSession;
import com.pearadmin.modules.chat.domain.TChatMessage;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/service")
public class AiChatController {

    @Value("${ai.doris.znws-table:khjy_znws_zb_hb}")
    private String tableName;

    @Value("${ai-doris-znws-table-d}")
    private String tableNameD;


    @Autowired
    private ITChatSessionService chatSessionService;

    @Autowired
    private ITChatMessageService chatMessageService;

    @Autowired
    private ITChatSessionService tChatSessionService;

    @Autowired
    private com.pearadmin.modules.chat.service.DynamicSqlService dynamicSqlService;

    @GetMapping("/sessionList")
    public Result getSessionList(@RequestParam(value = "sessionType", required = false) Long sessionType) {
        QueryWrapper<TChatSession> queryWrapper = new QueryWrapper<>();
        if (sessionType != null) {
            queryWrapper.eq("session_type", sessionType);
        }
        queryWrapper.eq("user_id", UserContext.currentUser().getUserId());
        queryWrapper.orderByDesc("is_top").orderByDesc("top_time").orderByDesc("create_time");
        List<TChatSession> list = chatSessionService.list(queryWrapper);
        return Result.success(list);
    }

    @PostMapping("/changeTopStatus")
    @ResponseBody
    public Result changeTopStatus(String sessionId, Integer isTop) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return Result.failure("会话ID不能为空");
        }
        TChatSession session = tChatSessionService.getById(sessionId);
        if (session == null) {
            return Result.failure("该对话已不存在");
        }
        session.setIsTop(isTop);
        if (isTop != null && isTop == 1) {
            session.setTopTime(LocalDateTime.now());
        } else {
            session.setTopTime(null);
        }
        boolean updateResult = tChatSessionService.updateById(session);
        if (updateResult) {
            return Result.success(isTop == 1 ? "置顶成功" : "已取消置顶");
        }
        return Result.failure("状态更新失败");
    }

    @PostMapping("/remove")
    @ResponseBody
    public Result remove(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return Result.failure("会话ID不能为空");
        }
        boolean success = tChatSessionService.removeById(sessionId);
        if (success) {
            QueryWrapper<TChatMessage> msgWrapper = new QueryWrapper<>();
            msgWrapper.eq("session_id", sessionId);
            chatMessageService.remove(msgWrapper);
            return Result.success("删除成功");
        }
        return Result.failure("删除失败，该对话可能已不存在");
    }

    @GetMapping("/message/history")
    public Result getMessageHistory(@RequestParam("sessionId") String sessionId) {
        QueryWrapper<TChatMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("session_id", sessionId);
        queryWrapper.orderByAsc("create_time");
        List<TChatMessage> history = chatMessageService.list(queryWrapper);
        return Result.success(history);
    }


    @PostMapping("/send")
    @Log(module = "智能问数", action = "发起对话")
    @ResponseBody
    public Result sendMessage(@RequestBody String rawBody) {
        try {
            ChatRequestDTO requestDTO = com.alibaba.fastjson.JSON.parseObject(rawBody, ChatRequestDTO.class);
            String sessionId = requestDTO.getSessionId();
            String userQuestion = requestDTO.getQuestion();

            if (userQuestion == null || userQuestion.trim().isEmpty()) {
                return Result.failure("问题不能为空");
            }

            boolean isNewSession = false;
            if (com.pearadmin.common.tools.string.StringUtil.isEmpty(sessionId)) {
                sessionId = UUID.randomUUID().toString().replace("-", "");
                isNewSession = true;
            }
            String userId = UserContext.currentUser().getUserId();
            LocalDateTime now = LocalDateTime.now();

            System.out.println("\n\n######################################################");
            System.out.println("====== 【1. 收到前端提问，开始请求 Dify】 ======");
            System.out.println("用户提问内容: " + userQuestion);
            System.out.println("######################################################\n");

            org.springframework.http.client.SimpleClientHttpRequestFactory factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
            factory.setReadTimeout(120000);    // 强制 Java 耐心等待 120 秒
            factory.setConnectTimeout(10000);  // 连接超时 10 秒
            RestTemplate restTemplate = new RestTemplate(factory);

            String aiEndpoint = "http://135.224.23.40:8081/v1/chat-messages";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer app-2tvNyBKR7XZNW0gZdPufBLJd");

            com.alibaba.fastjson.JSONObject firstRequest = new com.alibaba.fastjson.JSONObject();
            firstRequest.put("inputs", new com.alibaba.fastjson.JSONObject());
            firstRequest.put("query", userQuestion);
            firstRequest.put("response_mode", "streaming");
            firstRequest.put("conversation_id", isNewSession ? "" : sessionId);
            firstRequest.put("user", userId);

            org.springframework.http.HttpEntity<String> firstEntity = new org.springframework.http.HttpEntity<>(firstRequest.toJSONString(), headers);
            org.springframework.http.ResponseEntity<String> firstResponse = restTemplate.postForEntity(aiEndpoint, firstEntity, String.class);

            String finalHumanReply = "";
            String difyOfficialId = "";
            String finalThinking = "";
            String firstResponseBody = firstResponse.getBody();

            System.out.println("====== 🚨🚨🚨 【Dify 原始返回的报文内容】 🚨🚨🚨 ======");
            System.out.println(firstResponseBody);
            System.out.println("=========================================================");

            if (firstResponseBody != null) {
                StringBuilder sb = new StringBuilder();
                for (String line : firstResponseBody.split("\n")) {
                    if (line.trim().startsWith("data:")) {
                        String jsonData = line.substring(5).trim();
                        if (jsonData.startsWith("{")) {
                            try {
                                com.alibaba.fastjson.JSONObject chunk = com.alibaba.fastjson.JSONObject.parseObject(jsonData);

                                if (chunk.containsKey("answer")) {
                                    sb.append(chunk.getString("answer"));
                                }

                                if (chunk.containsKey("conversation_id")) {
                                    difyOfficialId = chunk.getString("conversation_id");
                                }

                                if (chunk.containsKey("event")) {
                                    String event = chunk.getString("event");
                                    if ("node_finished".equals(event) || "workflow_node_finished".equals(event)) {
                                        com.alibaba.fastjson.JSONObject dataObj = chunk.getJSONObject("data");
                                        if (dataObj != null && dataObj.containsKey("outputs")) {
                                            com.alibaba.fastjson.JSONObject outputs = dataObj.getJSONObject("outputs");

                                            if (outputs != null && outputs.containsKey("reasoning_content")) {
                                                String aiReasoning = outputs.getString("reasoning_content");
                                                if (aiReasoning != null && !aiReasoning.trim().isEmpty()) {
                                                    finalThinking = aiReasoning;
                                                }
                                            }
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                log.error("💥 JSON 解析 Dify 数据块失败! 这一截数据是: " + jsonData, e);
                            }
                        }
                    }
                }
                finalHumanReply = sb.toString().trim();

                log.info("\n🚀🚀🚀 ====== 【即将解析的最终 JSON 文本 - 智能问数】 ======\n{}", finalHumanReply);



                if (finalHumanReply.isEmpty()) {
                    finalHumanReply = "抱歉，AI 开小差了，未能返回结果。";
                } else {
                    // ==========================================
                    // 解析并路由 JSON 输出
                    // ==========================================
                    try {
                        com.alibaba.fastjson.JSONObject aiJson = com.alibaba.fastjson.JSON.parseObject(finalHumanReply);
                        String type = aiJson.getString("type");

                        if ("help".equalsIgnoreCase(type)) {
                            String[] helpReplies = {
                                    "您好！我是客经智数助手，您可以问我类似“2026年3月全疆移动新增量是多少”这样的问题哦。",
                                    "需要帮忙吗？您可以告诉我月份、地域和想看的指标，我来为您查询数据。",
                                    "您好！如果您不知道怎么提问，可以试试点击右侧的“提问指南”或“指标目录”哦。",
                                    "我是您的智能问数助手！支持查询各维度的业务数据，直接向我提问即可。",
                                    "别担心，问数很简单。您可以按照“时间+地点+指标”的格式发给我，例如“2026年2月乌鲁木齐5G发展量”。"
                            };
                            int randomIndex = new java.util.Random().nextInt(helpReplies.length);
                            finalHumanReply = helpReplies[randomIndex];

                        } else if ("error".equalsIgnoreCase(type)) {
                            String[] errorReplies = {
                                    "抱歉，您的问题似乎与数据查询无关，我目前只能回答业务数据相关的问题哦。",
                                    "哎呀，我只懂看数据，这个问题超纲啦。您可以换个业务指标问问我。",
                                    "我是专注于客经智数平台的助手，暂时无法回答此类与工作无关的问题呢。",
                                    "很遗憾，这个问题我无法解答。建议您尝试查询“新增用户”、“流失率”等业务指标。",
                                    "抱歉，我没有听懂。请确认您的问题中包含正确的月份、地域和指标名称。"
                            };
                            int randomIndex = new java.util.Random().nextInt(errorReplies.length);
                            finalHumanReply = errorReplies[randomIndex];

                        } else if ("indquery".equalsIgnoreCase(type)) {
                            finalHumanReply = this.handleIndQueryToChart(aiJson);
                        }

                        else if ("definition".equalsIgnoreCase(type)) {
                            com.alibaba.fastjson.JSONArray inds = aiJson.getJSONArray("ind");
                            if (inds != null && !inds.isEmpty()) {
                                StringBuilder defText = new StringBuilder("为您查到以下指标说明：\n\n");
                                for (int i = 0; i < inds.size(); i++) {
                                    com.alibaba.fastjson.JSONObject indItem = inds.getJSONObject(i);

                                    String name = indItem.getString("ind_name");
                                    if (name == null || name.trim().isEmpty()) {
                                        name = indItem.getString("input_ind");
                                    }
                                    String desc = indItem.getString("ind_describe");

                                    defText.append("🔹 **").append(name).append("**：\n")
                                            .append(desc).append("\n\n");
                                }
                                finalHumanReply = defText.toString().trim();
                            } else {
                                finalHumanReply = "抱歉，没有为您查到相关指标的详细定义。";
                            }
                        }
                    } catch (Exception e) {
                    }
                }
            }

            System.out.println("\n######################################################");
            System.out.println("====== 【4. Dify 完整思考过程 (Thought)】 ======");
            System.out.println(finalThinking.isEmpty() ? "（无思考过程返回）" : finalThinking);
            System.out.println("------------------------------------------------------");
            System.out.println("====== 【5. Dify 最终输出结果 (Answer)】 ======");
            System.out.println(finalHumanReply);
            System.out.println("######################################################\n\n");

            if (isNewSession && !difyOfficialId.isEmpty()) {
                sessionId = difyOfficialId;
            } else if (isNewSession) {
                sessionId = UUID.randomUUID().toString().replace("-", "");
            }

            if (isNewSession) {
                TChatSession session = new TChatSession();
                session.setSessionId(sessionId);
                String title = userQuestion.length() > 15 ? userQuestion.substring(0, 15) + "..." : userQuestion;
                session.setSessionTitle(title);
                session.setCreateTime(now);
                session.setIsTop(0);
                session.setUserId(userId);
                session.setSessionType(1L);
                chatSessionService.save(session);
            }

            TChatMessage userMsg = new TChatMessage();
            userMsg.setMsgId(UUID.randomUUID().toString().replace("-", ""));
            userMsg.setSessionId(sessionId);
            userMsg.setRole("user");
            userMsg.setMsgType("text");
            userMsg.setContent(userQuestion);
            userMsg.setCreateTime(now);
            chatMessageService.save(userMsg);

            TChatMessage aiMsg = new TChatMessage();
            aiMsg.setMsgId(UUID.randomUUID().toString().replace("-", ""));
            aiMsg.setSessionId(sessionId);
            aiMsg.setRole("ai");
            aiMsg.setMsgType("text");
            String dbContent = finalHumanReply;
            if (finalThinking != null && !finalThinking.trim().isEmpty()) {
                dbContent = "<think>" + finalThinking + "</think>\n\n" + finalHumanReply;
            }
            aiMsg.setContent(dbContent);

            aiMsg.setCreateTime(now.plusSeconds(1));
            chatMessageService.save(aiMsg);

            ChatResponseDTO responseDTO = new ChatResponseDTO();
            responseDTO.setSessionId(sessionId);
            responseDTO.setAnswer(finalHumanReply);
            responseDTO.setThinking(finalThinking);

            return Result.success(responseDTO);

        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            System.err.println("======  Dify 接口报错！ ======");
            System.err.println("HTTP 状态码: " + e.getStatusCode());
            return Result.failure("Dify报错：" + e.getResponseBodyAsString());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.failure("系统接口调度异常：" + e.getMessage());
        }
    }

    @GetMapping("/recommendations")
    @ResponseBody
    public Result getRecommendations() {
        try {
            org.springframework.http.client.SimpleClientHttpRequestFactory requestFactory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
            requestFactory.setConnectTimeout(120000);
            requestFactory.setReadTimeout(10000);
            RestTemplate restTemplate = new RestTemplate(requestFactory);
            String aiEndpoint = "http://135.224.23.40:8081/v1/workflows/run";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer app-ZG6JQitT3uuq5JjslPuDLa2N");

            com.alibaba.fastjson.JSONObject request = new com.alibaba.fastjson.JSONObject();
            request.put("inputs", new com.alibaba.fastjson.JSONObject());
            request.put("response_mode", "streaming");

            String userId = "ych";
            try {
                if (UserContext.currentUser() != null) {
                    userId = UserContext.currentUser().getUserId();
                }
            } catch (Exception e) {
            }
            request.put("user", userId);

            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(request.toJSONString(), headers);
            org.springframework.http.ResponseEntity<String> response = restTemplate.postForEntity(aiEndpoint, entity, String.class);

            String responseBody = response.getBody();
            StringBuilder fullText = new StringBuilder();

            if (responseBody != null) {
                for (String line : responseBody.split("\n")) {
                    if (line.trim().startsWith("data:")) {
                        String jsonData = line.substring(5).trim();
                        if (jsonData.startsWith("{")) {
                            try {
                                com.alibaba.fastjson.JSONObject chunk = com.alibaba.fastjson.JSONObject.parseObject(jsonData);
                                String event = chunk.getString("event");
                                if ("text_chunk".equals(event)) {
                                    com.alibaba.fastjson.JSONObject dataObj = chunk.getJSONObject("data");
                                    if (dataObj != null && dataObj.containsKey("text")) {
                                        fullText.append(dataObj.getString("text"));
                                    }
                                }
                            } catch (Exception ignore) {
                            }
                        }
                    }
                }
            }

            String resultText = fullText.toString().trim();

            System.out.println("\n######################################################");
            System.out.println("====== 【6.推荐问题接口：Dify 原始推演内容】 ======");
            System.out.println(resultText.isEmpty() ? "（警告：Dify 接口返回为空，请检查 Token 或工作流配置）" : resultText);
            System.out.println("######################################################\n");

            java.util.List<String> questions = new java.util.ArrayList<>();
            if (!resultText.isEmpty()) {
                // Dify 返回的是标准的 JSON 数组 ["问题1", "问题2"]
                if (resultText.trim().startsWith("[") && resultText.trim().endsWith("]")) {
                    try {
                        com.alibaba.fastjson.JSONArray arr = com.alibaba.fastjson.JSON.parseArray(resultText);
                        for (int i = 0; i < arr.size(); i++) {
                            String q = arr.getString(i).trim();
                            if (!q.isEmpty()) questions.add(q);
                        }
                    } catch (Exception e) {
                    }
                } else {
                    // Dify 返回的是回车换行的文本
                    String[] lines = resultText.split("\n");
                    for (String line : lines) {
                        String q = line.replaceAll("^\\d+[\\.\\x{3001}]\\s*|^-?\\s*", "").replace("\"", "").trim();
                        if (!q.isEmpty()) {
                            questions.add(q);
                        }
                    }
                }
            }

            // 兜底
            if (questions.isEmpty()) {
                questions.add("2026年2月全疆移动出账到达用户数");
                questions.add("2026年2月乌鲁木齐宽带月新增用户数");
                questions.add("2026年2月乌什5G月发展量");
            }

            return Result.success(questions);
            // 终极兜底
        } catch (Exception e) {
            System.err.println("❌ 推荐接口发生异常: " + e.getMessage());
            return Result.success(java.util.Arrays.asList("2026年2月全疆移动出账到达用户数", "2026年2月乌鲁木齐宽带月新增用户数", "2026年2月乌什5G月发展量"));
        }
    }

    // ==========================================
    // 数据分析工作流接口
    // ==========================================
    @PostMapping("/analyzeDifyData")
    @ResponseBody
    public Result analyzeDifyData(@RequestBody java.util.Map<String, String> requestMap) {
        try {
            String sessionId = requestMap.get("sessionId");
            String query = requestMap.get("query");
            String data = requestMap.get("data");

            System.out.println("\n====== 【触发深度分析工作流】 ======");
            System.out.println("分析问题: " + query);
            System.out.println("中文数据: " + data);

            org.springframework.http.client.SimpleClientHttpRequestFactory factory2 = new org.springframework.http.client.SimpleClientHttpRequestFactory();
            factory2.setReadTimeout(120000);
            factory2.setConnectTimeout(10000);
            RestTemplate restTemplate = new RestTemplate(factory2);
            String aiEndpoint = "http://135.224.23.40:8081/v1/workflows/run";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer app-8v3Ba3ZTwT9TAC8UjzCw8BHM");

            com.alibaba.fastjson.JSONObject request = new com.alibaba.fastjson.JSONObject();
            com.alibaba.fastjson.JSONObject inputs = new com.alibaba.fastjson.JSONObject();
            inputs.put("query", query);
            inputs.put("data", data);

            request.put("inputs", inputs);
            request.put("response_mode", "streaming");

            // 获取当前登录用户，兜底用 abc-123
            String userId = "abc-123";
            try {
                if (com.pearadmin.common.context.UserContext.currentUser() != null) {
                    userId = com.pearadmin.common.context.UserContext.currentUser().getUserId();
                }
            } catch (Exception e) {}
            request.put("user", userId);

            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(request.toJSONString(), headers);
            org.springframework.http.ResponseEntity<String> response = restTemplate.postForEntity(aiEndpoint, entity, String.class);

            StringBuilder analysisText = new StringBuilder();
            String responseBody = response.getBody();
            if (responseBody != null) {
                // 解析 Dify 工作流的 SSE 流式响应
                for (String line : responseBody.split("\n")) {
                    if (line.trim().startsWith("data:")) {
                        String jsonData = line.substring(5).trim();
                        if (jsonData.startsWith("{")) {
                            try {
                                com.alibaba.fastjson.JSONObject chunk = com.alibaba.fastjson.JSONObject.parseObject(jsonData);
                                if ("text_chunk".equals(chunk.getString("event"))) {
                                    com.alibaba.fastjson.JSONObject dataObj = chunk.getJSONObject("data");
                                    if (dataObj != null && dataObj.containsKey("text")) {
                                        analysisText.append(dataObj.getString("text"));
                                    }
                                }
                            } catch (Exception ignore) {}
                        }
                    }
                }
            }

            String finalText = analysisText.toString().trim();
            if (finalText.isEmpty()) finalText = "抱歉，分析接口未能生成有效内容，请稍后再试。";


            if (sessionId != null && !sessionId.trim().isEmpty() && !"null".equalsIgnoreCase(sessionId)) {
                LocalDateTime now = LocalDateTime.now();
                TChatMessage aiMsg = new TChatMessage();
                aiMsg.setMsgId(UUID.randomUUID().toString().replace("-", ""));
                aiMsg.setSessionId(sessionId); // 必须赋值，否则数据库会报错
                aiMsg.setRole("ai");
                aiMsg.setMsgType("text");
                aiMsg.setContent(finalText);
                aiMsg.setCreateTime(now);

                // 执行保存。如果 sessionId 还是 null，这里会抛出异常，
                chatMessageService.save(aiMsg);
            } else {
                System.err.println("CRITICAL ERROR: 尝试保存分析结果时发现 sessionId 为空！");
            }

            return Result.success(finalText);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.failure("数据分析请求异常：" + e.getMessage());
        }
    }




    /**
     * 核心查库与画图处理方法 (稳定同步版)
     */
    private String handleIndQueryToChart(com.alibaba.fastjson.JSONObject aiJson) {
        System.out.println("\n\n====== 【大模型原始 indquery 报文】 ======");
        System.out.println(aiJson.toJSONString());
        System.out.println("============================================\n\n");

        try {
            com.alibaba.fastjson.JSONObject resultObj = aiJson.getJSONObject("result");
            com.alibaba.fastjson.JSONObject questionTypeObj = aiJson.getJSONObject("question_type");

            String chartType = questionTypeObj != null ? questionTypeObj.getString("type") : "single";
            Integer topK = questionTypeObj != null ? questionTypeObj.getInteger("top") : null;
            Integer bottomK = questionTypeObj != null ? questionTypeObj.getInteger("bottom") : null;
            String specificChart = questionTypeObj != null ? questionTypeObj.getString("chart") : "";

            String dateBegin = resultObj.getString("dateBegin");
            String dateEnd = resultObj.getString("dateEnd");
            com.alibaba.fastjson.JSONArray inds = resultObj.getJSONArray("ind");
            com.alibaba.fastjson.JSONArray rows = resultObj.getJSONArray("row");

            if (inds == null || inds.isEmpty()) {
                return "{\"type\":\"normal\",\"text\":\"抱歉，未能从您的问题中识别到具体的业务指标。请包含像‘移动新增量’等具体指标名称后再试。\"}";
            }

            String metricField = inds.getJSONObject(0).getString("id");
            String metricName = metricField;

            // 获取指标中文名
            if (inds.getJSONObject(0).containsKey("name")) {
                String tempName = inds.getJSONObject(0).getString("name");
                if (tempName != null && !tempName.trim().isEmpty()) {
                    metricName = tempName;
                }
            }
            if (metricName.equals(metricField) && aiJson.containsKey("metrics")) {
                com.alibaba.fastjson.JSONArray metricsArr = aiJson.getJSONArray("metrics");
                if (metricsArr != null && !metricsArr.isEmpty()) {
                    String tempName = metricsArr.getJSONObject(0).getString("ind_name");
                    if (tempName != null && !tempName.trim().isEmpty()) {
                        metricName = tempName;
                    }
                }
            }

            java.util.List<String> selectFields = new java.util.ArrayList<>();
            java.util.List<String> groupByFields = new java.util.ArrayList<>();
            java.util.List<String> whereClauses = new java.util.ArrayList<>();

            selectFields.add("SUM(" + metricField + ") AS " + metricField);

            if (dateBegin.equals(dateEnd)) {
                whereClauses.add("op_date = '" + dateBegin + "'");
            } else {
                whereClauses.add("op_date >= '" + dateBegin + "' AND op_date <= '" + dateEnd + "'");
            }

            boolean hasWangGe = false, hasXianFen = false, hasFenGongSi = false;
            String lowestDimName = "";

            if (rows != null && !rows.isEmpty()) {
                for (int i = 0; i < rows.size(); i++) {
                    com.alibaba.fastjson.JSONObject rowItem = rows.getJSONObject(i);
                    String rawId = rowItem.getString("id");
                    String cond = rowItem.getString("cond");

                    String colName = rawId.contains("|") ? rawId.split("\\|")[1] : rawId;

                    if ("x_hx5_bp_name".equals(colName)) hasWangGe = true;
                    if ("hx_area_name".equals(colName)) hasXianFen = true;
                    if ("hx_latn_name".equals(colName)) hasFenGongSi = true;

                    if (cond != null && !cond.trim().isEmpty()) {
                        String idColName = colName.replace("_name", "_id");
                        if (cond.contains(",")) {
                            whereClauses.add(idColName + " IN (" + cond + ")");
                        } else {
                            whereClauses.add(idColName + " = " + cond);
                        }
                    }
                }
            }

            String hxType = "";
            if (hasWangGe) {
                hxType = "网格";
                lowestDimName = "x_hx5_bp_name";
            } else if (hasXianFen) {
                hxType = "县分";
                lowestDimName = "hx_area_name";
            } else if (hasFenGongSi) {
                hxType = "分公司";
                lowestDimName = "hx_latn_name";
            }

            if (!hxType.isEmpty()) {
                whereClauses.add("hx_type = '" + hxType + "'");
            }


            com.alibaba.fastjson.JSONObject kpiObj = null;

            try {
                java.util.List<String> kpiConds = new java.util.ArrayList<>();
                for (String cond : whereClauses) {
                    if (!cond.startsWith("op_date")) {
                        kpiConds.add(cond);
                    }
                }
                String baseWhere = String.join(" AND ", kpiConds);

                String targetMonth = dateEnd;
                if (targetMonth != null && targetMonth.length() == 6 && "single".equalsIgnoreCase(chartType)) {
                    int year = Integer.parseInt(targetMonth.substring(0, 4));
                    int month = Integer.parseInt(targetMonth.substring(4, 6));

                    String lastMonth = month == 1 ? (year - 1) + "12" : year + String.format("%02d", month - 1);
                    String lastYearMonth = (year - 1) + String.format("%02d", month);
                    String ytdStart = year + "01";
                    String lastYearYtdStart = (year - 1) + "01";
                    String lastMonthYtdEnd = lastMonth;

                    double currentVal = queryKpiValue(metricField, "op_date = '" + targetMonth + "'", baseWhere);
                    double lastMonthVal = queryKpiValue(metricField, "op_date = '" + lastMonth + "'", baseWhere);
                    double lastYearVal = queryKpiValue(metricField, "op_date = '" + lastYearMonth + "'", baseWhere);
                    double ytdVal = queryKpiValue(metricField, "op_date >= '" + ytdStart + "' AND op_date <= '" + targetMonth + "'", baseWhere);
                    double lastYearYtdVal = queryKpiValue(metricField, "op_date >= '" + lastYearYtdStart + "' AND op_date <= '" + lastYearMonth + "'", baseWhere);

                    // 查询“上月累计值”（1月则为0）
                    double lastMonthYtdVal = 0.0;
                    if (month > 1) {
                        lastMonthYtdVal = queryKpiValue(metricField, "op_date >= '" + ytdStart + "' AND op_date <= '" + lastMonthYtdEnd + "'", baseWhere);
                    }

                    String huanbi = lastMonthVal == 0 ? "-" : String.format("%.2f%%", (currentVal - lastMonthVal) / lastMonthVal * 100);
                    String tongbi = lastYearVal == 0 ? "-" : String.format("%.2f%%", (currentVal - lastYearVal) / lastYearVal * 100);
                    String ytdTongbi = lastYearYtdVal == 0 ? "-" : String.format("%.2f%%", (ytdVal - lastYearYtdVal) / lastYearYtdVal * 100);
                    String ytdHuanbi = lastMonthYtdVal == 0 ? "-" : String.format("%.2f%%", (ytdVal - lastMonthYtdVal) / lastMonthYtdVal * 100);

                    java.text.DecimalFormat df = new java.text.DecimalFormat("#,##0.##");

                    kpiObj = new com.alibaba.fastjson.JSONObject();
                    kpiObj.put("metricName", metricName);
                    String displayRange = dateBegin.equals(dateEnd) ? dateEnd : dateBegin + " ~ " + dateEnd;
                    kpiObj.put("dateRange", displayRange);
                    kpiObj.put("currentVal", df.format(currentVal));
                    kpiObj.put("huanbi", huanbi);
                    kpiObj.put("tongbi", tongbi);
                    kpiObj.put("lastMonthVal", df.format(lastMonthVal));
                    kpiObj.put("lastYearVal", df.format(lastYearVal));
                    kpiObj.put("ytdVal", df.format(ytdVal));
                    kpiObj.put("ytdTongbi", ytdTongbi);
                    kpiObj.put("ytdHuanbi", ytdHuanbi); // 新增累计环比
                    kpiObj.put("lastMonthYtdVal", df.format(lastMonthYtdVal)); // 新增上期累计值
                    kpiObj.put("lastYearYtdVal", df.format(lastYearYtdVal));
                }
            } catch (Exception e) {
                System.err.println("KPI 数据推算异常，已跳过: " + e.getMessage());
            }

            // ==========================================
            // 构建 SQL 获取图表趋势数据
            // ==========================================
            String xAxisField = "";
            if ("trend".equalsIgnoreCase(chartType)) {
                xAxisField = "op_date";
            } else {
                xAxisField = !lowestDimName.isEmpty() ? lowestDimName : "op_date";
            }
            selectFields.add(xAxisField);
            groupByFields.add(xAxisField);

            String sql = "SELECT " + String.join(", ", selectFields) +
                    " FROM " + tableName +
                    " WHERE " + String.join(" AND ", whereClauses) +
                    " GROUP BY " + String.join(", ", groupByFields);

            if ("rank".equalsIgnoreCase(chartType)) {
                if (topK != null) {
                    sql += " ORDER BY " + metricField + " DESC LIMIT " + topK;
                } else if (bottomK != null) {
                    sql += " ORDER BY " + metricField + " ASC LIMIT " + bottomK;
                } else {
                    sql += " ORDER BY " + metricField + " DESC LIMIT 1000";
                }
            } else if ("trend".equalsIgnoreCase(chartType) || "op_date".equals(xAxisField)) {
                sql += " ORDER BY " + xAxisField + " ASC LIMIT 1000";
            } else {
                sql += " ORDER BY " + metricField + " DESC LIMIT 1000";
            }

            System.out.println("\n====== 【智能问数 - 主图表最终执行的 SQL】 ======\n" + sql + "\n========================================================\n");

            java.util.List<java.util.Map<String, Object>> dataList = dynamicSqlService.executeDorisSql(sql);

            if (dataList == null || dataList.isEmpty()) {
                String emptyMsg = "为您查询完成。但在当前选择的时间和地域维度下，底层数据库中暂无相关业务数据记录。\\n\\n" +
                        "💡 建议您可以试试：\\n" +
                        "- 更换其他月份重试\\n" +
                        "- 查询更高层级的地域\\n" +
                        "- 检查指标是否在该维度下已上线";
                return "{\"type\":\"normal\",\"text\":\"" + emptyMsg + "\"}";
            }

            java.util.List<String> xAxisData = new java.util.ArrayList<>();
            java.util.List<Object> seriesData = new java.util.ArrayList<>();
            java.util.List<String> pieDataList = new java.util.ArrayList<>();
            StringBuilder rawDataStr = new StringBuilder("查询结果如下：");

            StringBuilder mdTable = new StringBuilder();
            mdTable.append("<details>\n");
            mdTable.append("<summary><i class=\"layui-icon layui-icon-table\"></i> 查看明细数据</summary>\n\n");

            String dimHeader = "数据维度";
            if ("hx_latn_name".equals(xAxisField)) dimHeader = "分公司";
            else if ("hx_area_name".equals(xAxisField)) dimHeader = "县分";
            else if ("hx_region_name".equals(xAxisField)) dimHeader = "支局";
            else if ("x_hx5_bp_name".equals(xAxisField)) dimHeader = "网格";
            else if ("op_date".equals(xAxisField)) dimHeader = "日期";

            mdTable.append("| ").append(dimHeader).append(" | ").append(metricName).append(" |\n");
            mdTable.append("| :--- | :--- |\n");

            for (java.util.Map<String, Object> rowMap : dataList) {
                Object xObj = rowMap.get(xAxisField) != null ? rowMap.get(xAxisField) : rowMap.get(xAxisField.toUpperCase());
                String xVal = xObj != null ? xObj.toString() : "未知";
                Object yObj = rowMap.get(metricField) != null ? rowMap.get(metricField) : rowMap.get(metricField.toUpperCase());
                Object yVal = yObj != null ? yObj : 0;

                xAxisData.add("\"" + xVal + "\"");
                seriesData.add(yVal);
                pieDataList.add("{\"name\":\"" + xVal + "\",\"value\":" + yVal + "}");
                rawDataStr.append(xVal).append(" 值为: ").append(yVal).append("； ");
                mdTable.append("| ").append(xVal).append(" | ").append(yVal).append(" |\n");
            }
            mdTable.append("\n</details>");

            String xAxisStr = "[" + String.join(", ", xAxisData) + "]";
            String seriesStr = "[" + seriesData.stream().map(String::valueOf).collect(java.util.stream.Collectors.joining(", ")) + "]";
            String pieDataStr = "[" + String.join(", ", pieDataList) + "]";

            // 新增：对比分析 (comparison) 专属高级数据构建
            com.alibaba.fastjson.JSONObject compareObj = null;
            if ("comparison".equalsIgnoreCase(chartType) && dataList.size() == 2 && dateEnd != null && dateEnd.length() == 6) {
                try {
                    // 重新组装基础的 Where 条件
                    java.util.List<String> compConds = new java.util.ArrayList<>();
                    for (String cond : whereClauses) {
                        if (!cond.startsWith("op_date") && !cond.startsWith(xAxisField.replace("_name", "_id"))) {
                            compConds.add(cond);
                        }
                    }
                    String compBaseWhere = String.join(" AND ", compConds);

                    int year = Integer.parseInt(dateEnd.substring(0, 4));
                    int month = Integer.parseInt(dateEnd.substring(4, 6));
                    String lastYearMonth = (year - 1) + String.format("%02d", month);
                    String ytdStart = year + "01";

                    java.util.List<com.alibaba.fastjson.JSONObject> compList = new java.util.ArrayList<>();
                    for (int i = 0; i < 2; i++) {
                        java.util.Map<String, Object> rowMap = dataList.get(i);
                        Object xObj = rowMap.get(xAxisField) != null ? rowMap.get(xAxisField) : rowMap.get(xAxisField.toUpperCase());
                        String xVal = xObj != null ? xObj.toString() : "";
                        Object yObj = rowMap.get(metricField) != null ? rowMap.get(metricField) : rowMap.get(metricField.toUpperCase());
                        double currentVal = yObj != null ? Double.parseDouble(yObj.toString()) : 0.0;

                        String specificWhere = compBaseWhere;
                        if (!specificWhere.isEmpty()) specificWhere += " AND ";
                        specificWhere += xAxisField + " = '" + xVal + "'";

                        double ytdVal = queryKpiValue(metricField, "op_date >= '" + ytdStart + "' AND op_date <= '" + dateEnd + "'", specificWhere);
                        double yoyVal = queryKpiValue(metricField, "op_date = '" + lastYearMonth + "'", specificWhere);

                        com.alibaba.fastjson.JSONObject item = new com.alibaba.fastjson.JSONObject();
                        item.put("name", xVal);
                        item.put("current", currentVal);
                        item.put("ytd", ytdVal);
                        item.put("yoy", yoyVal);
                        compList.add(item);
                    }

                    compareObj = new com.alibaba.fastjson.JSONObject();
                    compareObj.put("list", compList);
                    double diff = compList.get(0).getDouble("current") - compList.get(1).getDouble("current");
                    compareObj.put("diff", diff);
                    compareObj.put("metricName", metricName);
                    compareObj.put("date", dateEnd);
                } catch (Exception e) {
                    log.error("对比分析扩展指标查询异常", e);
                }
            }

            // ==========================================
            // 2：统一在这里构建最终图表配置
            // ==========================================
            String echartsOption = "";

            if (compareObj != null) {
                // 🌟 为对比界面生成包含【当期、累计、同期】的复合柱状图
                com.alibaba.fastjson.JSONArray clist = compareObj.getJSONArray("list");
                String name1 = clist.getJSONObject(0).getString("name");
                String name2 = clist.getJSONObject(1).getString("name");
                double c1 = clist.getJSONObject(0).getDouble("current");
                double c2 = clist.getJSONObject(1).getDouble("current");
                double yt1 = clist.getJSONObject(0).getDouble("ytd");
                double yt2 = clist.getJSONObject(1).getDouble("ytd");
                double yo1 = clist.getJSONObject(0).getDouble("yoy");
                double yo2 = clist.getJSONObject(1).getDouble("yoy");

                echartsOption = "{\n" +
                        "  \"tooltip\": {\"trigger\": \"axis\", \"axisPointer\": {\"type\": \"shadow\"}},\n" +
                        "  \"legend\": {\"data\": [\"当期\", \"累计\", \"同期\"]},\n" +
                        "  \"grid\": {\"left\": \"3%\", \"right\": \"4%\", \"bottom\": \"3%\", \"containLabel\": true},\n" +
                        "  \"xAxis\": {\"type\": \"category\", \"data\": [\"" + name1 + "\", \"" + name2 + "\"]},\n" +
                        "  \"yAxis\": {\"type\": \"value\"},\n" +
                        "  \"series\": [\n" +
                        "    {\"name\": \"当期\", \"type\": \"bar\", \"itemStyle\": {\"color\": \"#4388fa\"}, \"data\": [" + c1 + ", " + c2 + "]},\n" +
                        "    {\"name\": \"累计\", \"type\": \"bar\", \"itemStyle\": {\"color\": \"#6bca50\"}, \"data\": [" + yt1 + ", " + yt2 + "]},\n" +
                        "    {\"name\": \"同期\", \"type\": \"bar\", \"itemStyle\": {\"color\": \"#a0cfff\"}, \"data\": [" + yo1 + ", " + yo2 + "]}\n" +
                        "  ]\n" +
                        "}";


            }else if ("percent".equalsIgnoreCase(chartType)) {
                echartsOption = "{\n" +
                        "  \"tooltip\": {\"trigger\": \"item\"},\n" +
                        "  \"series\": [{\"name\":\"" + metricName + "\", \"type\": \"pie\", \"radius\": \"50%\", \"data\": " + pieDataStr + "}]\n" +
                        "}";
            } else if ("trend".equalsIgnoreCase(chartType) || "op_date".equals(xAxisField)) {
                if (kpiObj != null) {
                    // 高级版卡片：带渐变阴影和浅色网格的折线图
                    echartsOption = "{\n" +
                            "  \"grid\": {\"left\": \"2%\", \"right\": \"4%\", \"bottom\": \"3%\", \"containLabel\": true},\n" +
                            "  \"tooltip\": {\"trigger\": \"axis\"},\n" +
                            "  \"xAxis\": {\"type\": \"category\", \"boundaryGap\": false, \"data\": " + xAxisStr + ", \"axisLine\": {\"lineStyle\": {\"color\": \"#e2e8f0\"}}, \"axisLabel\": {\"color\": \"#64748b\"}},\n" +
                            "  \"yAxis\": {\"type\": \"value\", \"splitLine\": {\"lineStyle\": {\"color\": \"#f1f5f9\"}}, \"axisLabel\": {\"color\": \"#64748b\"}},\n" +
                            "  \"series\": [{\"name\":\"" + metricName + "\", \"data\": " + seriesStr + ", \"type\": \"line\", \"smooth\": true, \"symbolSize\": 6, \"itemStyle\": {\"color\": \"#3b82f6\"}, \"areaStyle\": {\"color\": \"rgba(59, 130, 246, 0.15)\"}}]\n" +
                            "}";
                } else {
                    // 普通折线图
                    echartsOption = "{\n" +
                            "  \"tooltip\": {\"trigger\": \"axis\"},\n" +
                            "  \"xAxis\": {\"type\": \"category\", \"data\": " + xAxisStr + "},\n" +
                            "  \"yAxis\": {\"type\": \"value\"},\n" +
                            "  \"series\": [{\"name\":\"" + metricName + "\", \"data\": " + seriesStr + ", \"type\": \"line\", \"smooth\": true}]\n" +
                            "}";
                }
            } else {
                echartsOption = "{\n" +
                        "  \"tooltip\": {\"trigger\": \"axis\"},\n" +
                        "  \"xAxis\": {\"type\": \"category\", \"data\": " + xAxisStr + ",\n" +
                        "      \"axisLabel\": {\"interval\": 0, \"rotate\": 30} \n" +
                        "  },\n" +
                        "  \"yAxis\": {\"type\": \"value\"},\n" +
                        "  \"series\": [{\"name\":\"" + metricName + "\", \"data\": " + seriesStr + ", \"type\": \"bar\", \"barWidth\": \"40%\"}]\n" +
                        "}";
            }

            // ==========================================
            //  3：统一组装返回给前端的 JSON
            // ==========================================
            com.alibaba.fastjson.JSONObject finalAnalysisJson = new com.alibaba.fastjson.JSONObject();

            if (kpiObj != null) {
                finalAnalysisJson.put("type", "dashboard");
                finalAnalysisJson.put("kpi", kpiObj);
            } else if (compareObj != null) {
                // 新做的 comparison_dashboard 类型
                finalAnalysisJson.put("type", "comparison_dashboard");
                finalAnalysisJson.put("compare", compareObj);
            } else {
                // 用普通的 "analysis" 图表兜底
                finalAnalysisJson.put("type", "analysis");
                String textAndButton = "为您查询到以下业务数据：\n\n" +
                        "<button class=\"layui-btn layui-btn-sm layui-btn-normal\" style=\"border-radius: 4px;\" onclick=\"window.triggerDataAnalysis('" + rawDataStr.toString() + "')\">" +
                        "<i class=\"layui-icon layui-icon-chart\"></i> 问数分析</button>";
                finalAnalysisJson.put("text", textAndButton);
            }
            finalAnalysisJson.put("query", aiJson.getString("query"));
            finalAnalysisJson.put("table", mdTable.toString());
            finalAnalysisJson.put("chart", echartsOption);

            // ==========================================
            // 为数据分析按钮生成“中文版数据”，并放进最终报文里
            // ==========================================
            java.util.Map<String, String> dict = new java.util.HashMap<>();
            dict.put("hx_latn_name", "分公司");
            dict.put("hx_area_name", "县分");
            dict.put("hx_region_name", "支局");
            dict.put("x_hx5_bp_name", "网格");
            dict.put("op_date", "日期");
            dict.put(metricField.toLowerCase(), metricName);

            java.util.List<java.util.Map<String, Object>> difyDataList = new java.util.ArrayList<>();
            for (java.util.Map<String, Object> rowMap : dataList) {
                java.util.Map<String, Object> newRow = new java.util.LinkedHashMap<>();
                for (java.util.Map.Entry<String, Object> entry : rowMap.entrySet()) {
                    String engKey = entry.getKey().toLowerCase();
                    String chnKey = dict.getOrDefault(engKey, engKey);
                    newRow.put(chnKey, entry.getValue());
                }
                difyDataList.add(newRow);
            }

            // 把给大模型看的纯净中文数据塞进去
            finalAnalysisJson.put("difyData", com.alibaba.fastjson.JSON.toJSONString(difyDataList));

            return finalAnalysisJson.toJSONString();

        } catch (Exception e) {
            System.err.println("===== Doris 数据库查询异常 =====");
            e.printStackTrace();
            System.err.println("================================");

            String errorStack = (e.toString() + (e.getCause() != null ? e.getCause().toString() : "")).toLowerCase();
            if (errorStack.contains("access denied") || errorStack.contains("sqlexception") ||
                    errorStack.contains("connection") || errorStack.contains("druid")) {
                throw new RuntimeException("数据库底层连接/认证异常请联系管理员详情：" + e.getMessage());
            }

            String errorMsg = "抱歉，由于您提问中的部分查询条件未能与底层数据库成功匹配，导致数据提取失败。\n\n" +
                    "您可以尝试使用标准的【月份】+【地市/区县】名称重新向我提问哦！";
            errorMsg = errorMsg.replace("\n", "\\n");

            return "{\"type\":\"normal\",\"text\":\"" + errorMsg + "\"}";
        }
    }
    /**
     * 查询单点 KPI 的 SUM 聚合值
     */
    private double queryKpiValue(String metricField, String dateCondition, String baseWhere) {
        String sql = "SELECT SUM(" + metricField + ") AS val FROM " + tableName +
                " WHERE " + dateCondition +
                (baseWhere == null || baseWhere.isEmpty() ? "" : " AND " + baseWhere);
        try {
            java.util.List<java.util.Map<String, Object>> res = dynamicSqlService.executeDorisSql(sql);
            if (res != null && !res.isEmpty()) {
                // 兼容 Doris 返回字段大小写的问题
                Object valObj = res.get(0).get("val") != null ? res.get(0).get("val") : res.get(0).get("VAL");
                if (valObj != null) {
                    return Double.parseDouble(valObj.toString());
                }
            }
        } catch (Exception e) {
            System.err.println("KPI 辅助查询失败: " + sql);
        }
        return 0.0;
    }
    public static class ChatRequestDTO {
        private String sessionId;
        private String question;

        public String getSessionId() {
            return sessionId;
        }

        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }

        public String getQuestion() {
            return question;
        }

        public void setQuestion(String question) {
            this.question = question;
        }
    }

    public static class ChatResponseDTO {
        private String sessionId;
        private String answer;
        private String thinking;

        public String getSessionId() {
            return sessionId;
        }

        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }

        public String getAnswer() {
            return answer;
        }

        public void setAnswer(String answer) {
            this.answer = answer;
        }

        public String getThinking() {
            return thinking;
        } // Getter

        public void setThinking(String thinking) {
            this.thinking = thinking;
        } // Setter
    }
}