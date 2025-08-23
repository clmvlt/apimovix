package bzh.stack.apimovix.controller;

import java.util.Optional;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import bzh.stack.apimovix.annotation.TokenRequired;
import bzh.stack.apimovix.dto.ors.DistanceRequestDTO;
import bzh.stack.apimovix.dto.ors.DistanceResponseDTO;
import bzh.stack.apimovix.dto.ors.OptimizeRequestDTO;
import bzh.stack.apimovix.dto.ors.OptimizeResponseDTO;
import bzh.stack.apimovix.dto.ors.RouteRequestDTO;
import bzh.stack.apimovix.dto.ors.RouteResponseDTO;
import bzh.stack.apimovix.service.ORSService;
import bzh.stack.apimovix.util.GLOBAL;
import bzh.stack.apimovix.util.MAPIR;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(path = "/ors", produces = MediaType.APPLICATION_JSON_VALUE)
@TokenRequired
@RequiredArgsConstructor
@ApiResponse(responseCode = "400", description = GLOBAL.ERROR_400, content = @Content)
@ApiResponse(responseCode = "401", description = GLOBAL.ERROR_401, content = @Content)
@Tag(name = "OpenRouteService", description = "API for route optimization and distance calculations using OpenRouteService")
public class ORSController {

    private final ORSService orsService;

    @PostMapping("/optimize")
    @Operation(summary = "Optimize route points", description = "Optimizes the order of points to minimize the total travel distance using OpenRouteService", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully optimized route points", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = OptimizeResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error - OpenRouteService unavailable or error occurred", content = @Content),
    })
    public ResponseEntity<?> optimize(
            @Parameter(description = "Request containing coordinates to optimize", required = true, schema = @Schema(implementation = OptimizeRequestDTO.class)) @RequestBody OptimizeRequestDTO requestDTO) {
        if (requestDTO.getCoordinates() == null || requestDTO.getCoordinates().size() < 2) {
            return MAPIR.fieldRequired("coordinates");
        }

        Optional<OptimizeResponseDTO> result = orsService.optimizePoints(requestDTO);
        if (result.isEmpty()) {
            return MAPIR.internalServerError();
        }

        return ResponseEntity.ok(result.get());
    }

    @PostMapping("/route")
    @Operation(summary = "Get route information", description = "Calculates detailed route information between multiple points using OpenRouteService", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully calculated route information", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = RouteResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error - OpenRouteService unavailable or error occurred", content = @Content),
    })
    public ResponseEntity<?> getRoute(
            @Parameter(description = "Request containing coordinates for route calculation", required = true, schema = @Schema(implementation = RouteRequestDTO.class)) @RequestBody RouteRequestDTO requestDTO) {
        if (requestDTO.getCoordinates() == null || requestDTO.getCoordinates().size() < 2) {
            return MAPIR.fieldRequired("coordinates");
        }

        Optional<RouteResponseDTO> result = orsService.getRouteInfo(requestDTO);

        if (result.isEmpty()) {
            return MAPIR.internalServerError();
        }

        return ResponseEntity.ok(result.get());
    }

    @PostMapping("/distances")
    @Operation(summary = "Calculate driving distances", description = "Calculates driving distances from a starting point to multiple destinations using OpenRouteService", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully calculated driving distances", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = DistanceResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error - OpenRouteService unavailable or error occurred", content = @Content),
    })
    public ResponseEntity<?> getDistances(
            @Parameter(description = "Request containing start point and destination coordinates", required = true, schema = @Schema(implementation = DistanceRequestDTO.class)) @RequestBody DistanceRequestDTO requestDTO) {
        if (requestDTO.getCoordinates() == null || requestDTO.getCoordinates().size() < 2) {
            return MAPIR.fieldRequired("coordinates");
        }
        if (requestDTO.getStart() == null) {
            return MAPIR.fieldRequired("start");
        }

        Optional<DistanceResponseDTO> result = orsService.getDrivingDistancesFromPoint(requestDTO);
        if (result.isEmpty()) {
            return MAPIR.internalServerError();
        }

        return ResponseEntity.ok(result.get());
    }
}