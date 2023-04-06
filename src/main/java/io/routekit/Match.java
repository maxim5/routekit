package io.routekit;

import io.routekit.util.CharArray;

import java.util.Map;

/**
 * The routing result of matching a query against the rules.
 * The {@code handler} is the endpoint set up in the rules.
 * The {@code variables} is a (possibly empty) map of variables from the matched rule.
 *
 * @param <T> handler type
 */
public record Match<T>(T handler, Map<String, CharArray> variables) {
}
