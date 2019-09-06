package com.github.charlemaznable.core.codec;

import com.google.common.hash.Hasher;

import static com.github.charlemaznable.core.codec.Base64.Format.Standard;
import static com.github.charlemaznable.core.codec.Base64.base64;
import static com.github.charlemaznable.core.codec.Bytes.bytes;
import static com.github.charlemaznable.core.codec.Hex.hex;
import static com.google.common.hash.Hashing.md5;
import static com.google.common.hash.Hashing.sha1;
import static com.google.common.hash.Hashing.sha256;
import static com.google.common.hash.Hashing.sha384;
import static com.google.common.hash.Hashing.sha512;

public enum Digest {

    @Deprecated
    MD5 {
        @Override
        protected Hasher digestHasher() {
            return md5().newHasher();
        }
    },
    @Deprecated
    SHA1 {
        @Override
        protected Hasher digestHasher() {
            return sha1().newHasher();
        }
    },
    SHA256 {
        @Override
        protected Hasher digestHasher() {
            return sha256().newHasher();
        }
    },
    SHA384 {
        @Override
        protected Hasher digestHasher() {
            return sha384().newHasher();
        }
    },
    SHA512 {
        @Override
        protected Hasher digestHasher() {
            return sha512().newHasher();
        }
    };

    protected abstract Hasher digestHasher();

    public byte[] digest(byte[] info) {
        return digestHasher().putBytes(info).hash().asBytes();
    }

    public byte[] digest(String info) {
        return digest(bytes(info));
    }

    public byte[] digest(byte[] info, byte[] salt) {
        return digestHasher().putBytes(salt).putBytes(info)
                .putBytes(salt).hash().asBytes();
    }

    public byte[] digest(byte[] info, String salt) {
        return digest(info, bytes(salt));
    }

    public byte[] digest(String info, byte[] salt) {
        return digest(bytes(info), salt);
    }

    public byte[] digest(String info, String salt) {
        return digest(bytes(info), bytes(salt));
    }

    public String digestBase64(byte[] info) {
        return base64(digest(info), Standard);
    }

    public String digestBase64(String info) {
        return digestBase64(bytes(info));
    }

    public String digestBase64(byte[] info, byte[] salt) {
        return base64(digest(info, salt), Standard);
    }

    public String digestBase64(byte[] info, String salt) {
        return digestBase64(info, bytes(salt));
    }

    public String digestBase64(String info, byte[] salt) {
        return digestBase64(bytes(info), salt);
    }

    public String digestBase64(String info, String salt) {
        return digestBase64(bytes(info), bytes(salt));
    }

    public String digestHex(byte[] info) {
        return hex(digest(info));
    }

    public String digestHex(String info) {
        return digestHex(bytes(info));
    }

    public String digestHex(byte[] info, byte[] salt) {
        return hex(digest(info, salt));
    }

    public String digestHex(byte[] info, String salt) {
        return digestHex(info, bytes(salt));
    }

    public String digestHex(String info, byte[] salt) {
        return digestHex(bytes(info), salt);
    }

    public String digestHex(String info, String salt) {
        return digestHex(bytes(info), bytes(salt));
    }

    @Deprecated
    public byte[] digestDeprecated(String info) {
        return digestHasher().putUnencodedChars(info).hash().asBytes();
    }

    @Deprecated
    public byte[] digestDeprecated(String info, String salt) {
        return digestHasher().putUnencodedChars(salt)
                .putUnencodedChars(info)
                .putUnencodedChars(salt).hash().asBytes();
    }

    @Deprecated
    public String digestBase64Deprecated(String info) {
        return base64(digestDeprecated(info), Standard);
    }

    @Deprecated
    public String digestBase64Deprecated(String info, String salt) {
        return base64(digestDeprecated(info, salt), Standard);
    }

    @Deprecated
    public String digestHexDeprecated(String info) {
        return hex(digestDeprecated(info));
    }

    @Deprecated
    public String digestHexDeprecated(String info, String salt) {
        return hex(digestDeprecated(info, salt));
    }
}
