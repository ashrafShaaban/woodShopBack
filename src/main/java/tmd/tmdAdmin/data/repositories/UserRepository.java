package tmd.tmdAdmin.data.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tmd.tmdAdmin.data.entities.User;

import java.util.List;

public interface UserRepository extends JpaRepository<User,Integer> {
    User findUserByUsername(String username);
//    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.rolename = 'ROLE_ADMIN'")
//    List<User> findAdminsOnly();
}
