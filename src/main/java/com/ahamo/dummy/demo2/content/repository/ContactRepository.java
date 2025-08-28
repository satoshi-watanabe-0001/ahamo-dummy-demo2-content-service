package com.ahamo.dummy.demo2.content.repository;

import com.ahamo.dummy.demo2.content.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {
}
