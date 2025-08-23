package bzh.stack.apimovix.util;

import java.util.Map;

public final class GLOBAL {
    private GLOBAL() {}

    public static final String REQUIRED = "is required";
    public static final String CANNOT_BE_EMPTY = "can't be empty";
    public static final String POSITIVE = "must be a positive number";
    public static final String NUMBER = "must be a number";
    public static final String FLOAT = "must be a float";
    public static final String INVALID_FORMAT_UUID = "must be uuid format";
    public static final String ALREADY_USED = "is already used";
    public static final String INVALID_FORMAT_EMAIL = "is invalid email format";

    public static final String ERROR_400 = "Invalid input data";
    public static final String ERROR_401 = "Unauthorized";
    public static final String ERROR_403 = "Forbidden";
    public static final String ERROR_404 = "Ressource not found";
    public static final String ERROR_409 = "Conflict with data";
    public static final String DELETED = "Deleted";

    public static final String INVALID_FORMAT_PARAMETER = "Invalid parameter";
    public static final String PATH_INVALID_FORMAT_UUID = "Invalid uuid format";
    public static final String PATH_INVALID_FORMAT_DATE = "Invalid format date : " + PATTERNS.DATE;

    public static final Map<String, String> SEARCH_ALIASES = Map.of(
        "st", "saint",
        "saint", "st",
        "ste", "sainte",
        "'", " ",
        "sainte", "ste"
    );
} 