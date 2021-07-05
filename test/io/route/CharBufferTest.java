package io.route;

import io.route.util.CharBuffer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CharBufferTest {
    @Test
    public void createEmpty() {
        CharBuffer buffer = new CharBuffer("");
        Assertions.assertEquals(buffer.start(), 0);
        Assertions.assertEquals(buffer.end(), 0);
        Assertions.assertEquals(buffer.length(), 0);
    }

    @Test
    public void createEmpty2() {
        CharBuffer buffer = new CharBuffer("foo", 3, 3);
        Assertions.assertEquals(buffer.start(), 3);
        Assertions.assertEquals(buffer.end(), 3);
        Assertions.assertEquals(buffer.length(), 0);
    }

    @Test
    public void equals() {
        Assertions.assertEquals(new CharBuffer(""), new CharBuffer(""));
        Assertions.assertEquals(new CharBuffer("foo"), new CharBuffer("foo"));
        Assertions.assertEquals(new CharBuffer("foo"), new CharBuffer("foobar", 0, 3));
    }

    @Test
    public void createInvalid() {
        Assertions.assertThrows(AssertionError.class, () -> new CharBuffer("foo", -1, 2));
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
}
