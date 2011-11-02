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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.MessageDigest;
//import java.util.Calendar;
//import java.util.Date;
import java.util.Arrays;
import java.util.zip.InflaterInputStream;
import java.util.zip.DeflaterOutputStream;

/**
 * Useful tools for doing fun things.
 * 
 * @author Ken Burford
 *
 */
public class Tools {
	
	/**
	 * Serialize an object to a zipped byte array.
	 * 
	 * @param obj	The object to serialize.
	 * @return	Byte array of the serialized object.
	 */
	public static byte[] serializeToBytes(Object obj) {
		byte[] bytes = null;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DeflaterOutputStream zot = new DeflaterOutputStream(baos);
			ObjectOutputStream oot = new ObjectOutputStream(zot);
			oot.writeObject(obj);
			oot.close();
			bytes = baos.toByteArray();
			baos.close();
			return bytes;
		} catch (Exception ex) {
			System.err.println(ex.getMessage());
			ex.printStackTrace();
		} // try
		return null;
	} // serializeToBytes
	
	/**
	 * Deserialize an object from bytes back out to the native Java object.
	 * This assumes that the object to deserialize has been zipped.
	 * 
	 * @param bytes	Byte array to deserialize.
	 * @return	The object to return.
	 */
	public static Object deserializeFromBytes(byte[] bytes) {
		Object object = null;
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
			InflaterInputStream zin = new InflaterInputStream(bais);
			ObjectInputStream oin = new ObjectInputStream(zin);
			object = oin.readObject();
			oin.close();
			return object;
		} catch (Exception ex) {
			ex.printStackTrace();
		} // try
		return null;
	} // deserializeFromBytes
	
	/**
	 * Hash the password used for seeding the {@link DeterministicECKeyPool}.
	 * This should be of a sufficient number of iterations as to add a
	 * substantial amount of CPU overhead, but without creating an unreasonable
	 * bottleneck to unlocking a wallet.
	 * 
	 * A few notes:
	 * 
	 * 1. The purpose of adding the CPU complexity to the hashing function is to
	 * add the needed CPU complexity into this stage, rather than through
	 * iterating through the key index. Since the server is tracking a shifting
	 * shared secret which changes for each key index, adding the CPU overhead
	 * at key generation would mean repeating it for each key. Instead, we
	 * add it here once, allowing it to be re-used for each subsequent key.
	 * 
	 * 2. The optimal number of iterations I'm selecting is 500,000. This allows
	 * it to run in <15 seconds on my netbook, and about 5 on my newer desktop.
	 * This seems like a reasonable number for now. It should perhaps be
	 * evaluated again later.
	 * 
	 * @param password		The password, in bytes, to hash.
	 * @param iterations	The number of hashing iterations to perform.
	 * 
	 * @return	The hashed password, with a degree of computational overhead
	 * 			required by the user for generating it.
	 */
	public static byte[] hashPassword(byte[] password, int iterations) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-512");
			//Calendar now = Calendar.getInstance();
			//System.out.println(now.get(now.SECOND) + ":" + now.get(now.MILLISECOND));
			for (int i = 0; i < iterations; i++) {
				//System.err.println(i);
				//System.out.println(password[0]);
				md.update(password);
				byte[] result = md.digest();
				password = result;
			} // for
			//now = Calendar.getInstance();
			//System.out.println(now.get(now.SECOND) + ":" + now.get(now.MILLISECOND));
			return password;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		} // try
	} // hashPassword
	
	/**
	 * Concatenate two byte arrays.
	 * 
	 * @param first		The first byte array.
	 * @param second	The second byte array.
	 * 
	 * @return	The concatenated byte arrays.
	 */
	public static byte[] concatBytes(byte[] first, byte[] second) {
		byte[] result = Arrays.copyOf(first, first.length + second.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	} // concatBytes
	
} // Tools
