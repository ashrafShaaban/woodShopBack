package tmd.tmdAdmin.data.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tmd.tmdAdmin.data.entities.Gallery;

import java.util.List;

public interface GalleryRepository extends JpaRepository<Gallery,Integer> {
    List<Gallery> findAllByType_Id(int id);
}
