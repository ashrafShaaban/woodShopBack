package tmd.tmdAdmin.data.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tmd.tmdAdmin.data.entities.SliderSlide;

public interface SliderSideRepository extends JpaRepository<SliderSlide,Integer> {
}
