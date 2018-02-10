/*----------------------------------------------------------
* File: Blockchain.java
* Compilation: javac Blockchain.java
* Usage (in different shell window):
* Files needed to run:
*   - Blockchain.java
----------------------------------------------------------*/

import java.util.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;


class BlockchainNode {
    private UnverifiedBlockClient unverifiedBlockClient; // client to do "work"
    private Stack<Blockchain> blockchainStack; // stack to store full blockchain
    private BlockingQueue<String> unverifiedQueue; // queue of unverified blocks
    private int privateKey; // private key for server
    private int pid;
    private int blockchainServerPort;
    private int unverifiedBlockServerPort;
    private int publicKeyServerPort;

    BlockchainNode() {
        // privateKey = Keys.getInstance.getPrivateKey();
        unverifiedBlockClient = new UnverifiedBlockClient();
        blockchainStack = new Stack<Blockchain>();
    }

    public static void main(String[] args) {
        int q_len = 6; // queue length
        BlockchainNode b = new BlockchainNode();
        b.setPid((args.length < 1) ? 0 : Integer.parseInt(args[0]));
    }

    private void setPid(int pnum) {
        pid = pnum;
    }
}

class Blockchain {
    private String previousBlockHash;
    private String currentBlockHash;
    private String currentBlockContents;

    Blockchain(String prevHash, String newBlockHash, String newBlockContents) {
        previousBlockHash = prevHash;
        currentBlockHash = newBlockHash;
        currentBlockContents = newBlockContents;
    }
}

class BlockchainNodeList {
    // singleton
    private static BlockchainNodeList instance;
    private ArrayList<BlockchainNode> blockchainNodeList;

    private BlockchainNodeList() {
        blockchainNodeList = new ArrayList<BlockchainNode>();
    }

    public BlockchainNodeList getInstance() {
        if (instance == null) {
            instance = new BlockchainNodeList();
        }
        return instance;
    }

    public void muliCast(String message) {
        // method to multicast string (i.e. new block, completed block) to all nodes
    }

    public void addBlockchainNode(BlockchainNode b) {
        blockchainNodeList.add(b);
    }

    public ArrayList<BlockchainNode> getBlockchainNodeList() {
        return blockchainNodeList;
    }
}

class UnverifiedBlockClient {

}

class Keys {
    // singleton
    // provide public key to all clients
    // calculate and return new private key to BlockchainNode
    private static Keys instance;

    private Keys() {

    }

    public Keys getInstance() {
        if (instance == null) {
            instance = new Keys();
        }
        return instance;
    }

}
