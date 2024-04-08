package org.example.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;

@Entity
@Table(name = "notes")
@Setter
@Getter
@Accessors(chain = true)
@ToString
@NoArgsConstructor
public class Note {
    @Id
    @GeneratedValue
    private Long id;
    @Column(nullable = false)
    private String text;
}
