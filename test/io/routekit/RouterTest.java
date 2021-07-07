package io.routekit;

import io.routekit.util.CharBuffer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class RouterTest {
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

        // Doesn't match the slash
        assert404(router.routeOrNull(""));
        assert404(router.routeOrNull("/"));
        assert404(router.routeOrNull("foo/"));
        assert404(router.routeOrNull("/foo/"));
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
    public void routeOrNull_variables_with_same_prefix_unreachable() {
        Router<String> router = new RouterSetup<String>()
                .add("/foo/{x}", "1")
                .add("/foo/{y}", "2")  // unreachable
                .build();

        assertOK(router.routeOrNull("/foo/foo"), "1", "x=foo");
        assertOK(router.routeOrNull("/foo/FOO"), "1", "x=FOO");
        assertOK(router.routeOrNull("/foo/_"), "1", "x=_");

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
    public void routeOrNull_variables_with_same_part_unreachable() {
        Router<String> router = new RouterSetup<String>()
                .add("/user/{name}", "1")
                .add("/user/id{id}", "2")  // unreachable
                .build();

        assertOK(router.routeOrNull("/user/foo"), "1", "name=foo");
        assertOK(router.routeOrNull("/user/_"), "1", "name=_");
        assertOK(router.routeOrNull("/user/id3"), "1", "name=id3");  // longer match selected

        // Not found
        assert404(router.routeOrNull("/user/"));
        assert404(router.routeOrNull("/user/foo/"));
    }

    @Test
    public void routeOrNull_variables_with_same_part_swap_unreachable() {
        Router<String> router = new RouterSetup<String>()
                .add("/user/id{id}", "2")  // reachable
                .add("/user/{name}", "1")
                .build();

        assertOK(router.routeOrNull("/user/foo"), "1", "name=foo");
        assertOK(router.routeOrNull("/user/_"), "1", "name=_");
        assertOK(router.routeOrNull("/user/id3"), "1", "name=id3");  // longer match selected

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
    public void routeOrNull_just_wildcard_rule() {
        Router<String> router = new RouterSetup<String>()
                .add("{*var}", "1")
                .build();

        assertOK(router.routeOrNull("foo"), "1", "var=foo");
        assertOK(router.routeOrNull("FOO"), "1", "var=FOO");
        assertOK(router.routeOrNull("foo/bar"), "1", "var=foo/bar");
        assertOK(router.routeOrNull("/"), "1", "var=/");
        assertOK(router.routeOrNull("_"), "1", "var=_");

        assert404(router.routeOrNull(""));
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

    @Test
    public void routeOrNull_const_and_two_vars() {
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
    public void routeOrNull_two_rules_two_vars_all_matching() {
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

    private static void assertOK(Router.Match<String> match, String tag, String ... variables) {
        Assertions.assertEquals(match(tag, variables), match);
    }

    private static void assert404(Router.Match<String> match) {
        Assertions.assertNull(match);
    }

    private static Router.Match<String> match(String tag, String ... variables) {
        Map<String, CharBuffer> map = Arrays.stream(variables)
                .map(var -> var.split("=", -1))
                .filter(split -> split.length == 2)
                .collect(Collectors.toMap(
                        split -> split[0],
                        split -> new CharBuffer(split[1]),
                        (val1, val2) -> { throw new IllegalStateException("Duplicate values: " + val1 + " " + val2); },
                        LinkedHashMap::new)
                );
        return match(tag, map);
    }

    private static Router.Match<String> match(String tag, Map<String, CharBuffer> variables) {
        return new Router.Match<>(tag, variables);
    }
}
