package io.routekit.util;

import java.util.Arrays;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;

/**
 * A String-like wrapper around `char[]` array, providing efficient slice, join and for-each operations.
 * <p>
 * `CharBuffer` owns the encapsulated char array, but, by default, is immutable (see also {@link MutableCharBuffer}).
 */
public class CharBuffer implements CharSequence {
    protected final char[] chars;
    protected int start;
    protected int end;

    public CharBuffer(char[] chars, int start, int end) {
        assert chars != null : "CharBuffer chars array is null";
        assert 0 <= start : "CharBuffer start=%d can't be negative".formatted(start);
        assert start <= end : "CharBuffer start=%d is greater than end=%d".formatted(start, end);
        assert end <= chars.length : "CharBuffer end=%d is greater than array.length=%d".formatted(end, chars.length);
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

    public CharBuffer(java.nio.CharBuffer buffer) {
        this(buffer.isReadOnly() ? buffer.toString().toCharArray() : buffer.array(),
             buffer.isReadOnly() ? 0 : buffer.position(),
             buffer.isReadOnly() ? buffer.length() : buffer.position() + buffer.length());
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
        assert index >= 0 : "Index can't be negative: %d".formatted(index);
        return chars[start + index];
    }

    public int at(int index) {
        return index >= 0 && index < length() ? chars[start + index] : -1;
    }

    public void forEach(IntConsumer consumer) {
        for (int i = start; i < end; ++i) {
            consumer.accept(chars[i]);
        }
    }

    @Override
    public IntStream chars() {
        return asRawBuffer().chars();
    }

    @Override
    public IntStream codePoints() {
        return asRawBuffer().codePoints();
    }

    public CharBuffer substringFrom(int start) {
        return substring(start, length());
    }

    public CharBuffer substringUntil(int end) {
        return substring(0, end);
    }

    public CharBuffer substring(int start, int end) {
        assert start >= 0 : "Start index can't be negative: %d".formatted(start);
        assert start <= end : "Start index can't be larger than end index: %d >= %d".formatted(start, end);
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

    public boolean startsWith(char ch) {
        return isNotEmpty() && chars[start] == ch;
    }

    public boolean endsWith(char ch) {
        return isNotEmpty() && chars[end - 1] == ch;
    }

    public boolean startsWith(CharSequence prefix) {
        int length = prefix.length();
        if (length() < length) {
            return false;
        }
        int i = 0;
        while (i < length && chars[i + start] == prefix.charAt(i)) {
            i++;
        }
        return i == length;
    }

    public boolean endsWith(CharSequence prefix) {
        int length = prefix.length();
        if (length() < length) {
            return false;
        }
        int i = 1;
        while (i <= length && chars[end - i] == prefix.charAt(length - i)) {
            i++;
        }
        return i > length;
    }

    public java.nio.CharBuffer asNioBuffer() {
        return asRawBuffer().asReadOnlyBuffer();
    }

    protected java.nio.CharBuffer asRawBuffer() {
        return java.nio.CharBuffer.wrap(chars, start, end - start);  // note: writable!
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
        assert from >= 0 : "From index can't be negative: %d".formatted(from);
        assert def < 0 || def >= length() : "Default index can't be within buffer bounds: %d".formatted(def);
        for (int i = start + from; i < end; ++i) {
            if (chars[i] == ch) {
                return i - start;
            }
        }
        return def;
    }

    public int lastIndexOf(char ch) {
        return lastIndexOf(ch, end - 1, -1);
    }

    public int lastIndexOf(char ch, int from) {
        return lastIndexOf(ch, from, -1);
    }

    public int lastIndexOf(char ch, int from, int def) {
        assert from >= 0 : "From index can't be negative: %d".formatted(from);
        assert def < 0 || def >= length() : "Default index can't be within buffer bounds: %d".formatted(def);
        for (int i = from; i >= start; --i) {
            if (chars[i] == ch) {
                return i - start;
            }
        }
        return def;
    }

    // Returns the length of the common prefix
    public int commonPrefix(CharBuffer buf) {
        int index = Arrays.mismatch(chars, start, end, buf.chars, buf.start, buf.end);
        return (index >= 0) ? index : length();
    }

    // Returns the length of the common suffix
    public int commonSuffix(CharBuffer buf) {
        int i = 1;
        int limit = Math.min(length(), buf.length());
        while (i <= limit && chars[end - i] == buf.chars[buf.end - i]) {
            i++;
        }
        return i - 1;
    }

    public static CharBuffer join(CharBuffer lhs, CharBuffer rhs) {
        if (lhs.chars == rhs.chars && lhs.end == rhs.start) {
            return new CharBuffer(lhs.chars, lhs.start, rhs.end);
        }
        return new CharBuffer(new StringBuilder(lhs.length() + rhs.length()).append(lhs).append(rhs));
    }

    public CharBuffer cutPrefix(CharBuffer prefix) {
        int len = commonPrefix(prefix);
        return len < prefix.length() ? this : substringFrom(len);
    }

    public CharBuffer cutSuffix(CharBuffer suffix) {
        int len = commonSuffix(suffix);
        return len < suffix.length() ? this : substringUntil(length() - len);
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
