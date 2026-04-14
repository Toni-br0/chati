package com.pearadmin.common.tools;

/**
 * 创建日期：2025-07-17
 * 脱敏
 **/

public class SensitiveDataUtils {

  // 手机号脱敏（保留前3位和后4位）
  public static String desensitizePhone(String phone) {
    if (phone == null || phone.length() < 11) return phone;
    return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
  }

  // 身份证号脱敏（保留前6位和后4位）
  public static String desensitizeIdCard(String idCard) {
    if (idCard == null || idCard.length() < 18) return idCard;
    return idCard.substring(0, 6) + "********" + idCard.substring(idCard.length() - 4);
  }

  // 姓名脱敏（保留姓氏）
  /*public static String desensitizeName(String name) {
    if (name == null || name.length() < 2) return name;
    return name.substring(0, 1) + "*".repeat(name.length() - 1);
  }*/

}
