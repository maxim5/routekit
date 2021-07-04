package io.route;

import java.util.List;

public class StringQuery implements Query {
    private final String query;
    private final List<Token> tokens;

    public StringQuery(String query) {
        this(query, SimpleQueryParser.DEFAULT);
    }

    public StringQuery(String query, QueryParser parser) {
        this.query = query;
        this.tokens = parser.parse(query);
    }

    public String getQuery() {
        return query;
    }

    @Override
    public List<Token> tokens() {
        return tokens;
    }
}
