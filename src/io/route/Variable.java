package io.route;

import java.util.Objects;

public class Variable {
    private final String name;

    public Variable(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    @Override
    public boolean equals(Object other) {
        // Note: does not matter if it's a wildcard or not
        return this == other || other instanceof Variable variable && Objects.equals(name, variable.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
