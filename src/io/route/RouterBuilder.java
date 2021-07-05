package io.route;

import io.route.util.CharBuffer;

import java.util.*;
import java.util.stream.Collectors;

public class RouterBuilder {
    public static <T> Node<T> buildNode(List<RouterSetup.Rule<T>> rules) {
        List<Sequence<T>> sequences = rules.stream()
                .map(rule -> new Sequence<>(new LinkedList<>(rule.query().tokens()), rule))
                .toList();

        return buildNode(new RootToken(), sequences);
    }

    private static <T> Node<T> buildNode(Token start, List<Sequence<T>> sequences) {
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
                .map(entry -> buildNode(entry.getKey(), entry.getValue()))
                .toList();
        RouterSetup.Rule<T> terminalRule = sequences.size() == 1 ? sequences.get(0).rule : null;
        return new Node<>(start, nodes, terminalRule);
    }

    public static <T> Map<CharBuffer, T> buildQuickMatchIndex(List<RouterSetup.Rule<T>> rules) {
        return rules.stream()
                .filter(rule -> rule.query().tokens().size() == 1)
                .filter(rule -> rule.query().tokens().get(0) instanceof ConstToken)
                .collect(Collectors.toMap(
                        rule -> ((ConstToken) rule.query().tokens().get(0)).buffer(),
                        RouterSetup.Rule::handler
                ));
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
