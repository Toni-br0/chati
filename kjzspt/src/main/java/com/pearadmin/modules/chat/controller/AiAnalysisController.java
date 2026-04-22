package com.pearadmin.modules.chat.controller;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.stream.Collectors;

import com.pearadmin.common.aop.annotation.Log;
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
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;
import java.io.File;

@RestController
@RequestMapping("/znfx")
public class AiAnalysisController {

    private final RestTemplate restTemplate;

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



    public AiAnalysisController() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(15000); // 连接超时 15 秒
        factory.setReadTimeout(120000);   // 读取超时 120 秒 (AI处理较慢，预留充足时间)
        this.restTemplate = new RestTemplate(factory);
    }

    /**
     * 获取报告文件类型字典（按 sort 降序）
     */
    @GetMapping("/reportFileTypes")
    public Result getReportFileTypes() {
        String sql = "SELECT data_label as dataLabel, data_value as dataValue " +
                "FROM sys_dict_data " +
                "WHERE type_code = 'report_file_type' AND enable = '0' " +
                "ORDER BY sort ASC";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);
        return Result.success(list);
    }

    /**
     * 获取系统文件列表
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
            } catch (Exception e) {

            }
        }

        if (dictLabel != null && dictLabel.contains("县分公司")) {
            com.pearadmin.modules.xfppt.domain.XfpptFilePushManage queryParam = new com.pearadmin.modules.xfppt.domain.XfpptFilePushManage();
            if (com.pearadmin.common.tools.string.StringUtil.isNotEmpty(keyword)) {
                queryParam.setFileName(keyword);
            }
            List<com.pearadmin.modules.xfppt.domain.XfpptFilePushManage> list = xfPptDownLoadService.getXfPptDownLoadDataList(queryParam);

            List<Map<String, Object>> resultList = new ArrayList<>();
            for (com.pearadmin.modules.xfppt.domain.XfpptFilePushManage item : list) {
                Map<String, Object> map = new java.util.HashMap<>();
                map.put("id", item.getManageId());
                map.put("modelName", item.getFileName());
                resultList.add(map);
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
    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/analyzeSysFile")
    @Log(module = "智能分析", action = "发起对话")
    public Result analyzeSysFile(
            @RequestParam("fileId") String fileId,
            @RequestParam(value = "fileType", required = false) String fileType,
            @RequestParam(value = "analysisType", defaultValue = "1") Integer analysisType,
            @RequestParam(value = "question", required = false) String question,
            @RequestParam(value = "sessionId", required = false) String sessionId,
            @RequestParam(value = "promptsContent", required = false) String promptsContent,
            @RequestParam(value = "promptTarget", required = false) String promptTarget) {

        System.out.println("\n========================================================");
        System.out.println(">>>>>> [DEBUG-1] 收到分析请求，fileId: " + fileId + ", fileType: " + fileType);

        try {
            boolean isCountyFile = (fileId != null && fileId.length() > 15);
            String targetPath = null;
            String modelName = null;
            System.out.println(">>>>>> [DEBUG-2] 路由判断结果：是否为县分材料 -> " + isCountyFile);

            if (isCountyFile) {
                String sql = "SELECT file_name, file_path FROM xfppt_file_push_manage WHERE manage_id = ?";
                try {
                    Map<String, Object> xfData = jdbcTemplate.queryForMap(sql, fileId);
                    targetPath = String.valueOf(xfData.get("file_path"));
                    modelName = String.valueOf(xfData.get("file_name"));
                } catch (Exception e) {
                    return Result.failure("县分表找不到对应文件：" + fileId);
                }
            } else {
                PptDownloadData pptData = pptDownloadDataMapper.selectById(fileId);
                if (pptData == null) return Result.failure("区公司表找不到对应文件：" + fileId);
                targetPath = pptData.getModelTargetPath();
                modelName = pptData.getModelName();
            }

            System.out.println(">>>>>> [DEBUG-3] 最终获取到的文件路径: " + targetPath);

            File file = new File(targetPath);
            if (!file.exists()) return Result.failure("服务器文件已丢失：" + targetPath);

            final String finalModelName = modelName;
            FileSystemResource fileAsResource = new FileSystemResource(file) {
                @Override
                public String getFilename() {
                    String name = StringUtils.hasText(finalModelName) ? finalModelName : super.getFilename();
                    return name.toLowerCase().endsWith(".pptx") ? name : name + ".pptx";
                }
            };

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", fileAsResource);

            // ==========================================
            // 所有维度文件统一走一次性解析 (single)
            // ==========================================
            String targetUrl = AI_BASE_URL + "/api/analyze";

            if (isCountyFile) {
                body.add("analysis_type", "county");
                System.out.println("====== [路由分发] 县分材料：执行一次性解析 (single) ======");
            } else {
                body.add("analysis_type", "local");
                System.out.println("====== [路由分发] 区/分公司材料：执行一次性解析 (single) ======");
            }

            body.add("analysis_mode", "single");

            if (StringUtils.hasText(sessionId)) {
                body.add("conversation_id", sessionId);
            }

            String finalPrompt = "";
            String targetScene = isCountyFile ? "PPT_PROMPT_YAML" : "PPT_PROMPT_MD";

            if (StringUtils.hasText(promptsContent)) {
                String saveScene = "county".equalsIgnoreCase(promptTarget) ? "PPT_PROMPT_YAML" : "PPT_PROMPT_MD";

                String updateSql = "UPDATE sys_ai_prompt SET prompt_content = ?, update_time = NOW() WHERE scene_code = ?";
                int rows = jdbcTemplate.update(updateSql, promptsContent, saveScene);

                if (rows == 0) {
                    String insertSql = "INSERT INTO sys_ai_prompt (scene_code, prompt_content, update_time) VALUES (?, ?, NOW())";
                    jdbcTemplate.update(insertSql, saveScene, promptsContent);
                }

                if (saveScene.equals(targetScene)) {
                    finalPrompt = promptsContent;
                }
                System.out.println("====== 提示词已手动更新至目标槽位：" + saveScene + " ======");
            }

            if (!StringUtils.hasText(finalPrompt)) {
                String querySql = "SELECT prompt_content FROM sys_ai_prompt WHERE scene_code = ? LIMIT 1";
                try {
                    finalPrompt = jdbcTemplate.queryForObject(querySql, String.class, targetScene);
                } catch (org.springframework.dao.EmptyResultDataAccessException e) {
                    finalPrompt = StringUtils.hasText(question) ? question : "你是资深数据分析专家，请帮我分析这份报告。";
                }
            }
            body.add("prompts_content", finalPrompt);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            System.out.println(">>>>>> [DEBUG-4] 准备向大模型服务器发起 post 请求...");
            ResponseEntity<String> response = restTemplate.postForEntity(targetUrl, requestEntity, String.class);

            System.out.println("\n\n🌈🌈🌈 ====== 【Dify 绝对原生报文 - 智能分析接口】 ====== 🌈🌈🌈");
            System.out.println(response.getBody());
            System.out.println("🌈🌈🌈 ======================================================== 🌈🌈🌈\n\n");

            JSONObject resJson = JSON.parseObject(response.getBody());

            String fileName = com.pearadmin.common.tools.string.StringUtil.isNotEmpty(modelName) ? modelName : file.getName();
            if (fileName.endsWith(".pptx")) {
                fileName = fileName.substring(0, fileName.lastIndexOf("."));
            }
            resJson.put("fileName", fileName);

            String taskId = resJson.getString("task_id");

            String immediateText = resJson.getString("analysis_text");
            if (!StringUtils.hasText(immediateText) && resJson.containsKey("result")) {
                JSONObject innerResult = resJson.getJSONObject("result");
                if (innerResult != null) immediateText = innerResult.getString("analysis_text");
            }

            if (StringUtils.hasText(taskId)) {
                java.time.LocalDateTime now = java.time.LocalDateTime.now();

                boolean isNewSession = false;
                if (!StringUtils.hasText(sessionId)) {
                    sessionId = java.util.UUID.randomUUID().toString().replace("-", "");
                    isNewSession = true;
                }

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

                TChatMessage userMsg = new TChatMessage();
                userMsg.setMsgId(java.util.UUID.randomUUID().toString().replace("-", ""));
                userMsg.setSessionId(sessionId);
                userMsg.setRole("user");
                userMsg.setMsgType("file");
                userMsg.setContent(fileName);
                userMsg.setCreateTime(now);
                tChatMessageService.save(userMsg);

                TChatMessage sysMsg = new TChatMessage();
                sysMsg.setMsgId("sys-" + taskId);
                sysMsg.setSessionId(sessionId);
                sysMsg.setRole("system");
                sysMsg.setMsgType("text");

                if (StringUtils.hasText(immediateText)) {
                    String finalCombinedContent = "<think>\n" + AI_THINKING_CONTENT + "\n</think>\n\n" + immediateText;
                    sysMsg.setContent(finalCombinedContent);
                    resJson.put("analysis_text", finalCombinedContent);
                } else {
                    String thinkingProcess = "<think>\n" + AI_THINKING_CONTENT + "\n</think>";
                    sysMsg.setContent(thinkingProcess);
                }

                sysMsg.setCreateTime(now.plusSeconds(1));
                tChatMessageService.save(sysMsg);
                resJson.put("sessionId", sessionId);
            }

            return Result.success(resJson);

        } catch (HttpStatusCodeException e) {
            System.err.println("❌❌❌ [ERROR] 大模型接口报错：" + e.getResponseBodyAsString());
            return Result.failure("AI分析拒绝处理：" + e.getResponseBodyAsString());
        } catch (Throwable e) {
            System.err.println("\n❌❌❌ [FATAL ERROR] 接口1(analyzeSysFile)后台异常崩溃！");
            e.printStackTrace();
            return Result.failure("转发AI分析失败：" + e.getMessage());
        }
    }

    /**
     * 接口2：查询任务进度
     */
    @GetMapping("/status")
    public Result checkStatus(@RequestParam("taskId") String taskId) {
        try {
            String targetUrl = AI_BASE_URL + "/api/status/" + taskId;
            ResponseEntity<String> response = restTemplate.getForEntity(targetUrl, String.class);
            System.out.println(">>>>>> [STATUS] 轮询 taskId: " + taskId + "，结果: " + response.getBody());
            JSONObject resJson = JSON.parseObject(response.getBody());
            return Result.success(resJson);
        } catch (Throwable e) {
            System.err.println("❌❌❌ [ERROR] 轮询进度接口异常崩溃！");
            e.printStackTrace();
            return Result.failure("查询进度失败：" + e.getMessage());
        }
    }

    /**
     * 接口3：获取分析结果并持久化
     * 【优化】：增加事务支持
     */
    @Transactional(rollbackFor = Exception.class)
    @GetMapping("/result")
    public Result getAnalysisResult(
            @RequestParam("taskId") String taskId,
            @RequestParam(value = "sessionId", required = false) String sessionId) {

        System.out.println("\n========================================================");
        System.out.println(">>>>>> [RESULT-1] 准备获取最终分析报告, taskId: " + taskId);
        try {
            String targetUrl = AI_BASE_URL + "/api/result/" + taskId;
            ResponseEntity<String> response = restTemplate.getForEntity(targetUrl, String.class);
            JSONObject resJson = JSON.parseObject(response.getBody());

            String analysisText = resJson.getString("analysis_text");
            if (!StringUtils.hasText(analysisText) && resJson.containsKey("result")) {
                JSONObject innerResult = resJson.getJSONObject("result");
                if (innerResult != null) analysisText = innerResult.getString("analysis_text");
            }

            if (StringUtils.hasText(analysisText)) {
                String finalSessionId = StringUtils.hasText(sessionId) ? sessionId : taskId;
                System.out.println(">>>>>> [RESULT-2] 从大模型拿到报告，准备删旧记录、插新记录...");

                tChatMessageService.removeById("sys-" + taskId);

                String finalCombinedContent = "<think>\n" + AI_THINKING_CONTENT + "\n</think>\n\n" + analysisText;

                TChatMessage aiMsg = new TChatMessage();
                aiMsg.setMsgId(java.util.UUID.randomUUID().toString().replace("-", ""));
                aiMsg.setSessionId(finalSessionId);
                aiMsg.setRole("ai");
                aiMsg.setMsgType("text");
                aiMsg.setContent(finalCombinedContent);
                aiMsg.setCreateTime(java.time.LocalDateTime.now());

                tChatMessageService.save(aiMsg);
                System.out.println(">>>>>> [RESULT-3] 报告数据库落库成功！");

                resJson.put("analysis_text", finalCombinedContent);
                if (resJson.containsKey("result")) resJson.getJSONObject("result").put("analysis_text", finalCombinedContent);
            }
            System.out.println("========================================================\n");
            return Result.success(resJson);

        } catch (Throwable e) {
            System.err.println("\n❌❌❌ [FATAL ERROR] 接口3(getAnalysisResult)最终报告落库时异常崩溃！");
            e.printStackTrace();
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