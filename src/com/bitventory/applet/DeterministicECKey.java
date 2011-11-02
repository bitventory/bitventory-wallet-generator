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

package com.bitventory.applet;

import com.google.bitcoin.core.ECKey;

import java.security.SecureRandom;

/**
 * Contains a deterministically generated keypair which is usable as an
 * ordinary ECKey in bitcoinj.
 * 
 * @author Ken Burford
 *
 */
public class DeterministicECKey extends ECKey {
	
	private static final long serialVersionUID = 1L;
	
	public DeterministicECKey(SecureRandom prng) {
		super(prng);
	} // DeterministicECKey
	
} // DeterministicECKey
