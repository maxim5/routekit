package io.routekit.util;

import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;

/**
 * A String-like wrapper around `char[]` array, providing efficient slice, join and for-each operations.
 * <p>
 * `CharArray` owns the encapsulated char array, but, by default, is immutable (see also {@link MutableCharArray}).
 */
public class CharArray implements CharSequence {
    protected final char[] chars;
    protected int start;
    protected int end;

    public CharArray(char[] chars, int start, int end) {
        assert chars != null : "CharArray chars array is null";
        assert 0 <= start : "CharArray start=%d can't be negative".formatted(start);
        assert start <= end : "CharArray start=%d is greater than end=%d".formatted(start, end);
        assert end <= chars.length : "CharArray end=%d is greater than array.length=%d".formatted(end, chars.length);
        this.chars = chars;
        this.start = start;
        this.end = end;
    }

    public CharArray(char[] chars) {
        this(chars, 0, chars.length);
    }

    public CharArray(String s, int start, int end) {
        this(s.toCharArray(), start, end);
    }

    public CharArray(String s) {
        this(s.toCharArray(), 0, s.length());
    }

    public CharArray(CharSequence s) {
        this(s.toString().toCharArray(), 0, s.length());
    }

    public CharArray(CharBuffer buffer) {
        this(buffer.isReadOnly() ? buffer.toString().toCharArray() : buffer.array(),
             buffer.isReadOnly() ? 0 : buffer.position(),
             buffer.isReadOnly() ? buffer.length() : buffer.position() + buffer.length());
    }

    public CharArray(CharArray s) {
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

    public CharArray substringFrom(int start) {
        return substring(start, length());
    }

    public CharArray substringUntil(int end) {
        return substring(0, end);
    }

    public CharArray substring(int start, int end) {
        assert start >= 0 : "Start index can't be negative: %d".formatted(start);
        assert start <= end : "Start index can't be larger than end index: %d >= %d".formatted(start, end);
        return new CharArray(chars, this.start + start, this.start + end);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return substring(start, end);
    }

    public boolean startsWith(CharArray prefix) {
        return length() >= prefix.length() &&
                Arrays.equals(chars, start, start + prefix.length(), prefix.chars, prefix.start, prefix.end);
    }

    public boolean endsWith(CharArray suffix) {
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

    public CharBuffer asNioBuffer() {
        return asRawBuffer().asReadOnlyBuffer();
    }

    protected CharBuffer asRawBuffer() {
        return CharBuffer.wrap(chars, start, end - start);  // note: writable!
    }

    public MutableCharArray mutable() {
        return mutableCopy();
    }

    public MutableCharArray mutableCopy() {
        return new MutableCharArray(chars, start, end);
    }

    public CharArray immutable() {
        return this;
    }

    public CharArray immutableCopy() {
        return new CharArray(this);
    }

    public int indexOf(char ch) {
        return indexOf(ch, 0, -1);
    }

    public int indexOf(char ch, int from) {
        return indexOf(ch, from, -1);
    }

    public int indexOf(char ch, int from, int def) {
        assert from >= 0 && from <= length() : "From index is out of array bounds: %d".formatted(from);
        assert def < 0 || def >= length() : "Default index can't be within array bounds: %d".formatted(def);
        for (int i = start + from; i < end; ++i) {
            if (chars[i] == ch) {
                return i - start;
            }
        }
        return def;
    }

    public int indexOfAny(char ch1, char ch2) {
        return indexOfAny(ch1, ch2, 0, -1);
    }

    public int indexOfAny(char ch1, char ch2, int from) {
        return indexOfAny(ch1, ch2, from, -1);
    }

    public int indexOfAny(char ch1, char ch2, int from, int def) {
        assert from >= 0 && from <= length() : "From index is out of array bounds: %d".formatted(from);
        assert def < 0 || def >= length() : "Default index can't be within array bounds: %d".formatted(def);
        for (int i = start + from; i < end; ++i) {
            if (chars[i] == ch1 || chars[i] == ch2) {
                return i - start;
            }
        }
        return def;
    }

    public int lastIndexOf(char ch) {
        return lastIndexOf(ch, length() - 1, -1);
    }

    public int lastIndexOf(char ch, int from) {
        return lastIndexOf(ch, from, -1);
    }

    public int lastIndexOf(char ch, int from, int def) {
        assert from >= 0 && from <= length() : "From index is out of array bounds: %d".formatted(from);
        assert def < 0 || def >= length() : "Default index can't be within array bounds: %d".formatted(def);
        for (int i = Math.min(start + from, end - 1); i >= start; --i) {
            if (chars[i] == ch) {
                return i - start;
            }
        }
        return def;
    }

    public int lastIndexOfAny(char ch1, char ch2) {
        return lastIndexOfAny(ch1, ch2, length() - 1, -1);
    }

    public int lastIndexOfAny(char ch1, char ch2, int from) {
        return lastIndexOfAny(ch1, ch2, from, -1);
    }

    public int lastIndexOfAny(char ch1, char ch2, int from, int def) {
        assert from >= 0 && from <= length() : "From index is out of array bounds: %d".formatted(from);
        assert def < 0 || def >= length() : "Default index can't be within array bounds: %d".formatted(def);
        for (int i = Math.min(start + from, end - 1); i >= start; --i) {
            if (chars[i] == ch1 || chars[i] == ch2) {
                return i - start;
            }
        }
        return def;
    }

    public boolean contains(char ch) {
        return indexOf(ch) >= 0;
    }

    public boolean containsAny(char ch1, char ch2) {
        return indexOfAny(ch1, ch2) >= 0;
    }

    // Returns the length of the common prefix
    public int commonPrefix(CharArray array) {
        int index = Arrays.mismatch(chars, start, end, array.chars, array.start, array.end);
        return (index >= 0) ? index : length();
    }

    public int commonPrefix(CharSequence str) {
        return commonPrefix(new CharArray(str));
    }

    // Returns the length of the common suffix
    public int commonSuffix(CharArray array) {
        int i = 1;
        int limit = Math.min(length(), array.length());
        while (i <= limit && chars[end - i] == array.chars[array.end - i]) {
            i++;
        }
        return i - 1;
    }

    public int commonSuffix(CharSequence str) {
        return commonSuffix(new CharArray(str));
    }

    public static CharArray join(CharArray lhs, CharArray rhs) {
        if (lhs.chars == rhs.chars && lhs.end == rhs.start) {
            return new CharArray(lhs.chars, lhs.start, rhs.end);
        }
        return new CharArray(new StringBuilder(lhs.length() + rhs.length()).append(lhs).append(rhs));
    }

    public CharArray cutPrefix(CharArray prefix) {
        int len = commonPrefix(prefix);
        return len < prefix.length() ? this : substringFrom(len);
    }

    public CharArray cutPrefix(CharSequence prefix) {
        return cutPrefix(new CharArray(prefix));
    }

    public CharArray cutPrefix(char ch) {
        return startsWith(ch) ? substringFrom(1) : this;
    }

    public CharArray cutSuffix(CharArray suffix) {
        int len = commonSuffix(suffix);
        return len < suffix.length() ? this : substringUntil(length() - len);
    }

    public CharArray cutSuffix(CharSequence suffix) {
        return cutSuffix(new CharArray(suffix));
    }

    public CharArray cutSuffix(char ch) {
        return endsWith(ch) ? substringUntil(length() - 1) : this;
    }

    @Override
    public String toString() {
        return new String(chars, start, end - start);
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof CharArray that && Arrays.equals(chars, start, end, that.chars, that.start, that.end);
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
