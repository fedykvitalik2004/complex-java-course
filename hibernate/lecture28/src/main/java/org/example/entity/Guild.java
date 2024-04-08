package org.example.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "guilds")
@Getter
@Setter
@Accessors(chain = true)
@ToString
@NoArgsConstructor
public class Guild {
    @Id
    @GeneratedValue
    private Long id;
    @Column(nullable = false, unique = true)
    private String name;
    @ManyToMany(mappedBy = "guilds", cascade = {CascadeType.PERSIST, CascadeType.MERGE}) //It depends on business logic
    @Setter(AccessLevel.PRIVATE)
    private List<Employee> employees = new ArrayList<>();

    public Guild addEmployee(final Employee employee) {
        employee.getGuilds().add(this);
        this.employees.add(employee);
        return this;
    }
}
