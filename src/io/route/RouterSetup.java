package io.route;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RouterSetup<T> {
    private QueryParser parser = SimpleQueryParser.DEFAULT;
    private RouterBuilder builder = new RouterBuilder();

    private final List<Rule<T>> rules = new ArrayList<>();

    public RouterSetup<T> withParser(QueryParser parser) {
        this.parser = parser;
        return this;
    }

    public RouterSetup<T> withBuilder(RouterBuilder builder) {
        this.builder = builder;
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
        return builder.buildRouter(rules);
    }

    record Rule<T>(Query query, T handler) {
        boolean isConstant() {
            List<Token> tokens = query.tokens();
            return tokens.size() == 1 && tokens.get(0) instanceof ConstToken;
        }
    }
}
