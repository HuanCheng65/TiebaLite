package com.huanchengfly.tieba.post.utils.helios;

import java.util.Arrays;

public class Hasher {
    static IEncoder[] sEncoders = new IEncoder[]{new CRC32Encoder(8, 0), new XXHashEncoder(0, 1), new XXHashEncoder(1, 1), new CRC32Encoder(7, 1)};

    public static byte[] hash(byte[] bytes) {
        HashResult result = new HashResult();
        byte[] newBytes = ByteArrayUtils.copyArray(bytes, bytes.length + (sEncoders.length + 1) * 5);
        ByteArrayUtils.copyArray(newBytes, result.getValue(), bytes.length);

        for (int i = 0; i < sEncoders.length; i++) {
            IEncoder encoder = sEncoders[i];
            int len = bytes.length + (i + 1) * 5;
            result.update(encoder.encode(newBytes, 0, len), encoder.getStart(), encoder.getLength(), encoder.getFlag());
            ByteArrayUtils.copyArray(newBytes, result.getValue(), len);
        }

        return Arrays.copyOf(result.getValue(), 5);
    }
}
