package com.github.charlemaznable.codec;

import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

import java.security.Key;

import static com.github.charlemaznable.codec.Base64.base64;
import static com.github.charlemaznable.codec.Bytes.bytes;
import static com.github.charlemaznable.codec.Hex.hex;

public enum DigestHMAC {

    MD5 {
        @Override
        protected Hasher digestHasher(Key key) {
            return Hashing.hmacMd5(key).newHasher();
        }

        @Override
        protected Hasher digestHasher(byte[] key) {
            return Hashing.hmacMd5(key).newHasher();
        }
    },
    SHA1 {
        @Override
        protected Hasher digestHasher(Key key) {
            return Hashing.hmacSha1(key).newHasher();
        }

        @Override
        protected Hasher digestHasher(byte[] key) {
            return Hashing.hmacSha1(key).newHasher();
        }
    },
    SHA256 {
        @Override
        protected Hasher digestHasher(Key key) {
            return Hashing.hmacSha256(key).newHasher();
        }

        @Override
        protected Hasher digestHasher(byte[] key) {
            return Hashing.hmacSha256(key).newHasher();
        }
    },
    SHA512 {
        @Override
        protected Hasher digestHasher(Key key) {
            return Hashing.hmacSha512(key).newHasher();
        }

        @Override
        protected Hasher digestHasher(byte[] key) {
            return Hashing.hmacSha512(key).newHasher();
        }
    };

    protected abstract Hasher digestHasher(Key key);

    protected abstract Hasher digestHasher(byte[] key);

    protected Hasher digestHasher(String key) {
        return digestHasher(bytes(key));
    }

    public byte[] digest(byte[] info, Key key) {
        return digestHasher(key).putBytes(info).hash().asBytes();
    }

    public byte[] digest(byte[] info, byte[] key) {
        return digestHasher(key).putBytes(info).hash().asBytes();
    }

    public byte[] digest(byte[] info, String key) {
        return digestHasher(key).putBytes(info).hash().asBytes();
    }

    public byte[] digest(String info, Key key) {
        return digest(bytes(info), key);
    }

    public byte[] digest(String info, byte[] key) {
        return digest(bytes(info), key);
    }

    public byte[] digest(String info, String key) {
        return digest(bytes(info), key);
    }

    public String digestBase64(byte[] info, Key key) {
        return base64(digest(info, key), Base64.Format.Standard);
    }

    public String digestBase64(byte[] info, byte[] key) {
        return base64(digest(info, key), Base64.Format.Standard);
    }

    public String digestBase64(byte[] info, String key) {
        return base64(digest(info, key), Base64.Format.Standard);
    }

    public String digestBase64(String info, Key key) {
        return digestBase64(bytes(info), key);
    }

    public String digestBase64(String info, byte[] key) {
        return digestBase64(bytes(info), key);
    }

    public String digestBase64(String info, String key) {
        return digestBase64(bytes(info), key);
    }

    public String digestHex(byte[] info, Key key) {
        return hex(digest(info, key));
    }

    public String digestHex(byte[] info, byte[] key) {
        return hex(digest(info, key));
    }

    public String digestHex(byte[] info, String key) {
        return hex(digest(info, key));
    }

    public String digestHex(String info, Key key) {
        return digestHex(bytes(info), key);
    }

    public String digestHex(String info, byte[] key) {
        return digestHex(bytes(info), key);
    }

    public String digestHex(String info, String key) {
        return digestHex(bytes(info), key);
    }
}
