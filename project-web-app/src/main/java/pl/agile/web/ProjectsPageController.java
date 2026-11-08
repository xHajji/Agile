package pl.agile.web;

import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
class ProjectsPageController {
    private final ProjectApiClient projects;

    ProjectsPageController(ProjectApiClient projects) {
        this.projects = projects;
    }

    @GetMapping("/")
    String index(Model model) {
        try {
            model.addAttribute("projects", projects.list());
        } catch (RestClientException exception) {
            model.addAttribute("listUnavailable", true);
        }
        return "index";
    }

    @PostMapping("/projects")
    String create(@RequestParam(defaultValue = "") String name, Model model, RedirectAttributes redirect) {
        try {
            projects.create(name);
            redirect.addFlashAttribute("saved", true);
            return "redirect:/";
        } catch (HttpClientErrorException.BadRequest exception) {
            model.addAttribute("nameError", validationMessage(exception));
        } catch (RestClientException exception) {
            model.addAttribute("saveError",
                    "Nie udało się potwierdzić zapisu. Sprawdź listę przed ponowną próbą.");
        }
        model.addAttribute("name", name);
        return index(model);
    }

    private String validationMessage(HttpClientErrorException.BadRequest exception) {
        try {
            ProblemDetail problem = exception.getResponseBodyAs(ProblemDetail.class);
            if (problem != null && problem.getDetail() != null) {
                return problem.getDetail();
            }
        } catch (RestClientException ignored) {
            // A proxy may return an error without a ProblemDetail body.
        }
        return "Sprawdź nazwę projektu.";
    }
}
