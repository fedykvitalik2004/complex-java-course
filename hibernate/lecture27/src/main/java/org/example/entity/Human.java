package org.example.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "humans")
@Setter
@Getter
@ToString
@Accessors(chain = true)
@NoArgsConstructor
public class Human {
    @Id
    @GeneratedValue
    private Long id;
    private String country;
    //"cascade" means cascade operations on child entities
    //"mappedBy" means the name of the field in the parent entity which says who are responsible for persisting.
    @OneToMany(mappedBy = "human", cascade = {CascadeType.PERSIST})
    @ToString.Exclude
    private List<Passport> passports = new ArrayList<>();

    public void addPassport(final Passport passport) {
        passport.setHuman(this);
        passports.add(passport);
    }

    public void addPassports(final List<Passport> list) {
        list.forEach(passport -> {
            passport.setHuman(this);
            passports.add(passport);
        });
    }
}
