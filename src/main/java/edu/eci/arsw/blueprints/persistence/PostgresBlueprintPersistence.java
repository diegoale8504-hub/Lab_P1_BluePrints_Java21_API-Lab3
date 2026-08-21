package edu.eci.arsw.blueprints.persistence;

import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import edu.eci.arsw.blueprints.persistence.entity.BlueprintEntity;
import edu.eci.arsw.blueprints.persistence.entity.BlueprintId;
import edu.eci.arsw.blueprints.persistence.entity.PointEmbeddable;
import edu.eci.arsw.blueprints.persistence.repository.SpringDataBlueprintRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@Primary
public class PostgresBlueprintPersistence implements BlueprintPersistence {

    private final SpringDataBlueprintRepository repository;

    public PostgresBlueprintPersistence(SpringDataBlueprintRepository repository) {
        this.repository = repository;
    }

    @PostConstruct
    public void seedInitialData() {
        if (repository.count() == 0) {
            Blueprint bp1 = new Blueprint("john", "house",
                    List.of(new Point(0, 0), new Point(10, 0), new Point(10, 10), new Point(0, 10)));
            Blueprint bp2 = new Blueprint("john", "garage",
                    List.of(new Point(5, 5), new Point(15, 5), new Point(15, 15)));
            Blueprint bp3 = new Blueprint("jane", "garden",
                    List.of(new Point(2, 2), new Point(3, 4), new Point(6, 7)));

            repository.save(toEntity(bp1));
            repository.save(toEntity(bp2));
            repository.save(toEntity(bp3));
        }
    }

    @Override
    public void saveBlueprint(Blueprint bp) throws BlueprintPersistenceException {
        BlueprintId id = new BlueprintId(bp.getAuthor(), bp.getName());
        if (repository.existsById(id)) {
            throw new BlueprintPersistenceException("Blueprint already exists: " + bp.getAuthor() + ":" + bp.getName());
        }
        repository.save(toEntity(bp));
    }

    @Override
    public Blueprint getBlueprint(String author, String name) throws BlueprintNotFoundException {
        BlueprintId id = new BlueprintId(author, name);
        return repository.findById(id)
                .map(this::toDomain)
                .orElseThrow(() -> new BlueprintNotFoundException("Blueprint not found: %s/%s".formatted(author, name)));
    }

    @Override
    public Set<Blueprint> getBlueprintsByAuthor(String author) throws BlueprintNotFoundException {
        List<BlueprintEntity> list = repository.findByIdAuthor(author);
        if (list.isEmpty()) {
            throw new BlueprintNotFoundException("No blueprints for author: " + author);
        }
        return list.stream()
                .map(this::toDomain)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Blueprint> getAllBlueprints() {
        return repository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toSet());
    }

    @Override
    public void addPoint(String author, String name, int x, int y) throws BlueprintNotFoundException {
        BlueprintId id = new BlueprintId(author, name);
        BlueprintEntity entity = repository.findById(id)
                .orElseThrow(() -> new BlueprintNotFoundException("Blueprint not found: %s/%s".formatted(author, name)));
        entity.getPoints().add(new PointEmbeddable(x, y));
        repository.save(entity);
    }

    private Blueprint toDomain(BlueprintEntity entity) {
        List<Point> points = entity.getPoints().stream()
                .map(p -> new Point(p.getX(), p.getY()))
                .collect(Collectors.toList());
        return new Blueprint(entity.getId().getAuthor(), entity.getId().getName(), points);
    }

    private BlueprintEntity toEntity(Blueprint bp) {
        BlueprintId id = new BlueprintId(bp.getAuthor(), bp.getName());
        List<PointEmbeddable> points = bp.getPoints().stream()
                .map(p -> new PointEmbeddable(p.x(), p.y()))
                .collect(Collectors.toList());
        return new BlueprintEntity(id, points);
    }
}
