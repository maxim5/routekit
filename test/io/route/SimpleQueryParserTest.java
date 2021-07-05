package io.route;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;

import static io.route.SimpleQueryParser.DEFAULT;

public class SimpleQueryParserTest {
    @Test
    public void parse_no_variables() {
        assertOrdered(DEFAULT.parse("/"), new ConstToken("/"));
        assertOrdered(DEFAULT.parse("foo-bar-baz"), new ConstToken("foo-bar-baz"));
    }

    @Test
    public void parse_variable() {
        assertOrdered(DEFAULT.parse("{foo}"), new SeparableVariableToken("foo"));
        assertOrdered(DEFAULT.parse("/{foo}"), new ConstToken("/"), new SeparableVariableToken("foo"));
        assertOrdered(DEFAULT.parse("{foo}/"), new SeparableVariableToken("foo"), new ConstToken("/"));
        assertOrdered(DEFAULT.parse("/{foo}/"), new ConstToken("/"), new SeparableVariableToken("foo"), new ConstToken("/"));
    }

    @Test
    public void parse_several_variables() {
        assertOrdered(DEFAULT.parse("/foo/{xxx}/{y}/{*z}"),
                new ConstToken("/foo/"),
                new SeparableVariableToken("xxx"),
                new ConstToken("/"),
                new SeparableVariableToken("y"),
                new ConstToken("/"),
                new WildcardToken("z"));
    }

    @Test
    public void parse_invalid() {
        // Invalid brackets
        Assertions.assertThrows(QueryParseException.class, () -> DEFAULT.parse("{"));
        // Assertions.assertThrows(QueryParseException.class, () -> DEFAULT.parse("}"));
        Assertions.assertThrows(QueryParseException.class, () -> DEFAULT.parse("}{"));
        Assertions.assertThrows(QueryParseException.class, () -> DEFAULT.parse("}{}{"));

        // Invalid name
        Assertions.assertThrows(QueryParseException.class, () -> DEFAULT.parse("{}"));
        Assertions.assertThrows(QueryParseException.class, () -> DEFAULT.parse("{dup}{dup}"));
        Assertions.assertThrows(QueryParseException.class, () -> DEFAULT.parse("{dup}{*dup}"));
    }

    @SafeVarargs
    private static <T> void assertOrdered(Collection<T> actual, T ... expected) {
        Assertions.assertEquals(Arrays.asList(expected), actual);
    }
}
