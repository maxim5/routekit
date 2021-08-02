package io.routekit;

import io.routekit.util.CharArray;
import io.routekit.util.MutableCharArray;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Router<T> {
    private static final Logger log = Logger.getLogger("RouteKit");

    private final Map<CharArray, T> quickMatchIndex;
    private final Node<T> root;

    public Router(Map<CharArray, T> quickMatchIndex, Node<T> root) {
        this.quickMatchIndex = quickMatchIndex;
        this.root = root;
    }

    public Match<T> routeOrNull(String input) {
        return routeOrNull(new MutableCharArray(input));
    }

    public Match<T> routeOrNull(CharSequence input) {
        return routeOrNull(new MutableCharArray(input));
    }

    public Match<T> routeOrNull(char[] input) {
        return routeOrNull(new MutableCharArray(input));
    }

    public Match<T> routeOrNull(CharArray input) {
        T match = quickMatchIndex.get(input);
        if (match != null) {
            log.log(Level.FINEST, () -> "Routing `%s`: return immediately from the quick-match index".formatted(input));
            return new Match<>(match, Collections.emptyMap());
        }

        return navigate(input, root);
    }

    private static <T> Match<T> navigate(CharArray input, Node<T> current) {
        MutableCharArray array = input.mutableCopy();  // copy to avoid modifying the input
        Map<String, CharArray> vars = new LinkedHashMap<>();  // preserve the order

        while (array.isNotEmpty()) {
            int maxMatch = -1;
            Node<T> maxNode = null;
            for (Node<T> next : current.next) {  // No allocations: https://stackoverflow.com/a/3433775
                int matchLength = next.token.match(array);
                if (matchLength > maxMatch) {
                    maxMatch = matchLength;
                    maxNode = next;
                }
            }
            if (maxNode == null) {
                log.log(Level.FINEST, () -> "Routing `%s`: no continuation found at `%s`".formatted(input, array));
                return null;  // no continuation found
            }
            if (maxNode.token instanceof Variable variable) {
                vars.put(variable.name(), array.substringUntil(maxMatch));
            }
            array.offsetStart(maxMatch);
            current = maxNode;
        }

        if (!current.isTerminal()) {
            log.log(Level.FINEST, () -> "Routing `%s`: matches non-terminal node (middle of the rule)".formatted(input));
            return null;  // matches part of the rule
        }
        log.log(Level.FINEST, () -> "Routing `%s`: matches with variables %s".formatted(input, vars));
        return new Match<>(current.terminalRule.handler(), vars);
    }

    /*package*/ record Node<T>(Token token, Node<T>[] next, RouterSetup.Rule<T> terminalRule) {
        Node {
            List<Node<T>> variables = Arrays.stream(next).filter(Node::isVar).toList();
            if (variables.size() > 1) {
                throw new RouteException(
                        ("A node `%s` can not have several follow-up variable tokens rules: " +
                        "%s (one of the variables will never match)").formatted(token, variables)
                );
            }
        }

        public boolean isTerminal() {
            return terminalRule != null;  // Note: non-leaf nodes can be terminal.
        }

        private boolean isVar() {
            return token instanceof SeparableVariableToken;
        }
    }
}
