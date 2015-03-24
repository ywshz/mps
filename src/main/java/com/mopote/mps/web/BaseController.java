package com.mopote.mps.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by wangshu.yang on 2015/3/24.
 */
@RequestMapping(value = "/")
@Controller
public class BaseController {

    @RequestMapping("/index.do")
    public String index(){
        return "index";
    }
}
