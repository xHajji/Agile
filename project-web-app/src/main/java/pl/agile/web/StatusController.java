package pl.agile.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Controller
class StatusController {
    private final RestClient api;

    StatusController(RestClient.Builder builder, @Value("${rest.base.url}") String apiBaseUrl) {
        api = builder.baseUrl(apiBaseUrl).build();
    }

    @GetMapping("/")
    String index(Model model) {
        model.addAttribute("connected", isApiHealthy());
        return "index";
    }

    private boolean isApiHealthy() {
        try {
            Health health = api.get().uri("/actuator/health").retrieve().body(Health.class);
            return health != null && "UP".equals(health.status());
        } catch (RestClientException exception) {
            return false;
        }
    }

    private record Health(String status) {}
}
