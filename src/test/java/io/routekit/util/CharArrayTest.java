package io.routekit.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.CharBuffer;

public class CharArrayTest {
    @Test
    public void create_empty_string() {
        CharArray array = new CharArray("");
        Assertions.assertEquals(array.start(), 0);
        Assertions.assertEquals(array.end(), 0);
        Assertions.assertEquals(array.length(), 0);
    }

    @Test
    public void create_empty_same_pointers() {
        CharArray array = new CharArray("foo", 3, 3);
        Assertions.assertEquals(array.start(), 3);
        Assertions.assertEquals(array.end(), 3);
        Assertions.assertEquals(array.length(), 0);
    }

    @Test
    public void create_from_nio_buffer_readonly() {
        CharBuffer nioBuffer = CharBuffer.wrap("foobar", 2, 5);
        Assertions.assertTrue(nioBuffer.isReadOnly());

        CharArray array = new CharArray(nioBuffer);
        Assertions.assertEquals(new CharArray("oba"), array);
        Assertions.assertArrayEquals("oba".toCharArray(), array.chars);
        Assertions.assertEquals(0, array.start);
        Assertions.assertEquals(3, array.end);
    }

    @Test
    public void create_from_nio_buffer_writable() {
        CharBuffer nioBuffer = CharBuffer.wrap("foobar".toCharArray(), 2, 3);
        Assertions.assertFalse(nioBuffer.isReadOnly());

        CharArray array = new CharArray(nioBuffer);
        Assertions.assertEquals(new CharArray("oba"), array);
        Assertions.assertArrayEquals("foobar".toCharArray(), array.chars);
        Assertions.assertEquals(2, array.start);
        Assertions.assertEquals(5, array.end);
    }

    @Test
    public void equals_and_hashCode() {
        assertEqualsHashCode(new CharArray(""), new CharArray(""));
        assertEqualsHashCode(new CharArray(""), new CharArray("foo", 0, 0));
        assertEqualsHashCode(new CharArray(""), new CharArray("foo", 1, 1));
        assertEqualsHashCode(new CharArray(""), new CharArray("foo", 2, 2));
        assertEqualsHashCode(new CharArray(""), new CharArray("foo", 3, 3));
        assertEqualsHashCode(new CharArray("foo"), new CharArray("foo"));
        assertEqualsHashCode(new CharArray("foo"), new CharArray("foobar", 0, 3));
        assertEqualsHashCode(new CharArray("foo"), new CharArray("barfoo", 3, 6));
    }

    @Test
    public void create_invalid_pointers() {
        //noinspection ConstantConditions
        Assertions.assertThrows(AssertionError.class, () -> new CharArray((char[]) null, 0, 0));
        Assertions.assertThrows(AssertionError.class, () -> new CharArray("foo", -1, 2));
        Assertions.assertThrows(AssertionError.class, () -> new CharArray("foo", 2, 1));
        Assertions.assertThrows(AssertionError.class, () -> new CharArray("foo", 0, 4));
        Assertions.assertThrows(AssertionError.class, () -> new CharArray("foo", 4, 4));
    }

    @Test
    public void indexOf() {
        CharArray array = new CharArray("foo-bar-baz");

        Assertions.assertEquals(0, array.indexOf('f'));
        Assertions.assertEquals(1, array.indexOf('o'));
        Assertions.assertEquals(3, array.indexOf('-'));
        Assertions.assertEquals(5, array.indexOf('a'));
        Assertions.assertEquals(10, array.indexOf('z'));
        Assertions.assertEquals(-1, array.indexOf('w'));

        Assertions.assertEquals(-1, array.indexOf('f', 1));
        Assertions.assertEquals(-2, array.indexOf('f', 1, -2));
        Assertions.assertEquals(11, array.indexOf('f', 1, array.length()));
        Assertions.assertEquals(1, array.indexOf('o', 1));
        Assertions.assertEquals(1, array.indexOf('o', 1, -2));
        Assertions.assertEquals(3, array.indexOf('-', 3));
        Assertions.assertEquals(3, array.indexOf('-', 3, array.length()));
        Assertions.assertEquals(7, array.indexOf('-', 4));
        Assertions.assertEquals(7, array.indexOf('-', 4, array.length()));
        Assertions.assertEquals(7, array.indexOf('-', 4, -2));
        Assertions.assertEquals(7, array.indexOf('-', 7, array.length()));
        Assertions.assertEquals(11, array.indexOf('-', 8, array.length()));
    }

    @Test
    public void indexOf_subarray() {
        CharArray array = new CharArray("foobar", 1, 4);  // oob

        Assertions.assertEquals(-1, array.indexOf('f'));
        Assertions.assertEquals(-1, array.indexOf('a'));
        Assertions.assertEquals(2, array.indexOf('b'));
        Assertions.assertEquals(-1, array.indexOf('o', 3));
        Assertions.assertEquals(-2, array.indexOf('o', 3, -2));
    }

    @Test
    public void indexOfAny() {
        CharArray array = new CharArray("foo-bar-baz");

        Assertions.assertEquals(0, array.indexOfAny('f', 'o'));
        Assertions.assertEquals(1, array.indexOfAny('o', 'o'));
        Assertions.assertEquals(3, array.indexOfAny('a', '-'));
        Assertions.assertEquals(5, array.indexOfAny('a', 'z'));
        Assertions.assertEquals(10, array.indexOfAny('z', 'w'));
        Assertions.assertEquals(-1, array.indexOfAny('x', 'y'));

        Assertions.assertEquals(-1, array.indexOfAny('f', 'g', 1));
        Assertions.assertEquals(-2, array.indexOfAny('f', 'g', 1, -2));
        Assertions.assertEquals(11, array.indexOfAny('f', 'g', 1, array.length()));
        Assertions.assertEquals(1, array.indexOfAny('o', 'a', 1));
        Assertions.assertEquals(1, array.indexOfAny('o', 'a', 1, -2));
        Assertions.assertEquals(3, array.indexOfAny('-', 'a', 3));
        Assertions.assertEquals(3, array.indexOfAny('-', 'a', 3, array.length()));
        Assertions.assertEquals(7, array.indexOfAny('z', '-', 4));
        Assertions.assertEquals(7, array.indexOfAny('z', '-', 4, array.length()));
        Assertions.assertEquals(7, array.indexOfAny('z', '-', 4, -2));
        Assertions.assertEquals(7, array.indexOfAny('z', '-', 7, array.length()));
        Assertions.assertEquals(9, array.indexOfAny('a', '-', 8, array.length()));
        Assertions.assertEquals(10, array.indexOfAny('z', '-', 8, array.length()));
        Assertions.assertEquals(11, array.indexOfAny('z', '-', 11, array.length()));
        Assertions.assertEquals(11, array.indexOfAny('o', '-', 8, array.length()));
    }

    @Test
    public void indexOfAny_subarray() {
        CharArray array = new CharArray("foobar", 1, 4);  // oob

        Assertions.assertEquals(-1, array.indexOfAny('f', 'a'));
        Assertions.assertEquals(2, array.indexOfAny('f', 'b'));
        Assertions.assertEquals(-1, array.indexOfAny('o', 'o', 3));
        Assertions.assertEquals(-2, array.indexOfAny('o', 'o', 3, -2));
    }

    @Test
    public void lastIndexOf() {
        CharArray array = new CharArray("foo-bar-baz");

        Assertions.assertEquals(0, array.lastIndexOf('f'));
        Assertions.assertEquals(2, array.lastIndexOf('o'));
        Assertions.assertEquals(7, array.lastIndexOf('-'));
        Assertions.assertEquals(9, array.lastIndexOf('a'));
        Assertions.assertEquals(10, array.lastIndexOf('z'));
        Assertions.assertEquals(-1, array.lastIndexOf('w'));

        Assertions.assertEquals(0, array.lastIndexOf('f', 10));
        Assertions.assertEquals(0, array.lastIndexOf('f', 10, -1));
        Assertions.assertEquals(9, array.lastIndexOf('a', 10));
        Assertions.assertEquals(9, array.lastIndexOf('a', 9));
        Assertions.assertEquals(5, array.lastIndexOf('a', 8));
        Assertions.assertEquals(5, array.lastIndexOf('a', 5));
        Assertions.assertEquals(-1, array.lastIndexOf('a', 4));
        Assertions.assertEquals(-2, array.lastIndexOf('a', 4, -2));
    }

    @Test
    public void lastIndexOf_subarray() {
        CharArray array = new CharArray("foobar", 1, 4);  // oob

        Assertions.assertEquals(-1, array.lastIndexOf('f'));
        Assertions.assertEquals(-1, array.lastIndexOf('a'));
        Assertions.assertEquals(-1, array.lastIndexOf('a', 2));
        Assertions.assertEquals(-1, array.lastIndexOf('a', 3));
        Assertions.assertEquals(2, array.lastIndexOf('b'));
        Assertions.assertEquals(2, array.lastIndexOf('b', 2));
        Assertions.assertEquals(2, array.lastIndexOf('b', 3));
        Assertions.assertEquals(0, array.lastIndexOf('o', 0));
        Assertions.assertEquals(1, array.lastIndexOf('o', 3));
        Assertions.assertEquals(1, array.lastIndexOf('o', 3, -2));
    }

    @Test
    public void lastIndexOfAny() {
        CharArray array = new CharArray("foo-bar-baz");

        Assertions.assertEquals(0, array.lastIndexOfAny('f', 'g'));
        Assertions.assertEquals(2, array.lastIndexOfAny('f', 'o'));
        Assertions.assertEquals(2, array.lastIndexOfAny('o', 'f'));
        Assertions.assertEquals(2, array.lastIndexOfAny('o', 'x'));
        Assertions.assertEquals(7, array.lastIndexOfAny('-', 'o'));
        Assertions.assertEquals(9, array.lastIndexOfAny('-', 'a'));
        Assertions.assertEquals(10, array.lastIndexOfAny('-', 'z'));
        Assertions.assertEquals(-1, array.lastIndexOfAny('x', 'y'));

        Assertions.assertEquals(0, array.lastIndexOfAny('f', 'g', 10));
        Assertions.assertEquals(0, array.lastIndexOfAny('f', 'g', 10, -1));
        Assertions.assertEquals(10, array.lastIndexOfAny('f', 'z', 10));
        Assertions.assertEquals(10, array.lastIndexOfAny('f', 'z', 10, -1));
        Assertions.assertEquals(10, array.lastIndexOfAny('a', 'z', 10));
        Assertions.assertEquals(9, array.lastIndexOfAny('a', 'r', 10));
        Assertions.assertEquals(9, array.lastIndexOfAny('a', 'z', 9));
        Assertions.assertEquals(5, array.lastIndexOfAny('a', 'z', 8));
        Assertions.assertEquals(5, array.lastIndexOfAny('a', 'r', 5));
        Assertions.assertEquals(-1, array.lastIndexOfAny('a', 'r', 4));
        Assertions.assertEquals(-2, array.lastIndexOfAny('a', 'r', 4, -2));
    }

    @Test
    public void lastIndexOfAny_subarray() {
        CharArray array = new CharArray("foobar", 1, 4);  // oob

        Assertions.assertEquals(-1, array.lastIndexOfAny('f', 'a'));
        Assertions.assertEquals(-1, array.lastIndexOfAny('a', 'f'));
        Assertions.assertEquals(-1, array.lastIndexOfAny('a', 'f', 2));
        Assertions.assertEquals(-1, array.lastIndexOfAny('a', 'f', 3));
        Assertions.assertEquals(2, array.lastIndexOfAny('b', 'o'));
        Assertions.assertEquals(2, array.lastIndexOfAny('b', 'o', 2));
        Assertions.assertEquals(2, array.lastIndexOfAny('b', 'o', 3));
        Assertions.assertEquals(0, array.lastIndexOfAny('o', 'f', 0));
        Assertions.assertEquals(1, array.lastIndexOfAny('o', 'f', 3));
        Assertions.assertEquals(1, array.lastIndexOfAny('o', 'f', 3, -2));
    }

    @Test
    public void contains() {
        CharArray array = new CharArray("foobar", 1, 4);  // oob

        Assertions.assertTrue(array.contains('o'));
        Assertions.assertTrue(array.contains('b'));
        Assertions.assertFalse(array.contains('f'));
        Assertions.assertFalse(array.contains('a'));
        Assertions.assertFalse(array.contains('x'));
    }

    @Test
    public void containsAny() {
        CharArray array = new CharArray("foobar", 1, 4);  // oob

        Assertions.assertTrue(array.containsAny('o', 'b'));
        Assertions.assertTrue(array.containsAny('o', 'x'));
        Assertions.assertTrue(array.containsAny('b', 'x'));
        Assertions.assertTrue(array.containsAny('b', 'a'));
        Assertions.assertFalse(array.containsAny('a', 'x'));
    }

    @Test
    public void commonPrefix() {
        Assertions.assertEquals(0, new CharArray("foo").commonPrefix("bar"));
        Assertions.assertEquals(2, new CharArray("bar").commonPrefix("baz"));
        Assertions.assertEquals(3, new CharArray("foo").commonPrefix("foo"));

        Assertions.assertEquals(0, new CharArray("foo").commonPrefix(new CharArray("bar")));
        Assertions.assertEquals(2, new CharArray("bar").commonPrefix(new CharArray("baz")));

        Assertions.assertEquals(0, new CharArray("foo").commonPrefix(new CharArray("foobar", 3, 6)));
        Assertions.assertEquals(2, new CharArray("bar").commonPrefix(new CharArray("barbaz", 3, 6)));
        Assertions.assertEquals(0, new CharArray("foobar", 3, 6).commonPrefix(new CharArray("foo")));
        Assertions.assertEquals(2, new CharArray("barbaz", 3, 6).commonPrefix(new CharArray("bar")));
    }

    @Test
    public void commonPrefix_empty() {
        Assertions.assertEquals(0, new CharArray("").commonPrefix(new CharArray("")));
        Assertions.assertEquals(0, new CharArray("foo").commonPrefix(new CharArray("")));
        Assertions.assertEquals(0, new CharArray("").commonPrefix(new CharArray("foo")));

        Assertions.assertEquals(0, new CharArray("foo", 1, 2).commonPrefix(new CharArray("foo", 3, 3)));
        Assertions.assertEquals(0, new CharArray("xxx", 1, 1).commonPrefix(new CharArray("xxx", 2, 2)));
    }

    @Test
    public void commonPrefix_same_prefix() {
        Assertions.assertEquals(3, new CharArray("foo").commonPrefix(new CharArray("foo")));
        Assertions.assertEquals(3, new CharArray("foo").commonPrefix(new CharArray("foobar")));
        Assertions.assertEquals(3, new CharArray("foobar").commonPrefix(new CharArray("foo")));

        Assertions.assertEquals(3, new CharArray("foo").commonPrefix(new CharArray("barfoo", 3, 6)));
        Assertions.assertEquals(3, new CharArray("barfoo", 3, 6).commonPrefix(new CharArray("foo")));
    }

    @Test
    public void commonSuffix() {
        Assertions.assertEquals(0, new CharArray("foo").commonSuffix("bar"));
        Assertions.assertEquals(2, new CharArray("foo").commonSuffix("boo"));
        Assertions.assertEquals(3, new CharArray("foo").commonSuffix("foo"));

        Assertions.assertEquals(0, new CharArray("foo").commonSuffix(new CharArray("bar")));
        Assertions.assertEquals(2, new CharArray("foo").commonSuffix(new CharArray("boo")));

        Assertions.assertEquals(0, new CharArray("foo").commonSuffix(new CharArray("foobar", 3, 6)));
        Assertions.assertEquals(2, new CharArray("foo").commonSuffix(new CharArray("fooboo", 3, 6)));
        Assertions.assertEquals(0, new CharArray("foobar", 3, 6).commonSuffix(new CharArray("foo")));
        Assertions.assertEquals(2, new CharArray("fooboo", 3, 6).commonSuffix(new CharArray("foo")));
    }

    @Test
    public void commonSuffix_empty() {
        Assertions.assertEquals(0, new CharArray("").commonSuffix(new CharArray("")));
        Assertions.assertEquals(0, new CharArray("foo").commonSuffix(new CharArray("")));
        Assertions.assertEquals(0, new CharArray("").commonSuffix(new CharArray("foo")));

        Assertions.assertEquals(0, new CharArray("foo", 1, 2).commonSuffix(new CharArray("foo", 3, 3)));
        Assertions.assertEquals(0, new CharArray("xxx", 1, 1).commonSuffix(new CharArray("xxx", 2, 2)));
    }

    @Test
    public void commonSuffix_same_suffix() {
        Assertions.assertEquals(3, new CharArray("foo").commonSuffix(new CharArray("foo")));
        Assertions.assertEquals(3, new CharArray("foo").commonSuffix(new CharArray("barfoo")));
        Assertions.assertEquals(3, new CharArray("barfoo").commonSuffix(new CharArray("foo")));

        Assertions.assertEquals(3, new CharArray("foo").commonSuffix(new CharArray("barfoo", 3, 6)));
        Assertions.assertEquals(3, new CharArray("barfoo", 3, 6).commonSuffix(new CharArray("foo")));
    }

    @Test
    public void startsWith() {
        CharArray array = new CharArray("foo");
        Assertions.assertTrue(array.startsWith(new CharArray("")));
        Assertions.assertTrue(array.startsWith(new CharArray("f")));
        Assertions.assertTrue(array.startsWith(new CharArray("fo")));
        Assertions.assertTrue(array.startsWith(new CharArray("foo")));

        Assertions.assertFalse(array.startsWith(new CharArray("x")));
        Assertions.assertFalse(array.startsWith(new CharArray("bar")));
        Assertions.assertFalse(array.startsWith(new CharArray("foe")));
        Assertions.assertFalse(array.startsWith(new CharArray("foo!")));
        Assertions.assertFalse(array.startsWith(new CharArray("foobar")));
    }

    @Test
    public void startsWith_string() {
        CharArray array = new CharArray("foo");
        Assertions.assertTrue(array.startsWith(""));
        Assertions.assertTrue(array.startsWith("f"));
        Assertions.assertTrue(array.startsWith("fo"));
        Assertions.assertTrue(array.startsWith("foo"));

        Assertions.assertFalse(array.startsWith("x"));
        Assertions.assertFalse(array.startsWith("bar"));
        Assertions.assertFalse(array.startsWith("foe"));
        Assertions.assertFalse(array.startsWith("foo!"));
        Assertions.assertFalse(array.startsWith("foobar"));
    }

    @Test
    public void startsWith_char() {
        CharArray array = new CharArray("foo");
        Assertions.assertTrue(array.startsWith('f'));
        Assertions.assertFalse(array.startsWith('o'));
        Assertions.assertFalse(array.startsWith('a'));
        Assertions.assertFalse(array.startsWith('x'));

        Assertions.assertFalse(new CharArray("").startsWith(' '));
    }

    @Test
    public void endsWith() {
        CharArray array = new CharArray("foo");
        Assertions.assertTrue(array.endsWith(new CharArray("")));
        Assertions.assertTrue(array.endsWith(new CharArray("o")));
        Assertions.assertTrue(array.endsWith(new CharArray("oo")));
        Assertions.assertTrue(array.endsWith(new CharArray("foo")));

        Assertions.assertFalse(array.endsWith(new CharArray("x")));
        Assertions.assertFalse(array.endsWith(new CharArray("bar")));
        Assertions.assertFalse(array.endsWith(new CharArray("boo")));
        Assertions.assertFalse(array.endsWith(new CharArray("!foo")));
        Assertions.assertFalse(array.endsWith(new CharArray("barfoo")));
    }

    @Test
    public void endsWith_string() {
        CharArray array = new CharArray("foo");
        Assertions.assertTrue(array.endsWith(""));
        Assertions.assertTrue(array.endsWith("o"));
        Assertions.assertTrue(array.endsWith("oo"));
        Assertions.assertTrue(array.endsWith("foo"));

        Assertions.assertFalse(array.endsWith("x"));
        Assertions.assertFalse(array.endsWith("bar"));
        Assertions.assertFalse(array.endsWith("boo"));
        Assertions.assertFalse(array.endsWith("!foo"));
        Assertions.assertFalse(array.endsWith("barfoo"));
    }

    @Test
    public void endsWith_char() {
        CharArray array = new CharArray("foo");
        Assertions.assertTrue(array.endsWith('o'));
        Assertions.assertFalse(array.endsWith('f'));
        Assertions.assertFalse(array.endsWith('a'));
        Assertions.assertFalse(array.endsWith('x'));

        Assertions.assertFalse(new CharArray("").endsWith(' '));
    }

    @Test
    public void join_same_buffer() {
        CharArray array = new CharArray("foobar");
        CharArray foo = array.substringUntil(3);
        CharArray bar = array.substringFrom(3);
        CharArray join = CharArray.join(foo, bar);

        Assertions.assertSame(array.chars, foo.chars);
        Assertions.assertSame(array.chars, bar.chars);
        Assertions.assertSame(array.chars, join.chars);
        Assertions.assertEquals(array, join);
    }

    @Test
    public void join_not_same_buffer() {
        CharArray array = new CharArray("foobar");
        CharArray foo = new CharArray("foo");
        CharArray bar = new CharArray("bar");
        CharArray join = CharArray.join(foo, bar);

        Assertions.assertNotSame(array.chars, foo.chars);
        Assertions.assertNotSame(array.chars, bar.chars);
        Assertions.assertNotSame(array.chars, join.chars);
        Assertions.assertEquals(array, join);
    }

    @Test
    public void cutPrefix_array() {
        CharArray foobar = new CharArray("foobar");
        CharArray foo = new CharArray("foo");
        CharArray bar = new CharArray("bar");
        CharArray empty = new CharArray("");

        Assertions.assertEquals(foobar.cutPrefix(empty), foobar);
        Assertions.assertEquals(foobar.cutPrefix(foo), bar);
        Assertions.assertEquals(foobar.cutPrefix(foo), bar);
        Assertions.assertEquals(foobar.cutPrefix(foobar), empty);

        Assertions.assertEquals(foobar.substringFrom(3), bar);
        Assertions.assertEquals(foobar.substringFrom(3).cutPrefix(foo), bar);
        Assertions.assertEquals(foobar.substringFrom(3).cutPrefix(bar), empty);
        Assertions.assertEquals(foobar.substringFrom(3).cutPrefix(bar.substringUntil(0)), bar);
        Assertions.assertEquals(foobar.substringFrom(3).cutPrefix(bar.substringUntil(1)), new CharArray("ar"));  // cut b
        Assertions.assertEquals(foobar.substringFrom(3).cutPrefix(bar.substringUntil(2)), new CharArray("r"));   // cut ba
    }

    @Test
    public void cutPrefix_str() {
        Assertions.assertEquals(new CharArray("foobar").cutPrefix(""), new CharArray("foobar"));
        Assertions.assertEquals(new CharArray("foobar").cutPrefix("foo"), new CharArray("bar"));
        Assertions.assertEquals(new CharArray("foobar").cutPrefix("bar"), new CharArray("foobar"));
        Assertions.assertEquals(new CharArray("foobar").cutPrefix("fooba"), new CharArray("r"));
        Assertions.assertEquals(new CharArray("foobar").cutPrefix("foobar"), new CharArray(""));
        Assertions.assertEquals(new CharArray("foobar").cutPrefix("foobarbaz"), new CharArray("foobar"));
        Assertions.assertEquals(new CharArray("foobar").cutPrefix('f'), new CharArray("oobar"));
        Assertions.assertEquals(new CharArray("foobar").cutPrefix('o'), new CharArray("foobar"));
    }

    @Test
    public void cutSuffix_array() {
        CharArray foobar = new CharArray("foobar");
        CharArray foo = new CharArray("foo");
        CharArray bar = new CharArray("bar");
        CharArray empty = new CharArray("");

        Assertions.assertEquals(foobar.cutSuffix(empty), foobar);
        Assertions.assertEquals(foobar.cutSuffix(bar), foo);
        Assertions.assertEquals(foobar.cutSuffix(foo), foobar);
        Assertions.assertEquals(foobar.cutSuffix(foobar), empty);

        Assertions.assertEquals(foobar.substringFrom(3), bar);
        Assertions.assertEquals(foobar.substringFrom(3).cutSuffix(foo), bar);
        Assertions.assertEquals(foobar.substringFrom(3).cutSuffix(bar), empty);
        Assertions.assertEquals(foobar.substringFrom(3).cutSuffix(bar.substringFrom(1)), new CharArray("b"));   // cut ar
        Assertions.assertEquals(foobar.substringFrom(3).cutSuffix(bar.substringFrom(2)), new CharArray("ba"));  // cut r
        Assertions.assertEquals(foobar.substringFrom(3).cutSuffix(bar.substringFrom(3)), bar);
    }

    @Test
    public void cutSuffix_str() {
        Assertions.assertEquals(new CharArray("foobar").cutSuffix(""), new CharArray("foobar"));
        Assertions.assertEquals(new CharArray("foobar").cutSuffix("bar"), new CharArray("foo"));
        Assertions.assertEquals(new CharArray("foobar").cutSuffix("foo"), new CharArray("foobar"));
        Assertions.assertEquals(new CharArray("foobar").cutSuffix("oobar"), new CharArray("f"));
        Assertions.assertEquals(new CharArray("foobar").cutSuffix("foobar"), new CharArray(""));
        Assertions.assertEquals(new CharArray("foobar").cutSuffix("foofoobar"), new CharArray("foobar"));
        Assertions.assertEquals(new CharArray("foobar").cutSuffix('r'), new CharArray("fooba"));
        Assertions.assertEquals(new CharArray("foobar").cutSuffix('a'), new CharArray("foobar"));
    }

    @Test
    public void chars() {
        int[] array = new CharArray("foobar").chars().toArray();
        Assertions.assertArrayEquals(new int[]{ 102, 111, 111,  98, 97, 114 }, array);
    }

    @Test
    public void chars_empty() {
        int[] array = new CharArray("").chars().toArray();
        Assertions.assertArrayEquals(new int[0], array);
    }

    @Test
    public void chars_of_subbuffer() {
        int[] array = new CharArray("foobar", 1, 3).chars().toArray();
        Assertions.assertArrayEquals(new int[]{ 111, 111 }, array);
    }

    @Test
    public void codepoints() {
        int[] array = new CharArray("foobar").codePoints().toArray();
        Assertions.assertArrayEquals(new int[]{ 102, 111, 111,  98, 97, 114 }, array);
    }

    @Test
    public void codepoints_empty() {
        int[] array = new CharArray("").codePoints().toArray();
        Assertions.assertArrayEquals(new int[0], array);
    }

    @Test
    public void codepoints_of_subbuffer() {
        int[] array = new CharArray("foobar", 1, 3).codePoints().toArray();
        Assertions.assertArrayEquals(new int[]{ 111, 111 }, array);
    }

    private static void assertEqualsHashCode(CharArray lhs, CharArray rhs) {
        Assertions.assertEquals(lhs, rhs);
        Assertions.assertEquals(lhs.hashCode(), rhs.hashCode());
    }
}
