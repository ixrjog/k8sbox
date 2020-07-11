package com.baiyi.opscloud.decorator.user;

import com.baiyi.opscloud.common.util.BeanCopierUtils;
import com.baiyi.opscloud.domain.generator.opscloud.OcUser;
import com.baiyi.opscloud.domain.vo.user.UserGroupVO;
import com.baiyi.opscloud.domain.vo.user.UserVO;
import com.baiyi.opscloud.service.user.OcUserService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author baiyi
 * @Date 2020/2/24 8:04 下午
 * @Version 1.0
 */
@Component("UserGroupDecorator")
public class UserGroupDecorator {

    @Resource
    private OcUserService ocUserService;

    public UserGroupVO.UserGroup decorator(UserGroupVO.UserGroup userGroup, Integer extend) {
        if (extend != null && extend == 1) {
            List<OcUser> userList = ocUserService.queryOcUserByUserGroupId(userGroup.getId());
            userGroup.setUsers(BeanCopierUtils.copyListProperties(userList, UserVO.User.class));
        }
        return userGroup;
    }

}
