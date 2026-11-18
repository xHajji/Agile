package pl.agile.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
class ProjectController {
    private final ProjectService projects;

    ProjectController(ProjectService projects) {
        this.projects = projects;
    }

    @GetMapping
    List<ProjectResponse> list() {
        return projects.list().stream().map(ProjectResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ProjectResponse create(@RequestBody CreateProject request) {
        return ProjectResponse.from(projects.create(request.name()));
    }

    @ExceptionHandler(ProjectService.InvalidNameException.class)
    ProblemDetail invalidName(ProjectService.InvalidNameException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    record CreateProject(String name) {}

    record ProjectResponse(UUID id, String name) {
        static ProjectResponse from(Project project) {
            return new ProjectResponse(project.getId(), project.getName());
        }
    }
}
