package pl.agile.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
class AuthPageController {
    @GetMapping("/login")
    String login() {
        return "login";
    }

    @PostMapping("/login")
    String handleLogin() {
        return "login";
    }

    @PostMapping("/register")
    String handleRegister() {
        return "login";
    }
}
