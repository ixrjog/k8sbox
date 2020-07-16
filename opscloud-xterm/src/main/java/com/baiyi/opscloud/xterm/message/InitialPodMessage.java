package com.baiyi.opscloud.xterm.message;

import lombok.Data;

/**
 * @Author baiyi
 * @Date 2020/7/14 2:36 下午
 * @Version 1.0
 */
@Data
public class InitialPodMessage extends BaseMessage {

    // serverName
    private String instanceId;

    private String ip;

}