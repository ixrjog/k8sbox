package com.baiyi.opscloud.bo;

import com.baiyi.opscloud.common.base.ServerStatus;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 * @Author baiyi
 * @Date 2020/4/1 11:07 上午
 * @Version 1.0
 */
@Data
@Builder
public class ServerBO {

    private Integer id;
    private String name;
    private Integer serverGroupId;
    private Integer loginType;
    @Builder.Default
    private String loginUser = "root";
    private Integer envType;
    private String publicIp;
    private String privateIp;
    private Integer serverType;
    private String area;
    @Builder.Default // 0 自动递增
    private Integer serialNumber = 0;
    @Builder.Default
    private Integer monitorStatus = -1;
    @Builder.Default
    private Boolean isActive = true;
    @Builder.Default
    private Integer serverStatus = ServerStatus.ONLINE.getStatus();
    private Date createTime;
    private Date updateTime;
    private String comment;
    // 顺序（仅在创建实例中使用）
    private Integer seq;
    private String hostname;
    private String vswitchId;
}
