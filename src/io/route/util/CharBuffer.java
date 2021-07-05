package io.route.util;

import java.util.Arrays;
import java.util.Objects;

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

    public MutableCharBuffer mutable() {
        return new MutableCharBuffer(chars, start, end);
    }

    public CharBuffer immutable() {
        return this;
    }

    public int matchUntil(char ch) {
        return matchUntil(0, ch);
    }

    public int matchUntil(int from, char ch) {
        for (int i = start + from; i < end; ++i) {
            if (chars[i] == ch) {
                return i - start;
            }
        }
        return end - start;
    }

    // Consider jdk.internal.util.ArraysSupport#mismatch(char[], char[], int)
    public int matchCommon(CharBuffer buf) {
        int i = 0;
        int limit = Math.min(length(), buf.length());
        while (i < limit && chars[i + start] == buf.chars[i + buf.start]) {
            i++;
        }
        return i;
    }

    @Override
    public String toString() {
        return new String(chars, start, end - start);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CharBuffer that = (CharBuffer) o;
        return Arrays.equals(chars, start, end, that.chars, that.start, that.end);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(start, end);
        result = 31 * result + Arrays.hashCode(chars);
        return result;
    }
}
