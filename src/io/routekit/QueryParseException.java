package io.routekit;

public class QueryParseException extends RuntimeException {
    public QueryParseException() {
    }

    public QueryParseException(String message) {
        super(message);
    }

    public static void failIf(boolean cond, String message) {
        if (cond) {
            throw new QueryParseException(message);
        }
    }
}
