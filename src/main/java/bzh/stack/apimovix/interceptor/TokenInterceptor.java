package bzh.stack.apimovix.interceptor;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class TokenInterceptor implements HandlerInterceptor {

    @Autowired
    private ProfileRepository profilRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
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

        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }

        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // Vérification spéciale pour HyperAdminRequired
        if (hyperAdminRequired != null) {
            if ("123456789clement".equals(token)) {
                return true;
            }
            response.setStatus(HttpStatus.FORBIDDEN.value());
            return false;
        }

        // Vérification spéciale pour ImporterRequired
        if (importerRequired != null) {
            if ("YWY4NGQxYzllMDdiNDNmOGU2Y2Q5MjQ1ZjY3M2IyYTFjZTkwYjdkYTZmMmU4NGMzZDVmN2E5ODFjNGIyZTZkOA==".equals(token)) {
                return true;
            }
            response.setStatus(HttpStatus.FORBIDDEN.value());
            return false;
        }

        Optional<Profil> profilOpt = profilRepository.findByToken(token);
        if (profilOpt.isEmpty()) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }

        Profil profil = profilOpt.get();

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
} 