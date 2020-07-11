package com.baiyi.opscloud.facade.impl;

import com.baiyi.opscloud.builder.ServerBuilder;
import com.baiyi.opscloud.common.util.BeanCopierUtils;
import com.baiyi.opscloud.common.util.IDUtils;
import com.baiyi.opscloud.common.util.RegexUtils;
import com.baiyi.opscloud.decorator.server.ServerDecorator;
import com.baiyi.opscloud.domain.BusinessWrapper;
import com.baiyi.opscloud.domain.DataTable;
import com.baiyi.opscloud.domain.ErrorEnum;
import com.baiyi.opscloud.domain.generator.opscloud.OcServer;
import com.baiyi.opscloud.domain.generator.opscloud.OcServerGroup;
import com.baiyi.opscloud.domain.param.server.ServerParam;
import com.baiyi.opscloud.domain.vo.server.ServerVO;
import com.baiyi.opscloud.facade.ServerCacheFacade;
import com.baiyi.opscloud.facade.ServerFacade;
import com.baiyi.opscloud.service.server.OcServerGroupService;
import com.baiyi.opscloud.service.server.OcServerService;
import com.google.common.collect.Lists;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author baiyi
 * @Date 2020/2/21 4:47 下午
 * @Version 1.0
 */
@Service
public class ServerFacadeImpl implements ServerFacade {

    @Resource
    private OcServerService ocServerService;

    @Resource
    private ServerDecorator serverDecorator;

    @Resource
    private OcServerGroupService ocServerGroupService;

    @Resource
    private ServerCacheFacade serverCacheFacade;

    @Override
    public DataTable<ServerVO.Server> queryServerPage(ServerParam.PageQuery pageQuery) {
        DataTable<OcServer> table = ocServerService.queryOcServerByParam(pageQuery);
        return toServerDataTable(table);
    }

    @Override
    public BusinessWrapper<ServerVO.Server> queryServerById(int id) {
        OcServer ocServer = ocServerService.queryOcServerById(id);
        if (ocServer == null)
            return new BusinessWrapper<>(ErrorEnum.SERVER_NOT_EXIST);
        return new BusinessWrapper(getServerVO(ocServer));
    }

    private ServerVO.Server getServerVO(OcServer ocServer) {
        return serverDecorator.decorator(BeanCopierUtils.copyProperties(ocServer, ServerVO.Server.class));
    }

    @Override
    public BusinessWrapper<List<ServerVO.Server>> queryServerByIds(ServerParam.QueryByServerIds queryByServerByIds) {
        List<ServerVO.Server> result = Lists.newArrayList();
        queryByServerByIds.getIds().forEach(e -> {
            OcServer ocServer = ocServerService.queryOcServerById(e);
            if (ocServer != null)
                result.add(serverDecorator.decorator(BeanCopierUtils.copyProperties(ocServer, ServerVO.Server.class)));
        });
        return new BusinessWrapper(result);
    }

    @Override
    public DataTable<ServerVO.Server> fuzzyQueryServerPage(ServerParam.PageQuery pageQuery) {
        DataTable<OcServer> table = ocServerService.fuzzyQueryOcServerByParam(pageQuery);
        return toServerDataTable(table);
    }

    @Override
    public BusinessWrapper<List<ServerVO.Server>> queryServerByServerGroup(ServerParam.QueryByServerGroup queryByServerGroup) {
        Integer serverGroupId = queryByServerGroup.getServerGroupId();
        if (IDUtils.isEmpty(serverGroupId)) {
            if (!StringUtils.isEmpty(queryByServerGroup.getServerGroupName())) {
                OcServerGroup ocServerGroup = ocServerGroupService.queryOcServerGroupByName(queryByServerGroup.getServerGroupName());
                if (ocServerGroup != null)
                    serverGroupId = ocServerGroup.getId();
            }
        }
        if (serverGroupId == null) return new BusinessWrapper<>(ErrorEnum.SERVERGROUP_NOT_EXIST);
        List<ServerVO.Server> servers = ocServerService.queryOcServerByServerGroupId(serverGroupId).stream().map(e ->
                serverDecorator.decorator(BeanCopierUtils.copyProperties(e, ServerVO.Server.class))
        ).collect(Collectors.toList());
        return new BusinessWrapper(servers);
    }





    private DataTable<ServerVO.Server> toServerDataTable(DataTable<OcServer> table) {
        List<ServerVO.Server> page = BeanCopierUtils.copyListProperties(table.getData(), ServerVO.Server.class);
        return new DataTable<>(page.stream().map(e -> serverDecorator.decorator(e)).collect(Collectors.toList()), table.getTotalNum());
    }

    @Override
    public BusinessWrapper<Boolean> addServer(ServerVO.Server server) {
        if (StringUtils.isEmpty(server.getPrivateIp()))
            return new BusinessWrapper<>(ErrorEnum.SERVER_PRIVATE_IP_IS_NAME);
        if (ocServerService.queryOcServerByPrivateIp(server.getPrivateIp()) != null)
            return new BusinessWrapper<>(ErrorEnum.SERVER_PRIVATE_IP_CONFLICT);
        if (StringUtils.isEmpty(server.getName()) || !RegexUtils.isServerNameRule(server.getName()))
            return new BusinessWrapper<>(ErrorEnum.SERVER_NAME_NON_COMPLIANCE_WITH_RULES);
        if (server.getServerGroupId() == null)
            return new BusinessWrapper<>(ErrorEnum.SERVER_GROUP_NOT_SELECTED);
        if (ocServerGroupService.queryOcServerGroupById(server.getServerGroupId()) == null)
            return new BusinessWrapper<>(ErrorEnum.SERVERGROUP_NOT_EXIST);
        // 校验SN
        Integer serialNumber = server.getSerialNumber();
        if (serialNumber == 0) {
            serialNumber = ocServerService.queryOcServerMaxSerialNumber(server.getServerGroupId(), server.getEnvType());
            server.setSerialNumber(serialNumber + 1);
        }
        OcServer ocServer = ServerBuilder.build(server);
        ocServerService.addOcServer(ocServer);
        // 清理缓存
        serverCacheFacade.evictServerCache(ocServer);
        return BusinessWrapper.SUCCESS;
    }

    @Override
    public BusinessWrapper<Boolean> updateServer(ServerVO.Server server) {
        // 校验服务器名称
        if (!RegexUtils.isServerNameRule(server.getName()))
            return new BusinessWrapper<>(ErrorEnum.SERVER_NAME_NON_COMPLIANCE_WITH_RULES);
        // 校验服务器组是否配置
        if (server.getServerGroupId() == null
                || server.getServerGroupId() <= 0
                || ocServerGroupService.queryOcServerGroupById(server.getServerGroupId()) == null) {
            return new BusinessWrapper<>(ErrorEnum.SERVER_GROUP_NOT_SELECTED);
        }
        OcServer ocServer = BeanCopierUtils.copyProperties(server, OcServer.class);
        ocServerService.updateOcServer(ocServer);
        // 清理缓存
        serverCacheFacade.evictServerCache(ocServer);
        return BusinessWrapper.SUCCESS;
    }

    @Override
    public BusinessWrapper<Boolean> deleteServerById(int id) {
        OcServer ocServer = ocServerService.queryOcServerById(id);
        if (ocServer == null)
            return new BusinessWrapper<>(ErrorEnum.SERVER_NOT_EXIST);
        // 清理缓存
        serverCacheFacade.evictServerCache(ocServer);
        // 删除server的属性
        ocServerService.deleteOcServerById(id);
        return BusinessWrapper.SUCCESS;
    }


}
