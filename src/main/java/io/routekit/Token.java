package io.routekit;

import io.routekit.util.CharArray;

public interface Token {
    // Returns the length of the match with the {@link charArray}.
    // Result is negative if doesn't match.
    int match(CharArray charArray);
}
