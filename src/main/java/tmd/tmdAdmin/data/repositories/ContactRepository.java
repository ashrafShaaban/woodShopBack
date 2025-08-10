package tmd.tmdAdmin.data.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tmd.tmdAdmin.data.entities.Contact;

public interface ContactRepository extends JpaRepository<Contact,Integer> {
}
