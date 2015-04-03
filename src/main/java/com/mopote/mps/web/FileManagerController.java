package com.mopote.mps.web;

import com.mopote.mps.job.JobInfo;
import com.mopote.mps.service.WebUIService;
import com.mopote.mps.web.webbean.ResponseBean;
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
    public @ResponseBody
    ResponseBean saveUpdateTask(String parent,JobInfo jobInfo, HttpServletRequest request){
        if(jobInfo.getId() == null){
            webService.addTask(parent,jobInfo);
        }else{
            webService.updateTask(parent, jobInfo);
        }
        request.setAttribute("parent",parent);
        return new ResponseBean(200,"成功");
    }

    @RequestMapping("/delete-task.do")
    public @ResponseBody
    ResponseBean deleteTask(String parent,String name){
        webService.delete(parent,name);
        return new ResponseBean(200,"成功");
    }

    @RequestMapping("/start-or-stop.do")
    public @ResponseBody
    ResponseBean startOrStop(String parent,String name){
        webService.startOrStop(parent,name);
        return new ResponseBean(200,"成功");
    }
}
