package tmd.tmdAdmin.data.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import tmd.tmdAdmin.data.entities.GalleryType;
import tmd.tmdAdmin.data.entities.VideosType;

import java.util.List;
import java.util.Optional;

public interface VideoTypeRepository extends JpaRepository<VideosType,Integer> {
    Optional<VideosType> findByName(String name);
    List<VideosType> findAll();
}
