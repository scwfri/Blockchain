/*--------------------------------------------------------
/* 2018-01-14:
   bc.java for BlockChain
   Dr. Clark Elliott for CSC435

   This is some quick sample code giving a simple framework for coordinating multiple processes in a blockchain group.

   INSTRUCTIONS:

   Set the numProceses class variable (e.g., 1,2,3), and use a batch file to match

   AllStart.bat:

   REM for three procesess:
   start java bc 0
   start java bc 1
   java bc 2

   You might want to start with just one process to see how it works.

   Thanks: http://www.javacodex.com/Concurrency/PriorityBlockingQueue-Example

   Notes to CDE:
   Optional: send public key as Base64 XML along with a signed string.
   Verfy the signature with public key that has been restored.

*/
/* * File: Blockchain.java
* Compilation: javac Blockchain.java
* Usage (in different shell window):
* Files needed to run:
*   - Blockchain.java
----------------------------------------------------------*/

/*
 * how this all works:
 *  - blockchain class created with each "java Blockchain <pid>"
 *      - each blockchain class will create its own server
 *          - UnverifiedBlockServer
 *              - process incoming unverified blocks
 *          - BlcokchainServer
 *              - process new blockchain incoming from other servers
 *      - each Blockchain instance has multiSend method
 *          - sends unverified blocks received out to all servers
 *  - UnverifiedBlockServer:
 *      - adds new unverified blocks into priority queue
 *      - starts new UnverifiedBlockWorker to process new unverified block
 *          - processing adds new unverified block to queue
 *  - UnverifiedBlockConsumer:
 *      - where "work" gets done
 *      - once work completed:
 *          - send to all clients as well so they can add to blockchain
 *          - send verified block to BlockChainServer
 *          - BlockChainServer spawns new BlockChainWorker
 *              - BlockChainWorker adds to blockchain
 *  - multiSend
 *      - initially sends key to each key server
 *      - series of for loops to iterate through each server and send data
 */

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
