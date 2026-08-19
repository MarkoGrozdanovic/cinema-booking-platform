package com.cinemabooking.platform.model;

import com.cinemabooking.platform.model.enums.HallType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "cinema_halls",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_hall_name_cinema",
                        columnNames = {"name", "cinema_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Hall {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Hall name is required")
    @Column(nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "hall_type", nullable = false, length = 30)
    private HallType hallType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "cinema_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_hall_cinema")
    )
    private Cinema cinema;

    @OneToMany(
            mappedBy = "hall",
            cascade = {
                    CascadeType.PERSIST,
                    CascadeType.MERGE
            }
    )
    private List<Seat> seats = new ArrayList<>();

    @Column(nullable = false)
    private boolean active = true;

    public void addSeat(Seat seat) {
        seats.add(seat);
        seat.setHall(this);
    }
}