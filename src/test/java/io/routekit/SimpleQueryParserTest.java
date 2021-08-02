package io.routekit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;

import static io.routekit.SimpleQueryParser.DEFAULT;

public class SimpleQueryParserTest {
    @Test
    public void parse_no_variables() {
        assertOrdered(DEFAULT.parse("/"), constant("/"));
        assertOrdered(DEFAULT.parse("//"), constant("//"));
        assertOrdered(DEFAULT.parse("//*"), constant("//*"));
        assertOrdered(DEFAULT.parse("foo/bar/baz"), constant("foo/bar/baz"));
        assertOrdered(DEFAULT.parse("/foo/bar/baz"), constant("/foo/bar/baz"));
        assertOrdered(DEFAULT.parse("foo-bar-baz"), constant("foo-bar-baz"));
    }

    @Test
    public void parse_variable() {
        assertOrdered(DEFAULT.parse("{foo}"), var("foo"));
        assertOrdered(DEFAULT.parse("/{foo}"), constant("/"), var("foo"));
        assertOrdered(DEFAULT.parse("{foo}/"), var("foo"), constant("/"));
        assertOrdered(DEFAULT.parse("/{foo}/"), constant("/"), var("foo"), constant("/"));
        assertOrdered(DEFAULT.parse("/foo/{foo}"), constant("/foo/"), var("foo"));
        assertOrdered(DEFAULT.parse("foo{foo}/foo"), constant("foo"), var("foo"), constant("/foo"));

        assertOrdered(DEFAULT.parse("{X}"), var("X"));
        assertOrdered(DEFAULT.parse("{XyZ}"), var("XyZ"));
    }

    @Test
    public void parse_two_variables() {
        assertOrdered(DEFAULT.parse("{foo}/{bar}"), var("foo"), constant("/"), var("bar"));
        assertOrdered(DEFAULT.parse("/{foo}/{bar}"), constant("/"), var("foo"), constant("/"), var("bar"));
        assertOrdered(DEFAULT.parse("/{foo}/{bar}/"), constant("/"), var("foo"), constant("/"), var("bar"), constant("/"));

        assertOrdered(DEFAULT.parse("{X}/{Y}"), var("X"), constant("/"), var("Y"));
        assertOrdered(DEFAULT.parse("{X}/ {Y}"), var("X"), constant("/ "), var("Y"));
        assertOrdered(DEFAULT.parse("{X}/_{Y}"), var("X"), constant("/_"), var("Y"));
        assertOrdered(DEFAULT.parse("{X}/-{Y}"), var("X"), constant("/-"), var("Y"));
    }

    @Test
    public void parse_wildcard() {
        assertOrdered(DEFAULT.parse("{*foo}"), wildcard("foo"));
        assertOrdered(DEFAULT.parse("*{*foo}"), constant("*"), wildcard("foo"));
        assertOrdered(DEFAULT.parse("/{*foo}"), constant("/"), wildcard("foo"));
        assertOrdered(DEFAULT.parse("/foo/{*foo}"), constant("/foo/"), wildcard("foo"));

        assertOrdered(DEFAULT.parse("{*X}"), wildcard("X"));
        assertOrdered(DEFAULT.parse("{*XyZ}"), wildcard("XyZ"));
    }

    @Test
    public void parse_wildcard_and_variable() {
        assertOrdered(DEFAULT.parse("{foo}/{*bar}"), var("foo"), constant("/"), wildcard("bar"));
        assertOrdered(DEFAULT.parse("/foo/{foo}/{*bar}"), constant("/foo/"), var("foo"), constant("/"), wildcard("bar"));
    }

    @Test
    public void parse_many_variables() {
        assertOrdered(
                DEFAULT.parse("/{foo}/{bar}/{baz}"),
                constant("/"), var("foo"), constant("/"), var("bar"), constant("/"), var("baz")
        );
        assertOrdered(
                DEFAULT.parse("/foo/{bar}/{baz}/"),
                constant("/foo/"), var("bar"), constant("/"), var("baz"), constant("/")
        );
        assertOrdered(
                DEFAULT.parse("/foo/{xxx}/{y}/{*z}"),
                constant("/foo/"), var("xxx"), constant("/"), var("y"), constant("/"), wildcard("z")
        );
    }

    @Test
    public void parse_invalid() {
        Assertions.assertThrows(QueryParseException.class, () -> DEFAULT.parse(""));

        // Invalid brackets
        Assertions.assertThrows(QueryParseException.class, () -> DEFAULT.parse("{"));
        Assertions.assertThrows(QueryParseException.class, () -> DEFAULT.parse("}"));
        Assertions.assertThrows(QueryParseException.class, () -> DEFAULT.parse("}{"));
        Assertions.assertThrows(QueryParseException.class, () -> DEFAULT.parse("}foo{"));
        Assertions.assertThrows(QueryParseException.class, () -> DEFAULT.parse("}{foo}"));
        Assertions.assertThrows(QueryParseException.class, () -> DEFAULT.parse("}{}{"));
        Assertions.assertThrows(QueryParseException.class, () -> DEFAULT.parse("{{}}"));  // nesting not allowed

        // Invalid name
        Assertions.assertThrows(QueryParseException.class, () -> DEFAULT.parse("{}"));
        Assertions.assertThrows(QueryParseException.class, () -> DEFAULT.parse("{*}"));
        Assertions.assertThrows(QueryParseException.class, () -> DEFAULT.parse("{**}"));
        Assertions.assertThrows(QueryParseException.class, () -> DEFAULT.parse("{foo*}"));
        Assertions.assertThrows(QueryParseException.class, () -> DEFAULT.parse("{foo*foo}"));
        Assertions.assertThrows(QueryParseException.class, () -> DEFAULT.parse("{*foo*}"));
        Assertions.assertThrows(QueryParseException.class, () -> DEFAULT.parse("{{foo}}"));
        Assertions.assertThrows(QueryParseException.class, () -> DEFAULT.parse("{foo-bar}"));
        Assertions.assertThrows(QueryParseException.class, () -> DEFAULT.parse("{foo+bar}"));

        // Duplicates
        Assertions.assertThrows(QueryParseException.class, () -> DEFAULT.parse("{dup}{dup}"));
        Assertions.assertThrows(QueryParseException.class, () -> DEFAULT.parse("/{dup}/{dup}"));
        Assertions.assertThrows(QueryParseException.class, () -> DEFAULT.parse("{dup}{*dup}"));
        Assertions.assertThrows(QueryParseException.class, () -> DEFAULT.parse("/{dup}/{*dup}"));

        // Wildcard not at the end
        Assertions.assertThrows(QueryParseException.class, () -> DEFAULT.parse("{*bar}/foo"));
        Assertions.assertThrows(QueryParseException.class, () -> DEFAULT.parse("{*bar}/{foo}"));
        Assertions.assertThrows(QueryParseException.class, () -> DEFAULT.parse("{*bar}{foo}"));
        Assertions.assertThrows(QueryParseException.class, () -> DEFAULT.parse("{*bar}{*bar}"));
        Assertions.assertThrows(QueryParseException.class, () -> DEFAULT.parse("/foo/{*bar}/"));
        Assertions.assertThrows(QueryParseException.class, () -> DEFAULT.parse("/foo/{foo}/{*bar}/"));

        // Vars not separated
        Assertions.assertThrows(QueryParseException.class, () -> DEFAULT.parse("{foo}{bar}"));
        Assertions.assertThrows(QueryParseException.class, () -> DEFAULT.parse("/{foo}{bar}"));
        Assertions.assertThrows(QueryParseException.class, () -> DEFAULT.parse("{foo}{bar}/"));
        Assertions.assertThrows(QueryParseException.class, () -> DEFAULT.parse("{foo}{*bar}"));
        Assertions.assertThrows(QueryParseException.class, () -> DEFAULT.parse("/{foo}{*bar}"));
        Assertions.assertThrows(QueryParseException.class, () -> DEFAULT.parse("/foo/{foo}{*bar}"));

        // Vars not properly separated
        Assertions.assertThrows(QueryParseException.class, () -> DEFAULT.parse("{X} {Y}"));
        Assertions.assertThrows(QueryParseException.class, () -> DEFAULT.parse("{X}_{Y}"));
        Assertions.assertThrows(QueryParseException.class, () -> DEFAULT.parse("{X}-{Y}"));
    }

    @SafeVarargs
    private static <T> void assertOrdered(Collection<T> actual, T ... expected) {
        Assertions.assertEquals(Arrays.asList(expected), actual);
    }

    private static ConstToken constant(String token) {
        return new ConstToken(token);
    }

    private static SeparableVariableToken var(String name) {
        return new SeparableVariableToken(name);
    }

    private static WildcardToken wildcard(String name) {
        return new WildcardToken(name);
    }
}
