package io.routekit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static io.routekit.NodePrinter.printlnToString;

public class RouterBuilderTest {
    @Test
    public void buildStateMachine_constants_common_part() {
        RouterBuilder builder = new RouterBuilder().setExcludeConstFromFSM(false);
        Router.Node<String> node = builder.buildStateMachine(Arrays.asList(
                rule("1", "/foo/bar"),
                rule("2", "/foo/baz")
        ));
        assertLines(printlnToString(node), """
        <root>
            ConstToken[/foo/ba]
                ConstToken[r] -> 1
                ConstToken[z] -> 2
        """);
    }

    @Test
    public void buildStateMachine_constants_common_at_level_2() {
        RouterBuilder builder = new RouterBuilder().setExcludeConstFromFSM(false);
        Router.Node<String> node = builder.buildStateMachine(Arrays.asList(
                rule("1", "/user/foo"),
                rule("2", "/user/bar"),
                rule("3", "/usergroup/"),
                rule("4", "/usergroup/baz"),
                rule("5", "/search/foo"),
                rule("6", "/search/bar")
        ));
        assertLines(printlnToString(node), """
        <root>
            ConstToken[/]
                ConstToken[user/]
                    ConstToken[foo] -> 1
                    ConstToken[bar] -> 2
                ConstToken[usergroup/] -> 3
                    ConstToken[baz] -> 4
                ConstToken[search/]
                    ConstToken[foo] -> 5
                    ConstToken[bar] -> 6
        """);
    }

    @Test
    public void buildStateMachine_constants_height_2() {
        RouterBuilder builder = new RouterBuilder().setExcludeConstFromFSM(false);
        Router.Node<String> node = builder.buildStateMachine(Arrays.asList(
                rule("1", "/"),
                rule("2", "/foo/bar"),
                rule("3", "/foo/bar/baz"),
                rule("4", "/bar")
        ));
        assertLines(printlnToString(node), """
        <root>
            ConstToken[/] -> 1
                ConstToken[foo/bar] -> 2
                    ConstToken[/baz] -> 3
                ConstToken[bar] -> 4
        """);
    }

    @Test
    public void buildStateMachine_const_excluded_and_vars_common_prefix() {
        RouterBuilder builder = new RouterBuilder().setExcludeConstFromFSM(false);
        Router.Node<String> node = builder.buildStateMachine(Arrays.asList(
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
    public void buildStateMachine_const_included_and_vars_common_prefix() {
        RouterBuilder builder = new RouterBuilder().setExcludeConstFromFSM(true);
        Router.Node<String> node = builder.buildStateMachine(Arrays.asList(
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
    public void buildStateMachine_vars_common_prefix() {
        RouterBuilder builder = new RouterBuilder().setExcludeConstFromFSM(false);
        Router.Node<String> node = builder.buildStateMachine(Arrays.asList(
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

    @Test
    public void buildStateMachine_two_vars_and_wildcard() {
        RouterBuilder builder = new RouterBuilder().setExcludeConstFromFSM(false);
        Router.Node<String> node = builder.buildStateMachine(Arrays.asList(
                rule("1", "/foo/", "{name}", "/default"),
                rule("2", "/foo/", "{name}", "/", "{age}"),
                rule("3", "/foo/", "{name}", "/", "{*rest}")
        ));
        assertLines(printlnToString(node), """
        <root>
            ConstToken[/foo/]
                SeparableVariableToken[name]
                    ConstToken[/]
                        ConstToken[default] -> 1
                        SeparableVariableToken[age] -> 2
                        SeparableVariableToken[*rest] -> 3
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
