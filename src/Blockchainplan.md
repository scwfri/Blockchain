# Blockchain

- BlockchainNode class created
    - each BlockchainNode class is a "node" or "
    - each BlockchainNode class will own:
        - UnverifiedBlockServer
            - reads in data from file (perhaps on loop????)
            - when new data received:
                - create new unverifiedBlockWorker to process
            - UnverifiedBlockWorker (subclass of UnverifiedBlockClient)
                - read in new unverified block
                - tell owner BlockchianNodeList class to multicast to everyone
                - starts new UnverifiedBlockConsumer process
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

---

## Notes from class

- Blockchain utilities sample program
    - shows ways to get uuid, etc
- Blockchain input utilities program
    - shows how to work with XML
- blockinput files
    - example input data files
    - program will be run on these data files, and others in same format
- process 0, 1 should sit and do nothing
    - process 2 should tell other processes to start working
- sample work program
    - need to read through, figure out how it works
    - will have to be added to program
- process coordination
    - does not perform real work
    - do not use as basis for blockchain problem
        - this is just spec to show how to coordinate 3 processes
- xml marshaling
    - tools for moving objects into XML string representation and back again
    - used for external data format and storing records on disk

## work
- hash string is random string
    - need to determine- does hash solve problem?
        - i.e. does hash value match requirements set forth in spec?
    - random string needs to be generated to solve puzzle
    - will have to generate a random alphanumeric string

- put data into unverified block
    - give UUID
    - multicast to peers
    - nodes compete to see who can verify the block first
- *concept of work*
    - work in computationally intensive task
        - impossible to cheat
    - common form:
        - take random string, combine with data, hash result
            - look for special quality in resulting data
                - quality e.g. < 20000, all 0s
        - SHA-256 hash algorithm
    - work:
        - trying to find random string, such that when we hash it:
            - resulting hash/string matches some pattern
    - start with data, guess random string
        - combine two and produce hash value
            - is hash value in the right range?
                - if it is, that hash value is solution to puzzle
- for this assignment, make puzzles easier
    - issue sleep() after each guess
- *verifying blocks*
    - to verify block containing data, we have to solve the puzzle
    - peers compete to complete the hash first
- e.g.
    - start: produce S by concanteating together:
        - SHA-256 hash value of previous block
        - data in the current block
        - random string (r)
    - hash S producing S-hash
    - if S-hash meets your specifications youve solved the puzzle
        -r is the answer
    - otherwise, return to start
- building blockchain:
    - insert r (solution to puzzle) into current block along with:
        - block sequence number that is one greater than the last block added to blockchain
    - create SHA-256 hash of current block (with r now in it) and put this in header
    - add new block to blockchain
        - multicast blockchain to all other nodes
- *repair block*
    - shows when revision has been made to previous block

## Signing

- create public/private key pair
- use private key to sign the SHA-256 message
- user public key to verify data

## base64 encoding

- turn hex string (not printable) into something we can read and print
- we will use Base64 encoding (adn then XML on the string)
    - use to ship hex data between processes
    - first turn into Base64 string, then put into XML to ship
        - binary -> Base64 -> XML -> ship XML -> Base64 -> binary

## Encryption

- show how to encrypt and decrypt a string using public key encrpytion

## input utility

- read in some strings of data from a file
- convert to XML

## process synchronization

- fake data shipped around
    - XML is not used
- each process reads data (hard coded in program)
- need to examine timestamp
    - i.e. sort by timestamp in queue
    - attempt to verify block with lowest timestamp first

## Concurrent priority queue

- want unverified blocks in order inside queue
-
