package tmd.tmdAdmin.data.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tmd.tmdAdmin.data.entities.Category;

public interface CategoryRepository extends JpaRepository<Category,Integer> {
}
