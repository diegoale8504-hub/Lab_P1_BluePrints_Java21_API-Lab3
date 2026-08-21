package edu.eci.arsw.blueprints.persistence;

import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PostgresBlueprintPersistenceTest {

    @Autowired
    private BlueprintPersistence persistence;

    @Test
    void testSaveAndGetBlueprint() throws BlueprintPersistenceException, BlueprintNotFoundException {
        Blueprint bp = new Blueprint("alice", "kitchen", List.of(new Point(1, 1), new Point(2, 2)));
        persistence.saveBlueprint(bp);

        Blueprint retrieved = persistence.getBlueprint("alice", "kitchen");
        assertNotNull(retrieved);
        assertEquals("alice", retrieved.getAuthor());
        assertEquals("kitchen", retrieved.getName());
        assertEquals(2, retrieved.getPoints().size());
    }

    @Test
    void testSaveDuplicateBlueprintThrowsException() throws BlueprintPersistenceException {
        Blueprint bp = new Blueprint("bob", "studio", List.of(new Point(0, 0)));
        persistence.saveBlueprint(bp);

        assertThrows(BlueprintPersistenceException.class, () -> persistence.saveBlueprint(bp));
    }

    @Test
    void testGetBlueprintsByAuthor() throws BlueprintPersistenceException, BlueprintNotFoundException {
        Blueprint bp1 = new Blueprint("carol", "p1", List.of(new Point(1, 0)));
        Blueprint bp2 = new Blueprint("carol", "p2", List.of(new Point(2, 0)));
        persistence.saveBlueprint(bp1);
        persistence.saveBlueprint(bp2);

        Set<Blueprint> set = persistence.getBlueprintsByAuthor("carol");
        assertEquals(2, set.size());
    }

    @Test
    void testAddPoint() throws BlueprintPersistenceException, BlueprintNotFoundException {
        Blueprint bp = new Blueprint("dave", "patio", List.of(new Point(1, 1)));
        persistence.saveBlueprint(bp);

        persistence.addPoint("dave", "patio", 5, 5);

        Blueprint updated = persistence.getBlueprint("dave", "patio");
        assertEquals(2, updated.getPoints().size());
        assertEquals(5, updated.getPoints().get(1).x());
        assertEquals(5, updated.getPoints().get(1).y());
    }
}
