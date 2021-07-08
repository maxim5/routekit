package io.routekit.util;

import java.util.Arrays;
import java.util.function.IntConsumer;

/**
 *
 */
public class CharBuffer implements CharSequence {
    protected final char[] chars;
    protected int start;
    protected int end;

    public CharBuffer(char[] chars, int start, int end) {
        assert 0 <= start;
        assert start <= end;
        assert end <= chars.length;
        this.chars = chars;
        this.start = start;
        this.end = end;
    }

    public CharBuffer(char[] chars) {
        this(chars, 0, chars.length);
    }

    public CharBuffer(String s, int start, int end) {
        this(s.toCharArray(), start, end);
    }

    public CharBuffer(String s) {
        this(s.toCharArray(), 0, s.length());
    }

    public CharBuffer(CharSequence s) {
        this(s.toString().toCharArray(), 0, s.length());
    }

    public CharBuffer(CharBuffer s) {
        this(s.chars, s.start, s.end);
    }

    public int start() {
        return start;
    }

    public int end() {
        return end;
    }

    public int length() {
        return end - start;
    }

    public boolean isEmpty() {
        return start == end;
    }

    public boolean isNotEmpty() {
        return start < end;
    }

    @Override
    public char charAt(int index) {
        return chars[start + index];
    }

    public int at(int index) {
        return index < length() ? chars[start + index] : -1;
    }

    public void forEach(IntConsumer consumer) {
        for (int i = start; i < end; ++i) {
            consumer.accept(chars[i]);
        }
    }

    public CharBuffer substringFrom(int start) {
        return substring(start, length());
    }

    public CharBuffer substringUntil(int end) {
        return substring(0, end);
    }

    public CharBuffer substring(int start, int end) {
        return new CharBuffer(chars, this.start + start, this.start + end);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return substring(start, end);
    }

    public boolean startsWith(CharBuffer prefix) {
        return length() >= prefix.length() &&
                Arrays.equals(chars, start, start + prefix.length(), prefix.chars, prefix.start, prefix.end);
    }

    public boolean endsWith(CharBuffer suffix) {
        return length() >= suffix.length() &&
                Arrays.equals(chars, end - suffix.length(), end, suffix.chars, suffix.start, suffix.end);
    }

    public MutableCharBuffer mutable() {
        return new MutableCharBuffer(chars, start, end);
    }

    public CharBuffer immutable() {
        return this;
    }

    public int indexOf(char ch) {
        return indexOf(ch, 0, -1);
    }

    public int indexOf(char ch, int from) {
        return indexOf(ch, from, -1);
    }

    public int indexOf(char ch, int from, int def) {
        for (int i = start + from; i < end; ++i) {
            if (chars[i] == ch) {
                return i - start;
            }
        }
        return def;
    }

    public int matchUntil(char ch) {
        return indexOf(ch, 0, length());
    }

    public int matchUntil(int from, char ch) {
        return indexOf(ch, from, length());
    }

    public int matchCommon(CharBuffer buf) {
        int index = Arrays.mismatch(chars, start, end, buf.chars, buf.start, buf.end);
        return (index >= 0) ? index : length();
    }

    public static CharBuffer join(CharBuffer lhs, CharBuffer rhs) {
        if (lhs.chars == rhs.chars && lhs.end == rhs.start) {
            return new CharBuffer(lhs.chars, lhs.start, rhs.end);
        }
        return new CharBuffer(new StringBuilder(lhs.length() + rhs.length()).append(lhs).append(rhs));
    }

    @Override
    public String toString() {
        return new String(chars, start, end - start);
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof CharBuffer that && Arrays.equals(chars, start, end, that.chars, that.start, that.end);
    }

    @Override
    public int hashCode() {
        int result = 1;
        for (int i = start; i < end; ++i) {
            result = 31 * result + chars[i];
        }
        return result;
    }
}
