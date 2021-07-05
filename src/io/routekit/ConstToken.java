package io.routekit;

import io.routekit.util.CharBuffer;

import java.util.Objects;

public class ConstToken implements Token {
    private final CharBuffer token;

    public ConstToken(CharBuffer token) {
        this.token = token;
    }

    public ConstToken(String token) {
        this(new CharBuffer(token));
    }

    @Override
    public int match(CharBuffer buffer) {
        // Prefix match should work: "/foo".match("/foo/bar") == 4
        // Partial match should not work: "/foo".match("/") == -1
        return buffer.startsWith(token) ? token.length() : -1;
    }

    public CharBuffer buffer() {
        return token;
    }

    @Override
    public String toString() {
        return "ConstToken[" + token + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConstToken that = (ConstToken) o;
        return Objects.equals(token, that.token);
    }

    @Override
    public int hashCode() {
        return Objects.hash(token);
    }
}
