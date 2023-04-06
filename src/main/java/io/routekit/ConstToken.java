package io.routekit;

import io.routekit.util.CharArray;

import java.util.Objects;

/**
 * A string literal token.
 */
public final class ConstToken implements Token {
    private final CharArray token;

    public ConstToken(CharArray token) {
        this.token = token;
    }

    public ConstToken(String token) {
        this(new CharArray(token));
    }

    @Override
    public int match(CharArray charArray) {
        // Prefix match should work: "/foo".match("/foo/bar") == 4
        // Partial match should not work: "/foo".match("/") == -1
        return charArray.startsWith(token) ? token.length() : -1;
    }

    public CharArray buffer() {
        return token;
    }

    @Override
    public String toString() {
        return "ConstToken[%s]".formatted(token);
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof ConstToken that && Objects.equals(token, that.token);
    }

    @Override
    public int hashCode() {
        return token.hashCode();
    }
}
