package org.example.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@DynamicUpdate
@Table(name = "roles")
@Setter
@Getter
@Accessors(chain = true)
@ToString
@NoArgsConstructor
public class Role {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    @ManyToOne
    @JoinColumn(
            name = "person_id",
            foreignKey = @ForeignKey(name = "fk_user_id")
    )
    //Two tables: roles, users.
    private User user;
}
