package com.efeiyi.ec.website.base.controller;

import com.ming800.core.base.service.BaseManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HomeController {


    private BaseManager baseManager;

    @Autowired
    public HomeController(BaseManager baseManager) {
        this.baseManager = baseManager;
    }

    @RequestMapping({"redirect.do"})
    public String redirect() {
        return "redirect:/app/index.html";
    }

    @RequestMapping({"/500"})
    public String show500() {
        return "/common/500";
    }

    @RequestMapping({"/404"})
    public String show404() {
        return "/common/404";
    }

    @RequestMapping("/401")
    public String show401() throws Exception {
        return "redirect:/";
    }


}
