package io.route;

import io.route.util.CharBuffer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RouterSetup<T> {
    private QueryParser parser = SimpleQueryParser.DEFAULT;
    private final List<Rule<T>> rules = new ArrayList<>();

    public RouterSetup<T> withParser(QueryParser parser) {
        this.parser = parser;
        return this;
    }

    public RouterSetup<T> add(Query query, T handler) {
        rules.add(new Rule<>(query, handler));
        return this;
    }

    public RouterSetup<T> add(String query, T handler) {
        return add(new StringQuery(query, parser), handler);
    }

    public RouterSetup<T> addManyQueries(Map<Query, T> handlers) {
        for (Map.Entry<Query, T> entry : handlers.entrySet()) {
            add(entry.getKey(), entry.getValue());
        }
        return this;
    }

    public RouterSetup<T> addMany(Map<String, T> handlers) {
        for (Map.Entry<String, T> entry : handlers.entrySet()) {
            add(entry.getKey(), entry.getValue());
        }
        return this;
    }

    public Router<T> build() {
        RouterBuilder.Node<T> root = RouterBuilder.buildNode(rules);
        Map<CharBuffer, T> quickMatchIndex = RouterBuilder.buildQuickMatchIndex(rules);
        return new Router<>(root, quickMatchIndex);
    }

    record Rule<T>(Query query, T handler) {}
}
