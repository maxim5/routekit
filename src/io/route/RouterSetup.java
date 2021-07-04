package io.route;

import java.util.ArrayList;
import java.util.List;

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
        RouterBuilder.Node<T> root = RouterBuilder.build(rules);
        return new Router<>(root);
    }

    record Rule<T>(Query query, T handler) {}
}
