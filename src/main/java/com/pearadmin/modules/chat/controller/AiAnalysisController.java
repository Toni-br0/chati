package com.pearadmin.modules.chat.controller;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.stream.Collectors;
import com.pearadmin.modules.sys.domain.SysUser;
import com.pearadmin.modules.sys.domain.SysDept;
import com.pearadmin.modules.sys.service.SysDeptService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pearadmin.common.web.domain.response.Result;
import com.pearadmin.modules.chat.domain.TChatMessage;
import com.pearadmin.modules.ppt.domain.PptDownloadData;
import com.pearadmin.modules.ppt.mapper.PptDownloadDataMapper;
import com.pearadmin.modules.sys.domain.SysDictData;
import com.pearadmin.modules.sys.service.SysDictDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import java.util.stream.Collectors;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.nio.file.Files;
import java.util.List;

@RestController
@RequestMapping("/znfx")
public class AiAnalysisController {

    private final RestTemplate restTemplate = new RestTemplate();


    /*@org.springframework.beans.factory.annotation.Value("${ai-analysis-base-url}")
    private String AI_BASE_URL;*/


    @Value("${ai-analysis-base-url}")
    private String AI_BASE_URL;


    @Autowired
    private com.pearadmin.modules.ppt.service.impl.DownLoadServiceImpl downLoadService;

    @Autowired
    private SysDictDataService sysDictDataService;

    @Autowired
    private PptDownloadDataMapper pptDownloadDataMapper;

    @Autowired
    private com.pearadmin.modules.chat.service.ITChatSessionService tChatSessionService;

    @Autowired
    private com.pearadmin.modules.chat.service.ITChatMessageService tChatMessageService;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Autowired
    private SysDeptService sysDeptService;

    @Autowired
    private com.pearadmin.modules.xfppt.service.impl.XfPptDownLoadServiceImpl xfPptDownLoadService;

    // === 统一的大模型思考过程硬编码文本 ===
    private static final String AI_THINKING_CONTENT = "收到这份经营分析后，我的首要任务是将分散在多页、多维度的图表信息，整合为一份逻辑清晰、结论明确的智能分析报告。整个分析过程遵循“从整体到局部，从结果到过程”的框架，具体分为以下三个步骤：\n\n" +
            "第一步：全局定位，确立基调。\n我会首先审视最核心的“总体概况”指标，如总收入、总客户数以及它们的环比、同比变化。这一步的目的是快速为整体经营情况定下基调——是“平稳”、“向好”还是“承压”。如果发现核心指标出现显著波动，我会将其标记为本次报告需要重点关注的核心观点。\n\n" +
            "第二步：结构拆解，定位病灶。\n在了解整体情况后，我会对收入和客户结构进行拆解。收入方面，我会将其拆分关键板块，通过对比各板块的整体的作用，来判断增长的主力和风险的来源。客户方面，我会区分客群，并特别关注核心业务的变动。\n\n" +
            "第三步：归纳提炼，形成报告。\n最后，我会将以上所有分析结果进行归纳和提炼。在报告中，我会先用“核心观点”部分简明扼要地概括整体情况与最突出的问题，再用分章节的详细数据，来提供支撑依据，确保最终的报告既有结论性的判断，又有详实的数据佐证，快速把握经营全貌并做出决策。";

    /**
     * 新增：获取报告文件类型字典（按 sort 降序）
     */
    @GetMapping("/reportFileTypes")
    public Result getReportFileTypes() {
        // 按 sort 降序查询字典项
        String sql = "SELECT data_label as dataLabel, data_value as dataValue " +
                "FROM sys_dict_data " +
                "WHERE type_code = 'report_file_type' AND enable = '0' " +
                "ORDER BY sort ASC";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);
        return Result.success(list);
    }


    /**
     * 修改：获取系统文件列表，支持多表路由和权限过滤
     */
    @GetMapping("/systemFiles")
    public Result getSystemFiles(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "fileType", required = false) String fileType) {

        SysUser currentUser = com.pearadmin.common.context.UserContext.currentUser();

        String dictLabel = "";
        if (com.pearadmin.common.tools.string.StringUtil.isNotEmpty(fileType)) {
            String labelSql = "SELECT data_label FROM sys_dict_data WHERE type_code = 'report_file_type' AND data_value = ? LIMIT 1";
            try {
                dictLabel = jdbcTemplate.queryForObject(labelSql, String.class, fileType);
            } catch (Exception e) { }
        }
        if (dictLabel != null && dictLabel.contains("县分公司")) {
            com.pearadmin.modules.xfppt.domain.XfpptFilePushManage queryParam = new com.pearadmin.modules.xfppt.domain.XfpptFilePushManage();
            if (com.pearadmin.common.tools.string.StringUtil.isNotEmpty(keyword)) {
                queryParam.setFileName(keyword);
            }

            // 调用Service 方法
            List<com.pearadmin.modules.xfppt.domain.XfpptFilePushManage> list = xfPptDownLoadService.getXfPptDownLoadDataList(queryParam);

            List<PptDownloadData> resultList = new ArrayList<>();
            for (com.pearadmin.modules.xfppt.domain.XfpptFilePushManage item : list) {
                PptDownloadData d = new PptDownloadData();

                if (item.getManageId() != null) {
                    try {
                        d.setId(Integer.valueOf(item.getManageId()));
                    } catch (NumberFormatException e) {
                        System.err.println("县分文件ID转换失败: " + item.getManageId());
                    }
                }

                d.setModelName(item.getFileName());
                resultList.add(d);
            }
            return Result.success(resultList);

        } else if (dictLabel != null && (dictLabel.contains("区公司") || dictLabel.contains("分公司"))) {
            PptDownloadData queryParam = new PptDownloadData();
            if (com.pearadmin.common.tools.string.StringUtil.isNotEmpty(keyword)) {
                queryParam.setModelName(keyword);
            }
            List<PptDownloadData> list = downLoadService.getPptDownLoadDataList(queryParam);

            if (dictLabel.contains("区公司")) {
                return Result.success(list.stream().filter(i -> "全疆".equals(i.getModelLevel())).collect(Collectors.toList()));
            } else {
                return Result.success(list.stream().filter(i -> !"全疆".equals(i.getModelLevel())).collect(Collectors.toList()));
            }
        }

        return Result.success(new ArrayList<PptDownloadData>());
    }


    /**
     * 接口1：发起分析请求
     */

    @PostMapping("/analyzeSysFile")
    public Result analyzeSysFile(
            @RequestParam("fileId") String fileId,
            @RequestParam("fileType") String fileType,
            @RequestParam(value = "analysisType", defaultValue = "1") Integer analysisType,
            @RequestParam(value = "question", required = false) String question,
            @RequestParam(value = "sessionId", required = false) String sessionId,
            @RequestParam(value = "promptsContent", required = false) String promptsContent){

        System.out.println("====== 接收到前端传来的提示词 ======");
        System.out.println(promptsContent);
        System.out.println("========================================");
        try {
            String targetPath = null;
            String modelName = null;

            // 获取当前选中的字典标签，判断是哪种类型的材料
            String labelSql = "SELECT dict_label FROM sys_dict_data WHERE type_code = 'report_file_type' AND dict_value = ? LIMIT 1";
            String currentTypeLabel = "";
            try {
                currentTypeLabel = jdbcTemplate.queryForObject(labelSql, String.class, fileType);
            } catch(Exception e) {
                currentTypeLabel = ""; // 兜底处理
            }

            if (currentTypeLabel != null && currentTypeLabel.contains("县分公司")) {
                String sql = "SELECT file_name, file_path FROM xfppt_file_push_manage WHERE manage_id = ?";
                try {
                    Map<String, Object> xfData = jdbcTemplate.queryForMap(sql, fileId);
                    targetPath = String.valueOf(xfData.get("file_path"));
                    modelName = String.valueOf(xfData.get("file_name"));
                } catch (org.springframework.dao.EmptyResultDataAccessException e) {
                    return Result.failure("在县分系统中未找到该报告记录！");
                }
            } else {
                // B 路由：区/分公司材料，查 ppt_download_data 表
                PptDownloadData pptData = pptDownloadDataMapper.selectById(fileId);
                if (pptData == null || !StringUtils.hasText(pptData.getModelTargetPath())) {
                    return Result.failure("在系统中未找到该报告记录或路径为空！");
                }
                targetPath = pptData.getModelTargetPath();
                modelName = pptData.getModelName();
            }


            File file = new File(targetPath);
            if (!file.exists()) {
                return Result.failure("服务器文件已丢失：" + targetPath);
            }

            byte[] fileBytes = Files.readAllBytes(file.toPath());
            final String finalModelName = modelName; // 内部类调用需要 final
            ByteArrayResource fileAsResource = new ByteArrayResource(fileBytes) {
                @Override
                public String getFilename() {
                    // 如果文件名不带后缀，手动补上，方便 AI 识别格式
                    String name = StringUtils.hasText(finalModelName) ? finalModelName : file.getName();
                    return name.endsWith(".pptx") ? name : name + ".pptx";
                }
            };


            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", fileAsResource);

            // 判断分析模式 (1=基础单次, 2=深度多次)
            String mode = (analysisType == 2) ? "multi" : "single";
            body.add("analysis_mode", mode);

            if (StringUtils.hasText(sessionId)) {
                body.add("conversation_id", sessionId);
            }
            // ==========================================
            // 提示词表持久化方案
            // ==========================================
            String finalPrompt = "";
            String sceneCode = "PPT_ANALYSIS_BASIC";

            if (StringUtils.hasText(promptsContent)) {
                finalPrompt = promptsContent;

                String updateSql = "UPDATE sys_ai_prompt SET prompt_content = ?, update_time = NOW() WHERE scene_code = ?";
                int rows = jdbcTemplate.update(updateSql, finalPrompt, sceneCode);

                if (rows == 0) {
                    String insertSql = "INSERT INTO sys_ai_prompt (scene_code, prompt_content, update_time) VALUES (?, ?, NOW())";
                    jdbcTemplate.update(insertSql, sceneCode, finalPrompt);
                }
                System.out.println("====== 检测到新提示词，已保存至 sys_ai_prompt 并生效 ======");

            } else {
                String querySql = "SELECT prompt_content FROM sys_ai_prompt WHERE scene_code = ? LIMIT 1";
                try {
                    String dbPrompt = jdbcTemplate.queryForObject(querySql, String.class, sceneCode);
                    if (StringUtils.hasText(dbPrompt)) {
                        finalPrompt = dbPrompt;
                    }
                } catch (org.springframework.dao.EmptyResultDataAccessException e) {
                }

                if (!StringUtils.hasText(finalPrompt)) {
                    finalPrompt = StringUtils.hasText(question) ? question : "你是资深数据分析专家，请帮我分析这份报告。";
                }
            }

            body.add("prompts_content", finalPrompt);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            String targetUrl = AI_BASE_URL + "/api/analyze";
            ResponseEntity<String> response = restTemplate.postForEntity(targetUrl, requestEntity, String.class);
            JSONObject resJson = JSON.parseObject(response.getBody());




            String fileName = com.pearadmin.common.tools.string.StringUtil.isNotEmpty(modelName) ? modelName : file.getName();
            if (fileName.endsWith(".pptx")) {
                fileName = fileName.substring(0, fileName.lastIndexOf("."));
            }
            resJson.put("fileName", fileName);
            // 2. 获取 AI 返回的 taskId 作为本次会话的 sessionId
            String taskId = resJson.getString("task_id");
            if (StringUtils.hasText(taskId)) {
                java.time.LocalDateTime now = java.time.LocalDateTime.now();

                boolean isNewSession = false;
                if (!StringUtils.hasText(sessionId)) {
                    sessionId = java.util.UUID.randomUUID().toString().replace("-", "");
                    isNewSession = true;
                }

                // 3. 写入 t_chat_session (创建左侧会话记录)
                if (isNewSession) {
                    com.pearadmin.modules.chat.domain.TChatSession session = new com.pearadmin.modules.chat.domain.TChatSession();
                    session.setSessionId(sessionId);
                    session.setUserId(com.pearadmin.common.context.UserContext.currentUser().getUserId());
                    session.setSessionType(3L);
                    session.setSessionTitle(fileName);
                    session.setIsTop(0);
                    session.setCreateTime(now);
                    tChatSessionService.save(session);
                }

                // 4. 写入 t_chat_message (把文件名放进 content 字段)
                TChatMessage userMsg = new TChatMessage();
                userMsg.setMsgId(java.util.UUID.randomUUID().toString().replace("-", ""));
                userMsg.setSessionId(sessionId);
                userMsg.setRole("user"); // 角色：用户
                userMsg.setMsgType("file"); // 消息类型：附件
                userMsg.setContent(fileName); // 内容：文件名
                userMsg.setCreateTime(now);
                tChatMessageService.save(userMsg);

                TChatMessage sysMsg = new TChatMessage();
                sysMsg.setMsgId("sys-" + taskId);
                sysMsg.setSessionId(sessionId);
                sysMsg.setRole("system"); // 角色：系统
                sysMsg.setMsgType("text");
                String thinkingProcess = "<think>\n" + AI_THINKING_CONTENT + "\n</think>";
                sysMsg.setContent(thinkingProcess);
                sysMsg.setCreateTime(now.plusSeconds(1));
                tChatMessageService.save(sysMsg);
                resJson.put("sessionId", sessionId);
            }

            return Result.success(resJson);

        } catch (HttpStatusCodeException e) {
            return Result.failure("AI分析拒绝处理：" + e.getResponseBodyAsString());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.failure("转发AI分析失败：" + e.getMessage());
        }
    }


    /**
     * 接口2：查询任务进度 (3秒调一次)
     */
    @GetMapping("/status")
    public Result checkStatus(@RequestParam("taskId") String taskId) {
        try {
            // 拼接URL
            String targetUrl = AI_BASE_URL + "/api/status/" + taskId;

            ResponseEntity<String> response = restTemplate.getForEntity(targetUrl, String.class);

            // {"task_id": "xxx", "status": "processing", "progress": 45...}
            JSONObject resJson = JSON.parseObject(response.getBody());

            return Result.success(resJson);
        } catch (HttpStatusCodeException e) {
            return Result.failure("查询进度异常：" + e.getResponseBodyAsString());
        } catch (Exception e) {
            return Result.failure("查询进度失败：" + e.getMessage());
        }
    }

    /**
     * 接口3：获取分析结果并持久化
     */
    @GetMapping("/result")
    public Result getAnalysisResult(
            @RequestParam("taskId") String taskId,
            @RequestParam(value = "sessionId", required = false) String sessionId) {
        try {
            String targetUrl = AI_BASE_URL + "/api/result/" + taskId;
            ResponseEntity<String> response = restTemplate.getForEntity(targetUrl, String.class);
            JSONObject resJson = JSON.parseObject(response.getBody());

            // 提取大模型的分析结果
            String analysisText = resJson.getString("analysis_text");
            if (!StringUtils.hasText(analysisText) && resJson.containsKey("result")) {
                JSONObject innerResult = resJson.getJSONObject("result");
                if (innerResult != null) {
                    analysisText = innerResult.getString("analysis_text");
                }
            }

            // 将最终报告放入 t_chat_message
            if (StringUtils.hasText(analysisText)) {
                String finalSessionId = StringUtils.hasText(sessionId) ? sessionId : taskId;

                // 分析完成后，先将原先创建的临时占位消息从数据库中清理掉
                tChatMessageService.removeById("sys-" + taskId);

                // 将硬编码的思考过程与返回的分析报告用换行符紧密连接，对齐智能问数结构
                String finalCombinedContent = "<think>\n" + AI_THINKING_CONTENT + "\n</think>\n\n" + analysisText;

                TChatMessage aiMsg = new TChatMessage();
                aiMsg.setMsgId(java.util.UUID.randomUUID().toString().replace("-", ""));
                aiMsg.setSessionId(finalSessionId);
                aiMsg.setRole("ai");
                aiMsg.setMsgType("text");
                // 落库统一格式拼接结果
                aiMsg.setContent(finalCombinedContent);
                aiMsg.setCreateTime(java.time.LocalDateTime.now());

                tChatMessageService.save(aiMsg);

                // 替换即将发回给前端的 resJson 内容，确保前端动态追加气泡时直接渲染收缩方块
                resJson.put("analysis_text", finalCombinedContent);
                if (resJson.containsKey("result")) {
                    resJson.getJSONObject("result").put("analysis_text", finalCombinedContent);
                }
            }
            return Result.success(resJson);

        } catch (HttpStatusCodeException e) {
            return Result.failure("获取结果异常：" + e.getResponseBodyAsString());
        } catch (Exception e) {
            return Result.failure("获取结果失败：" + e.getMessage());
        }
    }

    /**
     * 接口4：下载 Markdown 分析报告
     */
    @GetMapping("/downloadReport")
    public void downloadReport(@RequestParam("taskId") String taskId, HttpServletResponse response) {
        try {
            String targetUrl = AI_BASE_URL + "/api/download/" + taskId;
            ResponseEntity<byte[]> fileResponse = restTemplate.getForEntity(targetUrl, byte[].class);

            if (fileResponse.getStatusCode().is2xxSuccessful() && fileResponse.getBody() != null) {
                // 设置下载响应头
                response.setContentType("text/markdown; charset=utf-8");
                response.setHeader("Content-Disposition", "attachment; filename=\"AnalysisReport_" + taskId + ".md\"");
                response.getOutputStream().write(fileResponse.getBody());
                response.getOutputStream().flush();
            } else {
                response.setContentType("text/html; charset=utf-8");
                response.getWriter().write("下载失败，文件可能不存在或未完成。");
            }
        } catch (HttpStatusCodeException e) {
            try {
                response.setContentType("text/html; charset=utf-8");
                response.getWriter().write("下载报错：" + e.getResponseBodyAsString());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}