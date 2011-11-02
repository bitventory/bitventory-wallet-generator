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
 * Copyright 2011 bccapi.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bccapi.core;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * A Pseudo Random Number Generator based on SHA-256 which is wrapping
 * {@link SecureRandom}. This way we are certain that we use the same random
 * generator on all platforms, and can generate the same sequence of random
 * bytes from the same seed.
 */
public class PRNG extends SecureRandom {

   private static final long serialVersionUID = 5678497558585271430L;

   private byte[] _rootSeed;
   private byte[] _iterativeSeed;
   MessageDigest _digest;

   /**
    * Constructor based on an input seed.
    * 
    * @param seed
    *           The seed to use.
    * @throws NoSuchAlgorithmException
    */
   public PRNG(byte[] seed) throws NoSuchAlgorithmException {
      _rootSeed = seed;
      _iterativeSeed = new byte[32 - 1];
      _digest = MessageDigest.getInstance("SHA-256");
   }

   @Override
   public String getAlgorithm() {
      throw new RuntimeException("Not supported");
   }

   @Override
   public synchronized void setSeed(byte[] seed) {
      throw new RuntimeException("Not supported");
   }

   @Override
   public void setSeed(long seed) {
      // ignore
   }

   @Override
   public synchronized void nextBytes(byte[] bytes) {
      for (int i = 0; i < bytes.length; i++) {
         bytes[i] = nextByte();
      }
   }

   private byte nextByte() {
      _digest.update(_rootSeed);
      byte[] hash = _digest.digest(_iterativeSeed);
      _digest.reset();
      // Use the first 31 bytes as the next iterative seed
      System.arraycopy(hash, 0, _iterativeSeed, 0, _iterativeSeed.length);
      // Use the last byte as our random byte
      return hash[hash.length - 1];
   }

   @Override
   public byte[] generateSeed(int numBytes) {
      throw new RuntimeException("Not supported");
   }

   @Override
   public int nextInt() {
      throw new RuntimeException("Not supported");
   }

   @Override
   public int nextInt(int n) {
      throw new RuntimeException("Not supported");
   }

   @Override
   public long nextLong() {
      throw new RuntimeException("Not supported");
   }

   @Override
   public boolean nextBoolean() {
      throw new RuntimeException("Not supported");
   }

   @Override
   public float nextFloat() {
      throw new RuntimeException("Not supported");
   }

   @Override
   public double nextDouble() {
      throw new RuntimeException("Not supported");
   }

   @Override
   public synchronized double nextGaussian() {
      throw new RuntimeException("Not supported");
   }

}
