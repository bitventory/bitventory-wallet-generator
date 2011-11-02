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

package com.bitventory.core;

import java.security.MessageDigest;

import com.bccapi.core.PRNG;
import com.bitventory.applet.DeterministicECKey;
import com.google.bitcoin.core.ECKey;

/**
 * Helper library for generating keys.
 * 
 * @author Ken Burford
 *
 */
public class Keys {
	
	/**
	 * The number of iterations to hash the passphrase before unlocking
	 * the current wallet.
	 */
	private static final int ITERATIONS = 1500000;
	
	/**
	 * Generate a hash of the given passphrase which requires a decent
	 * amount of CPU work.
	 * 
	 * @param passphrase	The passphrase to hash.
	 * 
	 * @return	The iteratively hashed passphrase as a byte array.
	 */
	public static byte[] generatePassphraseHash(String passphrase) {
		return Tools.hashPassword(passphrase.getBytes(), ITERATIONS);
	} // generatePassphraseHash
	
	/**
	 * Create a keypair using the user's passphrase and the given token.
	 * 
	 * @param token		The token to use for key generation.
	 * 
	 * @return	The user's keypair.
	 */
	public static ECKey createKey(byte[] hash, byte[] token) {
		try {
			byte[] seedBytes = Tools.concatBytes(hash, token);
			return new DeterministicECKey(new PRNG(seedBytes));
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		} // try
	} // createKey
	
	/**
	 * Run the hashing function necessary to build the current token.
	 * 
	 * @param secret	The input value to hash
	 * 
	 * @return	The newly created token.
	 */
	public static byte[] hashThatBitch(byte[] input) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-512");
			md.update(input);
			return md.digest();
		} catch (Exception ex) {
			ex.printStackTrace();
		} // try
		return null;
	} // hashThatBitch
	
} // Keys
