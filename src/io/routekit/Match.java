package io.routekit;

import io.routekit.util.CharBuffer;

import java.util.Map;

record Match<T>(T handler, Map<String, CharBuffer> variables) {
}
