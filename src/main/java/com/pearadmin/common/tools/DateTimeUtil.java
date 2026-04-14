package com.pearadmin.common.tools;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;

import java.lang.management.ManagementFactory;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

/**
 * Describe: 日 期 工 具 类
 * Author: 就 眠 仪 式
 * CreateTime: 2019/10/23
 */
public class DateTimeUtil {

    /**
     * 获启动时间
     *
     * @return {@link Date}
     */
    public static Date getServerStartDate() {
        long time = ManagementFactory.getRuntimeMXBean().getStartTime();
        return new Date(time);
    }

    /**
     * 计算时间差
     *
     * {@link String}
     * */
    public static String getDatePoor(Date endDate, Date nowDate) {
        long nd = 1000 * 24 * 60 * 60;
        long nh = 1000 * 60 * 60;
        long nm = 1000 * 60;
        long diff = endDate.getTime() - nowDate.getTime();
        // 计算差多少天
        long day = diff / nd;
        // 计算差多少小时
        long hour = diff % nd / nh;
        // 计算差多少分钟
        long min = diff % nd % nh / nm;
        // 计算差多少秒//输出结果
        // long sec = diff % nd % nh % nm / ns;
        return day + "天" + hour + "小时" + min + "分钟";
    }

    /**
     * 默认日期格式, <code>yyyy-MM-dd</code>
     */
    public static final String PATTERN_DEFAULT = "yyyy-MM-dd";

    /**
     * 路径格式, <code>yyyy\MM\dd\</code>
     */
    public static final String PATTERN_DAYPATH = "yyyy\\MM\\dd\\";

    /**
     * 日期时间格式, <code>yyyy-MM-dd HH:mm:ss</code>, 24小时制
     */
    public static final String PATTERN_DATETIME = "yyyy-MM-dd HH:mm:ss";

    /**
     * 无间隔符的日期时间格式, <code>yyyyMMddHHmmss</code>, 24小时制
     */
    public static final String PATTERN_DATETIME_COMPACT = "yyyyMMddHHmmss";

    /**
     * yyMMddHHmmssSSS,毫秒级别
     */
    public static final String PATTERN_DATETIME_MILLISECOND = "yyyyMMddHHmmssSSS";

    /**
     * 无间隔符日期格式, <code>yyyyMMdd</code>
     */
    public static final String PATTERN_DATE_COMPACT = "yyyyMMdd";

    /**
     * 无间隔符日期格式, <code>hhmmssSSS</code>
     */
    public static final String PATTERN_MILLISECOND = "HHmmssSSS";

    /**
     * 无间隔符日期格式, <code>yyyyMMdd HH</code>
     */
    public static final String PATTERN_DATETIME_HH = "yyyy-MM-dd HH";

    /**
     * 无间隔符日期格式, <code>yyyyMMddHH</code>
     */
    public static final String PATTERN_DATE_COMPACT_HH = "yyyyMMddHH";

    /**
     * 无间隔符日期格式, <code>yyMMdd</code>
     */
    public static final String PATTERN_DATESHORT = "yyMMdd";

    /**
     * 年月, <code>yyyyMM</code>
     */
    public static final String PATTERN_YEARMONTH = "yyyyMM";

    /**
     * 年月, <code>yyyy-MM</code>
     */
    public static final String PATTERN_YEAR_MONTH = "yyyy-MM";
    /**
     * 年月, <code>yyyy年MM月</code>
     */
    public static final String PATTERN_YEAR_MONTH_C = "yyyy年MM月";

    /**
     * 年月日, <code>yyyy年M月d日</code>
     */
    public static final String PATTERN_YEAR_MONTH_DAY_C = "yyyy年M月d日";

    /**
     * 格式为：Sat Oct 10 00:00:00 CST 2009 这种
     */
    public static final String PATTERN_CST_FORMAT = "EEE MMM d hh:mm:ss z yyyy";

    /**
     * 年月日, <code>yyyy</code>
     */
    public static final String PATTERN_YEAR = "yyyy";

    public static String formatCSTDate(Date date, String pattern) {
        SimpleDateFormat bartDateFormat = new SimpleDateFormat(pattern, java.util.Locale.US);
        String targetDate = null;
        try {
            targetDate = bartDateFormat.format(date);
        } catch (Exception e) {
            return null;
        }
        return targetDate;
    }

    /**
     * <b> 根据默认格式(<code>yyyy-MM-dd</code>),格式化日期 </b>
     *
     * @param date
     *            日期
     * @return java.util.Date
     */
    public static String formatDate(Date date) {
        if (date == null) {
            return null;
        }
        return new SimpleDateFormat(PATTERN_DEFAULT).format(date);
    }

    /**
     * <b> 根据指定格式,格式化日期 </b>
     *
     * @param date
     *            日期
     * @param pattern
     *            指定格式,参照类中常量定义
     * @return java.util.Date
     */
    public static String formatDate(Date date, String pattern) {
        try {
            return new SimpleDateFormat(pattern).format(date);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 根据日期格式，获取当前日期字符串
     * @param pattern
     * @return
     */
    public static String formatDate(String pattern) {
        try {
            return new SimpleDateFormat(pattern).format(new Date());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 根据指定格式，获取当前日期
     *
     * @param pattern
     * @return
     */
    public static String getCurrentDate(String pattern) {
        try {
            return new SimpleDateFormat(pattern).format(new Date());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取当前日期的前一天
     *
     * @param pattern
     * @return
     */
    public static String getYesterday(String pattern) {
        try {
            Calendar calendar = Calendar.getInstance();// 此时打印它获取的是系统当前时间
            calendar.add(Calendar.DATE, -1); // 得到前一天
            return new SimpleDateFormat(pattern).format(calendar.getTime());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取当前日期的后一天
     *
     * @param pattern
     * @return
     */
    public static String getNextday(String pattern) {
        try {
            Calendar calendar = Calendar.getInstance();// 此时打印它获取的是系统当前时间
            calendar.add(Calendar.DATE, +1); // 得到后一天
            return new SimpleDateFormat(pattern).format(calendar.getTime());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取当前日期的前一个月
     *
     * @param pattern
     * @return
     */
    public static String getLastMonth(String pattern) {
        try {
            Calendar calendar = Calendar.getInstance();// 此时打印它获取的是系统当前时间
            calendar.add(Calendar.MONTH, -1);
            // int month = calendar.get(Calendar.MONTH)+1; //输出前一月的时候要记得加1
            return new SimpleDateFormat(pattern).format(calendar.getTime());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     *
     * <p>
     * 更换日期格式
     * </p>
     * <b>修改历史</b>
     *
     * @param datestr
     * @param orginPattern
     * @param targetPattern
     * @return
     */
    public static String changeDatePattern(String datestr, String orginPattern, String targetPattern) {
        return formatDate(parseDate(datestr, orginPattern), targetPattern);
    }

    /**
     * <b> 根据指定格式转换字符串为日期 </b> <br>
     *
     * 如果字符串格式不正确,则返回null
     *
     * @param dateString
     *            日期字符串
     * @param pattern
     *            指定格式,参照类中常量定义
     * @return java.util.Date
     */
    public static Date parseDate(String dateString, String pattern) {
        try {
            return new SimpleDateFormat(pattern).parse(dateString);
        } catch (ParseException pe) {
            return null;
        }
    }

    /**
     * <b> 将默认格式(<code>yyyy-MM-dd</code>)的日期字符串转换成<code>java.util.Date</code> 类型
     * </b>
     *
     * @param dateString
     *            日期字符串
     * @return java.util.Date
     */
    public static Date parseDate(String dateString) {
        return parseDate(dateString, PATTERN_DEFAULT);
    }

    public static Date parseDatePattern(String dateString, String pattern) {
        return parseDate(dateString, pattern);
    }

    /**
     * 计算日期
     *
     * @param date
     *            需要计算的日期
     * @param timeUnit
     *            时间单位 (Calendar.HOUR, Calendar.DATE, Calendar.MONTH, Calendar.YEAR)
     * @param amount
     *            增减数,可以为负数
     * @author LiuYuan
     * @return
     */
    public static Date accountDate(Date date, int timeUnit, int amount) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(timeUnit, amount);
        return c.getTime();
    }

    public static String getNextDay(Date date, int i, String pattern) {
        return DateTimeUtil.formatDate(accountDate(date, Calendar.DATE, i), pattern);
    }

    /**
     * 获取日期所在年份的总天数
     *
     * @param date
     * @return
     */
    public static int getDaysOfCurrentYear(Date date) {
        Calendar c = GregorianCalendar.getInstance();
        c.setTime(date);
        return c.isLenient() ? 366 : 365;
    }

    /**
     * 获取日期所在月份的总天数
     *
     * @param date
     * @return
     * @throws Exception
     */
    public static int getDaysOfCurrentMonth(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    /**
     * 计算两个日期相差的单位数,取整值
     *
     * @param date1
     *            日期1
     * @param date2
     *            日期2

     *            时间单位
     * @return
     * @throws Exception
     */
    public static int getDiscrepantUnits(Date date1, Date date2, int timeUnit) {
        if (isSameDate(date1, date2)) {
            return 0;
        }
        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        c1.setTime(date1.before(date2) ? date1 : date2);
        c2.setTime(date1.before(date2) ? date2 : date1);
        int count = -1;
        while (c1.before(c2)) {
            c1.add(timeUnit, 1);
            count++;
        }

        return date1.before(date2) ? count : -count;
    }

    /**
     * 返回两个日期相差的天数
     *
     * @param dateStart
     * @param dateEnd
     * @return
     */
    public static int getDiscrepantDays(Date dateStart, Date dateEnd) {
        return (int) ((dateEnd.getTime() - dateStart.getTime()) / 1000 / 60 / 60 / 24);
    }

    /**
     * 返回两个日期相差的分钟
     *
     * @param dateStart
     * @param dateEnd
     * @return
     */
    public static double getDiscrepantMinute(Date dateStart, Date dateEnd) {
        long createDate = dateStart.getTime();
        long nowDate = dateEnd.getTime();
        Double min = Double.valueOf(String.valueOf(nowDate - createDate)) / (60 * 1000.00);
        return min;
    }

    /**
     * 计算两个日期之间相差多少月 精确到月
     *
     * @param dateStart
     * @param dateEnd
     * @return
     * @throws Exception
     */
    public static int getDiscrepantMonthI(Date dateStart, Date dateEnd) {
        Calendar calendarEnd = Calendar.getInstance();
        Calendar calendarStart = Calendar.getInstance();
        calendarEnd.setTime(dateEnd);
        calendarStart.setTime(dateStart);
        return ((calendarEnd.get(Calendar.YEAR) - calendarStart.get(Calendar.YEAR)) * 12)
                + (calendarEnd.get(Calendar.MONTH) - calendarStart.get(Calendar.MONTH));
    }

    /**
     * 判断两个日期的大小关系
     *
     * @param d1
     * @param d2
     * @return -1 d1在d2之前, 0 d1与d2相等, 1 d1在d2之后
     * @throws Exception
     */
    public static int compareDay(Date d1, Date d2) {
        return isSameDay(d1, d2) ? 0 : (d1.before(d2) ? -1 : 1);
    }

    /**
     * 比较两个日期是否相同
     *
     * @param date1
     * @param date2
     * @return
     * @throws Exception
     */
    public static boolean isSameDate(Date date1, Date date2) {
        return !(date1.before(date2) || date2.before(date1));
    }

    /**
     * 比较两个日期是否相同, 只是比较年月日
     *
     * @param date1
     * @param date2
     * @return
     * @throws Exception
     */
    public static boolean isSameDay(Date date1, Date date2) {
        return formatDate(date1).equals(formatDate(date2));
    }

    /**
     * 根据日时间得到当今天开始时间
     *
     * @param date
     * @return
     */
    public static Date getStartOfDay(Date date) {
        return parseDate(formatDate(date, PATTERN_DEFAULT) + " 00:00:00", PATTERN_DATETIME);
    }

    /**
     * 根据日时间得到当今天结束时间
     *
     * @param date
     * @return
     */
    public static Date getEndOfDay(Date date) {
        return parseDate(formatDate(date, PATTERN_DEFAULT) + " 23:59:59", PATTERN_DATETIME);
    }

    /**
     * 获取当前年
     *
     * @return
     */
    public static String getCurrentYear() {
        Calendar date = Calendar.getInstance();
        return String.valueOf(date.get(Calendar.YEAR));
    }

    /**
     * 获取当前日期的上一年
     *
     * @return
     */
    public static String getLastYear() {
        Calendar date = Calendar.getInstance();
        date.setTime(new Date());
        date.add(Calendar.YEAR, -1);
        date.getTime();
        return formatDate(date.getTime(), PATTERN_YEAR);
    }

    /**
     * 获取当前日期的后一年
     *
     * @return
     */
    public static String getNextYear() {
        Calendar date = Calendar.getInstance();
        date.setTime(new Date());
        date.add(Calendar.YEAR, +1);
        date.getTime();
        return formatDate(date.getTime(), PATTERN_YEAR);
    }

    /**
     * 获取年底的日期
     *
     * @return
     */
    public static Date getEndOfYear(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return parseDate(calendar.get(Calendar.YEAR) + "-12-31");// 停止计费时间为年底
    }

    /**
     * 获取下月底日期
     *
     * @param date
     * @return
     * @throws Exception
     */
    public static Date getEndOfNextMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, 1);
        calendar.set(Calendar.DAY_OF_MONTH, getDaysOfCurrentMonth(
                parseDate(calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-1")));
        return calendar.getTime();
    }

    /**
     * <p>
     * 根据输入日期得到Calendar
     * </p>
     *
     * @param date
     *            格式需为yyyy-MM-dd
     * @return
     */
    public static Calendar getCalendarByDate(String date) {
        if (date == null || "".equals(date)) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(parseDate(date));
        return calendar;

    }

    /**
     * <p>
     * 判断两个日期的大小关系，只比较年月日
     * </p>
     *
     * @param d1
     * @param d2
     * @return -1 d1在d2之前, 0 d1与d2相等, 1 d1在d2之后
     */
    public static int compareDateYmd(Date d1, Date d2) {
        String d1s = formatDate(d1);
        String d2s = formatDate(d2);
        return d1s.equals(d2s) ? 0 : (parseDate(d1s).before(parseDate(d2s)) ? -1 : 1);
    }

    /**
     * 得到输入日期的前i个月,-1表示前一个月，-2表示前两个月
     *
     * @param date
     * @param i
     * @return
     */
    public static String getLastIMonth(Date date, int i, String pattern) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, i);
        return formatDate(calendar.getTime(), pattern);

    }

    /**
     * <p>
     * 得到输入日期的前i个月,-1表示前一个月，-2表示前两个月,输出格式为yyyy-MM
     * </p>
     *
     * @param date
     * @param i
     * @return
     */
    public static String getLastIMonth(Date date, int i) {
        return getLastIMonth(date, i, PATTERN_YEAR_MONTH);
    }

    /**
     *
     * <p>
     * 得到一个月份的开始或结束日期,0为开始，其他为结束
     * </p>
     *
     * @param datestr
     * @param pattern
     * @param startOrEnd
     * @return
     */
    public static String getStartOrEndOfMonth(String datestr, String pattern, int startOrEnd) {
        Date date = parseDate(datestr, pattern);
        return getStartOrEndOfMonth(date, startOrEnd);
    }

    /**
     *
     * <p>
     * 得到一个月份的开始或结束日期,0为开始，其他为结束.比如说输入2009-9或2009-9-9，得到开始为20090901，结束日期为2009 0930
     * </p>
     *
     * @param date
     * @param startOrEnd
     * @return
     */
    public static String getStartOrEndOfMonth(Date date, int startOrEnd) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        if (startOrEnd == 0) {
            calendar.set(Calendar.DAY_OF_MONTH, 1);
        } else {
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        }
        return formatDate(calendar.getTime(), PATTERN_DATE_COMPACT);
    }

    /**
     * <p>
     * 计算日期年、月、日的增加数，以yyyy年M月d日的格式返回
     * </p>
     *
     * @param date

     * @return String
     */
    public static String getAddDate(Date date, int timeUnit, int amount) {

        return formatDate(accountDate(date, timeUnit, amount), PATTERN_YEAR_MONTH_DAY_C);
    }

    /**
     * 检查字段是否为日期
     *
     * @return
     */
    public Boolean isDate() {

        return false;
    }

    /**
     * 得到当前日期前六个月,跨月
     *
     * @return
     */
    public static String[] getDateBeforeSixDate() {
        StringBuffer sb = new StringBuffer();
        for (int i = 1; i <= 6; i++) {
            sb.append(getLastIMonth(new Date(), -i, PATTERN_YEARMONTH)).append(",");
        }
        String mons = sb.toString();
        String[] tmons = mons.substring(0, mons.length() - 1).split(",");
        Arrays.sort(tmons);
        return tmons; // 进行排序
    }

    /**
     * 得到当前日期前12个月,跨月
     *
     * @return
     */
    public static String[] getDateBefore12Date() {
        StringBuffer sb = new StringBuffer();
        for (int i = 1; i <= 12; i++) {
            sb.append(getLastIMonth(new Date(), -i, PATTERN_YEARMONTH)).append(",");
        }
        String mons = sb.toString();
        String[] tmons = mons.substring(0, mons.length() - 1).split(",");
        Arrays.sort(tmons);
        return tmons; // 进行排序
    }

    /**
     * 得到前12个月,包含当月,跨月
     *
     * @return
     */
    public static String[] getDateBefore12Date(Date date) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i <= 11; i++) {
            sb.append(getLastIMonth(date, -i, PATTERN_YEARMONTH)).append(",");
        }
        String mons = sb.toString();
        String[] tmons = mons.substring(0, mons.length() - 1).split(",");
        Arrays.sort(tmons);
        return tmons; // 进行排序
    }

    /**
     * 得到当前日期当本年01月,跨月
     *
     * @return
     */
    public static String[] getDateBefore01Date(String dataTime) {
        int len = getDiscrepantMonthI(parseDate(dataTime.substring(0, 4) + "-01", "yyyy-MM"),
                parseDate(dataTime, "yyyy-MM"));
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i <= len; i++) {
            sb.append(getLastIMonth(parseDate(dataTime, "yyyy-MM"), -i, PATTERN_YEARMONTH)).append(",");
        }
        String mons = sb.toString();
        String[] tmons = mons.substring(0, mons.length() - 1).split(",");
        Arrays.sort(tmons);
        return tmons; // 进行排序
    }

    /**
     * 获得指定日期的前i天
     *
     * @param date
     * @return
     */
    public static String getDayBefore(Date date, String pattern, int i) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_WEEK, i);
        String dayBefore = new SimpleDateFormat(pattern).format(calendar.getTime());
        return dayBefore;
    }

    /**
     * 得到当前天的前七天，isCrossMon 是否跨月 前一天为-1,前两天-2,以此类推
     *
     * @return
     */
    public static String[] getWeekDayBefore(boolean isCrossMon) {
        Date date = new Date();
        int day = 7;
        if (!isCrossMon) { // 不跨月
            String d = formatDate(date, DateTimeUtil.PATTERN_DATE_COMPACT);
            String dayString = d.substring(6);
            if (dayString.startsWith("0")) {
                int acDay = Integer.valueOf(dayString.substring(1));
                if (acDay < 8 && acDay != 1) {
                    day = acDay - 1;
                }
            }
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 1; i <= day; i++) {
            sb.append(getDayBefore(date,DateTimeUtil.PATTERN_DATE_COMPACT, -i)).append(",");
        }
        String days = sb.toString();
        String[] ds = days.substring(0, days.length() - 1).split(",");
        Arrays.sort(ds);
        return ds;
    }

    public static String getJiduNo(String month) {

        int monthNo = Integer.parseInt(month);
        if (monthNo <= 3) {
            return "4";
        }

        if (monthNo % 3 == 0) {

            return String.valueOf(Math.floorDiv(monthNo, 3) - 1);

        } else {

            return String.valueOf(Math.floorDiv(monthNo, 3));

        }
    }

    public static String getJiduEndDay(String year, String jiduNo) {

        StringBuffer sb = new StringBuffer(year);
        if ("1".equals(jiduNo)) {
            sb.append("0331");
        } else if ("2".equals(jiduNo)) {
            sb.append("0630");
        } else if ("3".equals(jiduNo)) {
            sb.append("0930");
        } else if ("4".equals(jiduNo)) {
            sb.append("1231");
        } else {
            return "";
        }
        return sb.toString();
    }

    // 返回本季度末日期
    public static String getJiduDay(String datestr, String pattern) {

        Calendar calendar = new GregorianCalendar();
        calendar.setTime(parseDate(datestr, pattern));
        int month = getQuarterInMonth(calendar.get(Calendar.MONTH), false);
        calendar.set(Calendar.MONTH, month + 1);
        calendar.set(Calendar.DAY_OF_MONTH, 0);
        return formatDate(calendar.getTime(), pattern);

    }

    private static int getQuarterInMonth(int month, boolean isQuarterStart) {
        int[] months = { 1, 4, 7, 10 };
        if (!isQuarterStart) {
            months = new int[] { 3, 6, 9, 12 };
        }
        int res = 0;
        if (month >= 2 && month <= 4) {
            res = months[0];
        } else if (month >= 5 && month <= 7) {
            res = months[1];
        } else if (month >= 8 && month <= 10) {
            res = months[2];
        } else {
            res = months[3];
        }
        return res;
    }

    /**
     * 获取当前月第一天
     * @return
     */
    public static String monthFirstDate(){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MONTH, 0);
        c.set(Calendar.DAY_OF_MONTH,1);//设置为1号,当前日期既为本月第一天
        String firstDate = format.format(c.getTime());
        return firstDate;
    }

    /**
     * 获取当前月最后一天
     * @return
     */
    public static String monthLastDate(){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar ca = Calendar.getInstance();
        ca.set(Calendar.DAY_OF_MONTH, ca.getActualMaximum(Calendar.DAY_OF_MONTH));
        String lastDate = format.format(ca.getTime());
        return lastDate;
    }

    /**
     * 获取上几月
     * @param month
     * @param lastMonth
     * @return
     */
    public static String getLastMonth(String month,int lastMonth){
        String newnewDate ="";
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMM");
            Date newdate = simpleDateFormat.parse(month);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(newdate);
            calendar.add(Calendar.MONTH,lastMonth);
            newnewDate = simpleDateFormat.format(calendar.getTime());
            newnewDate = newnewDate.substring(4,6);
            int iNewDate = Integer.valueOf(newnewDate);
            if(iNewDate <10){
                newnewDate = newnewDate.substring(1,2);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return newnewDate;
    }

    /**
     * 获取前几年
     * @param year
     * @param lastYear
     * @return
     */
    public static String getLastYear(String year,int lastYear){
        String newnewDate ="";
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy");
            Date newdate = simpleDateFormat.parse(year);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(newdate);
            calendar.add(Calendar.YEAR,lastYear);
            newnewDate = simpleDateFormat.format(calendar.getTime());
        }catch (Exception e){
            e.printStackTrace();
        }
        return newnewDate;
    }

    /**
     * 获取季度
     * @param accountPeriod
     * @return
     */
    public static String getJidu(String accountPeriod){
        int iMonth = Integer.parseInt(accountPeriod.substring(4));
        String quarter ="";
        if (iMonth <= 3) {
            quarter = "一季度";
        } else if (iMonth <= 6) {
            quarter = "二季度";
        } else if (iMonth <= 9) {
            quarter = "三季度";
        } else {
            quarter = "四季度";
        }
        return  quarter;
    }

    public static String getWeeksInMonthOfDate(String strDate,int offSet){
        Date date = DateUtil.parse(strDate);
        Date date2 = DateUtil.offsetDay(date, offSet);
        String dateStr = DateUtil.format(date2, "yyyyMMdd");
        System.out.println(dateStr);


        SimpleDateFormat df=new SimpleDateFormat("yyyyMMdd");
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(df.parse(dateStr));
        } catch (Exception e) {
            e.printStackTrace();
        }
        //设置每周第一天为周一 默认每周第一天为周日
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        //获取当前日期所在周周日
        calendar.set(Calendar.DAY_OF_WEEK,Calendar.SUNDAY);
        /*return String.valueOf(calendar.get(Calendar.YEAR)).concat("年").
                concat(String.valueOf(calendar.get(Calendar.MONTH)+1)).concat("月第").
                concat(String.valueOf(calendar.get(Calendar.WEEK_OF_MONTH))).concat("周");*/
        return String.valueOf(calendar.get(Calendar.MONTH)+1).concat("月第").
                concat(String.valueOf(calendar.get(Calendar.WEEK_OF_MONTH))).concat("周");
    }

    public static String getWeekOfMonth(String strDate,int offSet) throws Exception {
        Date date = DateUtil.parse(strDate);
        Date date2 = DateUtil.offsetDay(date, offSet);
        String dateStr = DateUtil.format(date2, "yyyyMMdd");

        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        Date date3 = format.parse(dateStr);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date3);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String firstDayOfMonth = year + "-" + month + "-01";
        Date firstDate = df.parse(firstDayOfMonth);
        calendar.setTime(firstDate);
        int days = calendar.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY;
        int diff = calendar.getActualMaximum(Calendar.DAY_OF_MONTH) - days;
        int weeks = (diff + 6) / 7 + 1;
        int intervalDays = (int) ((date3.getTime() - firstDate.getTime()) / (1000 * 60 * 60 * 24));
        int weekOfMonth = intervalDays / 7 + 1;
        return weekOfMonth+"";
    }


    public static  String getWeekByDate(String strDate,String week){
        String subStrDate = strDate.substring(0,6);
        subStrDate = subStrDate+"01";

        String strMonth = strDate.substring(4,6);
        int iMonth = Integer.parseInt(strMonth);
        int lastMonth = iMonth-1;
        if(lastMonth ==0){
            lastMonth = 12;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate date = LocalDate.parse(subStrDate, formatter);

        //LocalDate date = LocalDate.now(); // 获取当前日期
        List<String> thursdays = new ArrayList<>();

        // 循环直到下个月的第一天
        /*while (date.getMonth() == LocalDate.now().withDayOfMonth(1).getMonth()) {
            if (date.getDayOfWeek() == java.time.DayOfWeek.THURSDAY) {
                thursdays.add(date.format(formatter));
            }
            date = date.plusDays(1); // 增加一天
        }*/

        // 获取当月最后一天
        LocalDate lastDayOfMonth = date.with(TemporalAdjusters.lastDayOfMonth());

        while (date.isBefore(lastDayOfMonth) || date.isEqual(lastDayOfMonth)) {
            if (date.getDayOfWeek() == java.time.DayOfWeek.THURSDAY) {
                thursdays.add(date.format(formatter));
            }

            date = date.plusDays(1); // 增加一天
        }

        int i =1;
        int index =0;
        for (String thursday : thursdays) {
            if(thursday.equals(strDate)){
                index = i;
            }
            i++;
        }

        String retStr = "";

        //System.out.println("thursdays-size: "+thursdays.size()+" --index: "+index);
        if(thursdays.size() >4){
            if(index ==5){
                if(week.equals("W周")){
                    retStr = iMonth+"月第4周";
                }else if(week.equals("W_1周")){
                    retStr = iMonth+"月第3周";
                }else if(week.equals("W_2周")){
                    retStr = iMonth+"月第2周";
                }else if(week.equals("W_3周")){
                    retStr = iMonth+"月第1周";
                }
            }else if(index ==4){
                if(week.equals("W周")){
                    retStr = iMonth+"月第3周";
                }else if(week.equals("W_1周")){
                    retStr = iMonth+"月第2周";
                }else if(week.equals("W_2周")){
                    retStr = iMonth+"月第1周";
                }else if(week.equals("W_3周")){
                    retStr = lastMonth+"月第4周";
                }
            }else if(index ==3){
                if(week.equals("W周")){
                    retStr = iMonth+"月第2周";
                }else if(week.equals("W_1周")){
                    retStr = iMonth+"月第1周";
                }else if(week.equals("W_2周")){
                    retStr = lastMonth+"月第4周";
                }else if(week.equals("W_3周")){
                    retStr = lastMonth+"月第3周";
                }
            }else if(index ==2){
                if(week.equals("W周")){
                    retStr = iMonth+"月第1周";
                }else if(week.equals("W_1周")){
                    retStr = lastMonth+"月第4周";
                }else if(week.equals("W_2周")){
                    retStr = lastMonth+"月第3周";
                }else if(week.equals("W_3周")){
                    retStr = lastMonth+"月第2周";
                }
            }else if(index ==1){
                if(week.equals("W周")){
                    retStr = lastMonth+"月第4周";
                }else if(week.equals("W_1周")){
                    retStr = lastMonth+"月第3周";
                }else if(week.equals("W_2周")){
                    retStr = lastMonth+"月第2周";
                }else if(week.equals("W_3周")){
                    retStr = lastMonth+"月第1周";
                }
            }
        }else{
            if(index ==4){
                if(week.equals("W周")){
                    retStr = iMonth+"月第4周";
                }else if(week.equals("W_1周")){
                    retStr = iMonth+"月第3周";
                }else if(week.equals("W_2周")){
                    retStr = iMonth+"月第2周";
                }else if(week.equals("W_3周")){
                    retStr = iMonth+"月第1周";
                }
            }else if(index ==3){
                if(week.equals("W周")){
                    retStr = iMonth+"月第3周";
                }else if(week.equals("W_1周")){
                    retStr = iMonth+"月第2周";
                }else if(week.equals("W_2周")){
                    retStr = iMonth+"月第1周";
                }else if(week.equals("W_3周")){
                    retStr = lastMonth+"月第4周";
                }
            }else if(index ==2){
                if(week.equals("W周")){
                    retStr = iMonth+"月第2周";
                }else if(week.equals("W_1周")){
                    retStr = iMonth+"月第1周";
                }else if(week.equals("W_2周")){
                    retStr = lastMonth+"月第4周";
                }else if(week.equals("W_3周")){
                    retStr = lastMonth+"月第3周";
                }
            }else if(index ==1){
                if(week.equals("W周")){
                    retStr = iMonth+"月第1周";
                }else if(week.equals("W_1周")){
                    retStr = lastMonth+"月第4周";
                }else if(week.equals("W_2周")){
                    retStr = lastMonth+"月第3周";
                }else if(week.equals("W_3周")){
                    retStr = lastMonth+"月第2周";
                }
            }
        }

        return retStr;
    }


    /**
     * 获取当前时间减相关天数的月份
     * @param iDate
     * @return
     */
    public static String getCurrMonthByDay(int iDate){
        String newMonth ="";
        try {

            LocalDate today = LocalDate.now(); // 获取当前日期
            LocalDate twoDaysBefore = today.minusDays(iDate); // 当前日期前几天
            int month = twoDaysBefore.getMonthValue();
            if(month <10){
                newMonth = "0"+month;
            }else{
                newMonth = month+"";
            }

            /*System.out.println("今天的日期: " + today);
            System.out.println("两天前的日期: " + twoDaysBefore);
            System.out.println("newMonth: " + newMonth);*/

        }catch (Exception e){
            e.printStackTrace();
        }
        return newMonth;
    }


    /**
     * 获取当前日期的前几天
     * @param iDate
     * @return
     */
    public static String getCurrDateBefore(String format,int iDate){
        String retDate ="";
        try {

            // 获取当前日期
            Calendar calendar = Calendar.getInstance();
            //
            calendar.add(Calendar.DAY_OF_YEAR, iDate);
            Date twoDaysAgo = calendar.getTime();

            // 定义日期格式（按需调整）
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            retDate = sdf.format(twoDaysAgo);

        }catch (Exception e){
            e.printStackTrace();
        }
        return retDate;
    }

    /**
     * 获取当前月的前几月
     * @param iMonth
     * @return
     */
    public static String getCurrMonthBefore(String format,int iMonth){
        String retDate ="";
        try {

            Calendar calendar = Calendar.getInstance();
            // 减去两个月
            calendar.add(Calendar.MONTH, iMonth);

            // 格式化为字符串
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            retDate = sdf.format(calendar.getTime());

        }catch (Exception e){
            e.printStackTrace();
        }
        return retDate;
    }

    /**
     * 获取指定字符串日期的前后几天
     * @param strDate
     * @param format
     * @param iDate
     * @return
     */
    public static String getStrDateBefore(String strDate,String format,int iDate){
        String retDate ="";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);

        // 2. 解析字符串为 LocalDate
        LocalDate specifiedDate = LocalDate.parse(strDate, formatter);
        LocalDate previousDay = specifiedDate.minusDays(iDate);
        retDate = previousDay.format(formatter);
        return  retDate;
    }

    /**
     * 获取指定字符串月份的前后几月
     * @param strDate
     * @param format
     * @param iMonth
     * @return
     */
    public static String getStrMonthBefore(String strDate,String format,int iMonth){
        String retDate ="";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        YearMonth yearMonth = YearMonth.parse(strDate, formatter);
        YearMonth previousYearMonth = yearMonth.minusMonths(iMonth);
        retDate = previousYearMonth.format(formatter);

        return  retDate;
    }




}
