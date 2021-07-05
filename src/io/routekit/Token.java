package io.routekit;

import io.routekit.util.CharBuffer;

public interface Token {
    int match(CharBuffer buffer);
}
