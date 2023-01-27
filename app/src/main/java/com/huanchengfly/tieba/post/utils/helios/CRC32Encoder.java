package com.huanchengfly.tieba.post.utils.helios;

import java.util.zip.CRC32;

final class CRC32Encoder extends IEncoder {
    public CRC32Encoder(int start, int flag) {
        this.length = 32;
        this.start = start;
        this.flag = flag;
    }

    public EncodeResult encode(byte[] bytes, int off, int len) {
        long result;
        try {
            CRC32 crc32 = new CRC32();
            crc32.update(bytes, off, len);
            result = crc32.getValue();
        } catch (Exception var9) {
            result = 4294967295L;
        }

        return EncodeResult.a(new long[]{result});
    }
}
