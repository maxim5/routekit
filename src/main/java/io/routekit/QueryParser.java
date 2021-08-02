package io.routekit;

import io.routekit.util.CharArray;

import java.util.List;

public interface QueryParser {
    List<Token> parse(CharArray input);

    default List<Token> parse(CharSequence input) {
        return parse(new CharArray(input));
    }
}
