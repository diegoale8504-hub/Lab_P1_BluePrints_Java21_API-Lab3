package edu.eci.arsw.blueprints.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Schema(description = "Representación de un plano arquitectónico con autor, nombre y lista de puntos")
public class Blueprint {

    @Schema(description = "Nombre o identificador del autor del plano", example = "john")
    private String author;

    @Schema(description = "Nombre único del plano para el autor dado", example = "house")
    private String name;

    @Schema(description = "Lista de coordenadas de puntos que componen el plano")
    private final List<Point> points = new ArrayList<>();

    public Blueprint(String author, String name, List<Point> pts) {
        this.author = author;
        this.name = name;
        if (pts != null) points.addAll(pts);
    }

    public String getAuthor() { return author; }
    public String getName() { return name; }
    public List<Point> getPoints() { return Collections.unmodifiableList(points); }

    public void addPoint(Point p) { points.add(p); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Blueprint bp)) return false;
        return Objects.equals(author, bp.author) && Objects.equals(name, bp.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(author, name);
    }
}
