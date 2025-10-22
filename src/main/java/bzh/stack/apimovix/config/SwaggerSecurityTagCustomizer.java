package bzh.stack.apimovix.config;

import java.lang.reflect.Method;

import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

import bzh.stack.apimovix.annotation.AdminRequired;
import bzh.stack.apimovix.annotation.HyperAdminRequired;
import bzh.stack.apimovix.annotation.ImporterRequired;
import bzh.stack.apimovix.annotation.MobileRequired;
import bzh.stack.apimovix.annotation.TokenNotRequired;
import bzh.stack.apimovix.annotation.TokenRequired;
import io.swagger.v3.oas.models.Operation;

@Component
public class SwaggerSecurityTagCustomizer implements OperationCustomizer {

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        Method method = handlerMethod.getMethod();
        Class<?> declaringClass = method.getDeclaringClass();

        // Vérifier les annotations sur la méthode et sur la classe
        if (isAnnotationPresent(method, declaringClass, HyperAdminRequired.class)) {
            operation.addTagsItem("🔴 HyperAdmin");
            operation.description((operation.getDescription() != null ? operation.getDescription() : "")
                + "\n\n**Accès requis:** HyperAdmin uniquement");
        } else if (isAnnotationPresent(method, declaringClass, AdminRequired.class)) {
            operation.addTagsItem("🟠 Admin");
            operation.description((operation.getDescription() != null ? operation.getDescription() : "")
                + "\n\n**Accès requis:** Admin ou HyperAdmin");
        } else if (isAnnotationPresent(method, declaringClass, ImporterRequired.class)) {
            operation.addTagsItem("🟡 Importer");
            operation.description((operation.getDescription() != null ? operation.getDescription() : "")
                + "\n\n**Accès requis:** Importer");
        } else if (isAnnotationPresent(method, declaringClass, MobileRequired.class)) {
            operation.addTagsItem("📱 Mobile");
            operation.description((operation.getDescription() != null ? operation.getDescription() : "")
                + "\n\n**Accès requis:** Application Mobile");
        } else if (isAnnotationPresent(method, declaringClass, TokenRequired.class)) {
            operation.addTagsItem("🔑 Token Required");
            operation.description((operation.getDescription() != null ? operation.getDescription() : "")
                + "\n\n**Accès requis:** Token d'authentification");
        } else if (isAnnotationPresent(method, declaringClass, TokenNotRequired.class)) {
            operation.addTagsItem("🟢 Public");
            operation.description((operation.getDescription() != null ? operation.getDescription() : "")
                + "\n\n**Accès:** Public (pas de token requis)");
        }

        return operation;
    }

    private boolean isAnnotationPresent(Method method, Class<?> declaringClass,
                                       Class<? extends java.lang.annotation.Annotation> annotationClass) {
        return method.isAnnotationPresent(annotationClass) || declaringClass.isAnnotationPresent(annotationClass);
    }
}
