package com.github.charlemaznable.core.codec;

import com.github.charlemaznable.core.lang.Str;
import lombok.val;

import java.util.Arrays;

import static com.github.charlemaznable.core.codec.Bytes.bytes;
import static com.github.charlemaznable.core.codec.Bytes.string;
import static com.github.charlemaznable.core.lang.Str.removeLastLetters;
import static java.lang.Math.min;
import static java.lang.String.format;
import static java.lang.System.arraycopy;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public final class Base64 {

    private Base64() {}

    public static String base64(byte[] bytes) {
        return base64(bytes, Format.STANDARD);
    }

    public static String padding(String s) {
        return Str.padding(s, '=', (4 - s.length() % 4) % 4);
    }

    public static String purify(String s) {
        return removeLastLetters(s, '=');
    }

    public static String base64(String s) {
        return base64(s, Format.STANDARD);
    }

    public static String base64(String s, Format format) {
        return base64(bytes(s), format);
    }

    public static String base64(byte[] bytes, Format format) {
        switch (format) {
            case STANDARD:
                return ApacheBase64.encodeBase64String(bytes);
            case URL_SAFE:
                return purify(ApacheBase64.encodeBase64URLSafeString(bytes));
            case PURIFIED:
                return purify(ApacheBase64.encodeBase64String(bytes));
        }
        return null;
    }

    public static byte[] unBase64(String value) {
        return ApacheBase64.decodeBase64(padding(value));
    }

    public static String unBase64AsString(String value) {
        return string(unBase64(value));
    }

    public enum Format {

        STANDARD,
        // URL安全(将Base64中的URL非法字符'+'和'/'转为'-'和'_', 见RFC3548)
        URL_SAFE,
        // 去除末尾=号
        PURIFIED,
    }

    @SuppressWarnings("SameParameterValue")
    private abstract static class ApacheBaseNCodec {

        public static final int MIME_CHUNK_SIZE = 76;
        protected static final int MASK_8BITS = 0xff;
        protected static final byte PAD_DEFAULT = '='; // Allow static access to default
        static final int EOF = -1;
        private static final int DEFAULT_BUFFER_RESIZE_FACTOR = 2;
        private static final int DEFAULT_BUFFER_SIZE = 8192;
        protected final int lineLength;
        private final int unencodedBlockSize;
        private final int encodedBlockSize;
        private final int chunkSeparatorLength;
        protected byte pad = PAD_DEFAULT; // instance variable just in case it needs to vary later

        protected ApacheBaseNCodec(final int unencodedBlockSize, final int encodedBlockSize,
                                   final int lineLength, final int chunkSeparatorLength) {
            this(unencodedBlockSize, encodedBlockSize, lineLength, chunkSeparatorLength, PAD_DEFAULT);
        }

        protected ApacheBaseNCodec(final int unencodedBlockSize, final int encodedBlockSize,
                                   final int lineLength, final int chunkSeparatorLength, final byte pad) {
            this.unencodedBlockSize = unencodedBlockSize;
            this.encodedBlockSize = encodedBlockSize;
            val useChunking = lineLength > 0 && chunkSeparatorLength > 0;
            this.lineLength = useChunking ? (lineLength / encodedBlockSize) * encodedBlockSize : 0;
            this.chunkSeparatorLength = chunkSeparatorLength;
            this.pad = pad;
        }

        int available(final Context context) {  // package protected for access from I/O streams
            return nonNull(context.buffer) ? context.pos - context.readPos : 0;
        }

        protected int getDefaultBufferSize() {
            return DEFAULT_BUFFER_SIZE;
        }

        private byte[] resizeBuffer(final Context context) {
            if (isNull(context.buffer)) {
                context.buffer = new byte[getDefaultBufferSize()];
                context.pos = 0;
                context.readPos = 0;
            } else {
                val b = new byte[context.buffer.length * DEFAULT_BUFFER_RESIZE_FACTOR];
                arraycopy(context.buffer, 0, b, 0, context.buffer.length);
                context.buffer = b;
            }
            return context.buffer;
        }

        protected byte[] ensureBufferSize(final int size, final Context context) {
            if (isNull(context.buffer) || (context.buffer.length < context.pos + size)) {
                return resizeBuffer(context);
            }
            return context.buffer;
        }

        @SuppressWarnings("UnusedReturnValue")
        int readResults(final byte[] b, final int bPos, final int bAvail, final Context context) {
            if (nonNull(context.buffer)) {
                val len = min(available(context), bAvail);
                arraycopy(context.buffer, context.readPos, b, bPos, len);
                context.readPos += len;
                if (context.readPos >= context.pos) {
                    context.buffer = null; // so hasData() will return false, and this method can return -1
                }
                return len;
            }
            return context.eof ? EOF : 0;
        }

        public byte[] decode(final String pArray) {
            return decode(bytes(pArray));
        }

        public byte[] decode(final byte[] pArray) {
            if (isNull(pArray) || pArray.length == 0) {
                return pArray;
            }
            val context = new Context();
            decode(pArray, 0, pArray.length, context);
            decode(pArray, 0, EOF, context); // Notify decoder of EOF.
            val result = new byte[context.pos];
            readResults(result, 0, result.length, context);
            return result;
        }

        public byte[] encode(final byte[] pArray) {
            if (isNull(pArray) || pArray.length == 0) {
                return pArray;
            }
            val context = new Context();
            encode(pArray, 0, pArray.length, context);
            encode(pArray, 0, EOF, context); // Notify encoder of EOF.
            val buf = new byte[context.pos - context.readPos];
            readResults(buf, 0, buf.length, context);
            return buf;
        }

        // package protected for access from I/O streams
        abstract void encode(byte[] pArray, int i, int length, Context context);

        // package protected for access from I/O streams
        abstract void decode(byte[] pArray, int i, int length, Context context);

        protected abstract boolean isInAlphabet(byte value);

        protected boolean containsAlphabetOrPad(final byte[] arrayOctet) {
            if (isNull(arrayOctet)) {
                return false;
            }
            for (val element : arrayOctet) {
                if (pad == element || isInAlphabet(element)) {
                    return true;
                }
            }
            return false;
        }

        public long getEncodedLength(final byte[] pArray) {
            // Calculate non-chunked size - rounded up to allow for padding
            // cast to long is needed to avoid possibility of overflow
            long len = ((pArray.length + unencodedBlockSize - 1) / unencodedBlockSize) * (long) encodedBlockSize;
            if (lineLength > 0) { // We're using chunking
                // Round up to nearest multiple
                len += ((len + lineLength - 1) / lineLength) * chunkSeparatorLength;
            }
            return len;
        }

        static class Context {

            int ibitWorkArea;

            long lbitWorkArea;

            byte[] buffer;

            int pos;

            int readPos;

            boolean eof;

            int currentLinePos;

            int modulus;

            Context() {}

            @SuppressWarnings("boxing") // OK to ignore boxing here
            @Override
            public String toString() {
                return format("%s[buffer=%s, currentLinePos=%s, eof=%s, ibitWorkArea=%s, lbitWorkArea=%s, " +
                                "modulus=%s, pos=%s, readPos=%s]", this.getClass().getSimpleName(), Arrays.toString(buffer),
                        currentLinePos, eof, ibitWorkArea, lbitWorkArea, modulus, pos, readPos);
            }
        }
    }

    private static final class ApacheBase64 extends ApacheBaseNCodec {

        static final byte[] CHUNK_SEPARATOR = {'\r', '\n'};
        private static final int BITS_PER_ENCODED_BYTE = 6;
        private static final int BYTES_PER_UNENCODED_BLOCK = 3;
        private static final int BYTES_PER_ENCODED_BLOCK = 4;
        private static final byte[] STANDARD_ENCODE_TABLE = {
                'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
                'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
                'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
                'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'
        };

        private static final byte[] URL_SAFE_ENCODE_TABLE = {
                'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
                'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
                'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
                'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-', '_'
        };

        private static final byte[] DECODE_TABLE = {
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, 62, -1, 63, 52, 53, 54,
                55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -1, -1, -1, -1, 0, 1, 2, 3, 4,
                5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23,
                24, 25, -1, -1, -1, -1, 63, -1, 26, 27, 28, 29, 30, 31, 32, 33, 34,
                35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51
        };

        private static final int MASK_6BITS = 0x3f;

        private final byte[] encodeTable;

        private final byte[] decodeTable = DECODE_TABLE;

        private final byte[] lineSeparator;

        private final int decodeSize;

        private final int encodeSize;

        public ApacheBase64() {
            this(0);
        }

        public ApacheBase64(final boolean urlSafe) {
            this(MIME_CHUNK_SIZE, CHUNK_SEPARATOR, urlSafe);
        }

        public ApacheBase64(final int lineLength) {
            this(lineLength, CHUNK_SEPARATOR);
        }

        public ApacheBase64(final int lineLength, final byte[] lineSeparator) {
            this(lineLength, lineSeparator, false);
        }

        public ApacheBase64(final int lineLength, final byte[] lineSeparator, final boolean urlSafe) {
            super(BYTES_PER_UNENCODED_BLOCK, BYTES_PER_ENCODED_BLOCK,
                    lineLength,
                    isNull(lineSeparator) ? 0 : lineSeparator.length);
            if (nonNull(lineSeparator)) {
                if (containsAlphabetOrPad(lineSeparator)) {
                    val sep = string(lineSeparator);
                    throw new IllegalArgumentException("lineSeparator must not contain base64 characters: [" + sep + "]");
                }
                if (lineLength > 0) { // null line-sep forces no chunking rather than throwing IAE
                    this.encodeSize = BYTES_PER_ENCODED_BLOCK + lineSeparator.length;
                    this.lineSeparator = new byte[lineSeparator.length];
                    arraycopy(lineSeparator, 0, this.lineSeparator, 0, lineSeparator.length);
                } else {
                    this.encodeSize = BYTES_PER_ENCODED_BLOCK;
                    this.lineSeparator = null;
                }
            } else {
                this.encodeSize = BYTES_PER_ENCODED_BLOCK;
                this.lineSeparator = null;
            }
            this.decodeSize = this.encodeSize - 1;
            this.encodeTable = urlSafe ? URL_SAFE_ENCODE_TABLE : STANDARD_ENCODE_TABLE;
        }

        public static byte[] encodeBase64(final byte[] binaryData) {
            return encodeBase64(binaryData, false);
        }

        public static String encodeBase64String(final byte[] binaryData) {
            return string(encodeBase64(binaryData));
        }

        public static byte[] encodeBase64URLSafe(final byte[] binaryData) {
            return encodeBase64(binaryData, false, true);
        }

        public static String encodeBase64URLSafeString(final byte[] binaryData) {
            return string(encodeBase64URLSafe(binaryData));
        }

        public static byte[] encodeBase64(final byte[] binaryData, final boolean isChunked) {
            return encodeBase64(binaryData, isChunked, false);
        }

        public static byte[] encodeBase64(final byte[] binaryData, final boolean isChunked, final boolean urlSafe) {
            return encodeBase64(binaryData, isChunked, urlSafe, Integer.MAX_VALUE);
        }

        public static byte[] encodeBase64(final byte[] binaryData, final boolean isChunked,
                                          final boolean urlSafe, final int maxResultSize) {
            if (isNull(binaryData) || binaryData.length == 0) {
                return binaryData;
            }

            // Create this so can use the super-class method
            // Also ensures that the same roundings are performed by the ctor and the code
            val b64 = isChunked ? new ApacheBase64(urlSafe) : new ApacheBase64(0, CHUNK_SEPARATOR, urlSafe);
            val len = b64.getEncodedLength(binaryData);
            if (len > maxResultSize) {
                throw new IllegalArgumentException("Input array too big, the output array would be bigger (" +
                        len +
                        ") than the specified maximum size of " +
                        maxResultSize);
            }
            return b64.encode(binaryData);
        }

        public static byte[] decodeBase64(final String base64String) {
            return new ApacheBase64().decode(base64String);
        }

        @Override
        void encode(final byte[] in, int inPos, final int inAvail, final Context context) {
            if (context.eof) {
                return;
            }
            // inAvail < 0 is how we're informed of EOF in the underlying data we're
            // encoding.
            if (inAvail < 0) {
                encodeInAvailLessThen0(context);
            } else {
                for (int i = 0; i < inAvail; i++) {
                    val buffer = ensureBufferSize(encodeSize, context);
                    context.modulus = (context.modulus + 1) % BYTES_PER_UNENCODED_BLOCK;
                    int b = (int) in[inPos++];
                    if (b < 0) {
                        b += 256;
                    }
                    context.ibitWorkArea = (context.ibitWorkArea << 8) + b; //  BITS_PER_BYTE
                    // 3 bytes = 24 bits = 4 * 6 bits to extract
                    if (0 == context.modulus) encodeContextModulus0(context, buffer);
                }
            }
        }

        private void encodeInAvailLessThen0(final Context context) {
            context.eof = true;
            if (0 == context.modulus && lineLength == 0) {
                return; // no leftovers to process and not using chunking
            }
            val buffer = ensureBufferSize(encodeSize, context);
            val savedPos = context.pos;
            switch (context.modulus) { // 0-2
                case 0: // nothing to do here
                    break;

                case 1: // 8 bits = 6 + 2
                    encodeContextModulus1(context, buffer);
                    break;

                case 2: // 16 bits = 6 + 6 + 4
                    encodeContextModulus2(context, buffer);
                    break;

                default:
                    throw new IllegalStateException("Impossible modulus " + context.modulus);
            }
            context.currentLinePos += context.pos - savedPos; // keep track of current line position
            // if currentPos == 0 we are at the start of a line, so don't add CRLF
            if (lineLength > 0 && context.currentLinePos > 0) {
                arraycopy(lineSeparator, 0, buffer, context.pos, lineSeparator.length);
                context.pos += lineSeparator.length;
            }
        }

        private void encodeContextModulus1(final Context context, byte[] buffer) {
            // top 6 bits:
            buffer[context.pos++] = encodeTable[(context.ibitWorkArea >> 2) & MASK_6BITS];
            // remaining 2:
            buffer[context.pos++] = encodeTable[(context.ibitWorkArea << 4) & MASK_6BITS];
            // URL-SAFE skips the padding to further reduce size.
            if (encodeTable == STANDARD_ENCODE_TABLE) {
                buffer[context.pos++] = pad;
                buffer[context.pos++] = pad;
            }
        }

        private void encodeContextModulus2(final Context context, byte[] buffer) {
            buffer[context.pos++] = encodeTable[(context.ibitWorkArea >> 10) & MASK_6BITS];
            buffer[context.pos++] = encodeTable[(context.ibitWorkArea >> 4) & MASK_6BITS];
            buffer[context.pos++] = encodeTable[(context.ibitWorkArea << 2) & MASK_6BITS];
            // URL-SAFE skips the padding to further reduce size.
            if (encodeTable == STANDARD_ENCODE_TABLE) {
                buffer[context.pos++] = pad;
            }
        }

        private void encodeContextModulus0(final Context context, byte[] buffer) {
            buffer[context.pos++] = encodeTable[(context.ibitWorkArea >> 18) & MASK_6BITS];
            buffer[context.pos++] = encodeTable[(context.ibitWorkArea >> 12) & MASK_6BITS];
            buffer[context.pos++] = encodeTable[(context.ibitWorkArea >> 6) & MASK_6BITS];
            buffer[context.pos++] = encodeTable[context.ibitWorkArea & MASK_6BITS];
            context.currentLinePos += BYTES_PER_ENCODED_BLOCK;
            if (lineLength > 0 && lineLength <= context.currentLinePos) {
                arraycopy(lineSeparator, 0, buffer, context.pos, lineSeparator.length);
                context.pos += lineSeparator.length;
                context.currentLinePos = 0;
            }
        }

        @Override
        void decode(final byte[] in, int inPos, final int inAvail, final Context context) {
            if (context.eof) {
                return;
            }
            if (inAvail < 0) {
                context.eof = true;
            }
            for (int i = 0; i < inAvail; i++) {
                val buffer = ensureBufferSize(decodeSize, context);
                val b = in[inPos++];
                if (b == pad) {
                    // We're done.
                    context.eof = true;
                    break;
                } else {
                    decodeNotDone(context, buffer, b);
                }
            }

            // Two forms of EOF as far as base64 decoder is concerned: actual
            // EOF (-1) and first time '=' character is encountered in stream.
            // This approach makes the '=' padding characters completely optional.
            if (context.eof && context.modulus != 0) {
                val buffer = ensureBufferSize(decodeSize, context);

                // We have some spare bits remaining
                // Output all whole multiples of 8 bits and ignore the rest
                switch (context.modulus) {
                    //case 0 : // impossible, as excluded above
                    case 1: // 6 bits - ignore entirely
                        break;
                    case 2: // 12 bits = 8 + 4
                        context.ibitWorkArea = context.ibitWorkArea >> 4; // dump the extra 4 bits
                        buffer[context.pos++] = (byte) ((context.ibitWorkArea) & MASK_8BITS);
                        break;
                    case 3: // 18 bits = 8 + 8 + 2
                        context.ibitWorkArea = context.ibitWorkArea >> 2; // dump 2 bits
                        buffer[context.pos++] = (byte) ((context.ibitWorkArea >> 8) & MASK_8BITS);
                        buffer[context.pos++] = (byte) ((context.ibitWorkArea) & MASK_8BITS);
                        break;
                    default:
                        throw new IllegalStateException("Impossible modulus " + context.modulus);
                }
            }
        }

        private void decodeNotDone(Context context, byte[] buffer, byte b) {
            if (b >= 0 && b < DECODE_TABLE.length) {
                val result = (int) DECODE_TABLE[b];
                if (result >= 0) {
                    context.modulus = (context.modulus + 1) % BYTES_PER_ENCODED_BLOCK;
                    context.ibitWorkArea = (context.ibitWorkArea << BITS_PER_ENCODED_BYTE) + result;
                    if (context.modulus == 0) {
                        buffer[context.pos++] = (byte) ((context.ibitWorkArea >> 16) & MASK_8BITS);
                        buffer[context.pos++] = (byte) ((context.ibitWorkArea >> 8) & MASK_8BITS);
                        buffer[context.pos++] = (byte) (context.ibitWorkArea & MASK_8BITS);
                    }
                }
            }
        }

        @Override
        protected boolean isInAlphabet(final byte octet) {
            return octet >= 0 && octet < decodeTable.length && decodeTable[octet] != -1;
        }
    }
}
