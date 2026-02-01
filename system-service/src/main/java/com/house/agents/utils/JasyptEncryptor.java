package com.house.agents.utils;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;

/**
 * Jasypt加密工具
 * 用于生成加密的配置文件字符串
 */
public class JasyptEncryptor {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("使用方法: java JasyptEncryptor <要加密的字符串> <加密密钥>");
            System.out.println("示例: java JasyptEncryptor \"root\" \"my-secret-key\"");
            return;
        }

        String plainText = args[0];
        String encryptionPassword = args[1];

        System.out.println("🔐 加密: " + plainText);
        System.out.println("🔑 加密密钥: " + encryptionPassword);

        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword(encryptionPassword);
        encryptor.setAlgorithm("PBEWITHHMACSHA512ANDAES_256");
        encryptor.setIvGenerator(new org.jasypt.iv.RandomIvGenerator());

        String encryptedText = encryptor.encrypt(plainText);

        System.out.println("✅ 加密结果: ENC(" + encryptedText + ")");

        // 验证解密
        String decryptedText = encryptor.decrypt(encryptedText);
        System.out.println("🔓 解密验证: " + decryptedText);

        System.out.println("\n💡 使用方法: 在配置文件中使用 ENC(" + encryptedText + ")");
    }
}
