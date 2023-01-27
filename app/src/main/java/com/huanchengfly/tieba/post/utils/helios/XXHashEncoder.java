package com.huanchengfly.tieba.post.utils.helios;

final class XXHashEncoder extends IEncoder {
    public XXHashEncoder(int start, int flag) {
        this.length = 32;
        this.start = start;
        this.flag = flag;
    }

    public EncodeResult encode(byte[] bytes, int off, int len) {
        XXHash xxHash = new XXHash();
        xxHash.update(bytes, off, len);
        return EncodeResult.a(new long[]{xxHash.getValue()});
    }
}

