package io.routekit.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MutableCharArrayTest {
    @Test
    public void create_empty_string() {
        MutableCharArray array = new MutableCharArray("");
        Assertions.assertEquals(array.start(), 0);
        Assertions.assertEquals(array.end(), 0);
        Assertions.assertEquals(array.length(), 0);
    }

    @Test
    public void create_empty_same_pointers() {
        MutableCharArray array = new MutableCharArray("foo", 3, 3);
        Assertions.assertEquals(array.start(), 3);
        Assertions.assertEquals(array.end(), 3);
        Assertions.assertEquals(array.length(), 0);
    }

    @Test
    public void immutable_to_others() {
        CharArray array = new CharArray("foo");

        Assertions.assertEquals(array, array.mutable());
        Assertions.assertEquals(array, array.mutableCopy());

        Assertions.assertSame(array, array.immutable());
        Assertions.assertEquals(array, array.immutableCopy());
        Assertions.assertFalse(array.immutable() instanceof MutableCharArray);
        Assertions.assertFalse(array.immutableCopy() instanceof MutableCharArray);
    }

    @Test
    public void mutable_to_others() {
        MutableCharArray array = new MutableCharArray("foo");

        Assertions.assertSame(array, array.mutable());
        Assertions.assertEquals(array, array.mutableCopy());
        Assertions.assertNotSame(array, array.mutableCopy());

        Assertions.assertEquals(array, array.immutable());
        Assertions.assertEquals(array, array.immutableCopy());
        Assertions.assertFalse(array.immutable() instanceof MutableCharArray);
        Assertions.assertFalse(array.immutableCopy() instanceof MutableCharArray);
    }

    @Test
    public void substring() {
        MutableCharArray array = new MutableCharArray("foobar");

        Assertions.assertFalse(array.substring(0, 3) instanceof MutableCharArray);
        Assertions.assertFalse(array.substring(3, 6) instanceof MutableCharArray);

        Assertions.assertEquals(array.substring(0, 3), array.mutableSubstring(0, 3));
        Assertions.assertEquals(array.substring(1, 4), array.mutableSubstring(1, 4));
        Assertions.assertEquals(array.substring(3, 6), array.mutableSubstring(3, 6));
    }

    @Test
    public void offset_start_and_end() {
        MutableCharArray array = new MutableCharArray("foobar");

        array.offsetStart(1);
        Assertions.assertEquals("oobar", array.toString());

        array.offsetEnd(1);
        Assertions.assertEquals("ooba", array.toString());

        array.offsetStart(2);
        Assertions.assertEquals("ba", array.toString());

        array.offsetEnd(2);
        Assertions.assertEquals("", array.toString());
    }

    @Test
    public void offset_prefix() {
        MutableCharArray array = new MutableCharArray("foobar");

        array.offsetPrefix(new CharArray("food"));
        Assertions.assertEquals("foobar", array.toString());

        array.offsetPrefix(new CharArray("foo"));
        Assertions.assertEquals("bar", array.toString());

        array.offsetPrefix(new CharArray("a"));
        Assertions.assertEquals("bar", array.toString());

        array.offsetPrefix(new CharArray("b"));
        Assertions.assertEquals("ar", array.toString());

        array.offsetPrefix('a');
        Assertions.assertEquals("r", array.toString());

        array.offsetPrefix('r');
        Assertions.assertEquals("", array.toString());
    }

    @Test
    public void offset_suffix() {
        MutableCharArray array = new MutableCharArray("foobar");

        array.offsetSuffix(new CharArray("var"));
        Assertions.assertEquals("foobar", array.toString());

        array.offsetSuffix(new CharArray("bar"));
        Assertions.assertEquals("foo", array.toString());

        array.offsetSuffix(new CharArray("a"));
        Assertions.assertEquals("foo", array.toString());

        array.offsetSuffix(new CharArray("o"));
        Assertions.assertEquals("fo", array.toString());

        array.offsetSuffix('o');
        Assertions.assertEquals("f", array.toString());

        array.offsetSuffix('f');
        Assertions.assertEquals("", array.toString());
    }

    @Test
    public void reset() {
        MutableCharArray array = new MutableCharArray("foobar", 3, 5);

        array.resetStart();
        Assertions.assertEquals("fooba", array.toString());

        array.resetEnd();
        Assertions.assertEquals("foobar", array.toString());

        array.offsetStart(2);
        array.offsetEnd(2);
        Assertions.assertEquals("ob", array.toString());

        array.reset();
        Assertions.assertEquals("foobar", array.toString());
    }

    @Test
    public void join_same_buffer() {
        MutableCharArray foobar = new MutableCharArray("foobar");
        CharArray foo = foobar.substring(0, 3);
        CharArray bar = foobar.substring(3, 6);

        Assertions.assertEquals(foobar, MutableCharArray.join(foo, bar));
        Assertions.assertSame(foobar.chars, MutableCharArray.join(foo, bar).chars);
        Assertions.assertSame(foobar.chars, MutableCharArray.join(foo.immutableCopy(), bar).chars);
        Assertions.assertSame(foobar.chars, MutableCharArray.join(foo, bar.immutableCopy()).chars);
        Assertions.assertSame(foobar.chars, MutableCharArray.join(foo.immutableCopy(), bar.immutableCopy()).chars);
        Assertions.assertSame(foobar.chars, MutableCharArray.join(foo.mutable(), bar).chars);
        Assertions.assertSame(foobar.chars, MutableCharArray.join(foo, bar.mutable()).chars);
        Assertions.assertSame(foobar.chars, MutableCharArray.join(foo.mutable(), bar.mutable()).chars);
    }

    @Test
    public void join_new_buffer() {
        MutableCharArray foobar = new MutableCharArray("foobar");
        CharArray foo = new CharArray("foo");
        CharArray bar = new CharArray("bar");

        Assertions.assertEquals(foobar, MutableCharArray.join(foo, bar));
        Assertions.assertNotSame(foobar.chars, MutableCharArray.join(foo, bar).chars);
        Assertions.assertNotSame(foobar.chars, MutableCharArray.join(foo.immutableCopy(), bar).chars);
        Assertions.assertNotSame(foobar.chars, MutableCharArray.join(foo, bar.immutableCopy()).chars);
        Assertions.assertNotSame(foobar.chars, MutableCharArray.join(foo.immutableCopy(), bar.immutableCopy()).chars);
        Assertions.assertNotSame(foobar.chars, MutableCharArray.join(foo.mutable(), bar).chars);
        Assertions.assertNotSame(foobar.chars, MutableCharArray.join(foo, bar.mutable()).chars);
        Assertions.assertNotSame(foobar.chars, MutableCharArray.join(foo.mutable(), bar.mutable()).chars);
    }
}
