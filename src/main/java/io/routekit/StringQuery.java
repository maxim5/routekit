package io.routekit;

import java.util.List;
import java.util.Objects;

/**
 * A query implementation which holds the original string {@code query} and the parsed {@code tokens} list.
 */
public final class StringQuery implements Query {
    private final String query;
    private final List<Token> tokens;

    public StringQuery(String query, List<Token> tokens) {
        this.query = query;
        this.tokens = tokens;
    }

    public String getQuery() {
        return query;
    }

    @Override
    public List<Token> tokens() {
        return tokens;
    }

    @Override
    public String toString() {
        return query;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof StringQuery that && Objects.equals(query, that.query);
    }

    @Override
    public int hashCode() {
        return query.hashCode();
    }
}
