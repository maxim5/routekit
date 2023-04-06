package io.routekit;

import java.util.Objects;

/**
 * A base class for a named variable.
 */
public abstract class Variable {
    private final String name;

    protected Variable(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    // Makes sure that empty match satisfies the option.
    protected static int handleEmptyMatch(int match) {
        return (match == 0) ? -1 : match;
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
