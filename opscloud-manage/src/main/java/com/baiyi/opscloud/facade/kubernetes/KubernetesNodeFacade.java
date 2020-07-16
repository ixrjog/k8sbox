package com.baiyi.opscloud.facade.kubernetes;

import com.baiyi.opscloud.builder.kubernetes.KubernetesNodeBuilder;
import com.baiyi.opscloud.common.base.Global;
import com.baiyi.opscloud.common.util.BeanCopierUtils;
import com.baiyi.opscloud.domain.generator.opscloud.OcEnv;
import com.baiyi.opscloud.domain.generator.opscloud.OcKubernetesCluster;
import com.baiyi.opscloud.domain.generator.opscloud.OcServer;
import com.baiyi.opscloud.domain.generator.opscloud.OcServerGroup;
import com.baiyi.opscloud.domain.vo.server.ServerVO;
import com.baiyi.opscloud.facade.ServerFacade;
import com.baiyi.opscloud.kubernetes.handler.KubernetesNodeHandler;
import com.baiyi.opscloud.service.env.OcEnvService;
import com.baiyi.opscloud.service.server.OcServerGroupService;
import com.baiyi.opscloud.service.server.OcServerService;
import com.google.common.base.Joiner;
import io.fabric8.kubernetes.api.model.Node;
import io.fabric8.kubernetes.api.model.NodeList;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;

import static com.baiyi.opscloud.common.base.Global.ASYNC_POOL_TASK_COMMON;

/**
 * @Author baiyi
 * @Date 2020/7/13 11:22 上午
 * @Version 1.0
 */
@Service
public class KubernetesNodeFacade extends BaseKubernetesFacade {

    @Resource
    private KubernetesNodeHandler kubernetesNodeHandler;

    @Resource
    private OcServerGroupService ocServerGroupService;

    @Resource
    private OcEnvService ocEnvService;

    @Resource
    private OcServerService ocServerService;

    @Resource
    private ServerFacade serverFacade;

    @Async(value = ASYNC_POOL_TASK_COMMON)
    public void syncKubernetesNode(int clusterId) {
        OcKubernetesCluster ocKubernetesCluster = getOcKubernetesClusterById(clusterId);
        NodeList nodeList = kubernetesNodeHandler.getNodeList(ocKubernetesCluster.getName());
        if (nodeList == null || CollectionUtils.isEmpty(nodeList.getItems())) return;
        OcServerGroup ocServerGroup = acqServerGroup(ocKubernetesCluster.getName());
        OcEnv ocEnv = acqEnv();
        nodeList.getItems().forEach(e -> saveNode(ocServerGroup, ocEnv, e));
    }

    private void saveNode(OcServerGroup ocServerGroup, OcEnv ocEnv, Node node) {
        OcServer pre = KubernetesNodeBuilder.build(ocServerGroup, ocEnv, node);
        OcServer ocServer = ocServerService.queryOcServerByIp(pre.getPrivateIp());
        if(ocServer == null)
            serverFacade.addServer(BeanCopierUtils.copyProperties(pre, ServerVO.Server.class));
    }

    private OcEnv acqEnv() {
        return ocEnvService.queryOcEnvByName(Global.ENV_PROD);
    }

    private OcServerGroup acqServerGroup(String clusterName) {
        String groupName = Joiner.on("_").join("group", clusterName);
        OcServerGroup ocServerGroup = ocServerGroupService.queryOcServerGroupByName(groupName);
        if (ocServerGroup != null) return ocServerGroup;
        ocServerGroup = new OcServerGroup();
        ocServerGroup.setName(groupName);
        ocServerGroup.setGrpType(0);
        ocServerGroup.setInWorkorder(0);
        ocServerGroup.setComment("Kunbernetes Cluster Nodes");
        ocServerGroupService.addOcServerGroup(ocServerGroup);
        return ocServerGroup;
    }
}
