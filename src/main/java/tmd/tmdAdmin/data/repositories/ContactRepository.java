package tmd.tmdAdmin.data.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import tmd.tmdAdmin.data.entities.Contact;

import java.util.List;

public interface ContactRepository extends CrudRepository<Contact,Integer> {
    List<Contact> findAll();
    long countByIsReadFalse();

    List<Contact> findAllByOrderByCreatedAtDesc();

}
