package com.github.charlemaznable.codec;

import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

import static com.github.charlemaznable.codec.Base64.base64;
import static com.github.charlemaznable.codec.Bytes.bytes;
import static com.github.charlemaznable.codec.Hex.hex;

public enum Digest {

    @Deprecated
    MD5 {
        @Override
        protected Hasher digestHasher() {
            return Hashing.md5().newHasher();
        }
    },
    @Deprecated
    SHA1 {
        @Override
        protected Hasher digestHasher() {
            return Hashing.sha1().newHasher();
        }
    },
    SHA256 {
        @Override
        protected Hasher digestHasher() {
            return Hashing.sha256().newHasher();
        }
    },
    SHA384 {
        @Override
        protected Hasher digestHasher() {
            return Hashing.sha384().newHasher();
        }
    },
    SHA512 {
        @Override
        protected Hasher digestHasher() {
            return Hashing.sha512().newHasher();
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
        return base64(digest(info), Base64.Format.Standard);
    }

    public String digestBase64(String info) {
        return base64(digest(info), Base64.Format.Standard);
    }

    public String digestBase64(byte[] info, byte[] salt) {
        return base64(digest(info, salt), Base64.Format.Standard);
    }

    public String digestBase64(byte[] info, String salt) {
        return base64(digest(info, salt), Base64.Format.Standard);
    }

    public String digestBase64(String info, byte[] salt) {
        return base64(digest(info, salt), Base64.Format.Standard);
    }

    public String digestBase64(String info, String salt) {
        return base64(digest(info, salt), Base64.Format.Standard);
    }

    public String digestHex(byte[] info) {
        return hex(digest(info));
    }

    public String digestHex(String info) {
        return hex(digest(info));
    }

    public String digestHex(byte[] info, byte[] salt) {
        return hex(digest(info, salt));
    }

    public String digestHex(byte[] info, String salt) {
        return hex(digest(info, salt));
    }

    public String digestHex(String info, byte[] salt) {
        return hex(digest(info, salt));
    }

    public String digestHex(String info, String salt) {
        return hex(digest(info, salt));
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
        return base64(digestDeprecated(info), Base64.Format.Standard);
    }

    @Deprecated
    public String digestBase64Deprecated(String info, String salt) {
        return base64(digestDeprecated(info, salt), Base64.Format.Standard);
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
