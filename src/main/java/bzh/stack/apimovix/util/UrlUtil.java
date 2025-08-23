package bzh.stack.apimovix.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class UrlUtil {
    
    private static String baseUrl;

    @Value("${app.base-url}")
    public void setBaseUrl(String url) {
        if (url.contains("0.0.0.0")) {
            try {
                String localIp = InetAddress.getLocalHost().getHostAddress();
                baseUrl = url.replace("0.0.0.0", localIp);
            } catch (UnknownHostException e) {
                baseUrl = url;
            }
        } else {
            baseUrl = url;
        }
    }

    public static String getBaseUrl() {
        return baseUrl;
    }
} 