package com.pearadmin.common.tools.check;

import com.pearadmin.modules.sys.domain.SysRole;
import com.pearadmin.modules.sys.domain.SysUser;

import java.util.List;

/**
 * 创建日期：2025-08-07
 **/

public class CheckUtil {

  /**
   * 验证是否是超级管理员或全部数据查看角色
   * @param sysUser
   * @return
   */
  public static boolean hasSuperAdmin(SysUser sysUser) {

    List<SysRole> sysRoles = sysUser.getRoles();
    if(sysRoles != null && sysRoles.size() >0){

      return sysRoles != null && sysRoles.stream()
              .filter(role -> role != null)  // 过滤掉null元素
              .anyMatch(role -> "admin".equals(role.getRoleCode())
                      || "allDataView".equals(role.getRoleCode()));

    }else{
      return false;
    }

  }
}
