package com.huanchengfly.tieba.post.utils.helios;

class XXHash {
    private final int[] i;
    private final byte[] j;
    private final int k;
    private int l;
    private int m;

    protected XXHash() {
        this(0);
    }

    protected XXHash(int var1) {
        this.i = new int[4];
        this.j = new byte[16];
        this.k = var1;
        this.c();
    }

    private static int a(byte[] var0, int var1) {
        return (int) (j(var0, var1) & 4294967295L);
    }

    private static long j(byte[] var0, int var1) {
        if (4 > 8) {
            throw new IllegalArgumentException("can't read more than eight bytes into a long value");
        } else {
            long var3 = 0L;

            for (int var5 = 0; var5 < 4; ++var5) {
                var3 |= ((long) var0[var1 + var5] & 255L) << var5 * 8;
            }

            return var3;
        }
    }

    private void c() {
        this.i[0] = this.k - 1640531535 - 2048144777;
        this.i[1] = this.k - 2048144777;
        this.i[2] = this.k;
        this.i[3] = this.k + 1640531535;
    }

    private void g(byte[] var1, int var2) {
        int var3 = this.i[0];
        int var4 = this.i[1];
        int var5 = this.i[2];
        int var6 = this.i[3];
        var3 = Integer.rotateLeft(var3 + a(var1, var2) * -2048144777, 13);
        var4 = Integer.rotateLeft(var4 + a(var1, var2 + 4) * -2048144777, 13);
        var5 = Integer.rotateLeft(var5 + a(var1, var2 + 8) * -2048144777, 13);
        var2 = Integer.rotateLeft(var6 + a(var1, var2 + 12) * -2048144777, 13);
        this.i[0] = var3 * -1640531535;
        this.i[1] = var4 * -1640531535;
        this.i[2] = var5 * -1640531535;
        this.i[3] = var2 * -1640531535;
        this.m = 0;
    }

    protected void update(byte[] var1, int var2, int var3) {
        if (var3 > 0) {
            this.l += var3;
            int var4 = var2 + var3;
            if (this.m + var3 < 16) {
                System.arraycopy(var1, var2, this.j, this.m, var3);
                this.m += var3;
            } else {
                var3 = var2;
                if (this.m > 0) {
                    var3 = 16 - this.m;
                    System.arraycopy(var1, var2, this.j, this.m, var3);
                    this.g(this.j, 0);
                    var3 += var2;
                }

                while (var3 <= var4 - 16) {
                    this.g(var1, var3);
                    var3 += 16;
                }

                if (var3 < var4) {
                    this.m = var4 - var3;
                    System.arraycopy(var1, var3, this.j, 0, this.m);
                }
            }
        }

    }

    protected long getValue() {
        int var1;
        if (this.l > 16) {
            var1 = Integer.rotateLeft(this.i[0], 1) + Integer.rotateLeft(this.i[1], 7) + Integer.rotateLeft(this.i[2], 12) + Integer.rotateLeft(this.i[3], 18);
        } else {
            var1 = this.i[2] + 374761393;
        }

        int var2 = this.l;
        int var3 = this.m;
        var2 += var1;

        for (var1 = 0; var1 <= var3 - 4; var1 += 4) {
            var2 = Integer.rotateLeft(var2 + a(this.j, var1) * -1028477379, 17) * 668265263;
        }

        while (var1 < this.m) {
            var2 = Integer.rotateLeft((this.j[var1] & 255) * 374761393 + var2, 11) * -1640531535;
            ++var1;
        }

        var1 = (var2 >>> 15 ^ var2) * -2048144777;
        var1 = (var1 ^ var1 >>> 13) * -1028477379;
        return (long) (var1 ^ var1 >>> 16) & 4294967295L;
    }
}
