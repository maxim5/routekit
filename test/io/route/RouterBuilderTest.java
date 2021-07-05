package io.route;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static io.route.NodePrinter.printlnToString;

public class RouterBuilderTest {
    @Test
    public void buildStateAutomaton_constants_common_part() {
        RouterBuilder builder = new RouterBuilder().setExcludeConstInSA(false);
        RouterBuilder.Node<String> node = builder.buildStateAutomaton(Arrays.asList(
                rule("1", "/foo/bar"),
                rule("2", "/foo/baz")
        ));
        assertLines(printlnToString(node), """
        <root>
            ConstToken[/foo/ba]
                ConstToken[z] -> 2
                ConstToken[r] -> 1
        """);
    }

    @Test
    public void buildStateAutomaton_constants_height_2() {
        RouterBuilder builder = new RouterBuilder().setExcludeConstInSA(false);
        RouterBuilder.Node<String> node = builder.buildStateAutomaton(Arrays.asList(
                rule("1", "/"),
                rule("2", "/foo/bar"),
                rule("3", "/foo/bar/baz"),
                rule("4", "/bar")
        ));
        assertLines(printlnToString(node), """
        <root>
            ConstToken[/] -> 1
                ConstToken[bar] -> 4
                ConstToken[foo/bar] -> 2
                ConstToken[foo/bar/baz] -> 3
        """);
    }

    @Test
    public void buildStateAutomaton_const_excluded_and_vars_common_prefix() {
        RouterBuilder builder = new RouterBuilder().setExcludeConstInSA(false);
        RouterBuilder.Node<String> node = builder.buildStateAutomaton(Arrays.asList(
                rule("1", "/foo/bar"),
                rule("2", "/foo/", "{name}"),
                rule("3", "/foo/", "{name}", "/", "{age}")
        ));
        assertLines(printlnToString(node), """
        <root>
            ConstToken[/foo/]
                ConstToken[bar] -> 1
                SeparableVariableToken[name] -> 2
                    ConstToken[/]
                        SeparableVariableToken[age] -> 3
        """);
    }

    @Test
    public void buildStateAutomaton_const_included_and_vars_common_prefix() {
        RouterBuilder builder = new RouterBuilder().setExcludeConstInSA(true);
        RouterBuilder.Node<String> node = builder.buildStateAutomaton(Arrays.asList(
                rule("1", "/foo/bar"),
                rule("2", "/foo/", "{name}"),
                rule("3", "/foo/", "{name}", "/", "{age}")
        ));
        assertLines(printlnToString(node), """
        <root>
            ConstToken[/foo/]
                SeparableVariableToken[name] -> 2
                    ConstToken[/]
                        SeparableVariableToken[age] -> 3
        """);
    }

    @Test
    public void buildStateAutomaton_vars_common_prefix() {
        RouterBuilder builder = new RouterBuilder().setExcludeConstInSA(false);
        RouterBuilder.Node<String> node = builder.buildStateAutomaton(Arrays.asList(
                rule("1", "/foo/", "{name}"),
                rule("2", "/foo/", "{name}", "/", "{age}")
        ));
        assertLines(printlnToString(node), """
        <root>
            ConstToken[/foo/]
                SeparableVariableToken[name] -> 1
                    ConstToken[/]
                        SeparableVariableToken[age] -> 2
        """);
    }

    private static RouterSetup.Rule<String> rule(String tag, String ... tokens) {
        return new RouterSetup.Rule<>(() -> Arrays.stream(tokens).map(RouterBuilderTest::convert).toList(), tag);
    }

    private static Token convert(String token) {
        return token.startsWith("{") && token.endsWith("}") ?
                new SeparableVariableToken(token.replaceAll("[{}]", "")) :
                new ConstToken(token);
    }

    private static void assertLines(String actual, String expected) {
        Assertions.assertLinesMatch(expected.lines(), actual.lines());
    }
}
