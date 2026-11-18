package pl.agile.api;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
class ProjectService {
    private final ProjectRepository projects;

    ProjectService(ProjectRepository projects) {
        this.projects = projects;
    }

    List<Project> list() {
        return projects.findAllByOrderByCreatedAtAscIdAsc();
    }

    Project create(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidNameException("Wpisz nazwę projektu");
        }
        String trimmed = name.strip();
        if (trimmed.length() > 120) {
            throw new InvalidNameException("Nazwa projektu może mieć najwyżej 120 znaków.");
        }
        if (trimmed.indexOf(0) >= 0) {
            throw new InvalidNameException("Nazwa projektu zawiera niedozwolony znak.");
        }
        return projects.save(new Project(trimmed));
    }

    static final class InvalidNameException extends IllegalArgumentException {
        InvalidNameException(String message) {
            super(message);
        }
    }
}
