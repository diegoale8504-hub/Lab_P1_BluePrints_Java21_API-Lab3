package edu.eci.arsw.blueprints.controllers;

import edu.eci.arsw.blueprints.dto.ApiResponse;
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
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/v1/blueprints")
@Tag(name = "Blueprints", description = "Controlador REST para la gestión, consulta y modificación de planos arquitectónicos")
public class BlueprintsAPIController {

    private final BlueprintsServices services;

    public BlueprintsAPIController(BlueprintsServices services) {
        this.services = services;
    }

    // GET /api/v1/blueprints
    @Operation(summary = "Obtener todos los planos", description = "Retorna el conjunto de todos los planos arquitectónicos registrados en el sistema.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Conjunto de planos obtenido exitosamente",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = Blueprint.class))))
    })
    @GetMapping
    public ResponseEntity<ApiResponse<Set<Blueprint>>> getAll() {
        Set<Blueprint> bps = services.getAllBlueprints();
        return ResponseEntity.ok(ApiResponse.ok(bps));
    }

    // GET /api/v1/blueprints/{author}
    @Operation(summary = "Obtener planos por autor", description = "Retorna todos los planos arquitectónicos pertenecientes al autor especificado.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Planos del autor encontrados exitosamente",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = Blueprint.class)))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "No se encontraron planos para el autor especificado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"error\": \"No blueprints found for author: john\"}")))
    })
    @GetMapping("/{author}")
    public ResponseEntity<ApiResponse<Set<Blueprint>>> byAuthor(
            @Parameter(description = "Nombre o identificador del autor", example = "john", required = true)
            @PathVariable String author) {
        try {
            Set<Blueprint> bps = services.getBlueprintsByAuthor(author);
            return ResponseEntity.ok(ApiResponse.ok(bps));
        } catch (BlueprintNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, e.getMessage()));
        }
    }

    // GET /api/v1/blueprints/{author}/{bpname}
    @Operation(summary = "Obtener un plano por autor y nombre", description = "Retorna un plano específico identificado por el nombre del autor y el nombre del plano.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Plano encontrado exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Blueprint.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Plano no encontrado para el autor y nombre indicados",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"error\": \"Blueprint house not found for author john\"}")))
    })
    @GetMapping("/{author}/{bpname}")
    public ResponseEntity<ApiResponse<Blueprint>> byAuthorAndName(
            @Parameter(description = "Nombre o identificador del autor", example = "john", required = true)
            @PathVariable String author,
            @Parameter(description = "Nombre del plano", example = "house", required = true)
            @PathVariable String bpname) {
        try {
            Blueprint bp = services.getBlueprint(author, bpname);
            return ResponseEntity.ok(ApiResponse.ok(bp));
        } catch (BlueprintNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, e.getMessage()));
        }
    }

    // POST /api/v1/blueprints
    @Operation(summary = "Registrar un nuevo plano", description = "Crea y almacena un nuevo plano arquitectónico con su autor, nombre y lista de puntos iniciales.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Plano creado exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos de entrada inválidos en el cuerpo de la solicitud o plano ya existente",
                    content = @Content)
    })
    @PostMapping
    public ResponseEntity<ApiResponse<Blueprint>> add(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Estructura del nuevo plano a registrar",
                    required = true)
            @Valid @RequestBody NewBlueprintRequest req) {
        try {
            if (req.author() == null || req.author().isBlank() || req.name() == null || req.name().isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error(400, "El autor y el nombre no pueden estar vacíos"));
            }
            Blueprint bp = new Blueprint(req.author(), req.name(), req.points());
            services.addNewBlueprint(bp);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.created(bp));
        } catch (BlueprintPersistenceException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(400, e.getMessage()));
        }
    }

    // PUT /api/v1/blueprints/{author}/{bpname}/points
    @Operation(summary = "Agregar un punto a un plano existente", description = "Agrega un nuevo punto de coordenadas (x, y) al plano especificado por autor y nombre.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "202", description = "Punto agregado exitosamente al plano"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos del punto inválidos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "El plano especificado no existe",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"error\": \"Blueprint house not found for author john\"}")))
    })
    @PutMapping("/{author}/{bpname}/points")
    public ResponseEntity<ApiResponse<Blueprint>> addPoint(
            @Parameter(description = "Nombre o identificador del autor", example = "john", required = true)
            @PathVariable String author,
            @Parameter(description = "Nombre del plano", example = "house", required = true)
            @PathVariable String bpname,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Punto con coordenadas (x, y) a agregar",
                    required = true)
            @RequestBody Point p) {
        try {
            if (p == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error(400, "El punto no puede ser nulo"));
            }
            services.addPoint(author, bpname, p.x(), p.y());
            Blueprint updated = services.getBlueprint(author, bpname);
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(ApiResponse.accepted(updated));
        } catch (BlueprintNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, e.getMessage()));
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

