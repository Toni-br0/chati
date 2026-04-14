package com.pearadmin.common.tools.server;


import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.NumberUtil;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;
import oshi.util.GlobalConfig;
import oshi.util.Util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Properties;

public class SystemUtil {

    private static final int WAIT_SECOND = 110;

    public static CpuInfo getCpu() {
        CpuInfo cpu = new CpuInfo();
        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hal = si.getHardware();
        setCpuInfo(hal.getProcessor(), cpu);
        MemInfo memInfo = setMemInfo(hal.getMemory());
        cpu.setMemInfo(memInfo);
        setSysInfo(cpu);
        setJvmInfo(cpu);
        setSysFiles(si.getOperatingSystem(), cpu);
        return cpu;
    }

    private static void setSysInfo(CpuInfo cpu) {
        Properties props = System.getProperties();
        cpu.setSysInfoComputerName(getHostName());
        cpu.setSysInfoComputerIp(NetUtil.getLocalhostStr());
        cpu.setSysInfoOsName(props.getProperty("os.name"));
        cpu.setSysInfoOsArch(props.getProperty("os.arch"));
        cpu.setSysInfoUserDir(props.getProperty("user.dir"));
    }

    /**
     * 获取客户端主机名称
     */
    public static String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException ignored) {
        }
        return "未知";
    }

    /**
     * 字节转换
     *
     * @param size 字节大小
     * @return 转换后值
     */
    public static String convertFileSize(long size) {
        long kb = 1024;
        long mb = kb * 1024;
        long gb = mb * 1024;
        if (size >= gb) {
            return String.format("%.1f GB", (float) size / gb);
        } else if (size >= mb) {
            float f = (float) size / mb;
            return String.format(f > 100 ? "%.0f MB" : "%.1f MB", f);
        } else if (size >= kb) {
            float f = (float) size / kb;
            return String.format(f > 100 ? "%.0f KB" : "%.1f KB", f);
        } else {
            return String.format("%d B", size);
        }
    }

    /**
     * 设置磁盘信息
     */
    private static void setSysFiles(OperatingSystem os, CpuInfo cpu) {
        FileSystem fileSystem = os.getFileSystem();
        List<OSFileStore> fsArray = fileSystem.getFileStores();
        for (OSFileStore fs : fsArray) {
            long free = fs.getUsableSpace();
            long total = fs.getTotalSpace();
            long used = total - free;
            SysFileInfo sysFile = new SysFileInfo();
            sysFile.setDirName(fs.getMount());
            sysFile.setSysTypeName(fs.getType());
            sysFile.setTypeName(fs.getName());
            sysFile.setTotal(convertFileSize(total));
            sysFile.setFree(convertFileSize(free));
            sysFile.setUsed(convertFileSize(used));
            if (total == 0) {
                sysFile.setUsage(0);
            } else {
                sysFile.setUsage(NumberUtil.mul(NumberUtil.div(used, total, 4), 100));
            }
            cpu.getSysFiles().add(sysFile);
        }
    }

    /**
     * 设置Java虚拟机
     */
    private static void setJvmInfo(CpuInfo cpu) {
        Properties props = System.getProperties();
        cpu.setJvmInfoTotal(Runtime.getRuntime().totalMemory());
        cpu.setJvmInfoMax(Runtime.getRuntime().maxMemory());
        cpu.setJvmInfoFree(Runtime.getRuntime().freeMemory());
        cpu.setJvmInfoVersion(props.getProperty("java.version"));
        cpu.setJvmInfoHome(props.getProperty("java.home"));

    }

    /**
     * 设置内存信息
     */
    private static MemInfo setMemInfo(GlobalMemory memory) {
        MemInfo mem = new MemInfo();
        mem.setTotal(memory.getTotal());
        mem.setUsed(memory.getTotal() - memory.getAvailable());
        mem.setFree(memory.getAvailable());
        return mem;
    }

    /**
     * 设置CPU信息
     */
    private static void setCpuInfo(CentralProcessor processor, CpuInfo cpu) {
        // CPU信息
        /*long[] prevTicks = processor.getSystemCpuLoadTicks();
        Util.sleep(WAIT_SECOND);

        long[] ticks = processor.getSystemCpuLoadTicks();
        long nice = ticks[CentralProcessor.TickType.NICE.getIndex()] - prevTicks[CentralProcessor.TickType.NICE.getIndex()];
        long irq = ticks[CentralProcessor.TickType.IRQ.getIndex()] - prevTicks[CentralProcessor.TickType.IRQ.getIndex()];
        long softirq = ticks[CentralProcessor.TickType.SOFTIRQ.getIndex()] - prevTicks[CentralProcessor.TickType.SOFTIRQ.getIndex()];
        long steal = ticks[CentralProcessor.TickType.STEAL.getIndex()] - prevTicks[CentralProcessor.TickType.STEAL.getIndex()];
        long cSys = ticks[CentralProcessor.TickType.SYSTEM.getIndex()] - prevTicks[CentralProcessor.TickType.SYSTEM.getIndex()];
        long user = ticks[CentralProcessor.TickType.USER.getIndex()] - prevTicks[CentralProcessor.TickType.USER.getIndex()];
        long iowait = ticks[CentralProcessor.TickType.IOWAIT.getIndex()] - prevTicks[CentralProcessor.TickType.IOWAIT.getIndex()];
        long idle = ticks[CentralProcessor.TickType.IDLE.getIndex()] - prevTicks[CentralProcessor.TickType.IDLE.getIndex()];
        long totalCpu = user + nice + cSys + idle + iowait + irq + softirq + steal;
        cpu.setCpuNum(processor.getLogicalProcessorCount());
        cpu.setTotal(totalCpu);
        cpu.setSys(cSys);
        cpu.setUsed(user);
        cpu.setWait(iowait);
        cpu.setFree(idle);*/



        long[] prevTicks = processor.getSystemCpuLoadTicks();
        // 睡眠1s
        try {
            Thread.sleep(1000);
        }catch (Exception e){
            e.printStackTrace();
        }

        long[] ticks = processor.getSystemCpuLoadTicks();
        long nice = ticks[CentralProcessor.TickType.NICE.getIndex()] - prevTicks[CentralProcessor.TickType.NICE.getIndex()];
        long irq = ticks[CentralProcessor.TickType.IRQ.getIndex()] - prevTicks[CentralProcessor.TickType.IRQ.getIndex()];
        long softirq = ticks[CentralProcessor.TickType.SOFTIRQ.getIndex()] - prevTicks[CentralProcessor.TickType.SOFTIRQ.getIndex()];
        long steal = ticks[CentralProcessor.TickType.STEAL.getIndex()] - prevTicks[CentralProcessor.TickType.STEAL.getIndex()];
        long cSys = ticks[CentralProcessor.TickType.SYSTEM.getIndex()] - prevTicks[CentralProcessor.TickType.SYSTEM.getIndex()];
        long user = ticks[CentralProcessor.TickType.USER.getIndex()] - prevTicks[CentralProcessor.TickType.USER.getIndex()];
        long iowait = ticks[CentralProcessor.TickType.IOWAIT.getIndex()] - prevTicks[CentralProcessor.TickType.IOWAIT.getIndex()];
        long idle = ticks[CentralProcessor.TickType.IDLE.getIndex()] - prevTicks[CentralProcessor.TickType.IDLE.getIndex()];
        long totalCpu = user + nice + cSys + idle + iowait + irq + softirq + steal;
        /*System.err.println("cpu核数:" + processor.getLogicalProcessorCount());
        System.err.println("cpu系统使用率:" + new DecimalFormat("#.##%").format(cSys * 1.0 / totalCpu));
        System.err.println("cpu用户使用率:" + new DecimalFormat("#.##%").format(user * 1.0 / totalCpu));
        System.err.println("cpu当前等待率:" + new DecimalFormat("#.##%").format(iowait * 1.0 / totalCpu));
        System.err.println("cpu当前空闲率:" + new DecimalFormat("#.##%").format(idle * 1.0 / totalCpu));*/

        // todo cup利用
        //  user + system + nice + iowait + irq + softirq + steal
        long cpuUtilization = user + nice + cSys  + iowait + irq + softirq + steal;
        //System.err.println("cpu利用率：" + new DecimalFormat("#.##%").format(cpuUtilization * 1.0 / totalCpu));

        cpu.setCpuNum(processor.getLogicalProcessorCount());
        cpu.setSys(Math.round((cSys * 1.0 / totalCpu*100) * 100.0) / 100.0);
        cpu.setUsed(Math.round((user * 1.0 / totalCpu*100) * 100.0) / 100.0);
        cpu.setWait(Math.round((iowait * 1.0 / totalCpu*100) * 100.0) / 100.0);
        cpu.setFree(Math.round(((idle * 1.0 / totalCpu)*100) * 100.0) / 100.0);
        cpu.setTotal(Math.round(((cpuUtilization * 1.0 / totalCpu)*100) * 100.0) / 100.0);


    }
}
