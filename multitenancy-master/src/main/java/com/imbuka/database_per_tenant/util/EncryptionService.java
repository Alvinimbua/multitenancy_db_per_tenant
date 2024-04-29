package com.imbuka.database_per_tenant.util;

public interface EncryptionService {
    String decrypt(String strToDecrypt, String secret, String salt);
}
