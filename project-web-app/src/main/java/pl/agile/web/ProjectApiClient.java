package pl.agile.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Component
class ProjectApiClient {
    private static final String PROJECTS = "/api/projects";
    private final RestClient api;

    ProjectApiClient(RestClient.Builder builder, @Value("${rest.base.url}") String apiBaseUrl) {
        api = builder.baseUrl(apiBaseUrl).build();
    }

    List<ProjectSummary> list() {
        List<ProjectSummary> projects = api.get().uri(PROJECTS).retrieve()
                .body(new ParameterizedTypeReference<>() {});
        if (projects == null) {
            throw new RestClientException("API did not return a project list");
        }
        return projects;
    }

    void create(String name) {
        api.post().uri(PROJECTS)
                .contentType(APPLICATION_JSON)
                .body(new CreateProject(name))
                .retrieve()
                .toBodilessEntity();
    }

    record ProjectSummary(UUID id, String name) {}
    private record CreateProject(String name) {}
}
