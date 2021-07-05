package io.routekit;

import io.routekit.util.CharBuffer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

public class RouterTest {
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
        Assertions.assertEquals(match("2", "name=XXX"), router.routeOrNull("/foo/XXX"));
        Assertions.assertNull(router.routeOrNull("/foo/"));
        Assertions.assertNull(router.routeOrNull("/foo/XXX/"));

        // Second var
        Assertions.assertEquals(match("3", "name=XXX", "age=25"), router.routeOrNull("/foo/XXX/25"));
        Assertions.assertNull(router.routeOrNull("/foo/XXX/25/"));
        Assertions.assertNull(router.routeOrNull("/foo/XXX//"));
    }

    private Router.Match<String> match(String tag) {
        return new Router.Match<>(tag, Collections.emptyMap());
    }

    private Router.Match<String> match(String tag, String ... variables) {
        Map<String, CharBuffer> map = Arrays.stream(variables)
                .map(var -> var.split("="))
                .filter(split -> split.length == 2)
                .collect(Collectors.toMap(split -> split[0], split -> new CharBuffer(split[1])));
        return match(tag, map);
    }

    private Router.Match<String> match(String tag, Map<String, CharBuffer> variables) {
        return new Router.Match<>(tag, variables);
    }
}
