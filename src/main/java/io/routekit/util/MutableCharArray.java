package io.routekit.util;

import java.nio.CharBuffer;

/**
 * A mutable version of the {@link CharArray}.
 */
public class MutableCharArray extends CharArray {
    public MutableCharArray(char[] chars, int start, int end) {
        super(chars, start, end);
    }

    public MutableCharArray(char[] chars) {
        super(chars);
    }

    public MutableCharArray(String s, int start, int end) {
        super(s, start, end);
    }

    public MutableCharArray(String s) {
        super(s);
    }

    public MutableCharArray(CharSequence s) {
        super(s);
    }

    public MutableCharArray(CharArray s) {
        super(s);
    }

    public MutableCharArray(CharBuffer buffer) {
        super(buffer);
    }

    @Override
    public CharBuffer asNioBuffer() {
        return asRawBuffer();
    }

    @Override
    public MutableCharArray mutable() {
        return this;
    }

    @Override
    public CharArray immutable() {
        return new CharArray(chars, start, end);
    }

    public MutableCharArray mutableSubstring(int start, int end) {
        return new MutableCharArray(chars, this.start + start, this.start + end);
    }

    public void reset() {
        start = 0;
        end = chars.length;
    }

    public void resetStart() {
        start = 0;
    }

    public void resetEnd() {
        end = chars.length;
    }

    public void offsetStart(int offset) {
        assert start+offset <= end : "Invalid offset: makes start=%d greater than end=%d".formatted(start+offset, end);
        start += offset;
    }

    public void offsetEnd(int offset) {
        assert start <= end-offset : "Invalid offset: makes start=%d greater than end=%d".formatted(start, end-offset);
        end -= offset;
    }

    public void offsetPrefix(CharArray prefix) {
        int len = commonPrefix(prefix);
        if (len == prefix.length()) {
            offsetStart(len);
        }
    }

    public void offsetPrefix(char ch) {
        if (startsWith(ch)) {
            offsetStart(1);
        }
    }

    public void offsetSuffix(CharArray suffix) {
        int len = commonSuffix(suffix);
        if (len == suffix.length()) {
            offsetEnd(len);
        }
    }

    public void offsetSuffix(char ch) {
        if (endsWith(ch)) {
            offsetEnd(1);
        }
    }

    public static MutableCharArray join(CharArray lhs, CharArray rhs) {
        if (lhs.chars == rhs.chars && lhs.end == rhs.start) {
            return new MutableCharArray(lhs.chars, lhs.start, rhs.end);
        }
        return new MutableCharArray(new StringBuilder(lhs.length() + rhs.length()).append(lhs).append(rhs));
    }
}
