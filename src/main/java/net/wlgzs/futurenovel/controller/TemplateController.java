package net.wlgzs.futurenovel.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TemplateController {

    @GetMapping(value = "/")
    public String home(Module m) {
        return "index.html";
    }

    @GetMapping("/hello")
    public String hello(Model m) {
        m.addAttribute("msg", "Hello world");
        return "hello.html";
    }
}
