package com.huanchengfly.tieba.post.utils.helios;

abstract class IEncoder {
    protected int length = 32;
    protected int start;
    protected int flag;

    IEncoder() {
    }

    public int getStart() {
        return this.start;
    }

    public int getLength() {
        return this.length;
    }

    public int getFlag() {
        return this.flag;
    }

    public abstract EncodeResult encode(byte[] bytes, int off, int len);
}