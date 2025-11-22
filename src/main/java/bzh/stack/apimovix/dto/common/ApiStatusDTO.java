package bzh.stack.apimovix.dto.common;

import lombok.Data;

@Data
public class ApiStatusDTO {
    private String version;
    private String dbVersion;
    private boolean dbConnected;
    private String databaseUrl;
    private String status;
    private String timestamp;
    private String environment;
    private double cpuUsage;
    private long totalMemory;
    private long usedMemory;
    private long freeMemory;

    public ApiStatusDTO() {
    }

    public ApiStatusDTO(String version, String dbVersion, boolean dbConnected, String databaseUrl, String status, String timestamp, String environment) {
        this.version = version;
        this.dbVersion = dbVersion;
        this.dbConnected = dbConnected;
        this.databaseUrl = databaseUrl;
        this.status = status;
        this.timestamp = timestamp;
        this.environment = environment;
    }

    public ApiStatusDTO(String version, String dbVersion, boolean dbConnected, String databaseUrl, String status, String timestamp, String environment,
            double cpuUsage, long totalMemory, long usedMemory, long freeMemory) {
        this.version = version;
        this.dbVersion = dbVersion;
        this.dbConnected = dbConnected;
        this.databaseUrl = databaseUrl;
        this.status = status;
        this.timestamp = timestamp;
        this.environment = environment;
        this.cpuUsage = cpuUsage;
        this.totalMemory = totalMemory;
        this.usedMemory = usedMemory;
        this.freeMemory = freeMemory;
    }
} 