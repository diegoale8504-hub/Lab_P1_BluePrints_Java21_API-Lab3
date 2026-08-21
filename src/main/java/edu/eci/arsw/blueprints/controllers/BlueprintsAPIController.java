package edu.eci.arsw.blueprints.controllers;

import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import edu.eci.arsw.blueprints.persistence.BlueprintNotFoundException;
import edu.eci.arsw.blueprints.persistence.BlueprintPersistenceException;
import edu.eci.arsw.blueprints.services.BlueprintsServices;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/blueprints")
@Tag(name = "Blueprints", description = "Controlador REST para la gestión, consulta y modificación de planos arquitectónicos")
public class BlueprintsAPIController {

    private final BlueprintsServices services;

    public BlueprintsAPIController(BlueprintsServices services) { this.services = services; }

    // GET /blueprints
    @Operation(summary = "Obtener todos los planos", description = "Retorna el conjunto de todos los planos arquitectónicos registrados en el sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conjunto de planos obtenido exitosamente",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = Blueprint.class))))
    })
    @GetMapping
    public ResponseEntity<Set<Blueprint>> getAll() {
        return ResponseEntity.ok(services.getAllBlueprints());
    }

    // GET /blueprints/{author}
    @Operation(summary = "Obtener planos por autor", description = "Retorna todos los planos arquitectónicos pertenecientes al autor especificado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Planos del autor encontrados exitosamente",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = Blueprint.class)))),
            @ApiResponse(responseCode = "404", description = "No se encontraron planos para el autor especificado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"error\": \"No blueprints found for author: john\"}")))
    })
    @GetMapping("/{author}")
    public ResponseEntity<?> byAuthor(
            @Parameter(description = "Nombre o identificador del autor", example = "john", required = true)
            @PathVariable String author) {
        try {
            return ResponseEntity.ok(services.getBlueprintsByAuthor(author));
        } catch (BlueprintNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    // GET /blueprints/{author}/{bpname}
    @Operation(summary = "Obtener un plano por autor y nombre", description = "Retorna un plano específico identificado por el nombre del autor y el nombre del plano.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Plano encontrado exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Blueprint.class))),
            @ApiResponse(responseCode = "404", description = "Plano no encontrado para el autor y nombre indicados",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"error\": \"Blueprint house not found for author john\"}")))
    })
    @GetMapping("/{author}/{bpname}")
    public ResponseEntity<?> byAuthorAndName(
            @Parameter(description = "Nombre o identificador del autor", example = "john", required = true)
            @PathVariable String author,
            @Parameter(description = "Nombre del plano", example = "house", required = true)
            @PathVariable String bpname) {
        try {
            return ResponseEntity.ok(services.getBlueprint(author, bpname));
        } catch (BlueprintNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    // POST /blueprints
    @Operation(summary = "Registrar un nuevo plano", description = "Crea y almacena un nuevo plano arquitectónico con su autor, nombre y lista de puntos iniciales.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Plano creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos en el cuerpo de la solicitud",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Ya existe un plano con el mismo autor y nombre",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"error\": \"The given blueprint already exists: john - house\"}")))
    })
    @PostMapping
    public ResponseEntity<?> add(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Estructura del nuevo plano a registrar",
                    required = true)
            @Valid @RequestBody NewBlueprintRequest req) {
        try {
            Blueprint bp = new Blueprint(req.author(), req.name(), req.points());
            services.addNewBlueprint(bp);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (BlueprintPersistenceException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        }
    }

    // PUT /blueprints/{author}/{bpname}/points
    @Operation(summary = "Agregar un punto a un plano existente", description = "Agrega un nuevo punto de coordenadas (x, y) al plano especificado por autor y nombre.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Punto agregado exitosamente al plano"),
            @ApiResponse(responseCode = "404", description = "El plano especificado no existe",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"error\": \"Blueprint house not found for author john\"}")))
    })
    @PutMapping("/{author}/{bpname}/points")
    public ResponseEntity<?> addPoint(
            @Parameter(description = "Nombre o identificador del autor", example = "john", required = true)
            @PathVariable String author,
            @Parameter(description = "Nombre del plano", example = "house", required = true)
            @PathVariable String bpname,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Punto con coordenadas (x, y) a agregar",
                    required = true)
            @RequestBody Point p) {
        try {
            services.addPoint(author, bpname, p.x(), p.y());
            return ResponseEntity.status(HttpStatus.ACCEPTED).build();
        } catch (BlueprintNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @Schema(description = "Objeto de transferencia para registrar un nuevo plano arquitectónico")
    public record NewBlueprintRequest(
            @Schema(description = "Nombre del autor del plano", example = "john", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotBlank String author,

            @Schema(description = "Nombre único del plano para este autor", example = "kitchen", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotBlank String name,

            @Schema(description = "Lista inicial de puntos del plano")
            @Valid java.util.List<Point> points
    ) { }
}

