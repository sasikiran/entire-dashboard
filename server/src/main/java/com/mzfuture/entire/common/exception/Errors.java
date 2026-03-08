package com.mzfuture.entire.common.exception;

public class Errors {
    public static final ErrorCode BAD_REQUEST = ErrorCode.of(400, "BAD_REQUEST", "Invalid request format");

    public static final ErrorCode INVALID_ARGUMENT = ErrorCode.of(400, "INVALID_ARGUMENT", "Invalid argument");

    public static final ErrorCode UNAUTHORIZED = ErrorCode.of(401, "UNAUTHORIZED", "Unauthorized");

    public static final ErrorCode ACCESS_DENIED = ErrorCode.of(403, "ACCESS_DENIED", "Access denied");

    public static final ErrorCode NOT_FOUND = ErrorCode.of(404, "NOT_FOUND", "Resource not found");

    public static final ErrorCode INTERNAL_ERROR = ErrorCode.of(500, "INTERNAL_ERROR", "Internal server error");
}
