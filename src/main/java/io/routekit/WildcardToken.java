package io.routekit;

import io.routekit.util.CharArray;

/**
 * A wildcard variable matches everything until the end of the input.
 */
public final class WildcardToken extends Variable implements Token {
    public WildcardToken(String name) {
        super(name);
    }

    @Override
    public int match(CharArray charArray) {
        return handleEmptyMatch(charArray.length());
    }

    @Override
    public String toString() {
        return "WildcardToken[%s]".formatted(name());
    }
}
