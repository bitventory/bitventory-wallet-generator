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

package com.google.bitcoin.store;

import com.google.bitcoin.core.*;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * Keeps {@link com.google.bitcoin.core.StoredBlock}s in memory. Used primarily for unit testing.
 */
public class MemoryBlockStore implements BlockStore {
    private Map<Sha256Hash, StoredBlock> blockMap;
    private StoredBlock chainHead;

    public MemoryBlockStore(NetworkParameters params) {
        blockMap = new HashMap<Sha256Hash, StoredBlock>();
        // Insert the genesis block.
        try {
            Block genesisHeader = params.genesisBlock.cloneAsHeader();
            StoredBlock storedGenesis = new StoredBlock(genesisHeader, genesisHeader.getWork(), 0);
            put(storedGenesis);
            setChainHead(storedGenesis);
        } catch (BlockStoreException e) {
            throw new RuntimeException(e);  // Cannot happen.
        } catch (VerificationException e) {
            throw new RuntimeException(e);  // Cannot happen.
        }
    }

    public synchronized void put(StoredBlock block) throws BlockStoreException {
        Sha256Hash hash = block.getHeader().getHash();
        blockMap.put(hash, block);
    }

    public synchronized StoredBlock get(Sha256Hash hash) throws BlockStoreException {
        return blockMap.get(hash);
    }

    public StoredBlock getChainHead() {
        return chainHead;
    }

    public void setChainHead(StoredBlock chainHead) throws BlockStoreException {
        this.chainHead = chainHead;
    }
}
