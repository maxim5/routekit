package io.route;

import io.route.util.CharBuffer;
import io.route.util.MutableCharBuffer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Router<T> {
    private final RouterBuilder.Node<T> root;
    private final Map<CharBuffer, T> quickMatchIndex;

    public Router(RouterBuilder.Node<T> root, Map<CharBuffer, T> quickMatchIndex) {
        this.root = root;
        this.quickMatchIndex = quickMatchIndex;
    }

    public Match<T> routeOrNull(CharBuffer input) {
        T match = quickMatchIndex.get(input);
        if (match != null) {
            return new Match<>(match, Collections.emptyMap());
        }

        MutableCharBuffer buffer = input.mutable();
        RouterBuilder.Node<T> current = root;
        Map<String, CharBuffer> vars = new HashMap<>();

        while (!current.isTerminal()) {
            int maxMatch = 0;
            RouterBuilder.Node<T> maxNode = null;
            for (RouterBuilder.Node<T> next : current.next()) {
                int matchLength = next.token().match(buffer);
                if (matchLength > maxMatch) {
                    maxMatch = matchLength;
                    maxNode = next;
                }
            }
            if (maxNode == null) {
                return null;  // no continuation found
            }
            if (maxNode.token() instanceof Variable variable) {
                vars.put(variable.name(), buffer.substringUntil(maxMatch));
            }
            buffer.offsetStart(maxMatch);
            current = maxNode;
        }

        if (buffer.isEmpty()) {
            return null;  // does not match the full input
        }
        return new Match<>(current.terminalRule().handler(), vars);
    }

    public Match<T> routeOrNull(String input) {
        return routeOrNull(new MutableCharBuffer(input));
    }

    public Match<T> routeOrNull(CharSequence input) {
        return routeOrNull(new MutableCharBuffer(input));
    }

    public Match<T> routeOrNull(char[] input) {
        return routeOrNull(new MutableCharBuffer(input));
    }

    record Match<T>(T handler, Map<String, CharBuffer> variables) {}
}
