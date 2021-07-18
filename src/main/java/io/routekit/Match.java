package io.routekit;

import io.routekit.util.CharBuffer;

import java.util.Map;

public record Match<T>(T handler, Map<String, CharBuffer> variables) {
}
