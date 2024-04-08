package org.example.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;

@Entity
@Table(name = "users")
@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue
    private Long id;
    @Column(nullable = false, unique = true)
    private String username;
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Address address;
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Profile profile;

    public User setAddress(final Address address) {
         address.setUser(this);
         this.address = address;
         return this;
    }

    public User setProfile(final Profile profile) {
        profile.setUser(this);
        this.profile = profile;
        return this;
    }
}
