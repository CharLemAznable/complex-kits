package com.github.charlemaznable.core.crypto;

import com.idealista.fpe.FormatPreservingEncryption;
import com.idealista.fpe.algorithm.Cipher;
import com.idealista.fpe.component.functions.prf.PseudoRandomFunction;
import com.idealista.fpe.config.Alphabet;
import com.idealista.fpe.config.Domain;
import com.idealista.fpe.config.GenericTransformations;
import com.idealista.fpe.config.LengthRange;
import com.idealista.fpe.transformer.IntToTextTransformer;
import com.idealista.fpe.transformer.TextToIntTransformer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.val;

import javax.crypto.spec.IvParameterSpec;
import java.security.Key;
import java.util.Arrays;

import static com.github.charlemaznable.core.codec.Bytes.bytes;
import static com.github.charlemaznable.core.crypto.FPE.AlphabetDomains.ALPHANUMERIC;
import static com.github.charlemaznable.core.lang.Condition.checkNotNull;
import static com.github.charlemaznable.core.lang.Condition.nullThen;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public final class FPE extends FormatPreservingEncryption {

    private static final Cipher ff1 = new com.idealista.fpe.algorithm.ff1.Cipher();

    public static FPEBuilder ff1() {
        return new FPEBuilder(ff1);
    }

    public FPE(Cipher cipher, Domain domain,
               PseudoRandomFunction pseudoRandomFunction,
               LengthRange lengthRange) {
        super(cipher, domain, pseudoRandomFunction, lengthRange);
    }

    public String encrypt(String plainText, String tweak) {
        return super.encrypt(plainText, bytes(tweak));
    }

    public String decrypt(String cipherText, String tweak) {
        return super.decrypt(cipherText, bytes(tweak));
    }

    public static final class FPEBuilder {

        public static final Domain DEFAULT_DOMAIN = ALPHANUMERIC.domain();
        public static final Integer DEFAULT_MIN_LENGTH = 2;
        public static final Integer DEFAULT_MAX_LENGTH = Integer.MAX_VALUE;
        public static final LengthRange DEFAULT_LENGTH_RANGE =
                new LengthRange(DEFAULT_MIN_LENGTH, DEFAULT_MAX_LENGTH);

        private final Cipher cipher;
        private Domain domain = DEFAULT_DOMAIN;
        private PseudoRandomFunction pseudoRandomFunction;
        private LengthRange lengthRange = DEFAULT_LENGTH_RANGE;

        private FPEBuilder(Cipher cipher) {
            this.cipher = checkNotNull(cipher);
        }

        public FPEBuilder withDomain(Domain domain) {
            if (nonNull(domain)) this.domain = domain;
            return this;
        }

        public FPEBuilder withDomain(AlphabetDomains alphabetDomains) {
            if (isNull(alphabetDomains)) return this;
            return withDomain(alphabetDomains.domain());
        }

        public FPEBuilder withDomain(String alphabetString) {
            if (isNull(alphabetString)) return this;
            return withDomain(alphabetString.toCharArray());
        }

        public FPEBuilder withDomain(char[] alphabetChars) {
            if (isNull(alphabetChars)) return this;
            return withDomain(new AlphabetDomain(
                    new GenericAlphabet(alphabetChars)));
        }

        public FPEBuilder withPseudoRandomFunction
                (PseudoRandomFunction pseudoRandomFunction) {
            if (nonNull(pseudoRandomFunction))
                this.pseudoRandomFunction = pseudoRandomFunction;
            return this;
        }

        public FPEBuilder withPseudoRandomKey(String keyString) {
            if (isNull(keyString)) return this;
            return withPseudoRandomFunction(new AESCBCNoPaddingPRF(keyString));
        }

        public FPEBuilder withPseudoRandomKey(String keyString, int size) {
            if (isNull(keyString)) return this;
            return withPseudoRandomFunction(new AESCBCNoPaddingPRF(keyString, size));
        }

        public FPEBuilder withPseudoRandomKey(byte[] keyBytes) {
            if (isNull(keyBytes)) return this;
            return withPseudoRandomFunction(new AESCBCNoPaddingPRF(keyBytes));
        }

        public FPEBuilder withPseudoRandomKey(byte[] keyBytes, int size) {
            if (isNull(keyBytes)) return this;
            return withPseudoRandomFunction(new AESCBCNoPaddingPRF(keyBytes, size));
        }

        public FPEBuilder withLengthRange(LengthRange lengthRange) {
            if (nonNull(lengthRange)) this.lengthRange = lengthRange;
            return this;
        }

        public FPEBuilder withLengthRange(Integer min, Integer max) {
            return withLengthRange(new LengthRange(
                    nullThen(min, () -> DEFAULT_MIN_LENGTH),
                    nullThen(max, () -> DEFAULT_MAX_LENGTH)));
        }

        public FPE build() {
            return new FPE(cipher, checkNotNull(domain),
                    checkNotNull(pseudoRandomFunction),
                    checkNotNull(lengthRange));
        }
    }

    @Getter
    @Accessors(fluent = true)
    public enum AlphabetDomains {

        NUMBERS("0123456789"),

        LOWER_LETTERS("abcdefghijklmnopqrstuvwxyz"),

        UPPER_LETTERS("ABCDEFGHIJKLMNOPQRSTUVWXYZ"),

        LETTERS("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"),

        LOWER_ALPHANUMERIC("0123456789abcdefghijklmnopqrstuvwxyz"),

        UPPER_ALPHANUMERIC("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"),

        ALPHANUMERIC("0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"),

        BASE64("0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ+/="),

        BASE64_URL_SAFE("0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-_="),

        BASE64_PURIFIED("0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ+/"),;

        private AlphabetDomain domain;

        AlphabetDomains(String alphabetString) {
            this.domain = new AlphabetDomain(new GenericAlphabet(
                    checkNotNull(alphabetString).toCharArray()));
        }
    }

    @AllArgsConstructor
    public static class GenericAlphabet implements Alphabet {

        private final char[] chars;

        @Override
        public char[] availableCharacters() {
            return chars;
        }

        @Override
        public Integer radix() {
            return chars.length;
        }
    }

    public static class AlphabetDomain implements Domain {

        private final Alphabet alphabet;
        private final TextToIntTransformer textToIntTransformer;
        private final IntToTextTransformer intToTextTransformer;

        public AlphabetDomain(Alphabet alphabet) {
            this.alphabet = alphabet;
            this.textToIntTransformer = new GenericTransformations(alphabet.availableCharacters());
            this.intToTextTransformer = new GenericTransformations(alphabet.availableCharacters());
        }

        @Override
        public Alphabet alphabet() {
            return alphabet;
        }

        @Override
        public int[] transform(String data) {
            return textToIntTransformer.transform(data);
        }

        @Override
        public String transform(int[] data) {
            return intToTextTransformer.transform(data);
        }
    }

    public static class AESCBCNoPaddingPRF implements PseudoRandomFunction {

        private static final String CIPHER_ALGORITHM = "AES/CBC/NoPadding";

        private Key key;
        private byte[] iv;

        public AESCBCNoPaddingPRF(String keyString) {
            this(AES.getKey(keyString));
        }

        public AESCBCNoPaddingPRF(String keyString, int size) {
            this(AES.getKey(keyString, size));
        }

        public AESCBCNoPaddingPRF(byte[] keyBytes) {
            this(AES.getKey(keyBytes));
        }

        public AESCBCNoPaddingPRF(byte[] keyBytes, int size) {
            this(AES.getKey(keyBytes, size));
        }

        public AESCBCNoPaddingPRF(Key key) {
            this.key = key;
            this.iv = new byte[16];
            for (int i = 0; i < iv.length; i++) {
                iv[i] = (byte) 0x00;
            }
        }

        @SneakyThrows
        public byte[] apply(byte[] plain) {
            val cipher = javax.crypto.Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
            val result = cipher.doFinal(plain);
            return Arrays.copyOfRange(result, result.length - iv.length, result.length);
        }
    }
}
