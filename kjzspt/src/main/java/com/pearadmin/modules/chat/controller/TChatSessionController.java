package com.pearadmin.modules.chat.controller;

import com.github.pagehelper.PageInfo;
import com.pearadmin.common.context.UserContext;
import com.pearadmin.modules.chat.domain.TChatSession;
import com.pearadmin.common.tools.string.Convert;
import com.pearadmin.common.web.base.BaseController;
import com.pearadmin.common.web.domain.request.PageDomain;
import com.pearadmin.common.web.domain.response.Result;
import com.pearadmin.common.web.domain.response.module.ResultTable;
import com.pearadmin.modules.sys.domain.SysDictData;
import com.pearadmin.modules.sys.service.SysDictDataService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import com.pearadmin.modules.chat.service.ITChatSessionService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * AI智能会话主Controller
 *
 * @author jmys
 * @date 2026-03-16
 */
@Controller
@RequestMapping("/znws")
public class TChatSessionController extends BaseController {

    private String prefix = "znws";

    @Autowired
    private ITChatSessionService tChatSessionService;

    @Autowired
    private SysDictDataService sysDictDataService;

    @Autowired
    private com.pearadmin.modules.sys.service.SysUserService sysUserService;


    @GetMapping("/toZnws")
    @PreAuthorize("hasPermission('/znws/toZnws','znws:toZnws')")
    public ModelAndView toZnws() {
        ModelAndView modelAndView = jumpPage(prefix + "/main");

        boolean isAdmin = false;
        try {
            com.pearadmin.modules.sys.domain.SysUser currentUser = com.pearadmin.common.context.UserContext.currentUser();
            if (currentUser != null) {
                // 1.账号名为 admin 的用户
                if ("admin".equals(currentUser.getUsername())) {
                    isAdmin = true;
                } else {
                    // 2. 其他用户
                    java.util.List<com.pearadmin.modules.sys.domain.SysRole> roles = sysUserService.getUserRole(currentUser.getUserId());

                    if (roles != null) {
                        for (com.pearadmin.modules.sys.domain.SysRole role : roles) {
                            if (role != null && "admin".equalsIgnoreCase(role.getRoleCode()) && role.isChecked()) {
                                isAdmin = true;
                                break;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        modelAndView.addObject("isAdmin", isAdmin);
        return modelAndView;
    }


    /**
     * 选择文件
     * @return
     */
    @GetMapping("selectFile")
    @ApiOperation(value = "选择文件页面",notes = "选择文件页面")
    public ModelAndView selectFile(Model model) {
        //文件类型
        List<SysDictData> list = sysDictDataService.selectByCode("report_file_type");
        model.addAttribute("fileTypes", list);
        return jumpPage(prefix + "/selectFile");
    }

    /**
     * 查询AI智能会话主列表
     */
    @ResponseBody
    @GetMapping("/data")
    @PreAuthorize("hasPermission('/znws/data','znws:data')")
    public ResultTable list(@ModelAttribute TChatSession tChatSession, PageDomain pageDomain) {
        tChatSession.setUserId(UserContext.currentUser().getUserId());
        PageInfo<TChatSession> pageInfo = tChatSessionService.selectTChatSessionPage(tChatSession, pageDomain);
        return pageTable(pageInfo.getList(), pageInfo.getTotal());
    }

    /**
     * 新增AI智能会话主
     */
    @GetMapping("/add")
    @PreAuthorize("hasPermission('/znws/add','znws:add')")
    public ModelAndView add() {
        return jumpPage(prefix + "/add");
    }

    /**
     * 新增AI智能会话主
     */
    @ResponseBody
    @PostMapping("/save")
    @PreAuthorize("hasPermission('/znws/add','znws:add')")
    public Result save(@RequestBody TChatSession tChatSession) {
        return decide(tChatSessionService.save(tChatSession));
    }

    /**
     * 修改AI智能会话主
     */
    @GetMapping("/edit")
    @PreAuthorize("hasPermission('/znws/edit','znws:edit')")
    public ModelAndView edit(String sessionId, ModelMap map) {
        TChatSession tChatSession =tChatSessionService.getById(sessionId);
        map.put("tChatSession", tChatSession);
        return jumpPage(prefix + "/edit");
    }

    /**
     * 修改AI智能会话主
     */
    @ResponseBody
    @PutMapping("/update")
    @PreAuthorize("hasPermission('/znws/edit','znws:edit')")
    public Result update(@RequestBody TChatSession tChatSession) {
        return decide(tChatSessionService.updateById(tChatSession));
    }

    /**
     * 删除AI智能会话主
     */
    @ResponseBody
    @DeleteMapping("/batchRemove")
    @PreAuthorize("hasPermission('/znws/remove','znws:remove')")
    public Result batchRemove(String ids) {
        return decide(tChatSessionService.removeByIds(Arrays.asList(ids.split(","))));
    }


    /**
     * 删除AI智能会话主
     */
    @ResponseBody
    @DeleteMapping("/remove/{sessionId}")
    @PreAuthorize("hasPermission('/znws/remove','znws:remove')")
    public Result remove(@PathVariable("sessionId") String sessionId) {
        return decide(tChatSessionService.removeById(sessionId));
    }
}
