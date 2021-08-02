package io.routekit;

import io.routekit.util.CharArray;

import java.util.Map;

public record Match<T>(T handler, Map<String, CharArray> variables) {
}
