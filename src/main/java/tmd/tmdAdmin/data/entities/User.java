package tmd.tmdAdmin.data.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;


@Entity
@Table(name="user")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private int id;

    @Column(name = "username",unique = true,nullable = false)
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "role")
    private String role;

    private Long createdAt;
    private Long lastLogin;
    private Boolean active;
    private Long updatedAt;
}
