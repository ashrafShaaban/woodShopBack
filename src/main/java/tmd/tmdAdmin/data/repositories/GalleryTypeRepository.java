package tmd.tmdAdmin.data.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tmd.tmdAdmin.data.entities.Gallery_Type;

public interface GalleryTypeRepository extends JpaRepository<Gallery_Type,Integer> {
}
