package com.chaitanya.evently.repository;

import com.chaitanya.evently.model.Venue;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VenueRepository extends JpaRepository<Venue, Long> {
    Optional<Venue> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}
