package tmd.tmdAdmin.data.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tmd.tmdAdmin.data.entities.VideosType;

public interface VideoTypeRepository extends JpaRepository<VideosType,Integer> {

}
