package io.route;

import io.route.util.CharBuffer;

import java.util.List;

public interface QueryParser {
    List<Token> parse(CharBuffer input);

    default List<Token> parse(CharSequence input) {
        return parse(new CharBuffer(input));
    }
}
