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

package com.bitventory.tools;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import org.bouncycastle.util.encoders.Hex;

import com.bitventory.core.Keys;
import com.bitventory.core.Tools;
import com.google.bitcoin.core.Address;
import com.google.bitcoin.core.ECKey;
import com.google.bitcoin.core.NetworkParameters;

/**
 * A tool to allow a user to enter their passphrase and shared secret in order
 * to generate the pool of keys used with the Bitventory e-wallet service.
 * 
 * Using this tool, it's possible to walk away from the service without
 * any kind of exit procedure. If Bitventory.com goes offline never to return,
 * this tool will let you access your funds through the official Bitcoin client.
 * 
 * @author Ken Burford
 *
 */
public class WalletGenerator {
	
	private byte[] hash;
	private byte[] secret;
	
	/**
	 * Initialize the WalletGenerator with the user's passphrase and
	 * shared secret, which are required to unlock.
	 * 
	 * @param email			User account email.
	 * @param passphrase	Wallet passphrase.
	 * @param secret		Shared secret between Bitventory and the user.
	 */
	public WalletGenerator(String email, String passphrase, String secret) {
		
		// Hash passphrase
		this.hash = Keys.generatePassphraseHash(email + passphrase);
		
		// Convert secret from readable hex string to byte array
		this.secret = getSecretAsBytes(secret);
		
	} // WalletGenerator
	
	/**
	 * Generate the specified number of keys.
	 * 
	 * @param total	Total keys to generate.
	 * 
	 * @return	List of keys.
	 */
	public List<ECKey> generateKeys(int total) {
		List<ECKey> keys = new ArrayList<ECKey>();
		
		// Get the origin token
		byte[] lastToken = Keys.hashThatBitch(this.secret);
		keys.add(Keys.createKey(this.hash, lastToken));
		
		// For all requested keys..
		for (int x = 1; x < total; x++) {
			lastToken = getNextToken(lastToken);
			keys.add(Keys.createKey(this.hash, lastToken));
		} // for
		
		return keys;
	} // generateKeys
	
	/**
	 * Generate the next token in sequence.
	 * 
	 * @param lastToken	The previous token.
	 * 
	 * @return	The new token!
	 */
	private byte[] getNextToken(byte[] lastToken) {
		byte[] input = Tools.concatBytes(this.secret, lastToken);
		return Keys.hashThatBitch(input);
	} // getNextToken
	
	/**
	 * Convert string hex string array to the byte array it represents.
	 * 
	 * @param secret	The shared secret as a string.
	 * 
	 * @return	The shared secret as a byte array.
	 */
	private byte[] getSecretAsBytes(String secret) {
		return Hex.decode(secret);
	} // getSecretAsBytes
	
	
	public static void main(String[] args) throws Exception {
		
		int defaultTotal = 5000;
		if (args.length == 0) {
			System.err.println("Usage: java -jar WalletGenerator.jar [NETWORK] [EMAIL] [PASSPHRASE] [SECRET]");
			System.err.println("       Optional final parameter specifying the number of keys.");
			System.err.println("       The default number generator is 5,000.");
			System.err.println("       [NETWORK] can be either prodnet or testnet.");
			System.exit(0);
		} else {
			if (args.length == 5) {
				try {
					defaultTotal = Integer.parseInt(args[4]);
				} catch (Exception ex) {
					System.err.println("Invalid number of keys to generate.");
					System.exit(1);
				} // try
			}
		}
		
		// If we get a passphrase and secret..
		if (args.length >= 4) {
			
			System.err.print("Generating wallet...");
			WalletGenerator wg = new WalletGenerator(args[1], args[2], args[3]);
			List<ECKey> keys = wg.generateKeys(defaultTotal);
			
			// Determine network type
			NetworkParameters network = NetworkParameters.prodNet();
			boolean isProd = true;
			if (args[0].toLowerCase().equals("testnet")) {
				System.err.println("Generating keys for chain: 'testnet'");
				network = NetworkParameters.testNet();
				isProd = false;
			} else System.err.println("Generating keys for chain: 'prodnet'");
			
			// Start file for public keys, one for private
			BufferedWriter writePublic =
				new BufferedWriter(new FileWriter("public.keys"));
			BufferedWriter writePrivate =
				new BufferedWriter(new FileWriter("private.keys"));
			BufferedWriter writePywallet =
				new BufferedWriter(new FileWriter("pywallet_doimport.sh"));
			writePywallet.write("#!/bin/bash");
			writePywallet.newLine();
			writePywallet.newLine();
			writePywallet.write("# Executing this script will import all of your Bitventory private keys into wallet.dat");
			writePywallet.newLine();
			String pyw = "./pywallet.py";
			if (!isProd) pyw += " --testnet";
			pyw += " --importprivkey=";
			for (ECKey key : keys) {
				
				// Write out private key
				String priv =
					key.getPrivateKeyEncoded(network).toString();
				writePrivate.write(priv);
				writePrivate.newLine();
				
				// Append to the pywallet import script
				writePywallet.write(pyw + priv);
				writePywallet.newLine();
				
				// Write out public key
				String pub =
					new Address(NetworkParameters.testNet(), key.getPubKeyHash()).toString();
				writePublic.write(pub);
				writePublic.newLine();
				
			} // for
			writePywallet.close();
			writePublic.close();
			writePrivate.close();
			
			System.err.println(" done.");
			System.err.println("Your keys are in:   public.keys , private.keys");
			
		}
		
	} // main
	
} // WalletGenerator
