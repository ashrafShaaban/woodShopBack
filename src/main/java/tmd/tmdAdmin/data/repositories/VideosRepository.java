package tmd.tmdAdmin.data.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tmd.tmdAdmin.data.entities.Videos;

public interface VideosRepository extends JpaRepository<Videos,Integer> {
}
