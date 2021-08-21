package io.routekit;

import io.routekit.util.CharArray;
import io.routekit.util.MutableCharArray;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class RouterTest {
    // Trivial cases

    @Test
    public void routeOrNull_empty() {
        Router<String> router = new RouterSetup<String>().build();
        assert404(router.routeOrNull(""));
        assert404(router.routeOrNull("/"));
        assert404(router.routeOrNull("a"));
        assert404(router.routeOrNull("foo"));
    }

    @Test
    public void routeOrNull_const_rules_no_prefix() {
        Router<String> router = new RouterSetup<String>()
                .add("foo", "1")
                .add("bar", "2")
                .build();

        assertOK(router.routeOrNull("foo"), "1");
        assertOK(router.routeOrNull("bar"), "2");

        assert404(router.routeOrNull(""));
        assert404(router.routeOrNull("/"));
        assert404(router.routeOrNull("/foo"));
        assert404(router.routeOrNull("foo/"));
    }

    @Test
    public void routeOrNull_const_rules_common_prefix() {
        Router<String> router = new RouterSetup<String>()
                .add("/foo/foo", "1")
                .add("/foo/bar", "2")
                .build();

        assertOK(router.routeOrNull("/foo/foo"), "1");
        assertOK(router.routeOrNull("/foo/bar"), "2");

        assert404(router.routeOrNull(""));
        assert404(router.routeOrNull("/"));
        assert404(router.routeOrNull("/foo"));
        assert404(router.routeOrNull("foo/"));
        assert404(router.routeOrNull("/foo/"));
        assert404(router.routeOrNull("/foo/foo/"));
        assert404(router.routeOrNull("/foo/bar/"));
        assert404(router.routeOrNull("/foo/foobar"));
    }

    @Test
    public void routeOrNull_just_variable_rule() {
        Router<String> router = new RouterSetup<String>()
                .add("{var}", "1")
                .build();

        assertOK(router.routeOrNull("foo"), "1", "var=foo");
        assertOK(router.routeOrNull("FOO"), "1", "var=FOO");
        assertOK(router.routeOrNull("_"), "1", "var=_");
        assertOK(router.routeOrNull("foo bar"), "1", "var=foo bar");      // whitespace
        assertOK(router.routeOrNull("foo\n\tbar"), "1", "var=foo\n\tbar");// whitespace
        assertOK(router.routeOrNull("foo%20bar"), "1", "var=foo%20bar");  // escaped " "
        assertOK(router.routeOrNull("foo%2Fbar"), "1", "var=foo%2Fbar");  // escaped "/"

        // Doesn't match the slash
        assert404(router.routeOrNull(""));
        assert404(router.routeOrNull("/"));
        assert404(router.routeOrNull("foo/"));
        assert404(router.routeOrNull("/foo/"));
    }

    @Test
    public void routeOrNull_just_wildcard_rule() {
        Router<String> router = new RouterSetup<String>()
                .add("{*var}", "1")
                .build();

        assertOK(router.routeOrNull("foo"), "1", "var=foo");
        assertOK(router.routeOrNull("FOO"), "1", "var=FOO");
        assertOK(router.routeOrNull("foo/"), "1", "var=foo/");
        assertOK(router.routeOrNull("foo/bar"), "1", "var=foo/bar");
        assertOK(router.routeOrNull("/"), "1", "var=/");
        assertOK(router.routeOrNull("/foo"), "1", "var=/foo");
        assertOK(router.routeOrNull("_"), "1", "var=_");

        assert404(router.routeOrNull(""));
    }

    @Test
    public void routeOrNull_const_variable_rule() {
        Router<String> router = new RouterSetup<String>()
                .add("/foo/{var}", "1")
                .build();

        assertOK(router.routeOrNull("/foo/foo"), "1", "var=foo");
        assertOK(router.routeOrNull("/foo/FOO"), "1", "var=FOO");
        assertOK(router.routeOrNull("/foo/_"), "1", "var=_");

        // Not found
        assert404(router.routeOrNull("/foo/"));
        assert404(router.routeOrNull("/"));
        assert404(router.routeOrNull("//"));
        assert404(router.routeOrNull("foo/"));
        assert404(router.routeOrNull("/foobar"));

        // Doesn't match the slash
        assert404(router.routeOrNull("foo//"));
        assert404(router.routeOrNull("foo/bar/"));
        assert404(router.routeOrNull("foo/bar//"));
    }

    @Test
    public void routeOrNull_const_variable_const_rule() {
        Router<String> router = new RouterSetup<String>()
                .add("/foo/{var}/bar", "1")
                .build();

        assertOK(router.routeOrNull("/foo/foo/bar"), "1", "var=foo");
        assertOK(router.routeOrNull("/foo/FOO/bar"), "1", "var=FOO");
        assertOK(router.routeOrNull("/foo/_/bar"), "1", "var=_");

        // Not found
        assert404(router.routeOrNull("/foo/"));
        assert404(router.routeOrNull("foo//"));
        assert404(router.routeOrNull("/foo/foo/"));
        assert404(router.routeOrNull("/foo/foo/baz"));
        assert404(router.routeOrNull("/foo//bar"));
        assert404(router.routeOrNull("/"));
        assert404(router.routeOrNull("//"));
        assert404(router.routeOrNull("foo/"));
        assert404(router.routeOrNull("/foobar"));
    }

    @Test
    public void routeOrNull_variable_const_rule() {
        Router<String> router = new RouterSetup<String>()
                .add("{var}/foo/", "1")
                .build();

        assertOK(router.routeOrNull("foo/foo/"), "1", "var=foo");
        assertOK(router.routeOrNull("FOO/foo/"), "1", "var=FOO");
        assertOK(router.routeOrNull("_/foo/"), "1", "var=_");

        // Not found
        assert404(router.routeOrNull("/"));
        assert404(router.routeOrNull("//"));
        assert404(router.routeOrNull("foo"));
        assert404(router.routeOrNull("/foo/"));
        assert404(router.routeOrNull("foo/"));
        assert404(router.routeOrNull("foo/foo"));
        assert404(router.routeOrNull("foo/foo/bar"));
        assert404(router.routeOrNull("/foo"));
        assert404(router.routeOrNull("/foo/foo"));
        assert404(router.routeOrNull("/foo/foo/"));
        assert404(router.routeOrNull("/foobar"));

        // Doesn't match the slash
        assert404(router.routeOrNull("//foo/"));
        assert404(router.routeOrNull("///foo/"));
    }

    @Test
    public void routeOrNull_const_wildcard_rule() {
        Router<String> router = new RouterSetup<String>()
                .add("/foo/{*var}", "1")
                .build();

        assertOK(router.routeOrNull("/foo/foo"), "1", "var=foo");
        assertOK(router.routeOrNull("/foo/FOO"), "1", "var=FOO");
        assertOK(router.routeOrNull("/foo/bar/"), "1", "var=bar/");
        assertOK(router.routeOrNull("/foo/bar//"), "1", "var=bar//");
        assertOK(router.routeOrNull("/foo/bar/baz"), "1", "var=bar/baz");
        assertOK(router.routeOrNull("/foo//"), "1", "var=/");
        assertOK(router.routeOrNull("/foo/_"), "1", "var=_");

        // Not found
        assert404(router.routeOrNull("/foo/"));
        assert404(router.routeOrNull("/"));
        assert404(router.routeOrNull("//"));
        assert404(router.routeOrNull("foo/"));
        assert404(router.routeOrNull("foo/bar"));
        assert404(router.routeOrNull("foo/bar/"));
        assert404(router.routeOrNull("/foobar"));
    }

    // Multiple rules

    @Test
    public void routeOrNull_variables_without_separator_invalid() {
        Assertions.assertThrows(QueryParseException.class, () ->
            new RouterSetup<String>()
                .add("/{x}", "1")
                .add("/{x}{y}", "2")  // `y` is unreachable
                .build()
        );
    }

    @Test
    public void routeOrNull_variables_separated_by_dash_invalid() {
        Assertions.assertThrows(QueryParseException.class, () ->
            new RouterSetup<String>()
                .add("/{x}", "1")
                .add("/{x}-{y}", "2")  // `y` is unreachable (recommended workaround: change variable separator).
                .build()
        );
    }

    @Test
    public void routeOrNull_variables_separated_by_slash_and_dash() {
        Router<String> router = new RouterSetup<String>()
                .add("/{x}", "1")
                .add("/{x}/-{y}", "2")
                .build();

        assertOK(router.routeOrNull("/foo"), "1", "x=foo");
        assertOK(router.routeOrNull("/foobar"), "1", "x=foobar");
        assertOK(router.routeOrNull("/foo-bar"), "1", "x=foo-bar");
        assertOK(router.routeOrNull("/foo/-bar"), "2", "x=foo", "y=bar");

        // Not found
        assert404(router.routeOrNull("/"));
        assert404(router.routeOrNull("//"));
        assert404(router.routeOrNull("/foo/"));
        assert404(router.routeOrNull("/foo//"));
        assert404(router.routeOrNull("/foo/bar"));
    }

    @Test
    public void routeOrNull_variables_with_same_prefix_invalid() {
        Assertions.assertThrows(RouteException.class, () ->
            new RouterSetup<String>()
                .add("/foo/{x}", "1")
                .add("/foo/{y}", "2")  // `y` is unreachable
                .build()
        );
    }

    @Test
    public void routeOrNull_variables_with_same_part_unreachable() {
        Router<String> router = new RouterSetup<String>()
                .add("/user/{name}", "1")
                .add("/user/id{id}", "2")  // unreachable (workaround: add a separator)
                .build();

        assertOK(router.routeOrNull("/user/foo"), "1", "name=foo");
        assertOK(router.routeOrNull("/user/_"), "1", "name=_");
        assertOK(router.routeOrNull("/user/id"), "1", "name=id");  // the only terminal
        assertOK(router.routeOrNull("/user/id3"), "1", "name=id3");  // longest match selected

        // Not found
        assert404(router.routeOrNull("/user/"));
        assert404(router.routeOrNull("/user/foo/"));
        assert404(router.routeOrNull("/user/id/"));
    }

    @Test
    public void routeOrNull_variables_with_same_part_swapped_unreachable() {
        Router<String> router = new RouterSetup<String>()
                .add("/user/id{id}", "2")  // unreachable (workaround: add a separator)
                .add("/user/{name}", "1")
                .build();

        assertOK(router.routeOrNull("/user/foo"), "1", "name=foo");
        assertOK(router.routeOrNull("/user/_"), "1", "name=_");
        assertOK(router.routeOrNull("/user/id3"), "1", "name=id3");  // longest match selected

        // Not found
        assert404(router.routeOrNull("/user/"));
        assert404(router.routeOrNull("/user/foo/"));
        assert404(router.routeOrNull("/user/id"));
        assert404(router.routeOrNull("/user/id/"));
    }

    @Test
    public void routeOrNull_variables_with_different_parts() {
        Router<String> router = new RouterSetup<String>()
                .add("/user/{name}", "1")
                .add("/user/id/{id}", "2")
                .build();

        assertOK(router.routeOrNull("/user/foo"), "1", "name=foo");
        assertOK(router.routeOrNull("/user/_"), "1", "name=_");
        assertOK(router.routeOrNull("/user/id"), "1", "name=id");
        assertOK(router.routeOrNull("/user/id/3"), "2", "id=3");

        // Not found
        assert404(router.routeOrNull("/user/"));
        assert404(router.routeOrNull("/user/foo/"));
        assert404(router.routeOrNull("/user/id/"));
        assert404(router.routeOrNull("/user/id//"));
    }

    @Test
    public void routeOrNull_optional_variables_defined_as_hierarchy_simple() {
        Router<String> router = new RouterSetup<String>()
                .add("/{first}/", "1")
                .add("/{first}/{last}/", "2")
                .add("/{first}/{last}/{age}", "3")
                .build();

        assertOK(router.routeOrNull("/foo bar/"), "1", "first=foo bar");
        assertOK(router.routeOrNull("/foo%20bar/"), "1", "first=foo%20bar");
        assertOK(router.routeOrNull("/Foo%20Bar/"), "1", "first=Foo%20Bar");
        assertOK(router.routeOrNull("/foo/bar/"), "2", "first=foo", "last=bar");
        assertOK(router.routeOrNull("/FOO/BAR/"), "2", "first=FOO", "last=BAR");
        assertOK(router.routeOrNull("/foo/bar/1"), "3", "first=foo", "last=bar", "age=1");
        assertOK(router.routeOrNull("/foo/bar/25"), "3", "first=foo", "last=bar", "age=25");
        assertOK(router.routeOrNull("/foo/bar/baz"), "3", "first=foo", "last=bar", "age=baz");

        assert404(router.routeOrNull("/"));
        assert404(router.routeOrNull("//"));
        assert404(router.routeOrNull("//foo"));
        assert404(router.routeOrNull("///"));
        assert404(router.routeOrNull("//foo/25"));
        assert404(router.routeOrNull("/foo//25"));
        assert404(router.routeOrNull("foo/bar/baz/25"));
    }

    @Test
    public void routeOrNull_optional_variables_defined_as_hierarchy() {
        Router<String> router = new RouterSetup<String>()
                .add("/post/{id}", "1")
                .add("/post/{id}/", "2")
                .add("/post/{id}/{slug}", "3")
                .add("/post/{id}/{slug}/{ref}", "4")
                .build();

        assertOK(router.routeOrNull("/post/1"), "1", "id=1");
        assertOK(router.routeOrNull("/post/1/"), "2", "id=1");
        assertOK(router.routeOrNull("/post/1/title"), "3", "id=1", "slug=title");
        assertOK(router.routeOrNull("/post/1/title/42"), "4", "id=1", "slug=title", "ref=42");

        // Not found
        assert404(router.routeOrNull("/post/"));
        assert404(router.routeOrNull("/post//"));
        assert404(router.routeOrNull("/post///"));
    }

    @Test
    public void routeOrNull_optional_variables_defined_as_hierarchy_with_defaults() {
        Router<String> router = new RouterSetup<String>()
                .add("/foo/bar", "1")
                .add("/foo/{name}", "2")
                .add("/foo/{name}/{age}", "3")
                .build();

        // Const
        assertOK(router.routeOrNull("/foo/bar"), "1");
        assert404(router.routeOrNull("/"));
        assert404(router.routeOrNull("/foo"));
        assert404(router.routeOrNull("/foobar"));
        assert404(router.routeOrNull("/foo/bar/"));

        // First var
        assertOK(router.routeOrNull("/foo/foo"), "2", "name=foo");
        assertOK(router.routeOrNull("/foo/XXX"), "2", "name=XXX");
        assertOK(router.routeOrNull("/foo/_"), "2", "name=_");
        assert404(router.routeOrNull("/foo/"));
        assert404(router.routeOrNull("/foo/XXX/"));

        // Second var
        assertOK(router.routeOrNull("/foo/XXX/25"), "3", "name=XXX", "age=25");
        assert404(router.routeOrNull("/foo/XXX/25/"));
        assert404(router.routeOrNull("/foo/XXX//"));
    }

    @Test
    public void routeOrNull_just_const_and_just_variable_rules() {
        Router<String> router = new RouterSetup<String>()
                .add("/foo", "1")
                .add("/{var}", "2")
                .build();

        assertOK(router.routeOrNull("/foo"), "1");
        assertOK(router.routeOrNull("/bar"), "2", "var=bar");
        assertOK(router.routeOrNull("/foobar"), "2", "var=foobar");

        // Not found
        assert404(router.routeOrNull("/foo/"));
        assert404(router.routeOrNull("/"));
        assert404(router.routeOrNull("//"));
        assert404(router.routeOrNull("foo"));
        assert404(router.routeOrNull("foo/"));

        // Doesn't match the slash
        assert404(router.routeOrNull("/foo/"));
    }

    @Test
    public void routeOrNull_just_const_and_just_variable_rules_swapped() {
        Router<String> router = new RouterSetup<String>()
                .add("/{var}", "2")
                .add("/foo", "1")
                .build();

        assertOK(router.routeOrNull("/foo"), "1");
        assertOK(router.routeOrNull("/bar"), "2", "var=bar");
        assertOK(router.routeOrNull("/foobar"), "2", "var=foobar");

        // Not found
        assert404(router.routeOrNull("/foo/"));
        assert404(router.routeOrNull("/"));
        assert404(router.routeOrNull("//"));
        assert404(router.routeOrNull("foo"));
        assert404(router.routeOrNull("foo/"));

        // Doesn't match the slash
        assert404(router.routeOrNull("/foo/"));
    }

    @Test
    public void routeOrNull_two_rules_variable_and_const_default() {
        Router<String> router = new RouterSetup<String>()
                .add("/foo/{name}/default", "1")
                .add("/foo/{name}/{age}", "2")
                .build();

        assertOK(router.routeOrNull("/foo/bar/1"), "2", "name=bar", "age=1");
        assertOK(router.routeOrNull("/foo/bar/25"), "2", "name=bar", "age=25");
        assertOK(router.routeOrNull("/foo/bar/def"), "2", "name=bar", "age=def");
        assertOK(router.routeOrNull("/foo/bar/default"), "1", "name=bar");
        assertOK(router.routeOrNull("/foo/bar/default25"), "2", "name=bar", "age=default25");
    }

    @Test
    public void routeOrNull_two_rules_variable_and_const_default_swapped() {
        Router<String> router = new RouterSetup<String>()
                .add("/foo/{name}/{age}", "2")
                .add("/foo/{name}/default", "1")
                .build();

        assertOK(router.routeOrNull("/foo/bar/1"), "2", "name=bar", "age=1");
        assertOK(router.routeOrNull("/foo/bar/25"), "2", "name=bar", "age=25");
        assertOK(router.routeOrNull("/foo/bar/def"), "2", "name=bar", "age=def");
        // assertOK(router.routeOrNull("/foo/bar/default"), "1", "name=bar");  // TODO: fix with priorities
        assertOK(router.routeOrNull("/foo/bar/default25"), "2", "name=bar", "age=default25");

        assert404(router.routeOrNull("/foo/"));
        assert404(router.routeOrNull("/foo//"));
        assert404(router.routeOrNull("/foo///"));
    }

    @Test
    public void routeOrNull_three_rules_two_vars_and_wildcard_all_matching() {
        Router<String> router = new RouterSetup<String>()
                .add("/foo/{name}/default", "1")
                .add("/foo/{name}/{age}", "2")
                .add("/foo/{name}/{*rest}", "3")
                .build();

        assertOK(router.routeOrNull("/foo/bar/1"), "2", "name=bar", "age=1");
        assertOK(router.routeOrNull("/foo/bar/25"), "2", "name=bar", "age=25");
        assertOK(router.routeOrNull("/foo/bar/def"), "2", "name=bar", "age=def");
        assertOK(router.routeOrNull("/foo/bar/default"), "1", "name=bar");
        assertOK(router.routeOrNull("/foo/bar/default25"), "2", "name=bar", "age=default25");
        assertOK(router.routeOrNull("/foo/bar/default/"), "3", "name=bar", "rest=default/");
        assertOK(router.routeOrNull("/foo/bar/default25/"), "3", "name=bar", "rest=default25/");
        assertOK(router.routeOrNull("/foo/bar/default/25"), "3", "name=bar", "rest=default/25");
    }

    @Test
    public void routeOrNull_char_buffer_as_input() {
        Router<String> router = new RouterSetup<String>()
                .add("/foo/{name}", "1")
                .build();

        assert404(router.routeOrNull(new CharArray("/bar/foo/name")));
        assertOK(router.routeOrNull(new CharArray("/bar/foo/name").substringFrom(4)), "1", "name=name");
        assertOK(router.routeOrNull(new CharArray("/foo/name?k=v")), "1", Collections.singletonMap("name", "name?k=v"));
        assertOK(router.routeOrNull(new CharArray("/foo/name?k=v").substringUntil(9)), "1", "name=name");
    }

    @Test
    public void routeOrNull_quick_match_char_buffer_as_input() {
        Router<String> router = new RouterSetup<String>()
                .add("/foo/bar", "1")
                .add("/foo/{name}", "2")
                .build();

        assertOK(router.routeOrNull("/foo/bar"), "1");
        assertOK(router.routeOrNull(new CharArray("/foo/bar")), "1");
        assertOK(router.routeOrNull(new CharArray("/foo/bar/", 0, 8)), "1");
        assertOK(router.routeOrNull(new CharArray("//foo/bar/", 1, 9)), "1");
        assertOK(router.routeOrNull(new MutableCharArray("//foo/bar/", 1, 9)), "1");

        assert404(router.routeOrNull(new CharArray("//foo/bar/", 0, 9)));
    }

    private static void assertOK(Match<String> match, String tag, String ... variables) {
        Assertions.assertEquals(match(tag, variables), match);
    }

    private static void assertOK(Match<String> match, String tag, Map<String, String> variables) {
        Assertions.assertEquals(match(tag, variables), match);
    }

    private static void assert404(Match<String> match) {
        Assertions.assertNull(match);
    }

    private static Match<String> match(String tag, String ... variables) {
        Map<String, CharArray> map = Arrays.stream(variables)
                .map(var -> var.split("=", -1))
                .filter(split -> split.length == 2)
                .collect(Collectors.toMap(
                        split -> split[0],
                        split -> new CharArray(split[1]),
                        (val1, val2) -> { throw new IllegalStateException("Duplicate values: " + val1 + " " + val2); },
                        LinkedHashMap::new)
                );
        return makeMatch(tag, map);
    }

    private static <T extends CharSequence> Match<String> match(String tag, Map<String, T> variables) {
        Map<String, CharArray> buffers = variables.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> new CharArray(e.getValue())));
        return makeMatch(tag, buffers);
    }

    private static Match<String> makeMatch(String tag, Map<String, CharArray> variables) {
        return new Match<>(tag, variables);
    }
}
