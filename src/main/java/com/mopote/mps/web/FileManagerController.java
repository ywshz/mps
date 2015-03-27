package com.mopote.mps.web;

import com.mopote.mps.job.JobInfo;
import com.mopote.mps.service.WebUIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

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
        mv.addObject("parent",parent);
        return mv;
    }

    @RequestMapping("/detail.do")
    public @ResponseBody
    JobInfo detail(String parent,String jobName){
        JobInfo job = webService.getJobDetail(parent,jobName);
        return job;
    }

    @RequestMapping("/add-group.do")
    public String addGroup(String parent,String name,HttpServletRequest request){
        webService.addFolder(parent, name);
        request.setAttribute("parent",parent);
        return "forward:/files/list.do";
    }

    @RequestMapping("/save-update-task.do")
    public String saveUpdateTask(String parent,String name,JobInfo jobInfo, HttpServletRequest request){
        
        request.setAttribute("parent",parent);
        return "forward:/files/list.do";
    }
}
