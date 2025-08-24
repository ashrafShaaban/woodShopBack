package tmd.tmdAdmin.data.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import tmd.tmdAdmin.data.entities.GalleryType;

import java.util.List;
import java.util.Optional;

public interface GalleryTypeRepository extends CrudRepository<GalleryType,Integer> {
    Optional<GalleryType> findByName(String name);
    List<GalleryType> findAll();

}
