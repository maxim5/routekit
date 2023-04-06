package io.routekit;

import io.routekit.util.CharArray;

import java.util.List;
import java.util.function.Consumer;

/*package*/ record QueryValidator(char separator, CharArray input) {
    QueryValidator {
        QueryParseException.failIf(input.isEmpty(), "Query can not be empty");
    }

    public void checkTokens(List<Token> tokens) {
        checkVarNames(tokens);
        checkVarsProperlySeparated(tokens);
        checkWildcardAtEnd(tokens);
    }

    public void checkVarNames(List<Token> tokens) {
        List<String> vars = tokens.stream().filter(QueryValidator::isVar).map(t -> ((Variable) t).name()).toList();
        vars.forEach(this::checkVarName);
        if (vars.size() > 1) {
            QueryParseException.failIf(vars.size() > vars.stream().distinct().count(),
                "Query contains duplicate variables", input);
        }
    }

    private void checkVarName(String varName) {
        QueryParseException.failIf(varName.isEmpty(), "Query contains empty variable", input);
        QueryParseException.failIf(!varName.matches("[a-zA-Z0-9_$]+"), "Query contains invalid variable", input);
    }

    public void checkVarsProperlySeparated(List<Token> tokens) {
        tokens.forEach(new Consumer<>() {
            private boolean wasVar = false;

            @Override
            public void accept(Token token) {
                boolean isVar = isVar(token);
                if (wasVar) {
                    QueryParseException.failIf(isVar,
                        "Query contains two non-separated variables, making the second variables unmatchable", input);
                    if (token instanceof ConstToken constToken) {
                        QueryParseException.failIf(
                            !constToken.buffer().startsWith(separator),
                            "Query contains a variable not followed by a '%s' separator".formatted(separator), input);
                    }
                }
                wasVar = isVar;
            }
        });
    }

    public void checkWildcardAtEnd(List<Token> tokens) {
        tokens.forEach(new Consumer<>() {
            private boolean wasWildcard = false;

            @Override
            public void accept(Token token) {
                QueryParseException.failIf(wasWildcard, "Query contain the wildcard, but doesn't end with it", input);
                wasWildcard = isWildcard(token);
            }
        });
    }

    private static boolean isVar(Token t) {
        return t instanceof Variable;
    }

    private static boolean isWildcard(Token t) {
        return t instanceof WildcardToken;
    }
}
