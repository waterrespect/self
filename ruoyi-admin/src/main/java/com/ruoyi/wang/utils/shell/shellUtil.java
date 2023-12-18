package com.ruoyi.wang.utils.shell;

import com.jcraft.jsch.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * @projectName: RuoYi-Vue
 * @package: com.ruoyi.wang.utils.shell
 * @className: shellUtil
 * @author: wang
 * @description: TODO
 * @date: 2023/11/14 22:22
 * @version: 1.0
 */
@Component
@Scope(value = "prototype")
public class shellUtil {

    @Value("no")
    private String strictHostKeyChecking;

    @Value("3000")
    private Integer timeout;

    private Session session;

    private Channel channel;

    private ChannelExec channelExec;

    private ChannelSftp channelSftp;

    private ChannelShell channelShell;

    /**
     * 初始化
     *
     * @param ip       远程主机IP地址
     * @param port     远程主机端口
     * @param username 远程主机登陆用户名
     * @param password 远程主机登陆密码
     * @throws JSchException JSch异常
     */
    public Boolean init(String ip, Integer port, String username, String password){
        System.out.println(ip + port + username + password);
        JSch jsch = new JSch();
        try {
            session = jsch.getSession(username, ip, port);
            session.setPassword(password);
            Properties sshConfig = new Properties();
            sshConfig.put("StrictHostKeyChecking", "no");
            session.setConfig(sshConfig);
            session.connect(3000);
            System.out.println("Session connected!");
        } catch (JSchException e) {
            System.out.println("Session connected fail!");
            return false;
        }
        return true;
    }

    public Boolean init(String ip, String username, String password) {
        return init(ip, 22, username, password);
    }

    public Boolean init() {
        String ip = "120.79.189.113";
        Integer port = 22;
        String username = "root";
        String password = "Gahulv#135396868";
        return init(ip, port, username, password);
    }

    /**
     * 连接多次执行命令，执行命令完毕后需要执行close()方法
     *
     * @param command 需要执行的指令
     * @return 执行结果
     * @throws Exception 没有执行初始化
     */
    public String execCmd(String command) throws Exception {
        initChannelExec();
        System.out.println("execCmd command - >" + command);
        channelExec.setCommand(command);
        channel.setInputStream(null);
        channelExec.setErrStream(System.err);
        channel.connect();
        StringBuilder sb = new StringBuilder(16);
        try (InputStream in = channelExec.getInputStream();
             InputStreamReader isr = new InputStreamReader(in, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(isr)) {
            String buffer;
            while ((buffer = reader.readLine()) != null) {
                sb.append("\n").append(buffer);
            }
            System.out.println("execCmd result - > "+ sb);
            return sb.toString();
        }
    }


    /**
     * 执行命令关闭连接
     *
     * @param command 需要执行的指令
     * @return 执行结果
     * @throws Exception 没有执行初始化
     */
    public String execCmdAndClose(String command) throws Exception {
        String result = execCmd(command);
        close();
        return result;
    }

    /**
     * 执行复杂shell命令
     *
     * @param cmds 多条命令
     * @return 执行结果
     * @throws Exception 连接异常
     */
    public String execCmdByShell(String... cmds) throws Exception {
        return execCmdByShell(Arrays.asList(cmds));
    }

    /**
     * 执行复杂shell命令
     * @param cmds 多条命令
     * @return 执行结果
     * @throws Exception 连接异常
     */
    public String execCmdByShell(List<String> cmds) throws Exception {
        String result = "";
        initChannelShell();
        InputStream inputStream = channelShell.getInputStream();
        channelShell.setPty(true);
        channelShell.connect();

        OutputStream outputStream = channelShell.getOutputStream();
        PrintWriter printWriter = new PrintWriter(outputStream);
        for (String cmd : cmds) {
            printWriter.println(cmd);
        }
        printWriter.flush();

        byte[] tmp = new byte[1024];
        while (true) {
            while (inputStream.available() > 0) {
                int i = inputStream.read(tmp, 0, 1024);
                if (i < 0) {
                    break;
                }
                String s = new String(tmp, 0, i);
                if (s.contains("--More--")) {
                    outputStream.write((" ").getBytes());
                    outputStream.flush();
                }
                System.out.println(s);
            }
            if (channelShell.isClosed()) {
                System.out.println("exit-status:" + channelShell.getExitStatus());
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        outputStream.close();
        inputStream.close();
        return result;
    }

//TODO 文件上传
    /**
     * SFTP文件上传（路径）
     *
     * @param src 源地址
     * @param dst 目的地址
     * @throws Exception 上传文件失败
     */
    public void putAndClose(String src, String dst) throws Exception {
        putAndClose(src, dst, ChannelSftp.OVERWRITE);
    }

    /**
     * SFTP文件上传
     *
     * @param src  源地址
     * @param dst  目的地址
     * @param mode 上传模式 默认为ChannelSftp.OVERWRITE
     * @throws Exception 上传文件失败
     */
    public void putAndClose(String src, String dst, int mode) throws Exception {
        put(src, dst, mode);
        close();
    }

    public void put(String src, String dst) throws Exception {
        put(src, dst, ChannelSftp.OVERWRITE);
    }

    public void put(String src, String dst, int mode) throws Exception {
        initChannelSftp();
        System.out.println("Upload File "+ src +"->"+ dst);
        channelSftp.put(src, dst, mode);
        System.out.println("Upload File Success!");
    }

    /**
     * SFTP文件上传并监控上传进度
     *
     * @param src 源地址
     * @param dst 目的地址
     * @throws Exception 上传文件失败
     */
    public void putMonitorAndClose(String src, String dst) throws Exception {
        putMonitorAndClose(src, dst, ChannelSftp.OVERWRITE);
    }

    /**
     * SFTP文件上传并监控上传进度
     *
     * @param src  源地址
     * @param dst  目的地址
     * @param mode 上传模式 默认为ChannelSftp.OVERWRITE
     * @throws Exception 上传文件失败
     */
    public void putMonitorAndClose(String src, String dst, int mode) throws Exception {
        initChannelSftp();
        FileProgressMonitor monitor = new FileProgressMonitor(new File(src).length());
        System.out.println("Upload File "+src+" -> "+dst);
        channelSftp.put(src, dst, monitor, mode);
        System.out.println("Upload File Success!");
        close();
    }

    /**
     * 传输文件
     * @param file
     * @param dst
     * @param mode
     * OVERWRITE = 0
     * RESUME = 1;
     * APPEND = 2;
     */
    public Boolean uploadAndClose(MultipartFile file, String dst, int mode) throws Exception {
        Boolean upload = upload(file, dst, mode);
        close();
        return upload;
    }

    /**
     * 传输文件
     * @param file
     * @param dst
     */
    public Boolean uploadAndClose(MultipartFile file, String dst) throws Exception {
        Boolean upload = upload(file, dst, ChannelSftp.OVERWRITE);
        close();
        return upload;
    }

    /**
     *
     * @param file
     * @param dst
     * @param mode
     */
    public Boolean upload(MultipartFile file, String dst, int mode) {
        try {
            initChannelSftp();
            FileProgressMonitor monitor = new FileProgressMonitor(file.getSize());
            byte [] byteArr = file.getBytes();
            InputStream src = new ByteArrayInputStream(byteArr);
            channelSftp.put(src, dst, monitor, mode);
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
        return true;
    }

//TODO 文件下载
    /**
     * SFTP文件下载
     *
     * @param src 源文件地址
     * @param dst 目的地址
     * @throws Exception 下载文件失败
     */
    public void getAndClose(String src, String dst) throws Exception {
        get(src,dst);
        close();
    }

    public void get(String src, String dst) throws Exception {
        initChannelSftp();
        System.out.println("Download File "+src+" -> "+dst);
        channelSftp.get(src, dst);
        System.out.println("Download File Success!");
    }

    /**
     * SFTP文件下载并监控下载进度
     *
     * @param src 源文件地址
     * @param dst 目的地址
     * @throws Exception 下载文件失败
     */
    public void getMonitorAndClose(String src, String dst) throws Exception {
        initChannelSftp();
        FileProgressMonitor monitor = new FileProgressMonitor(new File(src).length());
        System.out.println("Download File "+src+" -> "+dst);
        channelSftp.get(src, dst, monitor);
        System.out.println("Download File Success!");
        close();
    }

    /**
     * 删除指定目录文件
     *
     * @param path 删除路径
     * @throws Exception 远程主机连接异常
     */
    public void deleteFile(String path) throws Exception {
        initChannelSftp();
        channelSftp.rm(path);
        System.out.println("Delete File "+ path);
    }

    /**
     * 删除指定目录
     *
     * @param path 删除路径
     * @throws Exception 远程主机连接异常
     */
    public void deleteDir(String path) throws Exception {
        initChannelSftp();
        channelSftp.rmdir(path);
        System.out.println("Delete Dir " + path);
    }

    /**
     * 释放资源
     */
    public void close() {
        if (channelSftp != null && channelSftp.isConnected()) {
            channelSftp.disconnect();
        }
        if (channelExec != null && channelExec.isConnected()) {
            channelExec.disconnect();
        }
        if (channel != null && channel.isConnected()) {
            channel.disconnect();
        }
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
    }

    private void initChannelSftp() throws Exception {
        channel = session.openChannel("sftp");
        channel.connect(); // 建立SFTP通道的连接
        channelSftp = (ChannelSftp) channel;
        if (session == null || channel == null || channelSftp == null) {
            System.out.println("请先执行init()");
            throw new Exception("请先执行init()");
        }
    }

    private void initChannelExec() throws Exception {
        // 打开执行shell指令的通道
        channel = session.openChannel("exec");
        channelExec = (ChannelExec) channel;
        if (session == null || channel == null || channelExec == null) {
            System.out.println("请先执行init()");
            throw new Exception("请先执行init()");
        }
    }

    private void initChannelShell() throws Exception {
        // 打开执行shell指令的通道
        channel = session.openChannel("shell");
        channelShell = (ChannelShell) channel;
        if (session == null || channel == null || channelShell == null) {
            System.out.println("请先执行init()");
            throw new Exception("请先执行init()");
        }
    }


}
