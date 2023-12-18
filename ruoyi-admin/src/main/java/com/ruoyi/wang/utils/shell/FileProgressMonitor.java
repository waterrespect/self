package com.ruoyi.wang.utils.shell;

import com.jcraft.jsch.SftpProgressMonitor;

import java.text.DecimalFormat;
import java.util.TimerTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @projectName: RuoYi-Vue
 * @package: com.ruoyi.wang.utils.shell
 * @className: FileProgressMonitor 监控上传进度
 * @author: wang
 * @description: TODO
 * @date: 2023/11/14 22:23
 * @version: 1.0
 */
public class FileProgressMonitor extends TimerTask implements SftpProgressMonitor {

    private boolean isEnd = false;

    private long transfered;

    private long fileSize;

    private ScheduledExecutorService executorService;

    private boolean isScheduled = false;

    long startTime = 0L;

    public FileProgressMonitor(long fileSize) {
        this.fileSize = fileSize;
    }

    @Override
    public void run() {
        if (!isEnd()) {
            System.out.println("Transfering is in progress.");
            long transfered = getTransfered();
            // 判断当前已传输数据大小是否等于文件总大小
            if (transfered != fileSize) {
                System.out.println("Current transfered: "+transfered +" bytes");
                sendProgressMessage(transfered);
            } else {
                // 如果当前已传输数据大小等于文件总大小，说明已完成，设置end
                System.out.println("File transfering is done.");
                setEnd(true);
            }
        } else {
            System.out.println("Transfering done. Cancel timer.");
            // 如果传输结束，停止timer记时器
            stop();
            return;
        }
    }

    /**
     * 实现了SftpProgressMonitor接口的count方法
     */
    @Override
    public boolean count(long count) {
        if (isEnd()) {
            return false;
        }
        if (!isScheduled) {
            start();
        }
        add(count);
        return true;
    }

    /**
     * 实现了SftpProgressMonitor接口的end方法
     */
    @Override
    public void end() {
        setEnd(true);
        System.out.println("transfering end. time ->" +(System.currentTimeMillis() - startTime) / 1000+" s");
    }

    @Override
    public void init(int op, String src, String dest, long max) {
        startTime = System.currentTimeMillis();
    }

    public void stop() {
        System.out.println("Try to stop progress monitor.");
        boolean isShutdown = executorService.isShutdown();
        if (!isShutdown) {
            executorService.shutdown();
        }
        System.out.println("Progress monitor stoped.");
    }

    public void start() {
        System.out.println("Try to start progress monitor.");
        executorService = new ScheduledThreadPoolExecutor(1);
        //1秒钟后开始执行，每2杪钟执行一次
        executorService.scheduleWithFixedDelay(this, 1, 1, TimeUnit.SECONDS);
        isScheduled = true;
        System.out.println("Progress monitor started.");
    }

    /**
     * 打印progress信息
     *
     * @param transfered
     */
    private void sendProgressMessage(long transfered) {
        if (fileSize != 0) {
            double d = ((double) transfered * 100) / (double) fileSize;
            DecimalFormat df = new DecimalFormat("#.##");
            System.out.println("Sending progress message: "+df.format(d)+" %");
        } else {
            System.out.println("Sending progress message: " + transfered);
        }
    }

    private synchronized void add(long count) {
        transfered = transfered + count;
    }

    private synchronized long getTransfered() {
        return transfered;
    }

    public synchronized void setTransfered(long transfered) {
        this.transfered = transfered;
    }

    private synchronized void setEnd(boolean isEnd) {
        this.isEnd = isEnd;
    }

    private synchronized boolean isEnd() {
        return isEnd;
    }
}
