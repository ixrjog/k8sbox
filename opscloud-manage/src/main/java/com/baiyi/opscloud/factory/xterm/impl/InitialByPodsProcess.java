package com.baiyi.opscloud.factory.xterm.impl;

import com.baiyi.opscloud.builder.TerminalSessionInstanceBuilder;
import com.baiyi.opscloud.common.base.XTermRequestStatus;
import com.baiyi.opscloud.domain.generator.opscloud.OcTerminalSession;
import com.baiyi.opscloud.domain.generator.opscloud.OcUser;
import com.baiyi.opscloud.factory.xterm.IXTermProcess;
import com.baiyi.opscloud.xterm.handler.RemoteInvokeHandler;
import com.baiyi.opscloud.xterm.message.BaseMessage;
import com.baiyi.opscloud.xterm.message.InitialIpMessage;
import com.baiyi.opscloud.xterm.model.HostSystem;
import com.google.gson.GsonBuilder;
import org.springframework.stereotype.Component;

import javax.websocket.Session;

/**
 * @Author baiyi
 * @Date 2020/7/14 2:33 下午
 * @Version 1.0
 */
@Component
public class InitialByPodsProcess extends BaseProcess implements IXTermProcess {

    /**
     * 初始化XTerm
     *
     * @return
     */
    @Override
    public String getKey() {
        return XTermRequestStatus.INITIAL_IP.getCode();
    }

    @Override
    public void xtermProcess(String message, Session session, OcTerminalSession ocTerminalSession) {
        InitialIpMessage xtermMessage = (InitialIpMessage) getMessage(message);
        xtermMessage.setLoginUserType(1);
        OcUser ocUser = userFacade.getOcUserBySession();
        String ip = xtermMessage.getIp();

        HostSystem hostSystem = buildHostSystem(ocUser, ip, xtermMessage);
        RemoteInvokeHandler.openSSHTermOnSystem(ocTerminalSession.getSessionId(), xtermMessage.getInstanceId(), hostSystem);
        heartbeat(ocTerminalSession.getSessionId());
        terminalFacade.addOcTerminalSessionInstance(TerminalSessionInstanceBuilder.build(ocTerminalSession, hostSystem));
    }

    @Override
    protected BaseMessage getMessage(String message) {
        return new GsonBuilder().create().fromJson(message, InitialIpMessage.class);
    }
}
