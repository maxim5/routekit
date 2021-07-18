package io.routekit.util;

public class MutableCharBuffer extends CharBuffer {
    public MutableCharBuffer(char[] chars, int start, int end) {
        super(chars, start, end);
    }

    public MutableCharBuffer(char[] chars) {
        super(chars);
    }

    public MutableCharBuffer(String s, int start, int end) {
        super(s, start, end);
    }

    public MutableCharBuffer(String s) {
        super(s);
    }

    public MutableCharBuffer(CharSequence s) {
        super(s);
    }

    public MutableCharBuffer(CharBuffer s) {
        super(s);
    }

    public MutableCharBuffer(java.nio.CharBuffer buffer) {
        super(buffer);
    }

    public java.nio.CharBuffer asNioBuffer() {
        return asRawBuffer();
    }

    public MutableCharBuffer mutable() {
        return this;
    }

    public CharBuffer immutable() {
        return new CharBuffer(chars, start, end);
    }

    public MutableCharBuffer mutableSubstring(int start, int end) {
        return new MutableCharBuffer(chars, this.start + start, this.start + end);
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

    public void offsetPrefix(CharBuffer prefix) {
        int len = commonPrefix(prefix);
        if (len == prefix.length()) {
            offsetStart(len);
        }
    }

    public void offsetSuffix(CharBuffer suffix) {
        int len = commonSuffix(suffix);
        if (len == suffix.length()) {
            offsetEnd(len);
        }
    }

    public static MutableCharBuffer join(CharBuffer lhs, CharBuffer rhs) {
        if (lhs.chars == rhs.chars && lhs.end == rhs.start) {
            return new MutableCharBuffer(lhs.chars, lhs.start, rhs.end);
        }
        return new MutableCharBuffer(new StringBuilder(lhs.length() + rhs.length()).append(lhs).append(rhs));
    }
}
