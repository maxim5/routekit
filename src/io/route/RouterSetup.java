package io.route;

import io.route.util.CharBuffer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RouterSetup<T> {
    private QueryParser parser;
    private final List<Rule<T>> rules = new ArrayList<>();

    public RouterSetup<T> withParser(QueryParser parser) {
        this.parser = parser;
        return this;
    }

    public void add(Query query, T handler) {
        rules.add(new Rule<>(query, handler));
    }

    public void add(String query, T handler) {
        add(new StringQuery(query, parser), handler);
    }

    public Router<T> build() {
        RouterBuilder.Node<T> root = RouterBuilder.buildNode(rules);
        Map<CharBuffer, T> quickMatchIndex = RouterBuilder.buildQuickMatchIndex(rules);
        return new Router<>(root, quickMatchIndex);
    }

    record Rule<T>(Query query, T handler) {}
}
