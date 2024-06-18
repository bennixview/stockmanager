package com.trianel.masterdata.ui;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class DashboardWebController {

    public String dashboard() {
        return "index";
    }
}
