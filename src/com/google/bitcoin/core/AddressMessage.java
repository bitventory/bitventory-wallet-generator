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

package com.google.bitcoin.core;

import java.util.ArrayList;
import java.util.List;

public class AddressMessage extends Message {
    private static final long serialVersionUID = 8058283864924679460L;
    private static final long MAX_ADDRESSES = 1024;
    List<PeerAddress> addresses;

    AddressMessage(NetworkParameters params, byte[] payload, int offset) throws ProtocolException {
        super(params, payload, offset);
    }

    AddressMessage(NetworkParameters params, byte[] payload) throws ProtocolException {
        super(params, payload, 0);
    }

    @Override
    void parse() throws ProtocolException {
        long numAddresses = readVarInt();
        // Guard against ultra large messages that will crash us.
        if (numAddresses > MAX_ADDRESSES)
            throw new ProtocolException("Address message too large.");
        addresses = new ArrayList<PeerAddress>((int)numAddresses);
        for (int i = 0; i < numAddresses; i++) {
            PeerAddress addr = new PeerAddress(params, bytes, cursor, protocolVersion);
            addresses.add(addr);
            cursor += addr.getMessageSize();
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("addr: ");
        for (PeerAddress a : addresses) {
            builder.append(a.toString());
            builder.append(" ");
        }
        return builder.toString();
    }
}
