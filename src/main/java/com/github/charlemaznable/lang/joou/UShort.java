/**
 * Copyright (c) 2009-2011, Lukas Eder, lukas.eder@gmail.com
 * All rights reserved.
 * <p>
 * This software is licensed to you under the Apache License, Version 2.0
 * (the "License"); You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p>
 * . Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * <p>
 * . Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * <p>
 * . Neither the name "jOOU" nor the names of its contributors may be
 * used to endorse or promote products derived from this software without
 * specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.github.charlemaznable.lang.joou;

/**
 * The <code>unsigned short</code> type
 *
 * @author Lukas Eder
 */
public final class UShort extends UNumber implements Comparable<UShort> {

    /**
     * A constant holding the minimum value an <code>unsigned short</code> can
     * have, 0.
     */
    public static final int MIN_VALUE = 0x0000;
    /**
     * A constant holding the maximum value an <code>unsigned short</code> can
     * have, 2<sup>16</sup>-1.
     */
    public static final int MAX_VALUE = 0xffff;
    /**
     * Generated UID
     */
    private static final long serialVersionUID = -6821055240959745390L;
    /**
     * The value modelling the content of this <code>unsigned short</code>
     */
    private final int value;

    /**
     * Create an <code>unsigned short</code>
     *
     * @throws NumberFormatException If <code>value</code> is not in the range
     *                               of an <code>unsigned short</code>
     */
    public UShort(int value) throws NumberFormatException {
        this.value = value;
        rangeCheck();
    }

    /**
     * Create an <code>unsigned short</code> by masking it with
     * <code>0xFFFF</code> i.e. <code>(short) -1</code> becomes
     * <code>(ushort) 65535</code>
     */
    public UShort(short value) {
        this.value = value & MAX_VALUE;
    }

    /**
     * Create an <code>unsigned short</code>
     *
     * @throws NumberFormatException If <code>value</code> does not contain a
     *                               parsable <code>unsigned short</code>.
     */
    public UShort(String value) throws NumberFormatException {
        this.value = Integer.parseInt(value);
        rangeCheck();
    }

    /**
     * Create an <code>unsigned short</code>
     *
     * @throws NumberFormatException If <code>value</code> does not contain a
     *                               parsable <code>unsigned short</code>.
     * @see com.github.charlemaznable.lang.joou.UShort#UShort(String)
     */
    public static UShort valueOf(String value) throws NumberFormatException {
        return new UShort(value);
    }

    /**
     * Create an <code>unsigned short</code> by masking it with
     * <code>0xFFFF</code> i.e. <code>(short) -1</code> becomes
     * <code>(ushort) 65535</code>
     *
     * @see com.github.charlemaznable.lang.joou.UShort#UShort(short)
     */
    public static UShort valueOf(short value) {
        return new UShort(value);
    }

    private void rangeCheck() throws NumberFormatException {
        if (value < MIN_VALUE || value > MAX_VALUE) {
            throw new NumberFormatException("Value is out of range : " + value);
        }
    }

    @Override
    public int intValue() {
        return value;
    }

    @Override
    public long longValue() {
        return value;
    }

    @Override
    public float floatValue() {
        return value;
    }

    @Override
    public double doubleValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return Integer.valueOf(value).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof UShort && value == ((UShort) obj).value;
    }

    @Override
    public String toString() {
        return Integer.valueOf(value).toString();
    }

    @Override
    public int compareTo(UShort o) {
        return (value < o.value ? -1 : (value == o.value ? 0 : 1));
    }
}
