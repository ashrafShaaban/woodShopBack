package tmd.tmdAdmin.data.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tmd.tmdAdmin.data.entities.Dimension;

public interface DimensionRepository extends JpaRepository<Dimension,Integer> {
}
