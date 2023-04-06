package io.routekit;

import java.util.List;

/**
 * Represents a parsed query to be routed, i.e. a sequence of tokens.
 */
public interface Query {
    /**
     * Returns the list of parsed tokens
     */
    List<Token> tokens();
}
