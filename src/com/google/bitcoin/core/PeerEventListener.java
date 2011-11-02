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

/**
 * Implementing a PeerEventListener allows you to learn when significant Peer communication
 * has occurred.
 * 
 * <p>Methods are called with the event listener object locked so your
 * implementation does not have to be thread safe. 
 *  
 * @author miron@google.com (Miron Cuperman a.k.a devrandom)
 * 
 */
public interface PeerEventListener {
    /**
     * Called on a Peer thread when a block is received.
     * 
     * <p>The block may have transactions or may be a header only once getheaders is implemented.
     *
     * @param peer the peer receiving the block
     * @param block the downloaded block
     * @param blocksLeft the number of blocks left to download
     */
    public void onBlocksDownloaded(Peer peer, Block block, int blocksLeft);

    /**
     * Called when a download is started with the initial number of blocks to be downloaded.
     * 
     * @param peer the peer receiving the block
     * @param blocksLeft the number of blocks left to download
     */
    public void onChainDownloadStarted(Peer peer, int blocksLeft);
    
    /**
     * Called when a peer is connected
     * 
     * @param peer 
     * @param peerCount the total number of connected peers
     */
    public void onPeerConnected(Peer peer, int peerCount);
    
    /**
     * Called when a peer is disconnected
     * 
     * @param peer 
     * @param peerCount the total number of connected peers
     */
    public void onPeerDisconnected(Peer peer, int peerCount);
}
