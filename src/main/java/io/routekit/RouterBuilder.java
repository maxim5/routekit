package io.routekit;

import io.routekit.util.CharArray;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class RouterBuilder {
    private static final Logger log = Logger.getLogger("RouteKit");

    public static final char DEFAULT_SEPARATOR = '/';
    public static final int DEFAULT_MIN_COMMON_PREFIX = 1;

    private boolean quickMatchForConst = true;
    private boolean excludeConstFromFSM = true;
    private char separator = DEFAULT_SEPARATOR;
    private int minCommonPrefixLength = DEFAULT_MIN_COMMON_PREFIX;

    public RouterBuilder setQuickMatchForConst(boolean quickMatchForConst) {
        this.quickMatchForConst = quickMatchForConst;
        return this;
    }

    public RouterBuilder setExcludeConstFromFSM(boolean excludeConstFromFSM) {
        this.excludeConstFromFSM = excludeConstFromFSM;
        return this;
    }

    public RouterBuilder setSeparator(char separator) {
        this.separator = separator;
        return this;
    }

    public RouterBuilder setMinCommonPrefixLength(int minCommonPrefixLength) {
        this.minCommonPrefixLength = minCommonPrefixLength;
        return this;
    }

    public <T> Router<T> buildRouter(List<RouterSetup.Rule<T>> rules) {
        Map<CharArray, T> quickMatchIndex = buildQuickMatchIndex(rules);
        Router.Node<T> root = buildStateMachine(rules);
        log.log(Level.FINEST, () -> "Using quick-match index of size %d".formatted(quickMatchIndex.size()));
        return new Router<>(quickMatchIndex, root);
    }

    /*package*/ <T> Map<CharArray, T> buildQuickMatchIndex(List<RouterSetup.Rule<T>> rules) {
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

    private <T> Router.Node<T> buildNode(Token start, List<Sequence<T>> sequences) {
        RouterSetup.Rule<T> terminalRule = getTerminalRuleOrNull(sequences);
        if (!retokenizeByCommonPrefix(sequences)) {
            retokenizeBySeparator(sequences);
        }
        List<Router.Node<T>> nodes = groupByPeekToken(sequences);  // recursion here
        List<Router.Node<T>> compact = compactJoinableNodes(nodes);
        @SuppressWarnings("unchecked")
        Router.Node<T>[] array = compact.toArray(Router.Node[]::new);
        return new Router.Node<>(start, array, terminalRule);
    }

    private static <T> RouterSetup.Rule<T> getTerminalRuleOrNull(List<Sequence<T>> sequences) {
        List<RouterSetup.Rule<T>> terminal = sequences.stream()
                .filter(seq -> seq.tokens.isEmpty())
                .map(Sequence::rule)
                .toList();
        if (terminal.size() > 1) {
            throw new RouteException("Duplicate rules found: " + terminal.stream().map(RouterSetup.Rule::query).toList());
        }
        return terminal.stream().findFirst().orElse(null);
    }

    private <T> List<Router.Node<T>> groupByPeekToken(List<Sequence<T>> sequences) {
        Map<Token, List<Sequence<T>>> group = new LinkedHashMap<>();  // preserve the order
        for (Sequence<T> sequence : sequences) {
            Token peek = sequence.tokens.poll();
            group.computeIfAbsent(peek, key -> new ArrayList<>()).add(sequence);
        }

        return group.entrySet().stream()
                .filter(entry -> entry.getKey() != null)
                .map(entry -> buildNode(entry.getKey(), entry.getValue()))  // recursion
                .toList();
    }

    private <T> boolean retokenizeByCommonPrefix(List<Sequence<T>> sequences) {
        CharArray commonPrefix = sequences.stream()
                .map(seq -> seq.tokens.peek() instanceof ConstToken constToken ? constToken.buffer() : null)
                .reduce(null, (lhs, rhs) -> {
                    if (lhs == null) return rhs;
                    if (rhs == null) return lhs;
                    return lhs.substringUntil(lhs.commonPrefix(rhs));
                });

        if (commonPrefix != null && commonPrefix.isNotEmpty() && commonPrefix.length() >= minCommonPrefixLength) {
            Token commonToken = new ConstToken(commonPrefix);
            for (Sequence<T> sequence : sequences) {
                Token peek = sequence.tokens.peek();
                if (peek instanceof ConstToken constToken && !constToken.buffer().equals(commonPrefix)) {
                    sequence.tokens.poll();
                    sequence.tokens.addFirst(new ConstToken(constToken.buffer().substringFrom(commonPrefix.length())));
                    sequence.tokens.addFirst(commonToken);
                }
            }
            return true;
        }

        return false;
    }

    private <T> void retokenizeBySeparator(List<Sequence<T>> sequences) {
        sequences.stream()
                .filter(seq -> seq.tokens.peek() instanceof ConstToken)
                .forEach(sequence -> {
                    CharArray buffer = ((ConstToken) sequence.tokens.peek()).buffer();
                    int index = buffer.indexOf(separator, 1);
                    if (index >= 0) {
                        sequence.tokens.poll();
                        sequence.tokens.addFirst(new ConstToken(buffer.substringFrom(index)));
                        sequence.tokens.addFirst(new ConstToken(buffer.substringUntil(index)));
                    }
                });
    }

    private static <T> List<Router.Node<T>> compactJoinableNodes(List<Router.Node<T>> nodes) {
        return nodes.stream().map(node -> {
            if (node.next().length != 1 || node.isTerminal()) {
                return node;
            }
            Router.Node<T> child = node.next()[0];
            if (node.token() instanceof ConstToken lhs && child.token() instanceof ConstToken rhs) {
                CharArray join = CharArray.join(lhs.buffer(), rhs.buffer());
                return new Router.Node<>(new ConstToken(join), child.next(), child.terminalRule());
            }
            return node;
        }).toList();
    }

    private static class RootToken implements Token {
        @Override
        public int match(CharArray charArray) {
            return 0;
        }

        @Override
        public String toString() {
            return "<root>";
        }
    }

    private record Sequence<T>(LinkedList<Token> tokens, RouterSetup.Rule<T> rule) {}
}
