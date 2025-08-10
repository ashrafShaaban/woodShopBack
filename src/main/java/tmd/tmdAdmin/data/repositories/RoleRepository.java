package tmd.tmdAdmin.data.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tmd.tmdAdmin.data.entities.Role;

public interface RoleRepository extends JpaRepository<Role,Integer> {
}
