package io.routekit;

import io.routekit.util.CharBuffer;
import io.routekit.util.MutableCharBuffer;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Router<T> {
    private static final Logger log = Logger.getLogger("RouteKit");

    private final Map<CharBuffer, T> quickMatchIndex;
    private final Node<T> root;

    public Router(Map<CharBuffer, T> quickMatchIndex, Node<T> root) {
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
            log.log(Level.FINEST, () -> "Routing `%s`: return immediately from the quick-match index".formatted(input));
            return new Match<>(match, Collections.emptyMap());
        }

        return navigate(input, root);
    }

    private static <T> Match<T> navigate(CharBuffer input, Node<T> current) {
        MutableCharBuffer buffer = input.mutable();
        Map<String, CharBuffer> vars = new LinkedHashMap<>();  // preserve the order

        while (buffer.isNotEmpty()) {
            int maxMatch = -1;
            Node<T> maxNode = null;
            for (Node<T> next : current.next) {  // No allocations: https://stackoverflow.com/a/3433775
                int matchLength = next.token.match(buffer);
                if (matchLength > maxMatch) {
                    maxMatch = matchLength;
                    maxNode = next;
                }
            }
            if (maxNode == null) {
                log.log(Level.FINEST, () -> "Routing `%s`: no continuation found at `%s`".formatted(input, buffer));
                return null;  // no continuation found
            }
            if (maxNode.token instanceof Variable variable) {
                vars.put(variable.name(), buffer.substringUntil(maxMatch));
            }
            buffer.offsetStart(maxMatch);
            current = maxNode;
        }

        if (!current.isTerminal()) {
            log.log(Level.FINEST, () -> "Routing `%s`: matches non-terminal node (middle of the rule)".formatted(input));
            return null;  // matches part of the rule
        }
        log.log(Level.FINEST, () -> "Routing `%s`: matches with variables %s".formatted(input, vars));
        return new Match<>(current.terminalRule.handler(), vars);
    }

    record Node<T>(Token token, Node<T>[] next, RouterSetup.Rule<T> terminalRule) {
        public boolean isTerminal() {
            return terminalRule != null;  // Note: non-leaf nodes can be terminal.
        }
    }
}
