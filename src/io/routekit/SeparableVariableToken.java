package io.routekit;

import io.routekit.util.CharBuffer;

public class SeparableVariableToken extends Variable implements Token {
    private final char separator;

    public SeparableVariableToken(String name, char separator) {
        super(name);
        this.separator = separator;
    }

    public SeparableVariableToken(String name) {
        this(name, SimpleQueryParser.DEFAULT_SEPARATOR);
    }

    @Override
    public int match(CharBuffer buffer) {
        return buffer.matchUntil(separator);
    }

    @Override
    public String toString() {
        return "SeparableVariableToken[" + name() + "]";
    }
}
