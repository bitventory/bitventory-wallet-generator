/**
 * Copyright 2011 Ken Burford
 * 
 * This file is part of the Bitventory.com Wallet Generator.
 * 
 * The Bitventory.com Wallet Generator is free software:
 * you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * 
 * The Bitventory.com Wallet Generator  is distributed in the hope
 * that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with the Bitventory.com Wallet Generator. 
 * If not, see <http://www.gnu.org/licenses/>.
**/

/**
 * Copyright 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.bitcoin.core;

import org.bouncycastle.util.encoders.Hex;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * A Sha256Hash just wraps a byte[] so that equals and hashcode work correctly, allowing it to be used as keys in a
 * map. It also checks that the length is correct and provides a bit more type safety.
 */
public class Sha256Hash implements Serializable {
    private static final long serialVersionUID = 3778897922647016546L;

    private byte[] bytes;

    public static final Sha256Hash ZERO_HASH = new Sha256Hash(new byte[32]);

    /** Creates a Sha256Hash by wrapping the given byte array. It must be 32 bytes long. */
    public Sha256Hash(byte[] bytes) {
        assert bytes.length == 32;
        this.bytes = bytes;
    }

    /** Creates a Sha256Hash by decoding the given hex string. It must be 64 characters long. */
    public Sha256Hash(String string) {
        assert string.length() == 64;
        this.bytes = Hex.decode(string);
    }

    /** Calculates the (one-time) hash of contents and returns it as a new wrapped hash. */
    public static Sha256Hash create(byte[] contents) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return new Sha256Hash(digest.digest(contents));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);  // Cannot happen.
        }
    }

    /** Returns true if the hashes are equal. */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Sha256Hash)) return false;
        return Arrays.equals(bytes, ((Sha256Hash)other).bytes);
    }

    /**
     * Hash code of the byte array as calculated by {@link Arrays#hashCode()}. Note the difference between a SHA256
     * secure bytes and the type of quick/dirty bytes used by the Java hashCode method which is designed for use in
     * bytes tables.
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(bytes);
    }

    @Override
    public String toString() {
        return Utils.bytesToHexString(bytes);
    }

    /** Returns the bytes interpreted as a positive integer. */
    public BigInteger toBigInteger() {
        return new BigInteger(1, bytes);
    }

    public byte[] getBytes() {
        return bytes;
    }

    public Sha256Hash duplicate() {
        return new Sha256Hash(bytes);
    }
}
