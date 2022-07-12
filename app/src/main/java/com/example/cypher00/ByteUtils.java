package com.example.cypher00;

import java.nio.ByteBuffer;

class ByteUtils {
    // MAY BE TOO HIGH API !
//    private static ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
    private static ByteBuffer buffer = ByteBuffer.allocate(Long.SIZE / Byte.SIZE);

    static byte[] longToBytes(long x) {
        buffer.clear();
        buffer.putLong(0, x);
        return buffer.array();
    }

    static long bytesToLong(byte[] bytes) {
        buffer.clear();
        buffer.put(bytes, 0, bytes.length);
        buffer.flip();
        return buffer.getLong();
    }
}