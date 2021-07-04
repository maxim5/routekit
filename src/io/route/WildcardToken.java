package io.route;

public class WildcardToken extends Variable implements Token {
    public WildcardToken(String name) {
        super(name);
    }

    @Override
    public int match(CharBuffer buffer) {
        return buffer.length();
    }
}
