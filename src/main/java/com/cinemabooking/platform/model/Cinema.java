package com.cinemabooking.platform.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "cinemas",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_cinema_name_city",
                        columnNames = {"name", "city"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Cinema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Cinema name is required")
    @Size(max = 100, message = "Cinema name cannot exceed 100 characters")
    @Column(nullable = false, length = 100)
    private String name;

    @NotBlank(message = "Cinema address is required")
    @Size(max = 200, message = "Address cannot exceed 200 characters")
    @Column(nullable = false, length = 200)
    private String address;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City cannot exceed 100 characters")
    @Column(nullable = false, length = 100)
    private String city;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    @Column(length = 1000)
    private String description;

    @OneToMany(
            mappedBy = "cinema",
            cascade = { CascadeType.PERSIST,
                        CascadeType.MERGE
            }
    )
    private List<Hall> halls = new ArrayList<>();

    @Column(nullable = false)
    private boolean active = true;

    public void addHall(Hall hall) {
        halls.add(hall);
        hall.setCinema(this);
    }
}