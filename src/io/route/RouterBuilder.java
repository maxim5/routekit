package io.route;

import io.route.util.CharBuffer;

import java.util.*;
import java.util.stream.Collectors;

public class RouterBuilder {
    private boolean quickMatchForConst = true;
    private boolean excludeConstInSA = true;

    public RouterBuilder setQuickMatchForConst(boolean quickMatchForConst) {
        this.quickMatchForConst = quickMatchForConst;
        return this;
    }

    public RouterBuilder setExcludeConstInSA(boolean excludeConstInSA) {
        this.excludeConstInSA = excludeConstInSA;
        return this;
    }

    public <T> Router<T> buildRouter(List<RouterSetup.Rule<T>> rules) {
        Map<CharBuffer, T> quickMatchIndex = buildQuickMatchIndex(rules);
        RouterBuilder.Node<T> root = buildStateAutomaton(rules);
        return new Router<>(quickMatchIndex, root);
    }

    /*package*/ <T> Map<CharBuffer, T> buildQuickMatchIndex(List<RouterSetup.Rule<T>> rules) {
        return (quickMatchForConst) ?
            rules.stream()
                .filter(RouterSetup.Rule::isConstant)
                .collect(Collectors.toMap(
                        rule -> ((ConstToken) rule.query().tokens().get(0)).buffer(),
                        RouterSetup.Rule::handler
                )) :
            Collections.emptyMap();
    }

    /*package*/ <T> Node<T> buildStateAutomaton(List<RouterSetup.Rule<T>> rules) {
        List<Sequence<T>> sequences = rules.stream()
                .filter(rule -> !excludeConstInSA || !rule.isConstant())
                .map(rule -> new Sequence<>(new LinkedList<>(rule.query().tokens()), rule))
                .toList();

        return buildNode(new RootToken(), sequences);
    }

    private static <T> Node<T> buildNode(Token start, List<Sequence<T>> sequences) {
        RouterSetup.Rule<T> terminalRule = sequences.stream()
                .filter(seq -> seq.tokens.isEmpty())
                .map(seq -> seq.rule)
                .findFirst()
                .orElse(null);

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
            return terminalRule != null;  // Note: non-leaf nodes can be terminal.
        }

        public boolean isLeaf() {
            return next.isEmpty();
        }
    }
}
