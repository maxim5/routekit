package io.routekit;

import io.routekit.util.CharArray;

/**
 * A separable variable matches everything until the first {@code separator}.
 */
public final class SeparableVariableToken extends Variable implements Token {
    private final char separator;

    public SeparableVariableToken(String name, char separator) {
        super(name);
        this.separator = separator;
    }

    public SeparableVariableToken(String name) {
        this(name, SimpleQueryParser.DEFAULT_SEPARATOR);
    }

    @Override
    public int match(CharArray charArray) {
        return handleEmptyMatch(charArray.indexOf(separator, 0, charArray.length()));  // match until separator
    }

    @Override
    public String toString() {
        return "SeparableVariableToken[%s]".formatted(name());
    }
}
