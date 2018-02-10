# Blockchain

- BlockchainNode class created
    - each BlockchainNode class is a "node" or "
    - each BlockchainNode class will own:
        - UnverifiedBlockClient
            - reads in data from file (perhaps on loop????)
            - when new data received:
                - sends to UnverifiedBlockWorker
                - tell owner BlockchianNodeList class to multicast to everyone
            - UnverifiedBlockWorker (subclass of UnverifiedBlockClient)
                - does "work" on block to verify
                - once verified, tells owner blockchain class to multicast to everyone
        - Blockchain instance
                - copy of current blockchain
                - process 0 is responsible for writing to disk
        - BlockingQueue<String> unverifiedQueue
            - contains queue of unverified blocks
        - int privateKey

- **Port class**
    - creates all ports
    - ports are stored as instance variables in  each blockchain instance

- **BlockchainNodeList class**
    - singleton
    - owns ArrayList<Blockchain> to keep list of all nodes
    - responsible for multicast to all nodes

- **PublicKey class**
    - ArrayList<T> publicKeyList
    - uses BlockchainNode class to multicast public key to all nodes

- **MarshalXML class**
    - marshals XML, writes to disk
    - singleton
    - should only be owned by process 0

- **Blockchain class**
    - each block has previous block hash, as well as its current hash
    - each block points backwards to the one before it

'''Java
Class Blockchain {
    private long previousHash;
    private String contents;

    Blockchain(long previousHash, String contents) {
        this.previousHash = previousHash;
        this.contents = contents;
    }
}
'''

**to hash**

Block number
nonce- number you set to hash to match hash with requirements

## Public/private key pairs

- used for signatures
- 
