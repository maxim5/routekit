package io.routekit;

import io.routekit.util.CharBuffer;

import java.util.*;
import java.util.stream.Collectors;

public class RouterBuilder {
    private boolean quickMatchForConst = true;
    private boolean excludeConstFromFSM = true;

    public RouterBuilder setQuickMatchForConst(boolean quickMatchForConst) {
        this.quickMatchForConst = quickMatchForConst;
        return this;
    }

    public RouterBuilder setExcludeConstFromFSM(boolean excludeConstFromFSM) {
        this.excludeConstFromFSM = excludeConstFromFSM;
        return this;
    }

    public <T> Router<T> buildRouter(List<RouterSetup.Rule<T>> rules) {
        Map<CharBuffer, T> quickMatchIndex = buildQuickMatchIndex(rules);
        Router.Node<T> root = buildStateMachine(rules);
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

    /*package*/ <T> Router.Node<T> buildStateMachine(List<RouterSetup.Rule<T>> rules) {
        List<Sequence<T>> sequences = rules.stream()
                .filter(rule -> !excludeConstFromFSM || !rule.isConstant())
                .map(rule -> new Sequence<>(new LinkedList<>(rule.query().tokens()), rule))
                .toList();

        return buildNode(new RootToken(), sequences);
    }

    private static <T> Router.Node<T> buildNode(Token start, List<Sequence<T>> sequences) {
        RouterSetup.Rule<T> terminalRule = getTerminalRuleOrNull(sequences);
        retokenizeWithCommonPrefix(sequences);
        Router.Node<T>[] nodes = groupByPeekToken(sequences);
        return new Router.Node<>(start, nodes, terminalRule);
    }

    private static <T> RouterSetup.Rule<T> getTerminalRuleOrNull(List<Sequence<T>> sequences) {
        return sequences.stream()
                .filter(seq -> seq.tokens.isEmpty())
                .map(seq -> seq.rule)
                .findFirst()
                .orElse(null);
    }

    private static <T> Router.Node<T>[] groupByPeekToken(List<Sequence<T>> sequences) {
        Map<Token, List<Sequence<T>>> group = new LinkedHashMap<>();  // preserve the order
        for (Sequence<T> sequence : sequences) {
            Token peek = sequence.tokens.poll();
            group.computeIfAbsent(peek, key -> new ArrayList<>()).add(sequence);
        }

        // noinspection unchecked
        return group.entrySet().stream()
                .filter(entry -> entry.getKey() != null)
                .map(entry -> buildNode(entry.getKey(), entry.getValue()))
                .toArray(Router.Node[]::new);
    }

    private static <T> void retokenizeWithCommonPrefix(List<Sequence<T>> sequences) {
        CharBuffer commonPrefix = sequences.stream()
                .map(seq -> seq.tokens.peek() instanceof ConstToken constToken ? constToken.buffer() : null)
                .reduce(null, (lhs, rhs) -> {
                    if (lhs == null) return rhs;
                    if (rhs == null) return lhs;
                    return lhs.substringUntil(lhs.matchCommon(rhs));
                });

        if (commonPrefix != null && commonPrefix.isNotEmpty()) {
            Token commonToken = new ConstToken(commonPrefix);
            for (Sequence<T> sequence : sequences) {
                Token peek = sequence.tokens.peek();
                if (peek instanceof ConstToken constToken && !constToken.buffer().equals(commonPrefix)) {
                    sequence.tokens.poll();
                    sequence.tokens.addFirst(new ConstToken(constToken.buffer().substringFrom(commonPrefix.length())));
                    sequence.tokens.addFirst(commonToken);
                }
            }
        }
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
}
