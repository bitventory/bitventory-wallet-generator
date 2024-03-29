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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.bitcoin.core.Utils.*;

/**
 * A block is the foundation of the BitCoin system. It records a set of {@link Transaction}s together with
 * some data that links it into a place in the global block chain, and proves that a difficult calculation was done
 * over its contents. See the BitCoin technical paper for more detail on blocks.<p>
 *
 * To get a block, you can either build one from the raw bytes you can get from another implementation,
 * or request one specifically using {@link Peer#getBlock(Sha256Hash)}, or grab one from a downloaded {@link BlockChain}.
 */
public class Block extends Message {
    private static final Logger log = LoggerFactory.getLogger(Block.class);
    private static final long serialVersionUID = 2738848929966035281L;

    /** How many bytes are required to represent a block header. */
    public static final int HEADER_SIZE = 80;

    static final long ALLOWED_TIME_DRIFT = 2 * 60 * 60;  // Same value as official client.

    /** A value for difficultyTarget (nBits) that allows half of all possible hash solutions. Used in unit testing. */
    static final long EASIEST_DIFFICULTY_TARGET = 0x207fFFFFL;

    // For unit testing. If not zero, use this instead of the current time.
    static long fakeClock = 0;

    // Fields defined as part of the protocol format.
    private long version;
    private Sha256Hash prevBlockHash;
    private Sha256Hash merkleRoot;
    private long time;
    private long difficultyTarget;  // "nBits"
    private long nonce;

    /** If null, it means this object holds only the headers. */
    List<Transaction> transactions;
    /** Stores the hash of the block. If null, getHash() will recalculate it. */
    private transient Sha256Hash hash;

    /** Special case constructor, used for the genesis node, cloneAsHeader and unit tests. */
    Block(NetworkParameters params) {
        super(params);
        // Set up a few basic things. We are not complete after this though.
        version = 1;
        difficultyTarget = 0x1d07fff8L;
        time = System.currentTimeMillis() / 1000;
        prevBlockHash = Sha256Hash.ZERO_HASH;
    }

    /** Constructs a block object from the BitCoin wire format. */
    public Block(NetworkParameters params, byte[] payloadBytes) throws ProtocolException {
        super(params, payloadBytes, 0);
    }

    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        // This code is not actually necessary, as transient fields are initialized to the default value which is in
        // this case null. However it clears out a FindBugs warning and makes it explicit what we're doing.
        hash = null;
    }

    void parse() throws ProtocolException {
        version = readUint32();
        prevBlockHash = readHash();
        merkleRoot = readHash();
        time = readUint32();
        difficultyTarget = readUint32();
        nonce = readUint32();
        
        hash = new Sha256Hash(Utils.reverseBytes(Utils.doubleDigest(bytes, 0, cursor)));

        if (cursor == bytes.length) {
            // This message is just a header, it has no transactions.
            return;
        }

        int numTransactions = (int) readVarInt();
        transactions = new ArrayList<Transaction>(numTransactions);
        for (int i = 0; i < numTransactions; i++) {
            Transaction tx = new Transaction(params, bytes, cursor);
            transactions.add(tx);
            cursor += tx.getMessageSize();
        }
    }

    private void writeHeader(OutputStream stream) throws IOException {
        Utils.uint32ToByteStreamLE(version, stream);
        stream.write(Utils.reverseBytes(prevBlockHash.getBytes()));
        stream.write(Utils.reverseBytes(getMerkleRoot().getBytes()));
        Utils.uint32ToByteStreamLE(time, stream);
        Utils.uint32ToByteStreamLE(difficultyTarget, stream);
        Utils.uint32ToByteStreamLE(nonce, stream);
    }
    
    @Override
    void bitcoinSerializeToStream(OutputStream stream) throws IOException {
        writeHeader(stream);
        // We may only have enough data to write the header.
        if (transactions == null) return;
        stream.write(new VarInt(transactions.size()).encode());
        for (Transaction tx : transactions) {
            tx.bitcoinSerializeToStream(stream);
        }
    }

    /**
     * Calculates the block hash by serializing the block and hashing the resulting bytes.
     */
    private Sha256Hash calculateHash() {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            writeHeader(bos);
            return new Sha256Hash(Utils.reverseBytes(doubleDigest(bos.toByteArray())));
        } catch (IOException e) {
            throw new RuntimeException(e);  // Cannot happen.
        }
    }

    /**
     * Returns the hash of the block (which for a valid, solved block should be below the target) in the form seen
     * on the block explorer. If you call this on block 1 in the production chain, you will get
     * "00000000839a8e6886ab5951d76f411475428afc90947ee320161bbf18eb6048".
     */
    public String getHashAsString() {
        return getHash().toString();
    }

    /**
     * Returns the hash of the block (which for a valid, solved block should be below the target). Big endian.
     */
    public Sha256Hash getHash() {
        if (hash == null)
            hash = calculateHash();
        return hash;
    }

    /** The number that is one greater than the largest representable SHA-256 hash. */
    static private BigInteger LARGEST_HASH = BigInteger.ONE.shiftLeft(256);

    /**
     * Returns the work represented by this block.<p>
     *
     * Work is defined as the number of tries needed to solve a block in the average case. Consider a difficulty
     * target that covers 5% of all possible hash values. Then the work of the block will be 20. As the target gets
     * lower, the amount of work goes up.
     */
    public BigInteger getWork() throws VerificationException {
        BigInteger target = getDifficultyTargetAsInteger();
        return LARGEST_HASH.divide(target.add(BigInteger.ONE));
    }

    /** Returns a copy of the block, but without any transactions. */
    public Block cloneAsHeader() {
        Block block = new Block(params);
        block.nonce = nonce;
        block.prevBlockHash = prevBlockHash.duplicate();
        block.merkleRoot = getMerkleRoot().duplicate();
        block.version = version;
        block.time = time;
        block.difficultyTarget = difficultyTarget;
        block.transactions = null;
        block.hash = getHash().duplicate();
        return block;
    }

    /**
     * Returns a multi-line string containing a description of the contents of the block. Use for debugging purposes
     * only.
     */
    @Override
    public String toString() {
        StringBuffer s = new StringBuffer("v" + version + " block: \n" +
               "   previous block: " + prevBlockHash.toString() + "\n" +
               "   merkle root: " + getMerkleRoot().toString() + "\n" +
               "   time: [" + time + "] " + new Date(time * 1000).toString() + "\n" +
               "   difficulty target (nBits): " + difficultyTarget + "\n" +
               "   nonce: " + nonce + "\n");
        if (transactions != null && transactions.size() > 0) {
            s.append("   with ").append(transactions.size()).append(" transaction(s):\n");
            for (Transaction tx : transactions) {
                s.append(tx.toString());
            }
        }
        return s.toString();
    }

    /**
     * Finds a value of nonce that makes the blocks hash lower than the difficulty target. This is called mining,
     * but solve() is far too slow to do real mining with. It exists only for unit testing purposes and is not a part
     * of the public API.
     *
     * This can loop forever if a solution cannot be found solely by incrementing nonce. It doesn't change extraNonce.
     */
    void solve() {
        while (true) {
            try {
                // Is our proof of work valid yet?
                if (checkProofOfWork(false)) return;
                // No, so increment the nonce and try again.
                setNonce(getNonce() + 1);
            } catch (VerificationException e) {
                throw new RuntimeException(e);  // Cannot happen.
            }
        }
    }

    /**
     * Returns the difficulty target as a 256 bit value that can be compared to a SHA-256 hash. Inside a block the
     * target is represented using a compact form. If this form decodes to a value that is out of bounds,
     * an exception is thrown.
     */
    public BigInteger getDifficultyTargetAsInteger() throws VerificationException {
        BigInteger target = Utils.decodeCompactBits(difficultyTarget);
        if (target.compareTo(BigInteger.valueOf(0)) <= 0 || target.compareTo(params.proofOfWorkLimit) > 0)
            throw new VerificationException("Difficulty target is bad: " + target.toString());
        return target;
    }

    /** Returns true if the hash of the block is OK (lower than difficulty target). */
    private boolean checkProofOfWork(boolean throwException) throws VerificationException {
        // This part is key - it is what proves the block was as difficult to make as it claims
        // to be. Note however that in the context of this function, the block can claim to be
        // as difficult as it wants to be .... if somebody was able to take control of our network
        // connection and fork us onto a different chain, they could send us valid blocks with
        // ridiculously easy difficulty and this function would accept them.
        //
        // To prevent this attack from being possible, elsewhere we check that the difficultyTarget
        // field is of the right value. This requires us to have the preceeding blocks.
        BigInteger target = getDifficultyTargetAsInteger();

        BigInteger h = getHash().toBigInteger();
        if (h.compareTo(target) > 0) {
            // Proof of work check failed!
            if (throwException)
                throw new VerificationException("Hash is higher than target: " + getHashAsString() + " vs " +
                        target.toString(16));
            else
                return false;
        }
        return true;
    }
    
    private void checkTimestamp() throws VerificationException {
        // Allow injection of a fake clock to allow unit testing.
        long currentTime = fakeClock != 0 ? fakeClock : System.currentTimeMillis() / 1000;
        if (time > currentTime + ALLOWED_TIME_DRIFT)
            throw new VerificationException("Block too far in future");
    }
    
    private void checkMerkleRoot() throws VerificationException {
        Sha256Hash calculatedRoot = calculateMerkleRoot();
        if (!calculatedRoot.equals(merkleRoot)) {
            log.error("Merkle tree did not verify");
            throw new VerificationException("Merkle hashes do not match: " +
                    calculatedRoot + " vs " + merkleRoot);
        }
    }

    private Sha256Hash calculateMerkleRoot() {
        List<byte[]> tree = buildMerkleTree();
        return new Sha256Hash(tree.get(tree.size() - 1));
    }

    private List<byte[]> buildMerkleTree() {
        // The Merkle root is based on a tree of hashes calculated from the transactions:
        //
        //            root
        //             / \
        //            /   \
        //          A      B
        //         / \    / \
        //        t1 t2  t3 t4
        //
        // The tree is represented as a list: t1,t2,t3,t4,A,B,root where each entry is a hash.
        //
        // The hashing algorithm is double SHA-256. The leaves are a hash of the serialized contents of the
        // transaction. The interior nodes are hashes of the concenation of the two child hashes.
        //
        // This structure allows the creation of proof that a transaction was included into a block without having to
        // provide the full block contents. Instead, you can provide only a Merkle branch. For example to prove tx2 was
        // in a block you can just provide tx2, the hash(tx1) and B. Now the other party has everything they need to
        // derive the root, which can be checked against the block header. These proofs aren't used right now but
        // will be helpful later when we want to download partial block contents.
        //
        // Note that if the number of transactions is not even the last tx is repeated to make it so (see
        // tx3 above). A tree with 5 transactions would look like this:
        //
        //                root
        //                /  \
        //              1     \
        //            /  \     \
        //          2     3     4
        //         / \   / \   /  \
        //       t1 t2  t3 t4  t5 t5
        ArrayList<byte[]> tree = new ArrayList<byte[]>();
        // Start by adding all the hashes of the transactions as leaves of the tree.
        for (Transaction t : transactions) {
            tree.add(t.getHash().getBytes());
        }
        int levelOffset = 0;  // Offset in the list where the currently processed level starts.
        // Step through each level, stopping when we reach the root (levelSize == 1).
        for (int levelSize = transactions.size(); levelSize > 1; levelSize = (levelSize + 1) / 2) {
            // For each pair of nodes on that level:
            for (int left = 0; left < levelSize; left += 2) {
                // The right hand node can be the same as the left hand, in the case where we don't have enough
                // transactions.
                int right = Math.min(left + 1, levelSize - 1);
                byte[] leftBytes = Utils.reverseBytes(tree.get(levelOffset + left));
                byte[] rightBytes = Utils.reverseBytes(tree.get(levelOffset + right));
                tree.add(Utils.reverseBytes(doubleDigestTwoBuffers(leftBytes, 0, 32, rightBytes, 0, 32)));
            }
            // Move to the next level.
            levelOffset += levelSize;
        }
        return tree;
    }

    private void checkTransactions() throws VerificationException {
        // The first transaction in a block must always be a coinbase transaction.
        if (!transactions.get(0).isCoinBase())
            throw new VerificationException("First tx is not coinbase");
        // The rest must not be.
        for (int i = 1; i < transactions.size(); i++) {
            if (transactions.get(i).isCoinBase())
                throw new VerificationException("TX " + i + " is coinbase when it should not be.");
        }
    }

    /**
     * Checks the block data to ensure it follows the rules laid out in the network parameters. Specifically, throws
     * an exception if the proof of work is invalid, or if the timestamp is too far from what it should be. This is
     * <b>not</b> everything that is required for a block to be valid, only what is checkable independent of the
     * chain and without a transaction index.
     *
     * @throws VerificationException
     */
    public void verifyHeader() throws VerificationException {
        // Prove that this block is OK. It might seem that we can just ignore most of these checks given that the
        // network is also verifying the blocks, but we cannot as it'd open us to a variety of obscure attacks.
        //
        // Firstly we need to ensure this block does in fact represent real work done. If the difficulty is high
        // enough, it's probably been done by the network.
        checkProofOfWork(true);
        checkTimestamp();
    }

    /**
     * Checks the block contents
     * @throws VerificationException
     */
    public void verifyTransactions() throws VerificationException {
        // Now we need to check that the body of the block actually matches the headers. The network won't generate
        // an invalid block, but if we didn't validate this then an untrusted man-in-the-middle could obtain the next
        // valid block from the network and simply replace the transactions in it with their own fictional
        // transactions that reference spent or non-existant inputs.
        assert transactions.size() > 0;
        checkTransactions();
        checkMerkleRoot();
    }

    /**
     * Verifies both the header and that the transactions hash to the merkle root.
     */
    public void verify() throws VerificationException {
        verifyHeader();
        verifyTransactions();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Block)) return false;
        Block other = (Block) o;
        return getHash().equals(other.getHash());
    }

    @Override
    public int hashCode() {
        return getHash().hashCode();
    }

    /** Returns the merkle root in big endian form, calculating it from transactions if necessary. */
    public Sha256Hash getMerkleRoot() {
        if (merkleRoot == null)
            merkleRoot = calculateMerkleRoot();
        return merkleRoot;
    }

    /** Exists only for unit testing. */
    void setMerkleRoot(Sha256Hash value) {
        merkleRoot = value;
        hash = null;
    }

    /** Adds a transaction to this block. */
    void addTransaction(Transaction t) {
        if (transactions == null) {
            transactions = new ArrayList<Transaction>();
        }
        transactions.add(t);
        // Force a recalculation next time the values are needed.
        merkleRoot = null;
        hash = null;
    }

    /** Returns the version of the block data structure as defined by the BitCoin protocol. */
    public long getVersion() {
        return version;
    }

    /** Returns the hash of the previous block in the chain, as defined by the block header. */
    public Sha256Hash getPrevBlockHash() {
        return prevBlockHash;
    }

    void setPrevBlockHash(Sha256Hash prevBlockHash) {
        this.prevBlockHash = prevBlockHash;
        this.hash = null;
    }

    /**
     * Returns the time at which the block was solved and broadcast, according to the clock of the solving node.
     * This is measured in seconds since the UNIX epoch (midnight Jan 1st 1970).
     */
    public long getTimeSeconds() {
        return time;
    }

    void setTime(long time) {
        this.time = time;
        this.hash = null;
    }

    /**
     * Returns the difficulty of the proof of work that this block should meet encoded in compact form. The
     * {@link BlockChain} verifies that this is not too easy by looking at the length of the chain when the block is
     * added. To find the actual value the hash should be compared against, use getDifficultyTargetBI.
     */
    public long getDifficultyTarget() {
        return difficultyTarget;
    }

    void setDifficultyTarget(long compactForm) {
        this.difficultyTarget = compactForm;
        this.hash = null;
    }

    /**
     * Returns the nonce, an arbitrary value that exists only to make the hash of the block header fall below the
     * difficulty target.
     */
    public long getNonce() {
        return nonce;
    }

    void setNonce(long nonce) {
        this.nonce = nonce;
        this.hash = null;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    // Unit testing related methods.

    // Used to make transactions unique.
    static private int txCounter;

    /** Adds a coinbase transaction to the block. This exists for unit tests. */
    void addCoinbaseTransaction(byte[] pubKeyTo) {
        transactions = new ArrayList<Transaction>();
        Transaction coinbase = new Transaction(params);
        // A real coinbase transaction has some stuff in the scriptSig like the extraNonce and difficulty. The
        // transactions are distinguished by every TX output going to a different key.
        //
        // Here we will do things a bit differently so a new address isn't needed every time. We'll put a simple
        // counter in the scriptSig so every transaction has a different hash.
        coinbase.inputs.add(new TransactionInput(params, coinbase, new byte[] { (byte) txCounter++ } ));
        coinbase.outputs.add(new TransactionOutput(params, coinbase, Script.createOutputScript(pubKeyTo)));
        transactions.add(coinbase);
    }

    static final byte[] EMPTY_BYTES = new byte[32];

    /** Returns a solved block that builds on top of this one. This exists for unit tests. */
    Block createNextBlock(Address to, long time) {
        Block b = new Block(params);
        b.setDifficultyTarget(difficultyTarget);
        b.addCoinbaseTransaction(EMPTY_BYTES);

        // Add a transaction paying 50 coins to the "to" address.
        Transaction t = new Transaction(params);
        t.addOutput(new TransactionOutput(params, t, Utils.toNanoCoins(50, 0), to));
        // The input does not really need to be a valid signature, as long as it has the right general form.
        TransactionInput input = new TransactionInput(params, t, Script.createInputScript(EMPTY_BYTES, EMPTY_BYTES));
        // Importantly the outpoint hash cannot be zero as that's how we detect a coinbase transaction in isolation
        // but it must be unique to avoid 'different' transactions looking the same.
        byte[] counter = new byte[32];
        counter[0] = (byte) txCounter++;
        input.outpoint.hash = new Sha256Hash(counter);
        t.addInput(input);
        b.addTransaction(t);

        b.setPrevBlockHash(getHash());
        b.setTime(time);
        b.solve();
        try {
            b.verifyHeader();
        } catch (VerificationException e) {
            throw new RuntimeException(e);  // Cannot happen.
        }
        return b;
    }

    // Visible for testing.
    public Block createNextBlock(Address to) {
        return createNextBlock(to, System.currentTimeMillis() / 1000);
    }
}
