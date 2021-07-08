package io.routekit;

public class QueryParseException extends RouteException {
    public QueryParseException(String message) {
        super(message);
    }

    public static void failIf(boolean cond, String message) {
        if (cond) {
            throw new QueryParseException(message);
        }
    }
}
