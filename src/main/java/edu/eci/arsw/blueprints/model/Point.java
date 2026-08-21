package edu.eci.arsw.blueprints.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Representación de una coordenada bidimensional (x, y) en el plano")
public record Point(
        @Schema(description = "Coordenada en el eje X", example = "10") int x,
        @Schema(description = "Coordenada en el eje Y", example = "20") int y
) { }
