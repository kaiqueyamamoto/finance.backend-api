package com.finance.finance.util;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

public class CuidGenerator {
    
    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final int COUNTER_MAX = 16777216; // 24 bits
    private static final AtomicLong COUNTER = new AtomicLong(0);
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String FINGERPRINT = generateFingerprint();
    
    /**
     * Gera um CUID (Collision-resistant Unique Identifier)
     * Formato: c + timestamp + counter + random + fingerprint
     */
    public static String generateCuid() {
        StringBuilder cuid = new StringBuilder();
        
        // Prefixo 'c'
        cuid.append('c');
        
        // Timestamp em base36
        String timestamp = Long.toString(System.currentTimeMillis(), 36);
        cuid.append(timestamp);
        
        // Counter
        long counter = COUNTER.incrementAndGet() % COUNTER_MAX;
        String counterStr = Long.toString(counter, 36);
        cuid.append(counterStr);
        
        // Random (8 caracteres)
        cuid.append(generateRandomString(8));
        
        // Fingerprint (4 caracteres)
        cuid.append(FINGERPRINT);
        
        return cuid.toString();
    }
    
    /**
     * Gera string aleatória usando o alfabeto base36
     */
    private static String generateRandomString(int length) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < length; i++) {
            result.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return result.toString();
    }
    
    /**
     * Gera fingerprint baseado no hostname e PID
     */
    private static String generateFingerprint() {
        try {
            String hostname = java.net.InetAddress.getLocalHost().getHostName();
            long pid = ProcessHandle.current().pid();
            
            // Usar hash simples para gerar 4 caracteres
            String combined = hostname + pid;
            int hash = Math.abs(combined.hashCode());
            
            StringBuilder fingerprint = new StringBuilder();
            for (int i = 0; i < 4; i++) {
                fingerprint.append(ALPHABET.charAt(hash % ALPHABET.length()));
                hash /= ALPHABET.length();
            }
            
            return fingerprint.toString();
        } catch (Exception e) {
            // Fallback para fingerprint aleatório
            return generateRandomString(4);
        }
    }
    
    /**
     * Valida se uma string é um CUID válido
     */
    public static boolean isValidCuid(String cuid) {
        if (cuid == null || cuid.length() < 25) {
            return false;
        }
        
        // Verifica se começa com 'c'
        if (cuid.charAt(0) != 'c') {
            return false;
        }
        
        // Verifica se todos os caracteres são válidos
        for (char c : cuid.toCharArray()) {
            if (!ALPHABET.contains(String.valueOf(c))) {
                return false;
            }
        }
        
        return true;
    }
}
