package io.routekit;

import io.routekit.util.CharBuffer;

public interface Token {
    // Returns the length of the match with the {@link buffer}.
    // Result is negative if doesn't match.
    int match(CharBuffer buffer);
}
