package uz.epam.msa.resource.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.epam.msa.resource.domain.Resource;

@Repository
public interface ResourcesRepository extends JpaRepository<Resource, Integer> {}
