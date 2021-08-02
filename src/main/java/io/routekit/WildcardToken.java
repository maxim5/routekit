package io.routekit;

import io.routekit.util.CharArray;

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
