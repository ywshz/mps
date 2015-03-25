package com.mopote.mps.web;

import com.mopote.mps.service.WebUIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * Created by wangshu.yang on 2015/3/24.
 */
@RequestMapping(value = "/files")
@Controller
public class FileManagerController {

    @Autowired
    private WebUIService webService;

    @RequestMapping("/list.do")
    public ModelAndView list(String parent){
        if(StringUtils.isEmpty(parent)){
            parent="";
        }
        ModelAndView mv = new ModelAndView("pages/diaodu");
        mv.addObject("files",webService.list(parent));
        return mv;
    }
}
