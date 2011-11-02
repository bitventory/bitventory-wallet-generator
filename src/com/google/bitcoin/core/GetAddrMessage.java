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

public class GetAddrMessage extends Message {
	public GetAddrMessage(NetworkParameters params) {
		super(params);
	}

	@Override
	void parse() throws ProtocolException {
    // TODO: Implement this.
	}
}
