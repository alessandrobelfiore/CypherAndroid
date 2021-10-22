package com.example.cypher00;

import java.nio.ByteBuffer;

public class ByteUtils {
    // MAY BE TOO HIGH API !
//    private static ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
//    private static ByteBuffer buffer = ByteBuffer.allocate(1024);
    private static ByteBuffer buffer = ByteBuffer.allocate(Long.SIZE / Byte.SIZE);

    public static byte[] longToBytes(long x) {
        buffer.putLong(0, x);
        return buffer.array();
    }

    public static long bytesToLong(byte[] bytes) {
        buffer.put(bytes, 0, bytes.length);
        buffer.flip();//need flip
        return buffer.getLong();
    }
}