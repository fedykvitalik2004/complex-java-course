package org.example.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Entity
@Table(name = "passports")
@Getter
@Setter
@Accessors(chain = true)
@ToString
@NoArgsConstructor
public class Passport {
    @Id
    @GeneratedValue
    private Long id;
    @Column(nullable = false, unique = true)
    private String number;
    @ManyToOne(optional = false) //Necessary
    @JoinColumn(name = "human_id")
    /*
        In the case we can only add passport to human. We are supposed to use a helper method. @ManyToOne is responsible
        for adding.
    */
    private Human human;
}
