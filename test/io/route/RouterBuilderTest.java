package io.route;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static io.route.NodePrinter.println;
import static io.route.NodePrinter.printlnToString;

public class RouterBuilderTest {
    @Test
    public void build_constantsCommonPart() {
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
    public void build_constantsTwoLevels() {
        RouterBuilder builder = new RouterBuilder().setExcludeConstInSA(false);
        RouterBuilder.Node<String> node = builder.buildStateAutomaton(Arrays.asList(
                rule("1", "/"),
                rule("2", "/foo/bar"),
                rule("3", "/foo/bar/baz"),
                rule("4", "/bar")
        ));
        println(node);
    }

    private static RouterSetup.Rule<String> rule(String tag, String ... tokens) {
        return new RouterSetup.Rule<>(() -> Arrays.stream(tokens).map(RouterBuilderTest::convert).toList(), tag);
    }

    private static Token convert(String token) {
        return token.startsWith("$") ? new SeparableVariableToken(token) : new ConstToken(token);
    }

    private static void assertLines(String actual, String expected) {
        Assertions.assertLinesMatch(expected.lines(), actual.lines());
    }
}
