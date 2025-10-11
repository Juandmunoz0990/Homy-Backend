package co.edu.uniquindio.application.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.uniquindio.application.Models.HostDetails;

@Repository
public interface HostDetailsRepository extends JpaRepository<HostDetails, Long> {
    
}
