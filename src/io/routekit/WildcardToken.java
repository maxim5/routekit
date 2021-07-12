package io.routekit;

import io.routekit.util.CharBuffer;

public class WildcardToken extends Variable implements Token {
    public WildcardToken(String name) {
        super(name);
    }

    @Override
    public int match(CharBuffer buffer) {
        return handleEmptyMatch(buffer.length());
    }

    @Override
    public String toString() {
        return "WildcardToken[%s]".formatted(name());
    }
}
