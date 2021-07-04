package io.route;

public class Router<T> {
    private final RouterBuilder.Node<T> root;

    public Router(RouterBuilder.Node<T> root) {
        this.root = root;
    }
}
