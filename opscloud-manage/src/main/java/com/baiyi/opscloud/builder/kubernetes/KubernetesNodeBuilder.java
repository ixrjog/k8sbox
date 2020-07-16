package com.baiyi.opscloud.builder.kubernetes;

import com.baiyi.opscloud.bo.ServerBO;
import com.baiyi.opscloud.common.base.CloudServerType;
import com.baiyi.opscloud.common.base.LoginType;
import com.baiyi.opscloud.common.util.BeanCopierUtils;
import com.baiyi.opscloud.domain.generator.opscloud.OcEnv;
import com.baiyi.opscloud.domain.generator.opscloud.OcServer;
import com.baiyi.opscloud.domain.generator.opscloud.OcServerGroup;
import io.fabric8.kubernetes.api.model.Node;

/**
 * @Author baiyi
 * @Date 2020/7/13 11:32 上午
 * @Version 1.0
 */
public class KubernetesNodeBuilder {

    public static OcServer build(OcServerGroup ocServerGroup, OcEnv ocEnv, Node node) {
        ServerBO bo = ServerBO.builder()
                .privateIp(node.getStatus().getAddresses().get(0).getAddress())
                .envType(ocEnv.getEnvType())
                .name(node.getMetadata().getName())
                .serverGroupId(ocServerGroup.getId())
                .loginType(LoginType.KEY.getType())
                .serverType(CloudServerType.PS.getType())
                .area("IDC")
                .build();
        return covert(bo);
    }

    private static OcServer covert(ServerBO bo) {
        return BeanCopierUtils.copyProperties(bo, OcServer.class);
    }
}
