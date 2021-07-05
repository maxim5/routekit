package io.route;

import io.route.util.CharBuffer;

public interface Token {
    int match(CharBuffer buffer);
}
