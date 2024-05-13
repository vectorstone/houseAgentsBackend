package com.house.agents.entity;


import org.junit.Test;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import static org.junit.Assert.*;

public class HouseAttachmentTest {
    /**
     * 通过nio的方式来读取和写入文件,小文件是可以的,大文件的话会抛出java.lang.IllegalArgumentException: Size exceeds Integer.MAX_VALUE
     * @throws IOException
     */
    @Test
    public void test() throws IOException {
        // 测试一下nio
        long st = System.currentTimeMillis();
        File file = new File("D:\\test.mkv");
        FileInputStream fis = new FileInputStream(file);
        FileChannel inChannel = fis.getChannel();
        MappedByteBuffer mbb = inChannel.map(FileChannel.MapMode.READ_ONLY, 0, file.length());
        FileOutputStream fos = new FileOutputStream("D:\\2.test.mkv");
        FileChannel outChannel = fos.getChannel();
        outChannel.write(mbb);
        inChannel.close();
        outChannel.close();
        long et = System.currentTimeMillis();
        // 234MB 文件耗时3166ms
        System.out.println("耗时" + (et - st) + "ms");
    }

    /**
     * 大文件可以使用下面的这部分代码,不会抛出异常
     * @throws IOException
     */
    @Test
    public void test2() throws IOException {
        // 测试一下nio
        long st = System.currentTimeMillis();
        File file = new File("D:\\test.mkv");
        FileInputStream fis = new FileInputStream(file);
        FileChannel inChannel = fis.getChannel();

        long fileSize = inChannel.size();
        long position = 0;
        long remaining = fileSize;

        FileOutputStream fos = new FileOutputStream("D:\\2.test.mkv");
        FileChannel outChannel = fos.getChannel();

        while (remaining > 0) {
            long size = (int) Math.min(remaining, Integer.MAX_VALUE);
            MappedByteBuffer mbb = inChannel.map(FileChannel.MapMode.READ_ONLY, position, size);
            outChannel.write(mbb);
            position += size;
            remaining -= size;
        }

        inChannel.close();
        outChannel.close();
        long et = System.currentTimeMillis();
        // 234MB 文件耗时3166ms
        // 5.6GB 耗时63900ms
        System.out.println("耗时" + (et - st) + "ms");
    }



    /**
     * 传统的io方式读取和写入文件
     * @throws IOException
     */
    @Test
    public void test1() throws IOException {
        long st = System.currentTimeMillis();
        File file = new File("D:\\test.mkv");
        FileInputStream fis = new FileInputStream(file);
        FileOutputStream fos = new FileOutputStream("D:\\2.test.mkv");
        byte[] buf = new byte[1024];
        int len = 0;
        while((len = fis.read(buf)) > 0) {
            fos.write(buf, 0, len);
        }
        fis.close();
        fos.close();
        long et = System.currentTimeMillis();
        // 234MB 文件耗时2199ms
        // 5.6GB 文件耗时214575ms
        System.out.println("耗时" + (et - st) + "ms");
    }

}