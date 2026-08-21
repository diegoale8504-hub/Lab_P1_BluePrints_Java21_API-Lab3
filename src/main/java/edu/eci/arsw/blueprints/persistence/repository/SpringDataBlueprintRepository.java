package edu.eci.arsw.blueprints.persistence.repository;

import edu.eci.arsw.blueprints.persistence.entity.BlueprintEntity;
import edu.eci.arsw.blueprints.persistence.entity.BlueprintId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataBlueprintRepository extends JpaRepository<BlueprintEntity, BlueprintId> {

    List<BlueprintEntity> findByIdAuthor(String author);
}
