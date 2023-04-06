package io.routekit;

import io.routekit.util.CharArray;

/**
 * Represents a unit of the URL that can be matched with the input.
 * Can be a const string literal, a variable, wildcard, etc.
 */
public interface Token {
    /**
     * Returns the length of the match with the {@link CharArray}.
     * Result is negative if the token doesn't match.
     * <p>
     * Examples:
     * <pre>
     *     // If the const token is a prefix, the whole token is matched.
     *     new ConstToken("/foo").match("/foo/bar") == 4
     *
     *     // If the const token is only partially matched, it's discarded.
     *     new ConstToken("/foo").match("/") == -1
     *
     *     // The variable should match the prefix up until the first separator.
     *     new SeparableVariableToken("var", "/").match("foo/bar") == 3
     * </pre>
     */
    int match(CharArray charArray);
}
