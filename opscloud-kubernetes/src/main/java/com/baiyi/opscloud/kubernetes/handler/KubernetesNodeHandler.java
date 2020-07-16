package com.baiyi.opscloud.kubernetes.handler;

import com.baiyi.opscloud.kubernetes.client.KubernetesClientContainer;
import io.fabric8.kubernetes.api.model.NodeList;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @Author baiyi
 * @Date 2020/7/13 10:47 上午
 * @Version 1.0
 */
@Component
public class KubernetesNodeHandler {

    @Resource
    private KubernetesClientContainer kubernetesClientContainer;

    public NodeList getNodeList(String clusterName) {
        try {
            return kubernetesClientContainer.getClient(clusterName).nodes().list();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new NodeList();
    }
}
