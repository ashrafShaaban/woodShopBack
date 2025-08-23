package tmd.tmdAdmin.data.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tmd.tmdAdmin.data.entities.Contact;

public interface ContactRepository extends JpaRepository<Contact,Integer> {
//    @Query("SELECT COUNT(m) FROM Contact m WHERE MONTH(m.date) = :month")
//    int countByMonth(@Param("month") int month);
}
