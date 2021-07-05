package io.route;

import java.util.List;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof StringQuery that && Objects.equals(query, that.query);
    }

    @Override
    public int hashCode() {
        return Objects.hash(query);
    }
}
