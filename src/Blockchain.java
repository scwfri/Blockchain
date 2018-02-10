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
    private UnverifiedBlockServer unverifiedBlockServer; // server to receive in new block
    private UnverifiedBlockConsumer unverifiedBlockConsumer; // consumer to do "work"
    private Stack<BlockchainBlock> blockchainStack; // stack to store full blockchain
    private BlockingQueue<String> unverifiedQueue; // queue of unverified blocks
    private int privateKey; // private key for server
    private int pid;
    private int blockchainServerPort;
    private int unverifiedBlockServerPort;
    private int publicKeyServerPort;

    BlockchainNode(int pid) {
        // privateKey = Keys.getInstance.getPrivateKey();
        unverifiedBlockServer = new UnverifiedBlockServer();
        unverifiedBlockConsumer = new UnverifiedBlockConsumer();
        blockchainStack = new Stack<>();

        // intialize threads
        new Thread(unverifiedBlockServer).start();
        new Thread(unverifiedBlockConsumer).start();
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
        blockchainNodeList = new ArrayList<>();
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

class UnverifiedBlockServer implements Runnable {
    // read data in from text file
    // tell BlockchainNodeList class to multicast to everyone
    // UnverifiedBlockWorker does "work" on new block
    // once verified, UnverifiedBlockWorker tells BlockChainNodeList to multicast

    public void run() {
        //run method
        System.out.println("hello from unverifiedBlockServer");
        // read data in from text file
        BufferedReader fr = new BufferedReader(NewFileReader());
    }

    class UnverifiedBlockWorker implements Runnable {

        public void run() {
            //run method
            // add new unverified block to priority queue
        }
    }
}

class UnverifiedBlockConsumer implements Runnable {
    // class to do "work" on new block

    public void run() {
        // run method
        // do work in this thread
        System.out.println("Hello from UnverifiedBlockConsumer");
    }
}

class Keys {
    // singleton
    // provide public key to all clients
    // calculate and return new private key to BlockchainNode
    private static Keys instance;
    private ArrayList<String> publicKeyList;

    private Keys() {
        publicKeyList = new ArrayList<>();
    }

    public Keys getInstance() {
        if (instance == null) {
            instance = new Keys();
        }
        return instance;
    }

    public int getPrivateKey() {
        // calculate public and private key here
        // add public key to publicKeyList
        // return private key to BlockchainNode
        return 0;
    }
}

class Ports {
    // singleton
    private Ports instance;
    private int publicKeyServerBasePort;
    private int unverifiedBlockClientBasePort;

    private Ports() {
        publicKeyServerBasePort = 4701;
        unverifiedBlockClientBasePort = 4820;
    }

    public Ports getInstance() {
        if (instance == null) {
            instance = new Ports();
        }
        return instance;
    }

    public int getUnverifiedBlockClientPort(int pid) {
        return unverifiedBlockClientBasePort + pid;
    }
}
