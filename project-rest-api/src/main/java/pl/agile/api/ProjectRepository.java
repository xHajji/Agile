package pl.agile.api;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface ProjectRepository extends JpaRepository<Project, UUID> {
    List<Project> findAllByOrderByCreatedAtAscIdAsc();
}
