package io.route;

import io.route.util.CharBuffer;

import java.util.Map;

public class Router<T> {
    private final RouterBuilder.Node<T> root;
    private final Map<CharBuffer, T> quickMatchIndex;

    public Router(RouterBuilder.Node<T> root, Map<CharBuffer, T> quickMatchIndex) {
        this.root = root;
        this.quickMatchIndex = quickMatchIndex;
    }

    public T routeOrNull(CharBuffer input) {
        T match = quickMatchIndex.get(input);
        if (match != null) {
            return match;
        }

        return null;
    }

    public T routeOrNull(String input) {
        return routeOrNull(new CharBuffer(input));
    }

    public T routeOrNull(CharSequence input) {
        return routeOrNull(new CharBuffer(input));
    }

    public T routeOrNull(char[] input) {
        return routeOrNull(new CharBuffer(input));
    }
}
