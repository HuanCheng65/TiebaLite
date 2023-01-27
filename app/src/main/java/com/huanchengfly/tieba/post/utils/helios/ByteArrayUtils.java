package com.huanchengfly.tieba.post.utils.helios;

import java.util.Arrays;

class ByteArrayUtils {
    public static void copyArray(byte[] dest, byte[] src, int destPos) {
        if (destPos < 0) {
            throw new IllegalArgumentException("start should be more than zero!");
        } else if (dest != null && dest.length != 0) {
            if (src != null && src.length != 0) {
                if (dest.length < src.length) {
                    throw new IllegalArgumentException("dst array length should be longer than:" + src.length);
                } else {
                    int length = src.length;
                    if (dest.length < length + destPos) {
                        throw new IllegalArgumentException("start should be less than:" + (dest.length - src.length));
                    } else {
                        System.arraycopy(src, 0, dest, destPos, src.length);
                    }
                }
            } else {
                throw new IllegalArgumentException("src array should not be null or empty");
            }
        } else {
            throw new IllegalArgumentException("dst array should not be null or empty");
        }
    }

    public static byte[] copyArray(byte[] src, int newLength) {
        if (src != null && src.length != 0) {
            if (newLength < 0) {
                throw new IllegalArgumentException("length should be more than zero!");
            } else {
                return Arrays.copyOf(src, newLength);
            }
        } else {
            throw new IllegalArgumentException("original array should not be null or empty");
        }
    }
}
