package org.example.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

//In the case when profile cannot exist without a user, so it would be a good choice to implement. The id is PM and FK concurrently.
@Entity
@Table(name = "profiles")
@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@ToString(exclude = "user")
public class Profile {
    @Id //It is more optimized
    private Long id;
    private String photoUrl;
    private Boolean active; //It depends on business logic
    @OneToOne(optional = false)
    @JoinColumn(name = "user_id")
    @MapsId //It is the same as @Id
    private User user;
}
