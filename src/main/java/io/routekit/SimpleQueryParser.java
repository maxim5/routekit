package io.routekit;

import io.routekit.util.CharArray;
import io.routekit.util.MutableCharArray;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A query parser that recognizes basic token set. Example accepted queries:
 * <pre>
 *     /foo/bar          -> no variables, a single string literal
 *     /{foo}            -> string literals and one variable {foo}
 *     /foo/{foo}/{*bar} -> two variables {foo} and {*bar} (wildcard)
 * </pre>
 *
 * @param separator a char that marks the end of a variable
 */
public record SimpleQueryParser(char separator) implements QueryParser {
    public static final char VAR_OPEN = '{';
    public static final char VAR_CLOSE = '}';
    public static final char DEFAULT_SEPARATOR = '/';
    public static final SimpleQueryParser DEFAULT = new SimpleQueryParser(DEFAULT_SEPARATOR);

    @Override
    public List<Token> parse(CharArray input) {
        QueryValidator validator = new QueryValidator(separator, input);
        validateBracketSequence(input, VAR_OPEN, VAR_CLOSE);

        MutableCharArray array = input.mutableCopy();  // copy to avoid modifying the input
        ArrayList<Token> tokens = new ArrayList<>();
        while (true) {
            int length = array.length();
            int open = array.indexOf(VAR_OPEN, 0, length);  // match until variable start
            if (open > 0) {
                tokens.add(new ConstToken(array.substringUntil(open)));
            }
            if (open < length) {
                int close = array.indexOf(VAR_CLOSE, open);
                QueryParseException.failIf(close < 0, "Failed to parse variables in the query: " + input);

                boolean isWildcard = array.at(open + 1) == '*';
                String varName = array.substring(open + (isWildcard ? 2 : 1), close).toString();

                Token token = isWildcard ? new WildcardToken(varName) : new SeparableVariableToken(varName, separator);
                tokens.add(token);

                array.offsetStart(close + 1);
            } else {
                validator.checkTokens(tokens);
                return tokens;
            }
        }
    }

    private static void validateBracketSequence(CharArray input, int open, int close) {
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
