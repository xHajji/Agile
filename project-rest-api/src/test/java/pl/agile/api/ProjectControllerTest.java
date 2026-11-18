package pl.agile.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class ProjectControllerTest {
    private final ProjectRepository repository = mock(ProjectRepository.class);
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = standaloneSetup(new ProjectController(new ProjectService(repository))).build();
    }

    @Test
    void savesTrimmedNameAndReturnsCreatedProject() throws Exception {
        when(repository.save(any(Project.class))).thenAnswer(invocation -> {
            Project project = invocation.getArgument(0);
            assertEquals("Projekt Alfa", project.getName());
            return project;
        });

        mvc.perform(post("/api/projects").contentType(APPLICATION_JSON)
                .content("""
                        {"name":"  Projekt Alfa  "}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Projekt Alfa"));
    }

    @Test
    void returnsSavedProjects() throws Exception {
        when(repository.findAllByOrderByCreatedAtAscIdAsc()).thenReturn(List.of(new Project("Projekt Alfa"), new Project("Projekt Beta")));

        mvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Projekt Alfa"))
                .andExpect(jsonPath("$[1].name").value("Projekt Beta"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"name\":null}", "{\"name\":\"\"}", "{\"name\":\"   \"}"})
    void rejectsMissingOrBlankNameWithoutWriting(String payload) throws Exception {
        mvc.perform(post("/api/projects").contentType(APPLICATION_JSON).content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Wpisz nazwę projektu"));

        verifyNoInteractions(repository);
    }

    @Test
    void rejectsNameLongerThanColumnLimitWithoutWriting() throws Exception {
        String payload = "{\"name\":\"" + "a".repeat(121) + "\"}";

        mvc.perform(post("/api/projects").contentType(APPLICATION_JSON).content(payload))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(repository);
    }
}
