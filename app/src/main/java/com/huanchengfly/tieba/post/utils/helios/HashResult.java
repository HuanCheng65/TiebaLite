package com.huanchengfly.tieba.post.utils.helios;

class HashResult {
    public static int BITS = 40;
    private final EncodeResult mEncodeResult;

    public HashResult() {
        this.mEncodeResult = new EncodeResult(BITS);
        this.mEncodeResult.a(0, BITS, true);
    }

    public void update(EncodeResult var1, int start, int length, int var4) {
        EncodeResult var5 = this.mEncodeResult.d(start, start + length);
        switch (var4) {
            case 0:
                var5.b(var1);
                break;
            case 2:
                var5.e(var1);
                break;
            case 3:
                var5.c(var1);
                break;
            case 1:
            default:
                var5.d(var1);
        }

        for (var4 = 0; var4 < length; ++var4) {
            this.mEncodeResult.a(start + var4, var5.d(var4));
        }

    }

    public byte[] getValue() {
        return this.mEncodeResult.a();
    }
}
