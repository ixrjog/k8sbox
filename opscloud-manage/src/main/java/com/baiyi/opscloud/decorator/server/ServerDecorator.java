package com.baiyi.opscloud.decorator.server;

import com.baiyi.opscloud.common.util.BeanCopierUtils;
import com.baiyi.opscloud.domain.generator.opscloud.OcEnv;
import com.baiyi.opscloud.domain.generator.opscloud.OcServerGroup;
import com.baiyi.opscloud.domain.vo.env.EnvVO;
import com.baiyi.opscloud.domain.vo.server.ServerGroupVO;
import com.baiyi.opscloud.domain.vo.server.ServerVO;
import com.baiyi.opscloud.service.env.OcEnvService;
import com.baiyi.opscloud.service.server.OcServerGroupService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @Author baiyi
 * @Date 2020/2/22 10:46 上午
 * @Version 1.0
 */
@Component("ServerDecorator")
public class ServerDecorator {

    @Resource
    private OcEnvService ocEnvService;

    @Resource
    private OcServerGroupService ocServerGroupService;

    @Resource
    private ServerGroupDecorator serverGroupDecorator;

    public ServerVO.Server decorator(ServerVO.Server server) {
        // 装饰 环境信息
        OcEnv ocEnv = ocEnvService.queryOcEnvByType(server.getEnvType());
        if (ocEnv != null) {
            EnvVO.Env env = BeanCopierUtils.copyProperties(ocEnv, EnvVO.Env.class);
            server.setEnv(env);
        }
        // 装饰 服务器组信息
        OcServerGroup ocServerGroup = ocServerGroupService.queryOcServerGroupById(server.getServerGroupId());
        if (ocServerGroup != null) {
            ServerGroupVO.ServerGroup serverGroup = BeanCopierUtils.copyProperties(ocServerGroup, ServerGroupVO.ServerGroup.class);
            server.setServerGroup(serverGroupDecorator.decorator(serverGroup));
        }

        return server;
    }

}
