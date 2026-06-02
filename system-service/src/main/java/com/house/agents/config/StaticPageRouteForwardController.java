package com.house.agents.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Serves the embedded Vue app for direct local navigation to client-side routes.
 */
@Controller
public class StaticPageRouteForwardController {

    @GetMapping("/login")
    public String login() {
        return "forward:/index.html";
    }
}
