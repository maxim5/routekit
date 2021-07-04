package io.route;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;

import static io.route.SimpleQueryParser.DEFAULT;

public class SimpleQueryParserTest {
    @Test
    public void parseNoVariables() {
        assertOrdered(DEFAULT.parse("/"), new ConstToken("/"));
        assertOrdered(DEFAULT.parse("foo-bar-baz"), new ConstToken("foo-bar-baz"));
    }

    @Test
    public void parseVariable() {
        assertOrdered(DEFAULT.parse("{foo}"), new SeparableVariableToken("foo"));
        assertOrdered(DEFAULT.parse("/{foo}"), new ConstToken("/"), new SeparableVariableToken("foo"));
        assertOrdered(DEFAULT.parse("{foo}/"), new SeparableVariableToken("foo"), new ConstToken("/"));
        assertOrdered(DEFAULT.parse("/{foo}/"), new ConstToken("/"), new SeparableVariableToken("foo"), new ConstToken("/"));
    }

    @Test
    public void parseSeveralVariables() {
        assertOrdered(DEFAULT.parse("/foo/{xxx}/{y}/{*z}"),
                new ConstToken("/foo/"),
                new SeparableVariableToken("xxx"),
                new ConstToken("/"),
                new SeparableVariableToken("y"),
                new ConstToken("/"),
                new WildcardToken("z"));
    }

    @SafeVarargs
    private static <T> void assertOrdered(Collection<T> actual, T ... expected) {
        Assertions.assertEquals(Arrays.asList(expected), actual);
    }
}
