package tmd.tmdAdmin.data.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tmd.tmdAdmin.data.entities.Category;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category,Integer> {
    Optional<Category> findByName(String name);
}
