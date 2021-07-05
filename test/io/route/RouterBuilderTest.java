package io.route;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class RouterBuilderTest {
    @Test
    public void build_constants0() {
        RouterBuilder.Node<String> node = RouterBuilder.buildNode(Arrays.asList(
                rule("1", "/foo/bar"),
                rule("2", "/foo/baz")
        ));
        println(node, 0);
    }

    @Test
    public void build_constants1() {
        RouterBuilder.Node<String> node = RouterBuilder.buildNode(Arrays.asList(
                rule("1", "/"),
                rule("2", "/foo/bar"),
                rule("3", "/foo/bar/baz"),
                rule("4", "/bar")
        ));
        println(node, 0);
    }

    private RouterSetup.Rule<String> rule(String tag, String ... tokens) {
        return new RouterSetup.Rule<>(() -> Arrays.stream(tokens).map(this::convert).toList(), tag);
    }

    private Token convert(String token) {
        return token.startsWith("$") ? new SeparableVariableToken(token) : new ConstToken(token);
    }

    private static <T> void println(RouterBuilder.Node<T> node, int indent) {
        indent(indent);
        System.out.print(node.token());
        if (node.isTerminal()) {
            System.out.print(" -> ");
            System.out.print(node.terminalRule());
        }
        System.out.println();

        for (RouterBuilder.Node<T> next : node.next()) {
            println(next, indent + 4);
        }
    }

    private static void indent(int num) {
        for (int i = 0; i < num; i++) {
            System.out.print(' ');
        }
    }
}
