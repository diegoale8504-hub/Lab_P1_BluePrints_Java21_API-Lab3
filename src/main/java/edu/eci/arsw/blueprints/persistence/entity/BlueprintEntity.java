package edu.eci.arsw.blueprints.persistence.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "blueprints")
public class BlueprintEntity {

    @EmbeddedId
    private BlueprintId id;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "blueprint_points",
        joinColumns = {
            @JoinColumn(name = "author", referencedColumnName = "author"),
            @JoinColumn(name = "name", referencedColumnName = "name")
        }
    )
    @OrderColumn(name = "point_order")
    private List<PointEmbeddable> points = new ArrayList<>();

    public BlueprintEntity() {
    }

    public BlueprintEntity(BlueprintId id, List<PointEmbeddable> points) {
        this.id = id;
        if (points != null) {
            this.points = points;
        }
    }

    public BlueprintId getId() {
        return id;
    }

    public void setId(BlueprintId id) {
        this.id = id;
    }

    public List<PointEmbeddable> getPoints() {
        return points;
    }

    public void setPoints(List<PointEmbeddable> points) {
        this.points = points;
    }
}
