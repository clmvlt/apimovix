package bzh.stack.apimovix.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import bzh.stack.apimovix.dto.common.CoordsDTO;
import bzh.stack.apimovix.dto.ors.DistanceRequestDTO;
import bzh.stack.apimovix.dto.ors.DistanceResponseDTO;
import bzh.stack.apimovix.dto.ors.OptimizeRequestDTO;
import bzh.stack.apimovix.dto.ors.OptimizeResponseDTO;
import bzh.stack.apimovix.dto.ors.RouteRequestDTO;
import bzh.stack.apimovix.dto.ors.RouteResponseDTO;
import bzh.stack.apimovix.dto.ors.RouteSegmentDTO;
import bzh.stack.apimovix.model.Command;
import bzh.stack.apimovix.model.Tour;

/**
 * Service pour interagir avec l'API OpenRouteService (ORS)
 */
@Service
public class ORSService {
    private static final Logger logger = LoggerFactory.getLogger(ORSService.class);
    private static final String BASE_URL = "http://ors.stack.bzh/v2/directions/driving-car";
    private static final String OPTIMIZE_URL = "http://ors.stack.bzh/optimize";
    private static final double POLYLINE_FACTOR = 1e5;
    private static final double DISTANCE_CONVERSION_FACTOR = 100.0;
    private static final double DURATION_CONVERSION_FACTOR = 60.0;
    private static final double ROUNDING_FACTOR = 10.0;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public ORSService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Décode une chaîne polyline en liste de coordonnées
     * 
     * @param encoded La chaîne polyline encodée
     * @return Liste des coordonnées décodées
     */
    private List<List<Double>> decodePolyline(String encoded) {
        if (encoded == null || encoded.isEmpty()) {
            return new ArrayList<>();
        }

        List<List<Double>> points = new ArrayList<>();
        int index = 0;
        int lat = 0;
        int lng = 0;
        int length = encoded.length();

        while (index < length) {
            lat += decodePolylineValue(encoded, index);
            index = getNextIndex(encoded, index);

            lng += decodePolylineValue(encoded, index);
            index = getNextIndex(encoded, index);

            points.add(List.of(lng / POLYLINE_FACTOR, lat / POLYLINE_FACTOR));
        }

        return points;
    }

    private int decodePolylineValue(String encoded, int index) {
        int result = 1;
        int shift = 0;
        int b;
        do {
            b = encoded.charAt(index++) - 63 - 1;
            result += b << shift;
            shift += 5;
        } while (b >= 0x1f);
        return (result & 1) != 0 ? ~(result >> 1) : (result >> 1);
    }

    private int getNextIndex(String encoded, int currentIndex) {
        while (currentIndex < encoded.length() && encoded.charAt(currentIndex) - 63 - 1 >= 0x1f) {
            currentIndex++;
        }
        return currentIndex + 1;
    }

    /**
     * Récupère les informations d'itinéraire
     * 
     * @param request Les coordonnées de l'itinéraire
     * @return Les informations d'itinéraire
     */
    public Optional<RouteResponseDTO> getRouteInfo(RouteRequestDTO request) {
        try {
            List<List<Double>> reversedCoordinates = request.getCoordinates().stream()
                    .map(coord -> List.of(coord.getLon(), coord.getLat()))
                    .toList();

            Map<String, Object> body = Map.of(
                    "coordinates", reversedCoordinates,
                    "geometry", true);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            Map<String, Object> response = restTemplate.exchange(
                    BASE_URL,
                    HttpMethod.POST,
                    requestEntity,
                    new ParameterizedTypeReference<Map<String, Object>>() {
                    }).getBody();

            if (response == null || !response.containsKey("routes")) {
                logger.warn("Aucun itinéraire trouvé dans la réponse");
                return Optional.empty();
            }

            List<Map<String, Object>> routes = objectMapper.convertValue(response.get("routes"),
                    new TypeReference<List<Map<String, Object>>>() {
                    });

            if (routes.isEmpty()) {
                logger.warn("Liste d'itinéraires vide");
                return Optional.empty();
            }

            RouteResponseDTO routeResponse = createRouteResponse(routes.get(0), request.getReturnCoords());

            // Calculer les segments entre chaque point si plus de 2 points
            if (request.getCoordinates() != null && request.getCoordinates().size() > 1) {
                routeResponse.setSegments(calculateSegments(request.getCoordinates()));
            }

            return Optional.of(routeResponse);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération de l'itinéraire: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    private RouteResponseDTO createRouteResponse(Map<String, Object> route, boolean returnCoords) {
        Map<String, Object> summary = objectMapper.convertValue(route.get("summary"),
                new TypeReference<Map<String, Object>>() {
                });

        RouteResponseDTO responseDTO = new RouteResponseDTO();

        // Vérification que le summary n'est pas null
        if (summary == null) {
            logger.warn("Le summary de l'itinéraire est null, utilisation de valeurs par défaut");
            responseDTO.setDistance(0.0);
            responseDTO.setDuration(0.0);
            responseDTO.setGeometry((String) route.get("geometry"));
            responseDTO.setCoordinates(returnCoords ? decodePolyline(responseDTO.getGeometry()) : new ArrayList<>());
            return responseDTO;
        }

        // Gestion sécurisée de la distance avec valeur par défaut
        Object distanceObj = summary.get("distance");
        double distance = 0.0;
        if (distanceObj != null) {
            if (distanceObj instanceof Number) {
                distance = ((Number) distanceObj).doubleValue();
            } else if (distanceObj instanceof String) {
                try {
                    distance = Double.parseDouble((String) distanceObj);
                } catch (NumberFormatException e) {
                    logger.warn("Impossible de parser la distance: {}", distanceObj);
                }
            }
        }
        responseDTO.setDistance(round(distance / 1000.0 * DISTANCE_CONVERSION_FACTOR)
                / DISTANCE_CONVERSION_FACTOR);

        // Gestion sécurisée de la durée avec valeur par défaut
        Object durationObj = summary.get("duration");
        double duration = 0.0;
        if (durationObj != null) {
            if (durationObj instanceof Number) {
                duration = ((Number) durationObj).doubleValue();
            } else if (durationObj instanceof String) {
                try {
                    duration = Double.parseDouble((String) durationObj);
                } catch (NumberFormatException e) {
                    logger.warn("Impossible de parser la durée: {}", durationObj);
                }
            }
        }
        responseDTO.setDuration(round(duration / DURATION_CONVERSION_FACTOR * ROUNDING_FACTOR)
                / ROUNDING_FACTOR);

        responseDTO.setGeometry((String) route.get("geometry"));
        responseDTO.setCoordinates(returnCoords ? decodePolyline(responseDTO.getGeometry()) : new ArrayList<>());

        return responseDTO;
    }

    private double round(double value) {
        return Math.round(value);
    }

    /**
     * Calcule les segments (durées et distances) entre chaque point consécutif
     *
     * @param coordinates Liste des coordonnées
     * @return Liste des segments avec durées et distances
     */
    private List<RouteSegmentDTO> calculateSegments(List<CoordsDTO> coordinates) {
        List<RouteSegmentDTO> segments = new ArrayList<>();

        if (coordinates == null || coordinates.size() < 2) {
            return segments;
        }

        double cumulativeDistance = 0.0;
        double cumulativeDuration = 0.0;

        // Premier segment (point de départ)
        RouteSegmentDTO firstSegment = new RouteSegmentDTO();
        firstSegment.setCoord(coordinates.get(0));
        firstSegment.setDistance(0.0);
        firstSegment.setDuration(0.0);
        firstSegment.setCumulativeDistance(0.0);
        firstSegment.setCumulativeDuration(0.0);
        segments.add(firstSegment);

        // Calculer pour chaque segment suivant
        for (int i = 1; i < coordinates.size(); i++) {
            CoordsDTO from = coordinates.get(i - 1);
            CoordsDTO to = coordinates.get(i);

            RouteSegmentDTO segment = new RouteSegmentDTO();
            segment.setCoord(to);

            // Calculer la distance et durée depuis le point précédent
            RouteRequestDTO segmentRequest = new RouteRequestDTO();
            segmentRequest.setCoordinates(List.of(from, to));
            segmentRequest.setReturnCoords(false);

            Optional<RouteResponseDTO> segmentRoute = getSegmentRoute(segmentRequest);

            if (segmentRoute.isPresent()) {
                double segmentDistance = segmentRoute.get().getDistance();
                double segmentDuration = segmentRoute.get().getDuration();

                segment.setDistance(segmentDistance);
                segment.setDuration(segmentDuration);

                cumulativeDistance += segmentDistance;
                cumulativeDuration += segmentDuration;
            } else {
                segment.setDistance(0.0);
                segment.setDuration(0.0);
            }

            segment.setCumulativeDistance(cumulativeDistance);
            segment.setCumulativeDuration(cumulativeDuration);
            segments.add(segment);
        }

        return segments;
    }

    /**
     * Récupère les informations d'un segment sans recalculer les segments internes
     * (évite la récursion infinie)
     */
    private Optional<RouteResponseDTO> getSegmentRoute(RouteRequestDTO request) {
        try {
            List<List<Double>> reversedCoordinates = request.getCoordinates().stream()
                    .map(coord -> List.of(coord.getLon(), coord.getLat()))
                    .toList();

            Map<String, Object> body = Map.of(
                    "coordinates", reversedCoordinates,
                    "geometry", false);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            Map<String, Object> response = restTemplate.exchange(
                    BASE_URL,
                    HttpMethod.POST,
                    requestEntity,
                    new ParameterizedTypeReference<Map<String, Object>>() {
                    }).getBody();

            if (response == null || !response.containsKey("routes")) {
                return Optional.empty();
            }

            List<Map<String, Object>> routes = objectMapper.convertValue(response.get("routes"),
                    new TypeReference<List<Map<String, Object>>>() {
                    });

            if (routes.isEmpty()) {
                return Optional.empty();
            }

            return Optional.of(createRouteResponse(routes.get(0), false));
        } catch (Exception e) {
            logger.debug("Erreur lors du calcul d'un segment: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Optimise l'ordre des points pour minimiser la distance totale
     * 
     * @param request Les points à optimiser
     * @return L'ordre optimisé des points
     */
    public Optional<OptimizeResponseDTO> optimizePoints(OptimizeRequestDTO request) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = Map.of("points", request.getCoordinates());
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            Map<String, Object> response = restTemplate.exchange(
                    OPTIMIZE_URL,
                    HttpMethod.POST,
                    requestEntity,
                    new ParameterizedTypeReference<Map<String, Object>>() {
                    }).getBody();

            if (response == null || !response.containsKey("optimized_order")) {
                logger.warn("Aucun ordre optimisé trouvé dans la réponse");
                return Optional.empty();
            }

            return Optional.of(createOptimizeResponse(response));
        } catch (Exception e) {
            logger.error("Erreur lors de l'optimisation des points: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    private OptimizeResponseDTO createOptimizeResponse(Map<String, Object> response) {
        OptimizeResponseDTO responseDTO = new OptimizeResponseDTO();
        Object optimizedOrder = response.get("optimized_order");

        if (optimizedOrder instanceof List<?>) {
            responseDTO.setOptimizedOrder(((List<?>) optimizedOrder).stream()
                    .map(Object::toString)
                    .toList());
        }

        responseDTO.setGeometry((String) response.get("geometry"));
        return responseDTO;
    }

    /**
     * Calcule les distances de conduite à partir d'un point de départ
     * 
     * @param request Les coordonnées de départ et d'arrivée
     * @return Les distances calculées
     */
    public Optional<DistanceResponseDTO> getDrivingDistancesFromPoint(DistanceRequestDTO request) {
        try {
            DistanceResponseDTO responseDTO = new DistanceResponseDTO();
            List<RouteResponseDTO> distances = request.getCoordinates().stream()
                    .map(coord -> calculateDistanceForCoordinate(coord, request.getStart()))
                    .toList();

            responseDTO.setDistances(distances);
            return Optional.of(responseDTO);
        } catch (Exception e) {
            logger.error("Erreur lors du calcul des distances: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    private RouteResponseDTO calculateDistanceForCoordinate(CoordsDTO coord, CoordsDTO start) {
        if (isSameCoordinate(coord, start)) {
            return createZeroDistanceResponse(coord);
        }

        RouteRequestDTO routeRequest = new RouteRequestDTO();
        routeRequest.setCoordinates(List.of(start, coord));

        return getRouteInfo(routeRequest)
                .map(routeInfo -> {
                    routeInfo.setCoord(coord);
                    return routeInfo;
                })
                .orElseGet(() -> createEmptyRouteResponse(coord));
    }

    private boolean isSameCoordinate(CoordsDTO coord1, CoordsDTO coord2) {
        return coord1.getLat().equals(coord2.getLat()) &&
                coord1.getLon().equals(coord2.getLon());
    }

    private RouteResponseDTO createZeroDistanceResponse(CoordsDTO coord) {
        RouteResponseDTO response = new RouteResponseDTO();
        response.setDistance(0.0);
        response.setDuration(0.0);
        response.setCoordinates(new ArrayList<>());
        response.setCoord(coord);
        return response;
    }

    private RouteResponseDTO createEmptyRouteResponse(CoordsDTO coord) {
        RouteResponseDTO response = new RouteResponseDTO();
        response.setCoord(coord);
        return response;
    }

    public Optional<RouteResponseDTO> calculateTourRoute(Tour tour) {
        RouteRequestDTO routeRequestDTO = new RouteRequestDTO();
        
        CoordsDTO startCoords = new CoordsDTO();
        startCoords.setId("start");
        startCoords.setLat(tour.getAccount().getLatitude());
        startCoords.setLon(tour.getAccount().getLongitude());
        
        List<CoordsDTO> allCoordinates = new ArrayList<>();
        allCoordinates.add(startCoords); 
        
        allCoordinates.addAll(tour.getCommands().stream()
                .map(command -> {
                    CoordsDTO coords = new CoordsDTO();
                    coords.setLat(command.getPharmacy().getLatitude());
                    coords.setLon(command.getPharmacy().getLongitude());
                    coords.setId(command.getId().toString());
                    return coords;
                })
                .toList());
                
        allCoordinates.add(startCoords);
        
        routeRequestDTO.setCoordinates(allCoordinates);
        routeRequestDTO.setReturnCoords(false);

        return getRouteInfo(routeRequestDTO);
    }

    /**
     * Calcule la distance en kilomètres entre la pharmacie d'une commande et le compte de l'expéditeur
     * 
     * @param command La commande contenant la pharmacie et l'expéditeur
     * @return La distance en kilomètres, ou Optional.empty() en cas d'erreur
     */
    public Optional<Double> calculateCommandDistance(Command command) {
        
        try {
            // Vérification des paramètres
            if (command == null || command.getPharmacy() == null || 
                command.getSender() == null || command.getSender().getAccount() == null) {
                logger.warn("Commande, pharmacie ou compte manquant pour le calcul de distance");
                return Optional.empty();
            }

            // Récupération des coordonnées depuis la commande
            Double pharmacyLatitude = command.getPharmacy().getLatitude();
            Double pharmacyLongitude = command.getPharmacy().getLongitude();
            Double accountLatitude = command.getSender().getAccount().getLatitude();
            Double accountLongitude = command.getSender().getAccount().getLongitude();

            // Vérification des coordonnées
            if (pharmacyLatitude == null || pharmacyLongitude == null || 
                accountLatitude == null || accountLongitude == null) {
                logger.warn("Coordonnées manquantes pour le calcul de distance");
                return Optional.empty();
            }

            // Création des coordonnées
            CoordsDTO pharmacyCoords = new CoordsDTO();
            pharmacyCoords.setLat(pharmacyLatitude);
            pharmacyCoords.setLon(pharmacyLongitude);
            pharmacyCoords.setId("pharmacy");

            CoordsDTO accountCoords = new CoordsDTO();
            accountCoords.setLat(accountLatitude);
            accountCoords.setLon(accountLongitude);
            accountCoords.setId("account");

            // Création de la requête d'itinéraire
            RouteRequestDTO routeRequest = new RouteRequestDTO();
            routeRequest.setCoordinates(List.of(accountCoords, pharmacyCoords));
            routeRequest.setReturnCoords(false);

            // Récupération de l'itinéraire
            Optional<RouteResponseDTO> routeResponse = getRouteInfo(routeRequest);
            
            if (routeResponse.isPresent()) {
                double distanceInKm = routeResponse.get().getDistance();
                logger.debug("Distance calculée entre pharmacie et compte pour la commande {}: {} km", 
                           command.getId(), distanceInKm);
                return Optional.of(distanceInKm);
            } else {
                logger.warn("Impossible de calculer la distance entre la pharmacie et le compte pour la commande {}", 
                           command.getId());
                return Optional.empty();
            }
            
        } catch (Exception e) {
            logger.error("Erreur lors du calcul de la distance entre pharmacie et compte pour la commande {}: {}", 
                        command != null ? command.getId() : "null", e.getMessage(), e);
            return Optional.empty();
        }
    }
}