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
    public void equals_and_hashCode() {
        assertEqualsHashCode(new CharBuffer(""), new CharBuffer(""));
        assertEqualsHashCode(new CharBuffer(""), new CharBuffer("foo", 0, 0));
        assertEqualsHashCode(new CharBuffer(""), new CharBuffer("foo", 1, 1));
        assertEqualsHashCode(new CharBuffer(""), new CharBuffer("foo", 2, 2));
        assertEqualsHashCode(new CharBuffer(""), new CharBuffer("foo", 3, 3));
        assertEqualsHashCode(new CharBuffer("foo"), new CharBuffer("foo"));
        assertEqualsHashCode(new CharBuffer("foo"), new CharBuffer("foobar", 0, 3));
        assertEqualsHashCode(new CharBuffer("foo"), new CharBuffer("barfoo", 3, 6));
    }

    @Test
    public void create_invalid_pointers() {
        Assertions.assertThrows(AssertionError.class, () -> new CharBuffer("foo", -1, 2));
        Assertions.assertThrows(AssertionError.class, () -> new CharBuffer("foo", 2, 1));
        Assertions.assertThrows(AssertionError.class, () -> new CharBuffer("foo", 0, 4));
        Assertions.assertThrows(AssertionError.class, () -> new CharBuffer("foo", 4, 4));
    }

    @Test
    public void indexOf() {
        CharBuffer buffer = new CharBuffer("foo-bar-baz");

        Assertions.assertEquals(0, buffer.indexOf('f'));
        Assertions.assertEquals(1, buffer.indexOf('o'));
        Assertions.assertEquals(3, buffer.indexOf('-'));
        Assertions.assertEquals(5, buffer.indexOf('a'));
        Assertions.assertEquals(10, buffer.indexOf('z'));
        Assertions.assertEquals(-1, buffer.indexOf('w'));

        Assertions.assertEquals(-1, buffer.indexOf('f', 1));
        Assertions.assertEquals(-2, buffer.indexOf('f', 1, -2));
        Assertions.assertEquals(11, buffer.indexOf('f', 1, buffer.length()));
        Assertions.assertEquals(1, buffer.indexOf('o', 1));
        Assertions.assertEquals(1, buffer.indexOf('o', 1, -2));
        Assertions.assertEquals(3, buffer.indexOf('-', 3));
        Assertions.assertEquals(3, buffer.indexOf('-', 3, buffer.length()));
        Assertions.assertEquals(7, buffer.indexOf('-', 4));
        Assertions.assertEquals(7, buffer.indexOf('-', 4, buffer.length()));
        Assertions.assertEquals(7, buffer.indexOf('-', 4, -2));
        Assertions.assertEquals(7, buffer.indexOf('-', 7, buffer.length()));
        Assertions.assertEquals(11, buffer.indexOf('-', 8, buffer.length()));
    }

    @Test
    public void lastIndexOf() {
        CharBuffer buffer = new CharBuffer("foo-bar-baz");

        Assertions.assertEquals(0, buffer.lastIndexOf('f'));
        Assertions.assertEquals(2, buffer.lastIndexOf('o'));
        Assertions.assertEquals(7, buffer.lastIndexOf('-'));
        Assertions.assertEquals(9, buffer.lastIndexOf('a'));
        Assertions.assertEquals(10, buffer.lastIndexOf('z'));
        Assertions.assertEquals(-1, buffer.lastIndexOf('w'));

        Assertions.assertEquals(0, buffer.lastIndexOf('f', 10));
        Assertions.assertEquals(0, buffer.lastIndexOf('f', 10, -1));
        Assertions.assertEquals(9, buffer.lastIndexOf('a', 10));
        Assertions.assertEquals(9, buffer.lastIndexOf('a', 9));
        Assertions.assertEquals(5, buffer.lastIndexOf('a', 8));
        Assertions.assertEquals(5, buffer.lastIndexOf('a', 5));
        Assertions.assertEquals(-1, buffer.lastIndexOf('a', 4));
        Assertions.assertEquals(-2, buffer.lastIndexOf('a', 4, -2));
    }

    @Test
    public void commonPrefix() {
        Assertions.assertEquals(0, new CharBuffer("foo").commonPrefix(new CharBuffer("bar")));
        Assertions.assertEquals(2, new CharBuffer("bar").commonPrefix(new CharBuffer("baz")));

        Assertions.assertEquals(0, new CharBuffer("foo").commonPrefix(new CharBuffer("foobar", 3, 6)));
        Assertions.assertEquals(2, new CharBuffer("bar").commonPrefix(new CharBuffer("barbaz", 3, 6)));
        Assertions.assertEquals(0, new CharBuffer("foobar", 3, 6).commonPrefix(new CharBuffer("foo")));
        Assertions.assertEquals(2, new CharBuffer("barbaz", 3, 6).commonPrefix(new CharBuffer("bar")));
    }

    @Test
    public void commonPrefix_empty() {
        Assertions.assertEquals(0, new CharBuffer("").commonPrefix(new CharBuffer("")));
        Assertions.assertEquals(0, new CharBuffer("foo").commonPrefix(new CharBuffer("")));
        Assertions.assertEquals(0, new CharBuffer("").commonPrefix(new CharBuffer("foo")));

        Assertions.assertEquals(0, new CharBuffer("foo", 1, 2).commonPrefix(new CharBuffer("foo", 3, 3)));
        Assertions.assertEquals(0, new CharBuffer("xxx", 1, 1).commonPrefix(new CharBuffer("xxx", 2, 2)));
    }

    @Test
    public void commonPrefix_same_prefix() {
        Assertions.assertEquals(3, new CharBuffer("foo").commonPrefix(new CharBuffer("foo")));
        Assertions.assertEquals(3, new CharBuffer("foo").commonPrefix(new CharBuffer("foobar")));
        Assertions.assertEquals(3, new CharBuffer("foobar").commonPrefix(new CharBuffer("foo")));

        Assertions.assertEquals(3, new CharBuffer("foo").commonPrefix(new CharBuffer("barfoo", 3, 6)));
        Assertions.assertEquals(3, new CharBuffer("barfoo", 3, 6).commonPrefix(new CharBuffer("foo")));
    }

    @Test
    public void commonSuffix() {
        Assertions.assertEquals(0, new CharBuffer("foo").commonSuffix(new CharBuffer("bar")));
        Assertions.assertEquals(2, new CharBuffer("foo").commonSuffix(new CharBuffer("boo")));

        Assertions.assertEquals(0, new CharBuffer("foo").commonSuffix(new CharBuffer("foobar", 3, 6)));
        Assertions.assertEquals(2, new CharBuffer("foo").commonSuffix(new CharBuffer("fooboo", 3, 6)));
        Assertions.assertEquals(0, new CharBuffer("foobar", 3, 6).commonSuffix(new CharBuffer("foo")));
        Assertions.assertEquals(2, new CharBuffer("fooboo", 3, 6).commonSuffix(new CharBuffer("foo")));
    }

    @Test
    public void commonSuffix_empty() {
        Assertions.assertEquals(0, new CharBuffer("").commonSuffix(new CharBuffer("")));
        Assertions.assertEquals(0, new CharBuffer("foo").commonSuffix(new CharBuffer("")));
        Assertions.assertEquals(0, new CharBuffer("").commonSuffix(new CharBuffer("foo")));

        Assertions.assertEquals(0, new CharBuffer("foo", 1, 2).commonSuffix(new CharBuffer("foo", 3, 3)));
        Assertions.assertEquals(0, new CharBuffer("xxx", 1, 1).commonSuffix(new CharBuffer("xxx", 2, 2)));
    }

    @Test
    public void commonSuffix_same_suffix() {
        Assertions.assertEquals(3, new CharBuffer("foo").commonSuffix(new CharBuffer("foo")));
        Assertions.assertEquals(3, new CharBuffer("foo").commonSuffix(new CharBuffer("barfoo")));
        Assertions.assertEquals(3, new CharBuffer("barfoo").commonSuffix(new CharBuffer("foo")));

        Assertions.assertEquals(3, new CharBuffer("foo").commonSuffix(new CharBuffer("barfoo", 3, 6)));
        Assertions.assertEquals(3, new CharBuffer("barfoo", 3, 6).commonSuffix(new CharBuffer("foo")));
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
        Assertions.assertFalse(buffer.startsWith(new CharBuffer("foe")));
        Assertions.assertFalse(buffer.startsWith(new CharBuffer("foo!")));
        Assertions.assertFalse(buffer.startsWith(new CharBuffer("foobar")));
    }

    @Test
    public void startsWith_string() {
        CharBuffer buffer = new CharBuffer("foo");
        Assertions.assertTrue(buffer.startsWith(""));
        Assertions.assertTrue(buffer.startsWith("f"));
        Assertions.assertTrue(buffer.startsWith("fo"));
        Assertions.assertTrue(buffer.startsWith("foo"));

        Assertions.assertFalse(buffer.startsWith("x"));
        Assertions.assertFalse(buffer.startsWith("bar"));
        Assertions.assertFalse(buffer.startsWith("foe"));
        Assertions.assertFalse(buffer.startsWith("foo!"));
        Assertions.assertFalse(buffer.startsWith("foobar"));
    }

    @Test
    public void startsWith_char() {
        CharBuffer buffer = new CharBuffer("foo");
        Assertions.assertTrue(buffer.startsWith('f'));
        Assertions.assertFalse(buffer.startsWith('o'));
        Assertions.assertFalse(buffer.startsWith('a'));
        Assertions.assertFalse(buffer.startsWith('x'));

        Assertions.assertFalse(new CharBuffer("").startsWith(' '));
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
        Assertions.assertFalse(buffer.endsWith(new CharBuffer("boo")));
        Assertions.assertFalse(buffer.endsWith(new CharBuffer("!foo")));
        Assertions.assertFalse(buffer.endsWith(new CharBuffer("barfoo")));
    }

    @Test
    public void endsWith_string() {
        CharBuffer buffer = new CharBuffer("foo");
        Assertions.assertTrue(buffer.endsWith(""));
        Assertions.assertTrue(buffer.endsWith("o"));
        Assertions.assertTrue(buffer.endsWith("oo"));
        Assertions.assertTrue(buffer.endsWith("foo"));

        Assertions.assertFalse(buffer.endsWith("x"));
        Assertions.assertFalse(buffer.endsWith("bar"));
        Assertions.assertFalse(buffer.endsWith("boo"));
        Assertions.assertFalse(buffer.endsWith("!foo"));
        Assertions.assertFalse(buffer.endsWith("barfoo"));
    }

    @Test
    public void endsWith_char() {
        CharBuffer buffer = new CharBuffer("foo");
        Assertions.assertTrue(buffer.endsWith('o'));
        Assertions.assertFalse(buffer.endsWith('f'));
        Assertions.assertFalse(buffer.endsWith('a'));
        Assertions.assertFalse(buffer.endsWith('x'));

        Assertions.assertFalse(new CharBuffer("").endsWith(' '));
    }

    @Test
    public void join_same_buffer() {
        CharBuffer buffer = new CharBuffer("foobar");
        CharBuffer foo = buffer.substringUntil(3);
        CharBuffer bar = buffer.substringFrom(3);
        CharBuffer join = CharBuffer.join(foo, bar);

        Assertions.assertSame(buffer.chars, foo.chars);
        Assertions.assertSame(buffer.chars, bar.chars);
        Assertions.assertSame(buffer.chars, join.chars);
        Assertions.assertEquals(buffer, join);
    }

    @Test
    public void join_not_same_buffer() {
        CharBuffer buffer = new CharBuffer("foobar");
        CharBuffer foo = new CharBuffer("foo");
        CharBuffer bar = new CharBuffer("bar");
        CharBuffer join = CharBuffer.join(foo, bar);

        Assertions.assertNotSame(buffer.chars, foo.chars);
        Assertions.assertNotSame(buffer.chars, bar.chars);
        Assertions.assertNotSame(buffer.chars, join.chars);
        Assertions.assertEquals(buffer, join);
    }

    @Test
    public void cutPrefix() {
        CharBuffer foobar = new CharBuffer("foobar");
        CharBuffer foo = new CharBuffer("foo");
        CharBuffer bar = new CharBuffer("bar");
        CharBuffer empty = new CharBuffer("");

        Assertions.assertEquals(foobar.cutPrefix(empty), foobar);
        Assertions.assertEquals(foobar.cutPrefix(foo), bar);
        Assertions.assertEquals(foobar.cutPrefix(foo), bar);
        Assertions.assertEquals(foobar.cutPrefix(foobar), empty);

        Assertions.assertEquals(foobar.substringFrom(3), bar);
        Assertions.assertEquals(foobar.substringFrom(3).cutPrefix(foo), bar);
        Assertions.assertEquals(foobar.substringFrom(3).cutPrefix(bar), empty);
        Assertions.assertEquals(foobar.substringFrom(3).cutPrefix(bar.substringUntil(0)), bar);
        Assertions.assertEquals(foobar.substringFrom(3).cutPrefix(bar.substringUntil(1)), new CharBuffer("ar"));  // cut b
        Assertions.assertEquals(foobar.substringFrom(3).cutPrefix(bar.substringUntil(2)), new CharBuffer("r"));   // cut ba
    }

    @Test
    public void cutSuffix() {
        CharBuffer foobar = new CharBuffer("foobar");
        CharBuffer foo = new CharBuffer("foo");
        CharBuffer bar = new CharBuffer("bar");
        CharBuffer empty = new CharBuffer("");

        Assertions.assertEquals(foobar.cutSuffix(empty), foobar);
        Assertions.assertEquals(foobar.cutSuffix(bar), foo);
        Assertions.assertEquals(foobar.cutSuffix(foo), foobar);
        Assertions.assertEquals(foobar.cutSuffix(foobar), empty);

        Assertions.assertEquals(foobar.substringFrom(3), bar);
        Assertions.assertEquals(foobar.substringFrom(3).cutSuffix(foo), bar);
        Assertions.assertEquals(foobar.substringFrom(3).cutSuffix(bar), empty);
        Assertions.assertEquals(foobar.substringFrom(3).cutSuffix(bar.substringFrom(1)), new CharBuffer("b"));   // cut ar
        Assertions.assertEquals(foobar.substringFrom(3).cutSuffix(bar.substringFrom(2)), new CharBuffer("ba"));  // cut r
        Assertions.assertEquals(foobar.substringFrom(3).cutSuffix(bar.substringFrom(3)), bar);
    }

    private static void assertEqualsHashCode(CharBuffer lhs, CharBuffer rhs) {
        Assertions.assertEquals(lhs, rhs);
        Assertions.assertEquals(lhs.hashCode(), rhs.hashCode());
    }
}
