package io.routekit;

import io.routekit.util.CharBuffer;
import io.routekit.util.MutableCharBuffer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public record SimpleQueryParser(char separator) implements QueryParser {
    public static final char VAR_OPEN = '{';
    public static final char VAR_CLOSE = '}';
    public static final char DEFAULT_SEPARATOR = '/';
    public static final SimpleQueryParser DEFAULT = new SimpleQueryParser(DEFAULT_SEPARATOR);

    @Override
    public List<Token> parse(CharBuffer input) {
        QueryParseException.failIf(input.isEmpty(), "Input must not be empty");
        validateBracketSequence(input, VAR_OPEN, VAR_CLOSE);

        MutableCharBuffer buffer = new MutableCharBuffer(input);  // copy to avoid modifying the input
        ArrayList<Token> tokens = new ArrayList<>();
        while (true) {
            int length = buffer.length();
            int open = buffer.indexOf(VAR_OPEN, 0, length);  // match until variable start
            if (open > 0) {
                tokens.add(new ConstToken(buffer.substringUntil(open)));
            }
            if (open < length) {
                int close = buffer.indexOf(VAR_CLOSE, open);
                QueryParseException.failIf(close < 0, "Failed to parse variables in the query: " + input);

                boolean isWildcard = buffer.at(open + 1) == '*';
                String varName = buffer.substring(open + (isWildcard ? 2 : 1), close).toString();
                QueryParseException.failIf(varName.isEmpty(), "Query contains empty variable: " + input);
                QueryParseException.failIf(!varName.matches("[a-zA-Z0-9_$]+"), "Query contains invalid variable: " + input);

                Token token = isWildcard ? new WildcardToken(varName) : new SeparableVariableToken(varName, separator);
                QueryParseException.failIf(
                        tokens.stream().filter(t -> t instanceof Variable).anyMatch(t -> t.equals(token)),
                        "Query contains duplicate variables: " + input);
                tokens.add(token);

                buffer.offsetStart(close + 1);
                QueryParseException.failIf(isWildcard && buffer.isNotEmpty(),
                        "Query contain the wildcard, but doesn't end with it: " + input);
            } else {
                return tokens;
            }
        }
    }

    private static void validateBracketSequence(CharBuffer input, int open, int close) {
        AtomicInteger balance = new AtomicInteger();  // wish I could use an int here...
        input.forEach(value -> {
            if (value == open) {
                QueryParseException.failIf(balance.incrementAndGet() > 1, "Malformed query (nested variables): " + input);
            } else if (value == close) {
                QueryParseException.failIf(balance.decrementAndGet() < 0, "Malformed query (brackets don't match): " + input);
            }
        });
        QueryParseException.failIf(balance.intValue() != 0, "Malformed query (brackets aren't closed): " + input);
    }
}
