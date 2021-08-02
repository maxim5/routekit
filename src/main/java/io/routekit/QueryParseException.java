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

    public static void failIf(boolean cond, String message, CharSequence input) {
        if (cond) {
            throw new QueryParseException("%s: %s".formatted(message, input));
        }
    }
}
