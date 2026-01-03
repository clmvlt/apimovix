package bzh.stack.apimovix.interceptor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Collections;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import bzh.stack.apimovix.annotation.AdminRequired;
import bzh.stack.apimovix.annotation.HyperAdminRequired;
import bzh.stack.apimovix.annotation.ImporterRequired;
import bzh.stack.apimovix.annotation.MobileRequired;
import bzh.stack.apimovix.annotation.TokenNotRequired;
import bzh.stack.apimovix.annotation.TokenRequired;
import bzh.stack.apimovix.model.Profil;
import bzh.stack.apimovix.repository.ProfileRepository;
import bzh.stack.apimovix.service.ImporterTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class TokenInterceptor implements HandlerInterceptor {

    private static final String BETA_API_URL = "https://api.beta.movix.fr";

    @Autowired
    private ProfileRepository profilRepository;

    @Autowired
    private ImporterTokenService importerTokenService;

    @Value("${server.address}")
    private String serverAddress;

    private boolean isProduction() {
        return !serverAddress.contains("192.168.")
            && !serverAddress.contains("127.0.")
            && !serverAddress.contains("localhost")
            && !serverAddress.contains("0.0.0.0");
    }

    private boolean isBetaApi() {
        return serverAddress.contains("api.beta.movix.fr") || serverAddress.contains("beta");
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Vérifier si on doit proxifier vers beta (seulement en prod et pas sur l'API beta elle-même)
        String token = request.getHeader("Authorization");
        if (token != null && !token.isEmpty()) {
            String cleanToken = token.startsWith("Bearer ") ? token.substring(7) : token;
            // Si c'est un token importer avec isBetaProxy=true et qu'on est en prod (pas beta), on proxy vers beta
            if (isProduction() && !isBetaApi() && importerTokenService.isBetaProxyToken(cleanToken)) {
                proxyToBeta(request, response);
                return false;
            }
        }

        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        
        // Vérifier si la méthode a l'annotation TokenNotRequired
        TokenNotRequired tokenNotRequired = handlerMethod.getMethodAnnotation(TokenNotRequired.class);
        if (tokenNotRequired != null) {
            return true;
        }
        
        // Vérifier les annotations sur la méthode
        TokenRequired tokenRequired = handlerMethod.getMethodAnnotation(TokenRequired.class);
        AdminRequired adminRequired = handlerMethod.getMethodAnnotation(AdminRequired.class);
        MobileRequired mobileRequired = handlerMethod.getMethodAnnotation(MobileRequired.class);
        ImporterRequired importerRequired = handlerMethod.getMethodAnnotation(ImporterRequired.class);
        HyperAdminRequired hyperAdminRequired = handlerMethod.getMethodAnnotation(HyperAdminRequired.class);

        if (tokenRequired == null && adminRequired == null && mobileRequired == null && importerRequired == null && hyperAdminRequired == null) {
            tokenRequired = handlerMethod.getBeanType().getAnnotation(TokenRequired.class);
            adminRequired = handlerMethod.getBeanType().getAnnotation(AdminRequired.class);
            mobileRequired = handlerMethod.getBeanType().getAnnotation(MobileRequired.class);
            importerRequired = handlerMethod.getBeanType().getAnnotation(ImporterRequired.class);
            hyperAdminRequired = handlerMethod.getBeanType().getAnnotation(HyperAdminRequired.class);
        }

        if (tokenRequired == null && adminRequired == null && mobileRequired == null && importerRequired == null && hyperAdminRequired == null) {
            return true;
        }

        // Récupérer le token (déjà lu en début de méthode)
        if (token == null || token.isEmpty()) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }

        // Nettoyer le token si pas déjà fait
        String cleanToken = token.startsWith("Bearer ") ? token.substring(7) : token;

        // Vérification spéciale pour HyperAdminRequired
        if (hyperAdminRequired != null) {
            if ("123456789clement".equals(cleanToken)) {
                return true;
            }
            response.setStatus(HttpStatus.FORBIDDEN.value());
            return false;
        }

        // Vérification spéciale pour ImporterRequired
        if (importerRequired != null) {
            var importerToken = importerTokenService.findActiveByToken(cleanToken);
            if (importerToken.isPresent()) {
                // Mettre à jour la date de dernière utilisation
                importerTokenService.updateLastUsed(cleanToken);
                // Ajouter le token à la requête pour validation ultérieure
                request.setAttribute("importerToken", importerToken.get());
                return true;
            }
            response.setStatus(HttpStatus.FORBIDDEN.value());
            return false;
        }

        Optional<Profil> profilOpt = profilRepository.findByToken(cleanToken);
        if (profilOpt.isEmpty()) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }

        Profil profil = profilOpt.get();

        // Vérifier que le profil est actif
        if (Boolean.FALSE.equals(profil.getIsActive())) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            return false;
        }

        // Vérifier que l'account associé est actif
        if (profil.getAccount() != null && Boolean.FALSE.equals(profil.getAccount().getIsActive())) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            return false;
        }

        // Vérifier les autorisations selon la hiérarchie
        if (adminRequired != null) {
            // Pour AdminRequired, seul isAdmin est accepté
            if (!profil.getIsAdmin()) {
                response.setStatus(HttpStatus.FORBIDDEN.value());
                return false;
            }
        } else if (tokenRequired != null) {
            // Pour TokenRequired, isAdmin ou isWeb sont acceptés
            if (!profil.getIsAdmin() && !profil.getIsWeb()) {
                response.setStatus(HttpStatus.FORBIDDEN.value());
                return false;
            }
        } else if (mobileRequired != null) {
            // Pour MobileRequired, tous les niveaux sont acceptés
            if (!profil.getIsAdmin() && !profil.getIsWeb() && !profil.getIsMobile()) {
                response.setStatus(HttpStatus.FORBIDDEN.value());
                return false;
            }
        }

        // Ajouter le profil à la requête
        request.setAttribute("profil", profil);
        return true;
    }

    /**
     * Proxy la requête vers l'API beta (api.beta.movix.fr)
     */
    private void proxyToBeta(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Construire l'URL cible
        String targetUrl = BETA_API_URL + request.getRequestURI();
        String queryString = request.getQueryString();
        if (queryString != null && !queryString.isEmpty()) {
            targetUrl += "?" + queryString;
        }

        // Créer la connexion
        HttpURLConnection connection = (HttpURLConnection) URI.create(targetUrl).toURL().openConnection();
        connection.setRequestMethod(request.getMethod());
        connection.setConnectTimeout(30000);
        connection.setReadTimeout(60000);

        // Copier les headers de la requête originale
        Collections.list(request.getHeaderNames()).forEach(headerName -> {
            // Ne pas copier les headers de connexion/host
            if (!"host".equalsIgnoreCase(headerName)
                && !"connection".equalsIgnoreCase(headerName)
                && !"content-length".equalsIgnoreCase(headerName)) {
                connection.setRequestProperty(headerName, request.getHeader(headerName));
            }
        });

        // Si la requête a un body (POST, PUT, PATCH)
        if ("POST".equalsIgnoreCase(request.getMethod())
            || "PUT".equalsIgnoreCase(request.getMethod())
            || "PATCH".equalsIgnoreCase(request.getMethod())) {
            connection.setDoOutput(true);
            try (InputStream requestBody = request.getInputStream();
                 OutputStream connectionOutput = connection.getOutputStream()) {
                requestBody.transferTo(connectionOutput);
            }
        }

        // Récupérer la réponse
        int responseCode = connection.getResponseCode();
        response.setStatus(responseCode);

        // Copier les headers de réponse
        connection.getHeaderFields().forEach((key, values) -> {
            if (key != null && !"transfer-encoding".equalsIgnoreCase(key)) {
                values.forEach(value -> response.addHeader(key, value));
            }
        });

        // Copier le body de la réponse
        try (InputStream responseStream = responseCode >= 400
                ? connection.getErrorStream()
                : connection.getInputStream();
             OutputStream responseOutput = response.getOutputStream()) {
            if (responseStream != null) {
                responseStream.transferTo(responseOutput);
            }
        }
    }
} 