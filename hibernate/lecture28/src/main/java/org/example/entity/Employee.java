package org.example.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "employees")
@Setter
@Getter
@Accessors(chain = true)
@ToString
@NoArgsConstructor
public class Employee {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    @Setter(AccessLevel.PRIVATE) //Private setter
    @ManyToMany(fetch = FetchType.EAGER) //This is a bad practice to set a cascade type "All"
    @JoinTable(
            name = "employees_guilds",
            joinColumns = @JoinColumn(name = "employee_id"),
            inverseJoinColumns = @JoinColumn(name = "guild_id")
    )
    private List<Guild> guilds = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "employees_managers",
            joinColumns = @JoinColumn(name = "employee_id"),
            inverseJoinColumns = @JoinColumn(name = "manager_id")
    )
    private Set<Employee> managers = new HashSet<>(); //Self-joining. It is also possible to create a separate entity
    @ManyToMany(mappedBy = "managers")
    private Set<Employee> subordinates = new HashSet<>();
}
