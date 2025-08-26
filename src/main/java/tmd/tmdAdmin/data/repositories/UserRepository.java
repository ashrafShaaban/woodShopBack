package tmd.tmdAdmin.data.repositories;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tmd.tmdAdmin.data.entities.User;

import java.util.List;

public interface UserRepository extends JpaRepository<User,Integer> {
    User findUserByUsername(String username);
//    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.rolename = 'ROLE_ADMIN'")
//    List<User> findAdminsOnly();
@Modifying
@Transactional
@Query("UPDATE User u SET u.lastLogin = :lastLogin WHERE u.id = :id")
void updateLastLogin(@Param("id") int id, @Param("lastLogin") Long lastLogin);
}
