package io.route;

import io.route.util.CharBuffer;

import java.util.*;

public class RouterBuilder {
    public static <T> Node<T> build(List<RouterSetup.Rule<T>> rules) {
        List<Sequence<T>> sequences = rules.stream()
                .map(rule -> new Sequence<>(new LinkedList<>(rule.query().tokens()), rule))
                .toList();

        return build(new RootToken(), sequences);
    }

    private static <T> Node<T> build(Token start, List<Sequence<T>> sequences) {
        CharBuffer commonPrefix = sequences.stream()
                .map(seq -> seq.tokens.peek() instanceof ConstToken constToken ? constToken.buffer() : null)
                .reduce(null, (lhs, rhs) -> {
                    if (lhs == null) return rhs;
                    if (rhs == null) return lhs;
                    return lhs.substringUntil(lhs.matchCommon(rhs));
                });
        Token commonToken = (commonPrefix != null && commonPrefix.isNotEmpty()) ? new ConstToken(commonPrefix) : null;

        Map<Token, List<Sequence<T>>> group = new HashMap<>();
        for (Sequence<T> sequence : sequences) {
            Token peek = sequence.tokens.poll();
            if (commonToken != null && peek instanceof ConstToken constToken) {
                if (!constToken.buffer().equals(commonPrefix)) {
                    sequence.tokens.addFirst(new ConstToken(constToken.buffer().substringFrom(commonPrefix.length())));
                }
                group.computeIfAbsent(commonToken, __ -> new ArrayList<>()).add(sequence);
            } else if (peek != null) {
                group.computeIfAbsent(peek, __ -> new ArrayList<>()).add(sequence);
            }
        }

        List<Node<T>> nodes = group.entrySet().stream()
                .map(entry -> build(entry.getKey(), entry.getValue()))
                .toList();
        RouterSetup.Rule<T> terminalRule = sequences.size() == 1 ? sequences.get(0).rule : null;
        return new Node<>(start, nodes, terminalRule);
    }

    private static class RootToken implements Token {
        @Override
        public int match(CharBuffer buffer) {
            return 0;
        }

        @Override
        public String toString() {
            return "<root>";
        }
    }

    private record Sequence<T>(LinkedList<Token> tokens, RouterSetup.Rule<T> rule) {}

    record Node<T>(Token token, List<Node<T>> next, RouterSetup.Rule<T> terminalRule) {
        public boolean isTerminal() {
            return next.isEmpty();
        }
    }
}
