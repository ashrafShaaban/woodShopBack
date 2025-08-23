package tmd.tmdAdmin.data.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tmd.tmdAdmin.data.entities.GalleryType;

public interface GalleryTypeRepository extends JpaRepository<GalleryType,Integer> {
}
