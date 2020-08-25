package com.github.charlemaznable.core.codec;

import com.google.common.hash.Hasher;

import java.security.Key;

import static com.github.charlemaznable.core.codec.Base64.Format.STANDARD;
import static com.github.charlemaznable.core.codec.Base64.base64;
import static com.github.charlemaznable.core.codec.Bytes.bytes;
import static com.github.charlemaznable.core.codec.Hex.hex;
import static com.google.common.hash.Hashing.hmacMd5;
import static com.google.common.hash.Hashing.hmacSha1;
import static com.google.common.hash.Hashing.hmacSha256;
import static com.google.common.hash.Hashing.hmacSha512;

public enum DigestHMAC {

    MD5 {
        @Override
        protected final Hasher digestHasher(Key key) {
            return hmacMd5(key).newHasher();
        }

        @Override
        protected final Hasher digestHasher(byte[] key) {
            return hmacMd5(key).newHasher();
        }
    },
    SHA1 {
        @Override
        protected final Hasher digestHasher(Key key) {
            return hmacSha1(key).newHasher();
        }

        @Override
        protected final Hasher digestHasher(byte[] key) {
            return hmacSha1(key).newHasher();
        }
    },
    SHA256 {
        @Override
        protected final Hasher digestHasher(Key key) {
            return hmacSha256(key).newHasher();
        }

        @Override
        protected final Hasher digestHasher(byte[] key) {
            return hmacSha256(key).newHasher();
        }
    },
    SHA512 {
        @Override
        protected final Hasher digestHasher(Key key) {
            return hmacSha512(key).newHasher();
        }

        @Override
        protected final Hasher digestHasher(byte[] key) {
            return hmacSha512(key).newHasher();
        }
    },;

    protected abstract Hasher digestHasher(Key key);

    protected abstract Hasher digestHasher(byte[] key);

    protected final Hasher digestHasher(String key) {
        return digestHasher(bytes(key));
    }

    public final byte[] digest(byte[] info, Key key) {
        return digestHasher(key).putBytes(info).hash().asBytes();
    }

    public final byte[] digest(byte[] info, byte[] key) {
        return digestHasher(key).putBytes(info).hash().asBytes();
    }

    public final byte[] digest(byte[] info, String key) {
        return digestHasher(key).putBytes(info).hash().asBytes();
    }

    public final byte[] digest(String info, Key key) {
        return digest(bytes(info), key);
    }

    public final byte[] digest(String info, byte[] key) {
        return digest(bytes(info), key);
    }

    public final byte[] digest(String info, String key) {
        return digest(bytes(info), key);
    }

    public final String digestBase64(byte[] info, Key key) {
        return base64(digest(info, key), STANDARD);
    }

    public final String digestBase64(byte[] info, byte[] key) {
        return base64(digest(info, key), STANDARD);
    }

    public final String digestBase64(byte[] info, String key) {
        return base64(digest(info, key), STANDARD);
    }

    public final String digestBase64(String info, Key key) {
        return digestBase64(bytes(info), key);
    }

    public final String digestBase64(String info, byte[] key) {
        return digestBase64(bytes(info), key);
    }

    public final String digestBase64(String info, String key) {
        return digestBase64(bytes(info), key);
    }

    public final String digestHex(byte[] info, Key key) {
        return hex(digest(info, key));
    }

    public final String digestHex(byte[] info, byte[] key) {
        return hex(digest(info, key));
    }

    public final String digestHex(byte[] info, String key) {
        return hex(digest(info, key));
    }

    public final String digestHex(String info, Key key) {
        return digestHex(bytes(info), key);
    }

    public final String digestHex(String info, byte[] key) {
        return digestHex(bytes(info), key);
    }

    public final String digestHex(String info, String key) {
        return digestHex(bytes(info), key);
    }
}
