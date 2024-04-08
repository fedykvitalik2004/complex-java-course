package org.example.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicUpdate;

import java.util.ArrayList;
import java.util.List;

@Entity
@DynamicUpdate
@Table(name = "persons")
@Setter
@Accessors(chain = true)
@Getter
@ToString
@NoArgsConstructor
public class Person {
    @Id
    @GeneratedValue
    private Long id;
    private String firstName;
    private String lastName;
    @Column(unique = true)
    private String email;
    @OneToMany
    /*
        Three tables: person_notes, notes, persons.
        This is a bad solution (not effective), even if to use @JoinColumn here (name = "person_id").
        If we use @JoinColumn and "Cascade Persist" here, Insert and then Update will work for the child, but it is slower.
        The child doesn't know about the parent, therefore the child is created without the foreign key, and then it
        is updated (setting the foreign key), because it is in the context and linked to the parent.
    */
    private List<Note> notes = new ArrayList<>();
}
