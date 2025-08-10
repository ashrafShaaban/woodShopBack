package tmd.tmdAdmin.data.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tmd.tmdAdmin.data.entities.User;

public interface UserRepository extends JpaRepository<User,Integer> {
    User findUserByUsername(String username);
}
