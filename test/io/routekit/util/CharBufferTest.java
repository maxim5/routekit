package io.routekit.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CharBufferTest {
    @Test
    public void create_empty_string() {
        CharBuffer buffer = new CharBuffer("");
        Assertions.assertEquals(buffer.start(), 0);
        Assertions.assertEquals(buffer.end(), 0);
        Assertions.assertEquals(buffer.length(), 0);
    }

    @Test
    public void create_empty_same_pointers() {
        CharBuffer buffer = new CharBuffer("foo", 3, 3);
        Assertions.assertEquals(buffer.start(), 3);
        Assertions.assertEquals(buffer.end(), 3);
        Assertions.assertEquals(buffer.length(), 0);
    }

    @Test
    public void equals() {
        Assertions.assertEquals(new CharBuffer(""), new CharBuffer(""));
        Assertions.assertEquals(new CharBuffer(""), new CharBuffer("foo", 0, 0));
        Assertions.assertEquals(new CharBuffer(""), new CharBuffer("foo", 1, 1));
        Assertions.assertEquals(new CharBuffer(""), new CharBuffer("foo", 2, 2));
        Assertions.assertEquals(new CharBuffer(""), new CharBuffer("foo", 3, 3));
        Assertions.assertEquals(new CharBuffer("foo"), new CharBuffer("foo"));
        Assertions.assertEquals(new CharBuffer("foo"), new CharBuffer("foobar", 0, 3));
    }

    @Test
    public void create_invalid_pointers() {
        Assertions.assertThrows(AssertionError.class, () -> new CharBuffer("foo", -1, 2));
        Assertions.assertThrows(AssertionError.class, () -> new CharBuffer("foo", 2, 1));
        Assertions.assertThrows(AssertionError.class, () -> new CharBuffer("foo", 0, 4));
        Assertions.assertThrows(AssertionError.class, () -> new CharBuffer("foo", 4, 4));
    }

    @Test
    public void matchUntil() {
        CharBuffer buffer = new CharBuffer("foo-bar-baz");
        Assertions.assertEquals(0, buffer.matchUntil('f'));
        Assertions.assertEquals(1, buffer.matchUntil('o'));
        Assertions.assertEquals(3, buffer.matchUntil('-'));
        Assertions.assertEquals(10, buffer.matchUntil('z'));
        Assertions.assertEquals(11, buffer.matchUntil('w'));
    }

    @Test
    public void matchUntilFrom() {
        CharBuffer buffer = new CharBuffer("foo-bar-baz");
        Assertions.assertEquals(11, buffer.matchUntil(1, 'f'));
        Assertions.assertEquals(3, buffer.matchUntil(3, '-'));
        Assertions.assertEquals(7, buffer.matchUntil(4, '-'));
        Assertions.assertEquals(7, buffer.matchUntil(7, '-'));
        Assertions.assertEquals(11, buffer.matchUntil(8, '-'));
    }

    @Test
    public void matchCommon() {
        Assertions.assertEquals(0, new CharBuffer("foo").matchCommon(new CharBuffer("bar")));
        Assertions.assertEquals(2, new CharBuffer("bar").matchCommon(new CharBuffer("baz")));

        Assertions.assertEquals(0, new CharBuffer("foo").matchCommon(new CharBuffer("foobar", 3, 6)));
        Assertions.assertEquals(2, new CharBuffer("bar").matchCommon(new CharBuffer("barbaz", 3, 6)));
        Assertions.assertEquals(0, new CharBuffer("foobar", 3, 6).matchCommon(new CharBuffer("foo")));
        Assertions.assertEquals(2, new CharBuffer("barbaz", 3, 6).matchCommon(new CharBuffer("bar")));
    }

    @Test
    public void matchCommon_empty() {
        Assertions.assertEquals(0, new CharBuffer("").matchCommon(new CharBuffer("")));
        Assertions.assertEquals(0, new CharBuffer("foo").matchCommon(new CharBuffer("")));
        Assertions.assertEquals(0, new CharBuffer("").matchCommon(new CharBuffer("foo")));

        Assertions.assertEquals(0, new CharBuffer("foo", 1, 2).matchCommon(new CharBuffer("foo", 3, 3)));
        Assertions.assertEquals(0, new CharBuffer("xxx", 1, 1).matchCommon(new CharBuffer("xxx", 2, 2)));
    }

    @Test
    public void matchCommon_common_prefix() {
        Assertions.assertEquals(3, new CharBuffer("foo").matchCommon(new CharBuffer("foo")));
        Assertions.assertEquals(3, new CharBuffer("foo").matchCommon(new CharBuffer("foobar")));
        Assertions.assertEquals(3, new CharBuffer("foobar").matchCommon(new CharBuffer("foo")));

        Assertions.assertEquals(3, new CharBuffer("foo").matchCommon(new CharBuffer("barfoo", 3, 6)));
        Assertions.assertEquals(3, new CharBuffer("barfoo", 3, 6).matchCommon(new CharBuffer("foo")));
    }

    @Test
    public void startsWith() {
        CharBuffer buffer = new CharBuffer("foo");
        Assertions.assertTrue(buffer.startsWith(new CharBuffer("")));
        Assertions.assertTrue(buffer.startsWith(new CharBuffer("f")));
        Assertions.assertTrue(buffer.startsWith(new CharBuffer("fo")));
        Assertions.assertTrue(buffer.startsWith(new CharBuffer("foo")));

        Assertions.assertFalse(buffer.startsWith(new CharBuffer("x")));
        Assertions.assertFalse(buffer.startsWith(new CharBuffer("bar")));
        Assertions.assertFalse(buffer.startsWith(new CharBuffer("foo!")));
        Assertions.assertFalse(buffer.startsWith(new CharBuffer("foobar")));
    }

    @Test
    public void endsWith() {
        CharBuffer buffer = new CharBuffer("foo");
        Assertions.assertTrue(buffer.endsWith(new CharBuffer("")));
        Assertions.assertTrue(buffer.endsWith(new CharBuffer("o")));
        Assertions.assertTrue(buffer.endsWith(new CharBuffer("oo")));
        Assertions.assertTrue(buffer.endsWith(new CharBuffer("foo")));

        Assertions.assertFalse(buffer.endsWith(new CharBuffer("x")));
        Assertions.assertFalse(buffer.endsWith(new CharBuffer("bar")));
        Assertions.assertFalse(buffer.endsWith(new CharBuffer("!foo")));
        Assertions.assertFalse(buffer.endsWith(new CharBuffer("barfoo")));
    }
}
