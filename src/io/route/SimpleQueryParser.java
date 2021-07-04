package io.route;

import java.util.ArrayList;
import java.util.List;

public record SimpleQueryParser(char separator) implements QueryParser {
    public static final char DEFAULT_SEPARATOR = '/';
    public static final SimpleQueryParser DEFAULT = new SimpleQueryParser(DEFAULT_SEPARATOR);

    @Override
    public List<Token> parse(CharBuffer input) {
        QueryParseException.failIf(input.isEmpty(), "Input must not be empty");

        CharBuffer buffer = new CharBuffer(input);
        ArrayList<Token> tokens = new ArrayList<>();
        while (true) {
            int length = buffer.length();
            int open = buffer.matchUntil('{');
            if (open > 0) {
                tokens.add(new ConstToken(buffer.substringUntil(open)));
            }
            if (open < length) {
                int close = buffer.matchUntil(open, '}');
                QueryParseException.failIf(close >= length, "Failed to parse variables in the query: " + input);

                boolean isWildcard = buffer.at(open + 1) == '*';
                String varName = buffer.substring(open + (isWildcard ? 2 : 1), close).toString();
                QueryParseException.failIf(varName.isEmpty(), "Query contains empty variable: " + input);
                QueryParseException.failIf(!varName.matches("[^{}*]+"), "Query contains invalid variable: " + input);

                Token token = isWildcard ? new WildcardToken(varName) : new SeparableVariableToken(varName, separator);
                tokens.add(token);

                buffer.offsetStart(close + 1);
                QueryParseException.failIf(isWildcard && buffer.isNotEmpty(),
                        "Query contain the wildcard, but doesn't end with it: " + input);
            } else {
                return tokens;
            }
        }
    }
}
