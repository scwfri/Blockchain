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

class Blockchain {
    public static void main(String[] args) {
        int q_len = 6; // queue length
        int pid = (args.length < 1) ? 0 : Integer.parseInt(args[0]);
        new BlockchainNode(pid);
        System.out.println("Scott Friedrich's blockchain framework.");
        System.out.println("Using processID: " + pid + "\n");
    }
}

class BlockchainNode {
    private UnverifiedBlockClient unverifiedBlockClient; // client to do "work"
    private Stack<BlockchainBlock> blockchainStack; // stack to store full blockchain
    private BlockingQueue<String> unverifiedQueue; // queue of unverified blocks
    private int privateKey; // private key for server
    private int pid;
    private int blockchainServerPort;
    private int unverifiedBlockServerPort;
    private int publicKeyServerPort;

    BlockchainNode(int pid) {
        // privateKey = Keys.getInstance.getPrivateKey();
        unverifiedBlockClient = new UnverifiedBlockClient();
        blockchainStack = new Stack<BlockchainBlock>();

        // intialize threads
        new Thread(unverifiedBlockClient).start();
    }


    private void setPid(int pnum) {
        pid = pnum;
    }

    private int getPid() {
        return pid;
    }
}

class BlockchainBlock {
    private String previousBlockHash;
    private String currentBlockHash;
    private String currentBlockContents;

    BlockchainBlock(String prevHash, String newBlockHash, String newBlockContents) {
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

class UnverifiedBlockClient implements Runnable {
    
    public void run() {
        //run method
        System.out.println("hello from unverified block client");
    }
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
