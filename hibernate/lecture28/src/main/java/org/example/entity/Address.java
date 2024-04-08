package org.example.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;

@Entity
@Table(name = "addresses")
@Getter
@Setter
@Accessors(chain = true)
@ToString(exclude = "user")
@NoArgsConstructor
public class Address {
    @Id
    @GeneratedValue //It is the default sequence for PostgreSQL
    private Long id;
    @Column(nullable = false, unique = true)
    private String city;
    private String street;
    private Integer houseNumber;
    @OneToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;
}
