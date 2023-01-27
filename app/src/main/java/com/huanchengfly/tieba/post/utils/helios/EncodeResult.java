package com.huanchengfly.tieba.post.utils.helios;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.LongBuffer;
import java.util.Arrays;

public class EncodeResult implements Serializable, Cloneable {
    static final boolean a;

    static {
        a = !EncodeResult.class.desiredAssertionStatus();
    }

    private long[] g;
    private transient int h = 0;
    private transient boolean i = false;

    public EncodeResult() {
        this.j(64);
        this.i = false;
    }

    public EncodeResult(int bits) {
        if (bits < 0) {
            throw new NegativeArraySizeException("nbits < 0: " + bits);
        } else {
            this.j(bits);
            this.i = true;
        }
    }

    private EncodeResult(long[] var1) {
        this.g = var1;
        this.h = var1.length;
        this.h();
    }

    public static EncodeResult a(ByteBuffer var0) {
        int var1 = 0;
        ByteBuffer var2 = var0.slice().order(ByteOrder.LITTLE_ENDIAN);

        int var3 = var2.remaining();
        while (var3 > 0 && var2.get(var3 - 1) == 0) --var3;

        long[] var5 = new long[(var3 + 7) / 8];
        var2.limit(var3);

        for (var3 = 0; var2.remaining() >= 8; ++var3) {
            var5[var3] = var2.getLong();
        }

        for (int var4 = var2.remaining(); var1 < var4; ++var1) {
            var5[var3] |= ((long) var2.get() & 255L) << var1 * 8;
        }

        return new EncodeResult(var5);
    }

    public static EncodeResult a(LongBuffer var0) {
        var0 = var0.slice();

        int var1 = var0.remaining();
        while (var1 > 0 && var0.get(var1 - 1) == 0L) --var1;

        long[] var2 = new long[var1];
        var0.get(var2);
        return new EncodeResult(var2);
    }

    public static EncodeResult a(byte[] var0) {
        return a(ByteBuffer.wrap(var0));
    }

    public static EncodeResult a(long[] var0) {
        int var1 = var0.length;
        while (var1 > 0 && var0[var1 - 1] == 0) --var1;

        return new EncodeResult(Arrays.copyOf(var0, var1));
    }

    private static void checkIndex(int fromIndex, int toIndex) {
        if (fromIndex < 0) {
            throw new IndexOutOfBoundsException("fromIndex < 0: " + fromIndex);
        } else if (toIndex < 0) {
            throw new IndexOutOfBoundsException("toIndex < 0: " + toIndex);
        } else if (fromIndex > toIndex) {
            throw new IndexOutOfBoundsException("fromIndex: " + fromIndex + " > toIndex: " + toIndex);
        }
    }

    private static int i(int var0) {
        return var0 >> 6;
    }

    private void h() {
        if (!a && this.h != 0 && this.g[this.h - 1] == 0L) {
            throw new AssertionError();
        } else if (!a && this.h > this.g.length) {
            throw new AssertionError();
        } else if (!a && this.h != this.g.length && this.g[this.h] != 0L) {
            throw new AssertionError();
        }
    }

    private void i() {
        int var1 = this.h - 1;
        while (var1 >= 0 && this.g[var1] == 0L) {
            --var1;
        }
        this.h = var1 + 1;
    }

    private void j() {
        if (this.h != this.g.length) {
            this.g = Arrays.copyOf(this.g, this.h);
            this.h();
        }
    }

    private void j(int bits) {
        this.g = new long[i(bits - 1) + 1];
    }

    private void k(int var1) {
        if (this.g.length < var1) {
            var1 = Math.max(this.g.length * 2, var1);
            this.g = Arrays.copyOf(this.g, var1);
            this.i = false;
        }

    }

    private void l(int var1) {
        ++var1;
        if (this.h < var1) {
            this.k(var1);
            this.h = var1;
        }

    }

    public void a(int var1) {
        if (var1 < 0) {
            throw new IndexOutOfBoundsException("bitIndex < 0: " + var1);
        } else {
            int var2 = i(var1);
            this.l(var2);
            long[] var3 = this.g;
            var3[var2] ^= 1L << var1;
            this.i();
            this.h();
        }
    }

    public void a(int fromIndex, int toIndex) {
        checkIndex(fromIndex, toIndex);
        if (fromIndex != toIndex) {
            int var3 = i(fromIndex);
            int var4 = i(toIndex - 1);
            this.l(var4);
            long var5 = -1L << fromIndex;
            long var7 = -1L >>> -toIndex;
            long[] var9 = this.g;
            if (var3 == var4) {
                var9[var3] ^= var5 & var7;
            } else {
                var9[var3] ^= var5;

                for (fromIndex = var3 + 1; fromIndex < var4; ++fromIndex) {
                    var9[fromIndex] = ~var9[fromIndex];
                }

                var9[var4] ^= var7;
            }

            this.i();
            this.h();
        }

    }

    public void a(int fromIndex, int toIndex, boolean var3) {
        if (var3) {
            this.b(fromIndex, toIndex);
        } else {
            this.c(fromIndex, toIndex);
        }

    }

    public void a(int var1, boolean var2) {
        if (var2) {
            this.b(var1);
        } else {
            this.c(var1);
        }

    }

    public boolean a(EncodeResult var1) {
        int var2 = Math.min(this.h, var1.h) - 1;

        boolean var3;
        while (true) {
            if (var2 < 0) {
                var3 = false;
                break;
            }

            if ((this.g[var2] & var1.g[var2]) != 0L) {
                var3 = true;
                break;
            }

            --var2;
        }

        return var3;
    }

    public byte[] a() {
        byte var1 = 0;
        int var2 = this.h;
        byte[] var3;
        if (var2 == 0) {
            var3 = new byte[0];
        } else {
            int var4 = (var2 - 1) * 8;

            long var5;
            for (var5 = this.g[var2 - 1]; var5 != 0L; var5 >>>= 8) {
                ++var4;
            }

            byte[] var7 = new byte[var4];
            ByteBuffer var8 = ByteBuffer.wrap(var7).order(ByteOrder.LITTLE_ENDIAN);

            for (var4 = var1; var4 < var2 - 1; ++var4) {
                var8.putLong(this.g[var4]);
            }

            var5 = this.g[var2 - 1];

            while (true) {
                var3 = var7;
                if (var5 == 0L) {
                    break;
                }

                var8.put((byte) ((int) (255L & var5)));
                var5 >>>= 8;
            }
        }

        return var3;
    }

    public void b(int var1) {
        if (var1 < 0) {
            throw new IndexOutOfBoundsException("bitIndex < 0: " + var1);
        } else {
            int var2 = i(var1);
            this.l(var2);
            long[] var3 = this.g;
            var3[var2] |= 1L << var1;
            this.h();
        }
    }

    public void b(int fromIndex, int toIndex) {
        checkIndex(fromIndex, toIndex);
        if (fromIndex != toIndex) {
            int var3 = i(fromIndex);
            int var4 = i(toIndex - 1);
            this.l(var4);
            long var5 = -1L << fromIndex;
            long var7 = -1L >>> -toIndex;
            long[] var9 = this.g;
            if (var3 == var4) {
                var9[var3] |= var5 & var7;
            } else {
                var9[var3] |= var5;

                for (fromIndex = var3 + 1; fromIndex < var4; ++fromIndex) {
                    this.g[fromIndex] = -1L;
                }

                var9[var4] |= var7;
            }

            this.h();
        }

    }

    public void b(EncodeResult var1) {
        if (this != var1) {
            while (true) {
                long[] var2;
                int var3;
                if (this.h <= var1.h) {
                    for (var3 = 0; var3 < this.h; ++var3) {
                        var2 = this.g;
                        var2[var3] &= var1.g[var3];
                    }

                    this.i();
                    this.h();
                    break;
                }

                var2 = this.g;
                var3 = this.h - 1;
                this.h = var3;
                var2[var3] = 0L;
            }
        }

    }

    public long[] b() {
        return Arrays.copyOf(this.g, this.h);
    }

    public void c() {
        while (this.h > 0) {
            long[] var1 = this.g;
            int var2 = this.h - 1;
            this.h = var2;
            var1[var2] = 0L;
        }

    }

    public void c(int var1) {
        if (var1 < 0) {
            throw new IndexOutOfBoundsException("bitIndex < 0: " + var1);
        } else {
            int var2 = i(var1);
            if (var2 < this.h) {
                long[] var3 = this.g;
                var3[var2] &= ~(1L << var1);
                this.i();
                this.h();
            }

        }
    }

    public void c(int fromIndex, int toIndex) {
        checkIndex(fromIndex, toIndex);
        if (fromIndex != toIndex) {
            int var3 = i(fromIndex);
            if (var3 < this.h) {
                int var4 = i(toIndex - 1);
                if (var4 >= this.h) {
                    toIndex = this.d();
                    var4 = this.h - 1;
                }

                long var5 = -1L << fromIndex;
                long var7 = -1L >>> -toIndex;
                long[] var9 = this.g;
                if (var3 == var4) {
                    var9[var3] &= ~(var5 & var7);
                } else {
                    var9[var3] &= ~var5;

                    for (fromIndex = var3 + 1; fromIndex < var4; ++fromIndex) {
                        this.g[fromIndex] = 0L;
                    }

                    var9[var4] &= ~var7;
                }

                this.i();
                this.h();
            }
        }

    }

    public void c(EncodeResult var1) {
        if (this != var1) {
            int var2 = Math.min(this.h, var1.h);
            if (this.h < var1.h) {
                this.k(var1.h);
                this.h = var1.h;
            }

            for (int var3 = 0; var3 < var2; ++var3) {
                long[] var4 = this.g;
                var4[var3] |= var1.g[var3];
            }

            if (var2 < var1.h) {
                System.arraycopy(var1.g, var2, this.g, var2, this.h - var2);
            }

            this.h();
        }

    }

    @NonNull
    public Object clone() {
        if (!this.i) {
            this.j();
        }

        try {
            EncodeResult var1 = (EncodeResult) super.clone();
            var1.g = this.g.clone();
            var1.h();
            return var1;
        } catch (CloneNotSupportedException var2) {
            throw new InternalError();
        }
    }

    public int d() {
        int var1;
        if (this.h == 0) {
            var1 = 0;
        } else {
            var1 = (this.h - 1) * 64 + (64 - Long.numberOfLeadingZeros(this.g[this.h - 1]));
        }

        return var1;
    }

    public EncodeResult d(int fromIndex, int toIndex) {
        int var3 = 0;
        checkIndex(fromIndex, toIndex);
        this.h();
        int var4 = this.d();
        EncodeResult var5;
        if (var4 > fromIndex && fromIndex != toIndex) {
            int var6 = Math.min(toIndex, var4);

            var5 = new EncodeResult(var6 - fromIndex);
            int var7 = i(var6 - fromIndex - 1) + 1;
            var4 = i(fromIndex);
            boolean var13;
            var13 = (fromIndex & 63) == 0;

            long[] var8;
            long var9;
            while (var3 < var7 - 1) {
                var8 = var5.g;
                if (var13) {
                    var9 = this.g[var4];
                } else {
                    var9 = this.g[var4] >>> fromIndex | this.g[var4 + 1] << -fromIndex;
                }

                var8[var3] = var9;
                ++var3;
                ++var4;
            }

            var9 = -1L >>> -var6;
            var8 = var5.g;
            if ((var6 - 1 & 63) < (fromIndex & 63)) {
                long var11 = this.g[var4];
                var9 = (var9 & this.g[var4 + 1]) << -fromIndex | var11 >>> fromIndex;
            } else {
                var9 = (var9 & this.g[var4]) >>> fromIndex;
            }

            var8[var7 - 1] = var9;
            var5.h = var7;
            var5.i();
            var5.h();
        } else {
            var5 = new EncodeResult(0);
        }

        return var5;
    }

    public void d(EncodeResult var1) {
        int var2 = Math.min(this.h, var1.h);
        if (this.h < var1.h) {
            this.k(var1.h);
            this.h = var1.h;
        }

        for (int var3 = 0; var3 < var2; ++var3) {
            long[] var4 = this.g;
            var4[var3] ^= var1.g[var3];
        }

        if (var2 < var1.h) {
            System.arraycopy(var1.g, var2, this.g, var2, var1.h - var2);
        }

        this.i();
        this.h();
    }

    public boolean d(int var1) {
        if (var1 < 0) {
            throw new IndexOutOfBoundsException("bitIndex < 0: " + var1);
        } else {
            this.h();
            int var2 = i(var1);
            boolean var3;
            var3 = var2 < this.h && (this.g[var2] & 1L << var1) != 0L;

            return var3;
        }
    }

    public int e(int var1) {
        byte var2 = -1;
        if (var1 < 0) {
            throw new IndexOutOfBoundsException("fromIndex < 0: " + var1);
        } else {
            this.h();
            int var3 = i(var1);
            if (var3 >= this.h) {
                var1 = var2;
            } else {
                long var4 = this.g[var3] & -1L << var1;
                var1 = var3;

                while (true) {
                    if (var4 != 0L) {
                        var1 = var1 * 64 + Long.numberOfTrailingZeros(var4);
                        break;
                    }

                    var3 = var1 + 1;
                    var1 = var2;
                    if (var3 == this.h) {
                        break;
                    }

                    var4 = this.g[var3];
                    var1 = var3;
                }
            }

            return var1;
        }
    }

    public void e(EncodeResult var1) {
        for (int var2 = Math.min(this.h, var1.h) - 1; var2 >= 0; --var2) {
            long[] var3 = this.g;
            var3[var2] &= ~var1.g[var2];
        }

        this.i();
        this.h();
    }

    public boolean e() {
        return this.h == 0;
    }

    public boolean equals(Object var1) {
        boolean var2 = false;
        boolean var3;
        if (!(var1 instanceof EncodeResult)) {
            var3 = var2;
        } else if (this == var1) {
            var3 = true;
        } else {
            EncodeResult var5 = (EncodeResult) var1;
            this.h();
            var5.h();
            var3 = var2;
            if (this.h == var5.h) {
                int var4 = 0;

                while (true) {
                    if (var4 >= this.h) {
                        var3 = true;
                        break;
                    }

                    if (this.g[var4] != var5.g[var4]) {
                        break;
                    }

                    ++var4;
                }
            }
        }

        return var3;
    }

    public int f() {
        int var1 = 0;

        int var2;
        for (var2 = 0; var1 < this.h; ++var1) {
            var2 += Long.bitCount(this.g[var1]);
        }

        return var2;
    }

    public int f(int var1) {
        if (var1 < 0) {
            throw new IndexOutOfBoundsException("fromIndex < 0: " + var1);
        } else {
            this.h();
            int var2 = i(var1);
            if (var2 < this.h) {
                long var3 = ~this.g[var2] & -1L << var1;

                for (var1 = var2; var3 == 0L; var3 = ~this.g[var1]) {
                    ++var1;
                    if (var1 == this.h) {
                        var1 = this.h * 64;
                        return var1;
                    }
                }

                var1 = var1 * 64 + Long.numberOfTrailingZeros(var3);
            }

            return var1;
        }
    }

    public int h(int var1) {
        int var2;
        if (var1 < 0) {
            if (var1 != -1) {
                throw new IndexOutOfBoundsException("fromIndex < -1: " + var1);
            }

            var2 = -1;
        } else {
            this.h();
            int var3 = i(var1);
            var2 = var1;
            if (var3 < this.h) {
                long var4 = ~this.g[var3] & -1L >>> -(var1 + 1);

                for (var1 = var3; var4 == 0L; var1 = var2) {
                    var2 = var1 - 1;
                    if (var1 == 0) {
                        var2 = -1;
                        return var2;
                    }

                    var4 = ~this.g[var2];
                }

                var2 = (var1 + 1) * 64 - 1 - Long.numberOfLeadingZeros(var4);
            }
        }

        return var2;
    }

    public int hashCode() {
        long var1 = 1234L;
        int var3 = this.h;

        while (true) {
            --var3;
            if (var3 < 0) {
                return (int) (var1 >> 32 ^ var1);
            }

            var1 ^= this.g[var3] * (long) (var3 + 1);
        }
    }

    @NonNull
    public String toString() {
        this.h();
        int var1;
        if (this.h > 128) {
            var1 = this.f();
        } else {
            var1 = this.h * 64;
        }

        StringBuilder var2 = new StringBuilder(var1 * 6 + 2);
        var2.append('{');
        var1 = this.e(0);
        if (var1 != -1) {
            var2.append(var1);

            int var4;
            for (var1 = this.e(var1 + 1); var1 >= 0; var1 = this.e(var4 + 1)) {
                int var3 = this.f(var1);

                do {
                    var2.append(", ").append(var1);
                    var4 = var1 + 1;
                    var1 = var4;
                } while (var4 < var3);
            }
        }

        var2.append('}');
        return var2.toString();
    }
}