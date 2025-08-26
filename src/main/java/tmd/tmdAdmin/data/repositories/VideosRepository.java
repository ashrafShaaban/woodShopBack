package tmd.tmdAdmin.data.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tmd.tmdAdmin.data.entities.Gallery;
import tmd.tmdAdmin.data.entities.Videos;

import java.util.List;

public interface VideosRepository extends JpaRepository<Videos,Integer> {
    List<Videos> findAllByVideosType_Id(int id);
}
