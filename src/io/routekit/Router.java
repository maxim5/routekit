package io.routekit;

import io.routekit.util.CharBuffer;
import io.routekit.util.MutableCharBuffer;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class Router<T> {
    private final Map<CharBuffer, T> quickMatchIndex;
    private final RouterBuilder.Node<T> root;

    public Router(Map<CharBuffer, T> quickMatchIndex, RouterBuilder.Node<T> root) {
        this.quickMatchIndex = quickMatchIndex;
        this.root = root;
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

    public Match<T> routeOrNull(CharBuffer input) {
        T match = quickMatchIndex.get(input);
        if (match != null) {
            return new Match<>(match, Collections.emptyMap());
        }

        return navigate(input, root);
    }

    private static <T> Match<T> navigate(CharBuffer input, RouterBuilder.Node<T> current) {
        MutableCharBuffer buffer = input.mutable();
        Map<String, CharBuffer> vars = new LinkedHashMap<>();  // retain the order

        while (buffer.isNotEmpty()) {
            int maxMatch = -1;
            RouterBuilder.Node<T> maxNode = null;
            for (RouterBuilder.Node<T> next : current.next()) {  // No allocations: https://stackoverflow.com/a/3433775
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

        if (!current.isTerminal()) {
            return null;  // matches part of the rule
        }
        return new Match<>(current.terminalRule().handler(), vars);
    }

    record Match<T>(T handler, Map<String, CharBuffer> variables) {}
}
