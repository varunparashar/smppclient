package org.bulatnig.smpp.util;

import java.io.UnsupportedEncodingException;

/**
 * Converts java types to byte array according to SMPP protocol.
 * You should remember, that all SMPP simple numeric types are unsigned,
 * but Java types are always signed.
 * <p/>
 * Not thread safe.
 *
 * @author Bulat Nigmatullin
 */
public class SmppByteBuffer {

    /**
     * PDU byte size.
     */
    public static final byte BYTE_SZ = 1;
    /**
     * PDU short type.
     */
    public static final byte SHORT_SZ = 2;
    /**
     * PDU Integer type size.
     */
    public static final byte INT_SZ = 4;

    /**
     * PDU byte max size.
     */
    public static final int BYTE_MAX_VAL = 256;
    /**
     * PDU short max size.
     */
    public static final int SHORT_MAX_VAL = 65536;
    /**
     * PDU Integer max size.
     */
    public static final long INT_MAX_VAL = 4294967296L;

    /**
     * Octet mask.
     */
    public static final int OCTET_MASK = 0xff;
    /**
     * Half octet mask.
     */
    public static final int HALF_OCTET_MASK = 0x0f;


    /**
     * C-Octet String encoding and default Octet String encoding.
     */
    private static final String ASCII = "US-ASCII";

    /**
     * Empty byte array.
     */
    private static final byte[] EMPTY = new byte[0];

    /**
     * SMPP NULL character to append at the end of C-Octet String.
     */
    private static final byte[] ZERO = new byte[]{0};

    /**
     * Byte buffer.
     */
    private byte[] buffer;

    /**
     * Create empty buffer.
     */
    public SmppByteBuffer() {
        buffer = EMPTY;
    }

    /**
     * Create buffer based on provided array.
     *
     * @param b byte array
     */
    public SmppByteBuffer(final byte[] b) {
        buffer = b;
    }

    /**
     * Возвращает массив байтов.
     *
     * @return массив байтов
     */
    public byte[] array() {
        return buffer;
    }

    /**
     * Возвращает длину массива.
     *
     * @return длина массива
     */
    public int length() {
        return buffer.length;
    }

    /**
     * Добавляет байты в массив.
     *
     * @param bytes byte array
     */
    public void appendBytes(final byte[] bytes) {
        byte[] newBuffer = new byte[buffer.length + bytes.length];
        System.arraycopy(buffer, 0, newBuffer, 0, buffer.length);
        System.arraycopy(bytes, 0, newBuffer, buffer.length, bytes.length);
        buffer = newBuffer;
    }

    /**
     * Добавляет переменную типа byte в массив.
     * Значение переменной должно быть в диапазоне от 0 до 255 включительно.
     *
     * @param value byte value to be appended
     * @return  this buffer
     * @throws IllegalArgumentException задан неверный параметр
     */
    public SmppByteBuffer appendByte(final int value) throws IllegalArgumentException {
        if (value >= 0 && value < BYTE_MAX_VAL) {
            appendBytes(new byte[] {(byte) value});
        } else
            throw new IllegalArgumentException("Byte value should be between 0 and 255.");
        return this;
    }

    /**
     * Добавляет переменную типа short в массив.
     * Значение переменной должно быть в диапазоне от 0 до 65535 включительно.
     *
     * @param data short-переменная
     * @throws WrongParameterException задан неверный параметр
     */
    public void appendShort(final int data) throws WrongParameterException {
        if (data >= SHORT_MAX_VAL) {
            throw new WrongParameterException("value too big for SMPP short field");
        }
        if (data < 0) {
            throw new WrongParameterException("value too small for SMPP short field");
        }
        short s = (short) data;
        byte[] shortBuf = new byte[SHORT_SZ];
        shortBuf[1] = (byte) (s & OCTET_MASK);
        shortBuf[0] = (byte) ((s >>> 8) & OCTET_MASK);
        appendBytes(shortBuf);
    }

    /**
     * Добавляет переменную типа int в массив.
     * Значение переменной должно быть в диапазоне от 0 до 4294967295 включительно.
     *
     * @param data short-переменная
     * @throws WrongParameterException задан неверный параметр
     */
    public void appendInt(final long data) throws WrongParameterException {
        if (data >= INT_MAX_VAL) {
            throw new WrongParameterException("value too big for SMPP int field");
        }
        if (data < 0) {
            throw new WrongParameterException("value too small for SMPP int field");
        }
        int i = (int) data;
        byte[] intBuf = new byte[INT_SZ];
        intBuf[3] = (byte) (i & OCTET_MASK);
        intBuf[2] = (byte) ((i >>> 8) & OCTET_MASK);
        intBuf[1] = (byte) ((i >>> 16) & OCTET_MASK);
        intBuf[0] = (byte) ((i >>> 24) & OCTET_MASK);
        appendBytes(intBuf);
    }

    /**
     * Добавляет строку C-String в массив.
     * null string allowed.
     *
     * @param cstring строка типа C-Octet (по протоколу SMPP)
     */
    public void appendCString(final String cstring) {
        if (cstring != null && cstring.length() > 0) {
            try {
                byte[] stringBuf = cstring.getBytes(ASCII);
                appendBytes(stringBuf);
            } catch (UnsupportedEncodingException e) {
                // omit it
            }
        }
        appendBytes(ZERO); // always append terminating ZERO
    }

    /**
     * Append string using US-ASCII charset.
     *
     * @param string string value, may be null
     */
    public void appendString(final String string) {
        appendString(string, ASCII);
    }

    /**
     * Append string using charset name.
     * Note: UTF-16(UCS2) uses Byte Order Mark at the head of string and
     * it may be unsupported by your Operator. So you should consider using
     * UTF-16BE or UTF-16LE instead of UTF-16.
     *
     * @param string      encoded string, null allowed
     * @param charsetName encoding character set name
     */
    public void appendString(final String string, final String charsetName) {
        if ((string != null) && (string.length() > 0)) {
            try {
                byte[] stringBuf = string.getBytes(charsetName);
                appendBytes(stringBuf);
            } catch (UnsupportedEncodingException e) {
                // omit it
            }
        }
    }

    /**
     * Удаляет один byte из массива и возвращает его.
     *
     * @return удаленный byte
     * @throws WrongLengthException буфер недостаточной длины для завершения операции
     */
    public short removeByte() throws WrongLengthException {
        byte result = 0;
        try {
            byte[] resBuff = removeBytes(BYTE_SZ).array();
            result = resBuff[0];
        } catch (WrongParameterException wpe) {
            // omit it
        }
        if (result > -1) {
            return result;
        } else {
            return (short) (BYTE_MAX_VAL + result);
        }
    }

    /**
     * Удаляет один short из массива и возвращает его.
     *
     * @return удаленный short
     * @throws WrongLengthException буфер недостаточной длины для завершения операции
     */
    public int removeShort() throws WrongLengthException {
        int result = 0;
        try {
            byte[] resBuff = removeBytes(SHORT_SZ).array();
            result |= resBuff[0] & OCTET_MASK;
            result <<= 8;
            result |= resBuff[1] & OCTET_MASK;
        } catch (WrongParameterException wpe) {
            // omit it
        }
        if (result > -1) {
            return result;
        } else {
            return SHORT_MAX_VAL + result;
        }
    }

    /**
     * Удаляет один int из массива и возвращает его.
     *
     * @return удаленные int
     * @throws WrongLengthException буфер недостаточной длины для завершения операции
     */
    public long removeInt() throws WrongLengthException {
        long result = readInt();
        try {
            removeBytes0(INT_SZ);
        } catch (WrongParameterException wpe) {
            // omit it
        }
        return result;
    }

    /**
     * Читает один int из массива и возвращает его.
     *
     * @return int
     * @throws WrongLengthException ошибка длины
     */
    public long readInt() throws WrongLengthException {
        if (length() >= INT_SZ) {
            long result = 0;
            result |= buffer[0] & OCTET_MASK;
            result <<= 8;
            result |= buffer[1] & OCTET_MASK;
            result <<= 8;
            result |= buffer[2] & OCTET_MASK;
            result <<= 8;
            result |= buffer[3] & OCTET_MASK;
            return result;
        } else {
            throw new WrongLengthException("buffer have not enough length");
        }
    }

    /**
     * Удаляет строку CString из массива и возращает строку.
     *
     * @return строка
     * @throws WrongLengthException ошибка длины
     */
    public String removeCString()
            throws WrongLengthException {
        int len = length();
        int zeroPos = 0;
        if (len == 0) {
            throw new WrongLengthException("buffer have not enough length");
        }
        while ((zeroPos < len) && (buffer[zeroPos] != 0)) {
            zeroPos++;
        }
        if (zeroPos < len) { // found terminating ZERO
            String result = "";
            if (zeroPos > 0) {
                try {
                    result = new String(buffer, 0, zeroPos, ASCII);
                } catch (UnsupportedEncodingException e) {
                    // omit it
                }
            }
            try {
                removeBytes0(zeroPos + 1);
            } catch (WrongParameterException wpe) {
                // omit it
            }
            return result;
        } else {
            throw new WrongLengthException("terminating ZERO not found");
        }
    }

    public String removeString(final int size) throws WrongLengthException {
        return removeString(size, ASCII);
    }

    public String removeString(final int size, final String charsetName) throws WrongLengthException {
        int len = length();
        if (len < size) {
            throw new WrongLengthException("buffer have not enough length");
        }
        String result = "";
        if (len > 0) {
            try {
                result = new String(buffer, 0, size, charsetName);
            } catch (UnsupportedEncodingException e) {
                // omit it
            }
            try {
                removeBytes0(size);
            } catch (WrongParameterException wpe) {
                // omit it
            }
        }
        return result;
    }

    /**
     * Removes bytes from buffer and returns them.
     *
     * @param count count of bytes to remove
     * @return removed bytes
     * @throws WrongLengthException    wrong length of bytes to remove
     * @throws WrongParameterException неверный параметр
     */
    public SmppByteBuffer removeBytes(final int count)
            throws WrongParameterException, WrongLengthException {
        SmppByteBuffer result = readBytes(count);
        removeBytes0(count);
        return result;
    }

    /**
     * Just removes bytes from the buffer and doesnt return anything.
     *
     * @param count removed bytes
     * @throws WrongLengthException    wrong count of removed bytes
     * @throws WrongParameterException неверный параметр
     */
    private void removeBytes0(final int count) throws WrongParameterException,
            WrongLengthException {
        if (count < 0) {
            throw new WrongParameterException("unable remove negative count of bytes");
        } else if (count == 0) {
            return;
        }
        int lefts = length() - count;
        if (lefts > 0) {
            byte[] newBuf = new byte[lefts];
            System.arraycopy(buffer, count, newBuf, 0, lefts);
            buffer = newBuf;
        } else {
            buffer = EMPTY;
        }
    }

    /**
     * Read bytes.
     *
     * @param count count of bytes to read
     * @return readed bytes
     * @throws WrongLengthException wrong count of bytes to read
     */
    public SmppByteBuffer readBytes(final int count) throws WrongLengthException {
        if (count > 0) {
            if (length() >= count) {
                byte[] resBuf = new byte[count];
                System.arraycopy(buffer, 0, resBuf, 0, count);
                return new SmppByteBuffer(resBuf);
            } else {
                throw new WrongLengthException("buffer have not enough length");
            }
        } else {
            return new SmppByteBuffer();
        }
    }

    /**
     * Возвращает строку отображающую содержимое массива.
     *
     * @return содержимое массива
     */
    public String getHexDump() {
        String dump = "";
        byte[] b = array();
        int dataLen = b.length;
        for (int i = 0; i < dataLen; i++) {
            dump += Character.forDigit((b[i] >> 4) & HALF_OCTET_MASK, 16);
            dump += Character.forDigit(b[i] & HALF_OCTET_MASK, 16);
        }
        return dump;
    }
}