package com.ruoyi.wang.task;

import com.ruoyi.wang.service.IAPostService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @projectName: RuoYi-Vue
 * @package: com.ruoyi.wang.task
 * @className: shellTask
 * @author: wang
 * @description: TODO
 * @date: 2023/11/14 22:25
 * @version: 1.0
 */
@Component("shellTask")
public class shellTask {
    @Resource
    private IAPostService iaPostService;

    public void postFile() throws Exception {
        String ip = "120.79.189.113";
        Integer port = 22;
        String username = "root";
        String pwd = "Gahulv#135396868";

        Boolean bool = iaPostService.shellPost(ip, port, username, pwd);
    }

    public void videoFile() {
        String ip = "120.79.189.113";
        Integer post = 22;
        String username = "root";
        String pwd = "Wang#135396868";
    }

    public void hexoDeploy() {
        iaPostService.hexoCleanD();
    }

}
