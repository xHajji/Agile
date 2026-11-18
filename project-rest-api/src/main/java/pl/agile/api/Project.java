package pl.agile.api;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "projects")
class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tytul", nullable = false, columnDefinition = "text")
    private String name;

    @Column(name = "id_owner", nullable = false)
    private UUID ownerId;

    @Column(name = "data_utw", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected Project() {}

    Project(String name) {
        this.name = name;
        // Technical local owner until the login form has authentication.
        this.ownerId = UUID.fromString("00000000-0000-0000-0000-000000000001");
    }

    UUID getId() {
        return id;
    }

    String getName() {
        return name;
    }
}
