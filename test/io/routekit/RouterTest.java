package io.routekit;

import io.routekit.util.CharBuffer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
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

        Assertions.assertEquals(match("1"), router.routeOrNull("foo"));
        Assertions.assertEquals(match("2"), router.routeOrNull("bar"));

        Assertions.assertNull(router.routeOrNull(""));
        Assertions.assertNull(router.routeOrNull("/"));
        Assertions.assertNull(router.routeOrNull("/foo"));
        Assertions.assertNull(router.routeOrNull("foo/"));
    }

    @Test
    public void routeOrNull_const_rules_common_prefix() {
        Router<String> router = new RouterSetup<String>()
                .add("/foo/foo", "1")
                .add("/foo/bar", "2")
                .build();

        Assertions.assertEquals(match("1"), router.routeOrNull("/foo/foo"));
        Assertions.assertEquals(match("2"), router.routeOrNull("/foo/bar"));

        Assertions.assertNull(router.routeOrNull(""));
        Assertions.assertNull(router.routeOrNull("/"));
        Assertions.assertNull(router.routeOrNull("/foo"));
        Assertions.assertNull(router.routeOrNull("foo/"));
        Assertions.assertNull(router.routeOrNull("/foo/"));
        Assertions.assertNull(router.routeOrNull("/foo/foo/"));
        Assertions.assertNull(router.routeOrNull("/foo/bar/"));
        Assertions.assertNull(router.routeOrNull("/foo/foobar"));
    }

    @Test
    public void routeOrNull_just_variable_rule() {
        Router<String> router = new RouterSetup<String>()
                .add("{var}", "1")
                .build();

        Assertions.assertEquals(match("1", "var=foo"), router.routeOrNull("foo"));
        Assertions.assertEquals(match("1", "var=FOO"), router.routeOrNull("FOO"));
        Assertions.assertEquals(match("1", "var=_"), router.routeOrNull("_"));

        // Doesn't match the slash
        Assertions.assertNull(router.routeOrNull(""));  // var can't be empty
        Assertions.assertNull(router.routeOrNull("/"));
        Assertions.assertNull(router.routeOrNull("foo/"));
        Assertions.assertNull(router.routeOrNull("/foo/"));
    }

    @Test
    public void routeOrNull_const_variable_rule() {
        Router<String> router = new RouterSetup<String>()
                .add("/foo/{var}", "1")
                .build();

        Assertions.assertEquals(match("1", "var=foo"), router.routeOrNull("/foo/foo"));
        Assertions.assertEquals(match("1", "var=FOO"), router.routeOrNull("/foo/FOO"));
        Assertions.assertEquals(match("1", "var=_"), router.routeOrNull("/foo/_"));

        // Not found
        Assertions.assertNull(router.routeOrNull("/foo/"));  // var can't be empty
        Assertions.assertNull(router.routeOrNull("/"));
        Assertions.assertNull(router.routeOrNull("//"));
        Assertions.assertNull(router.routeOrNull("foo/"));
        Assertions.assertNull(router.routeOrNull("/foobar"));

        // Doesn't match the slash
        Assertions.assertNull(router.routeOrNull("foo//"));
        Assertions.assertNull(router.routeOrNull("foo/bar/"));
        Assertions.assertNull(router.routeOrNull("foo/bar//"));
    }

    @Test
    public void routeOrNull_just_const_and_just_variable_rules() {
        Router<String> router = new RouterSetup<String>()
                .add("/foo", "1")
                .add("/{var}", "2")
                .build();

        Assertions.assertEquals(match("1"), router.routeOrNull("/foo"));
        Assertions.assertEquals(match("2", "var=bar"), router.routeOrNull("/bar"));
        Assertions.assertEquals(match("2", "var=foobar"), router.routeOrNull("/foobar"));

        // Not found
        Assertions.assertNull(router.routeOrNull("/foo/"));  // var can't be empty
        Assertions.assertNull(router.routeOrNull("/"));
        Assertions.assertNull(router.routeOrNull("//"));
        Assertions.assertNull(router.routeOrNull("foo"));
        Assertions.assertNull(router.routeOrNull("foo/"));

        // Doesn't match the slash
        Assertions.assertNull(router.routeOrNull("/foo/"));
    }

    @Test
    public void routeOrNull_just_wildcard_rule() {
        Router<String> router = new RouterSetup<String>()
                .add("{*var}", "1")
                .build();

        Assertions.assertEquals(match("1", "var=foo"), router.routeOrNull("foo"));
        Assertions.assertEquals(match("1", "var=FOO"), router.routeOrNull("FOO"));
        Assertions.assertEquals(match("1", "var=foo/bar"), router.routeOrNull("foo/bar"));
        Assertions.assertEquals(match("1", "var=/"), router.routeOrNull("/"));
        Assertions.assertEquals(match("1", "var=_"), router.routeOrNull("_"));

        Assertions.assertNull(router.routeOrNull(""));  // var can't be empty
    }

    @Test
    public void routeOrNull_const_wildcard_rule() {
        Router<String> router = new RouterSetup<String>()
                .add("/foo/{*var}", "1")
                .build();

        Assertions.assertEquals(match("1", "var=foo"), router.routeOrNull("/foo/foo"));
        Assertions.assertEquals(match("1", "var=FOO"), router.routeOrNull("/foo/FOO"));
        Assertions.assertEquals(match("1", "var=bar/"), router.routeOrNull("/foo/bar/"));
        Assertions.assertEquals(match("1", "var=bar//"), router.routeOrNull("/foo/bar//"));
        Assertions.assertEquals(match("1", "var=bar/baz"), router.routeOrNull("/foo/bar/baz"));
        Assertions.assertEquals(match("1", "var=/"), router.routeOrNull("/foo//"));
        Assertions.assertEquals(match("1", "var=_"), router.routeOrNull("/foo/_"));

        // Not found
        Assertions.assertNull(router.routeOrNull("/foo/"));  // var can't be empty
        Assertions.assertNull(router.routeOrNull("/"));
        Assertions.assertNull(router.routeOrNull("//"));
        Assertions.assertNull(router.routeOrNull("foo/"));
        Assertions.assertNull(router.routeOrNull("foo/bar"));
        Assertions.assertNull(router.routeOrNull("foo/bar/"));
        Assertions.assertNull(router.routeOrNull("/foobar"));
    }

    @Test
    public void routeOrNull_const_and_two_vars() {
        Router<String> router = new RouterSetup<String>()
                .add("/foo/bar", "1")
                .add("/foo/{name}", "2")
                .add("/foo/{name}/{age}", "3")
                .build();

        // Const
        Assertions.assertEquals(match("1"), router.routeOrNull("/foo/bar"));
        Assertions.assertNull(router.routeOrNull("/"));
        Assertions.assertNull(router.routeOrNull("/foo"));
        Assertions.assertNull(router.routeOrNull("/foobar"));
        Assertions.assertNull(router.routeOrNull("/foo/bar/"));

        // First var
        Assertions.assertEquals(match("2", "name=foo"), router.routeOrNull("/foo/foo"));
        Assertions.assertEquals(match("2", "name=XXX"), router.routeOrNull("/foo/XXX"));
        Assertions.assertEquals(match("2", "name=_"), router.routeOrNull("/foo/_"));
        Assertions.assertNull(router.routeOrNull("/foo/"));  // name can't be empty
        Assertions.assertNull(router.routeOrNull("/foo/XXX/"));

        // Second var
        Assertions.assertEquals(match("3", "name=XXX", "age=25"), router.routeOrNull("/foo/XXX/25"));
        Assertions.assertNull(router.routeOrNull("/foo/XXX/25/"));
        Assertions.assertNull(router.routeOrNull("/foo/XXX//"));
    }

    @Test
    public void routeOrNull_two_rules_two_vars_all_matching() {
        Router<String> router = new RouterSetup<String>()
                .add("/foo/{name}/default", "1")
                .add("/foo/{name}/{age}", "2")
                .build();

        Assertions.assertEquals(match("2", "name=bar", "age=1"), router.routeOrNull("/foo/bar/1"));
        Assertions.assertEquals(match("2", "name=bar", "age=25"), router.routeOrNull("/foo/bar/25"));
        Assertions.assertEquals(match("2", "name=bar", "age=def"), router.routeOrNull("/foo/bar/def"));
        Assertions.assertEquals(match("1", "name=bar"), router.routeOrNull("/foo/bar/default"));
        Assertions.assertEquals(match("2", "name=bar", "age=default25"), router.routeOrNull("/foo/bar/default25"));
    }

    @Test
    public void routeOrNull_three_rules_two_vars_and_wildcard_all_matching() {
        Router<String> router = new RouterSetup<String>()
                .add("/foo/{name}/default", "1")
                .add("/foo/{name}/{age}", "2")
                .add("/foo/{name}/{*rest}", "3")
                .build();

        Assertions.assertEquals(match("2", "name=bar", "age=1"), router.routeOrNull("/foo/bar/1"));
        Assertions.assertEquals(match("2", "name=bar", "age=25"), router.routeOrNull("/foo/bar/25"));
        Assertions.assertEquals(match("2", "name=bar", "age=def"), router.routeOrNull("/foo/bar/def"));
        Assertions.assertEquals(match("1", "name=bar"), router.routeOrNull("/foo/bar/default"));
        Assertions.assertEquals(match("2", "name=bar", "age=default25"), router.routeOrNull("/foo/bar/default25"));
        Assertions.assertEquals(match("3", "name=bar", "rest=default/"), router.routeOrNull("/foo/bar/default/"));
        Assertions.assertEquals(match("3", "name=bar", "rest=default25/"), router.routeOrNull("/foo/bar/default25/"));
        Assertions.assertEquals(match("3", "name=bar", "rest=default/25"), router.routeOrNull("/foo/bar/default/25"));
    }

    private Router.Match<String> match(String tag) {
        return new Router.Match<>(tag, Collections.emptyMap());
    }

    private Router.Match<String> match(String tag, String ... variables) {
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

    private Router.Match<String> match(String tag, Map<String, CharBuffer> variables) {
        return new Router.Match<>(tag, variables);
    }
}
