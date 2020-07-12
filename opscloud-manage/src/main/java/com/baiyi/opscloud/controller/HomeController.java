package com.baiyi.opscloud.controller;

import io.swagger.annotations.Api;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import static com.baiyi.opscloud.common.base.Global.BASE_URL;


@Controller
@Api(tags = "首页")
public class HomeController {

    @RequestMapping(BASE_URL +"/home")
    public String index() {
        return "index.html";
    }
}
