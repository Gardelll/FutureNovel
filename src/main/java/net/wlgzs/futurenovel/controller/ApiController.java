package net.wlgzs.futurenovel.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Controller
public class ApiController {
    @GetMapping("/test")
    public Map<?, ?> test() {
        return Map.ofEntries(
                Map.entry("a", "233"),
                Map.entry("b", "666")
                );
    }
}
