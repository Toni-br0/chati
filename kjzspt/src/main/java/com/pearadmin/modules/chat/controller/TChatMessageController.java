package com.pearadmin.modules.chat.controller;

import com.github.pagehelper.PageInfo;
import com.pearadmin.common.context.UserContext;
import com.pearadmin.modules.chat.domain.TChatMessage;
import com.pearadmin.common.tools.string.Convert;
import com.pearadmin.common.web.base.BaseController;
import com.pearadmin.common.web.domain.request.PageDomain;
import com.pearadmin.common.web.domain.response.Result;
import com.pearadmin.common.web.domain.response.module.ResultTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import com.pearadmin.modules.chat.service.ITChatMessageService;

import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * AI智能会话消息明细Controller
 *
 * @author jmys
 * @date 2026-03-16
 */
@RestController
@RequestMapping("/sys/chatMessage")
public class TChatMessageController extends BaseController {

    private String prefix = "sys/chatMessage";

    @Autowired
    private ITChatMessageService tChatMessageService;

    @GetMapping("/main")
    @PreAuthorize("hasPermission('/sys/chatMessage/main','sys:chatMessage:main')")
    public ModelAndView main() {
        return jumpPage(prefix + "/main");
    }
    /**
     * 查询AI智能会话消息明细列表
     */
    @ResponseBody
    @GetMapping("/data")
    @PreAuthorize("hasPermission('/sys/chatMessage/data','sys:chatMessage:data')")
    public ResultTable list(@ModelAttribute TChatMessage tChatMessage, PageDomain pageDomain) {
        PageInfo<TChatMessage> pageInfo = tChatMessageService.selectTChatMessagePage(tChatMessage, pageDomain);
        return pageTable(pageInfo.getList(), pageInfo.getTotal());
    }

    /**
     * 新增AI智能会话消息明细
     */
    @GetMapping("/add")
    @PreAuthorize("hasPermission('/sys/chatMessage/add','sys:chatMessage:add')")
    public ModelAndView add() {
        return jumpPage(prefix + "/add");
    }

    /**
     * 新增AI智能会话消息明细
     */
    @ResponseBody
    @PostMapping("/save")
    @PreAuthorize("hasPermission('/sys/chatMessage/add','sys:chatMessage:add')")
    public Result save(@RequestBody TChatMessage tChatMessage) {
        return decide(tChatMessageService.save(tChatMessage));
    }

    /**
     * 修改AI智能会话消息明细
     */
    @GetMapping("/edit")
    @PreAuthorize("hasPermission('/sys/chatMessage/edit','sys:chatMessage:edit')")
    public ModelAndView edit(String msgId, ModelMap map) {
        TChatMessage tChatMessage =tChatMessageService.getById(msgId);
        map.put("tChatMessage", tChatMessage);
        return jumpPage(prefix + "/edit");
    }

    /**
     * 修改AI智能会话消息明细
     */
    @ResponseBody
    @PutMapping("/update")
    @PreAuthorize("hasPermission('/sys/chatMessage/edit','sys:chatMessage:edit')")
    public Result update(@RequestBody TChatMessage tChatMessage) {
        return decide(tChatMessageService.updateById(tChatMessage));
    }

    /**
     * 删除AI智能会话消息明细
     */
    @ResponseBody
    @DeleteMapping("/batchRemove")
    @PreAuthorize("hasPermission('/sys/chatMessage/remove','sys:chatMessage:remove')")
    public Result batchRemove(String ids) {
        return decide(tChatMessageService.removeByIds(Arrays.asList(ids.split(","))));
    }

    /**
     * 删除AI智能会话消息明细
     */
    @ResponseBody
    @DeleteMapping("/remove/{msgId}")
    @PreAuthorize("hasPermission('/sys/chatMessage/remove','sys:chatMessage:remove')")
    public Result remove(@PathVariable("msgId") String msgId) {
        return decide(tChatMessageService.removeById(msgId));
    }
}
