/*----------------------------------------------------------
* File: Blockchain.java
* Compilation: javac Blockchain.java
* Usage:
* on Unix: ./all.sh
*   will create 3 processes, and prompt for user input
*   on command line: 
*       R <filename> to read file in and begin work to create blockchain
*       quit to exit
* Files needed to run:
*   - Blockchain.java
*
*
* web sources:
*  Reading lines and tokens from a file:
*  http://www.fredosaurus.com/notes-java/data/strings/96string_examples/example_stringToArray.html

XML validator:
https://www.w3schools.com/xml/xml_validator.asp

XML / Object conversion:
https://www.mkyong.com/java/jaxb-hello-world-example
http://www.java2s.com/Code/Java/Security/SignatureSignAndVerify.htm
https://www.mkyong.com/java/java-digital-signatures-example/ (not so clear)
https://javadigest.wordpress.com/2012/08/26/rsa-encryption-example/
https://www.programcreek.com/java-api-examples/index.php?api=java.security.SecureRandom
https://www.mkyong.com/java/java-sha-hashing-example/
https://stackoverflow.com/questions/19818550/java-retrieve-the-actual-value-of-the-public-key-from-the-keypair-object
/----------------------------------------------------------*/

import java.util.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.security.MessageDigest;
import java.math.BigInteger;
import java.security.*;

// XML libraries
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.DatatypeConverter;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

class Blockchain {
    public static void main(String[] args) {
        int q_len = 6; // queue length
        int pid = ((args.length < 1) ? 0 : Integer.parseInt(args[0]));
        BlockchainNode bc = new BlockchainNode(pid); // create new blockchain node instance
        System.out.println("Scott Friedrich's blockchain framework.");
        System.out.println("Using processID: " + pid + "\n");
    }
}

class BlockchainNode {
    private int numProcesses = 3; // number of processes
    private UnverifiedBlockServer unverifiedBlockServer; // server to receive in new block
    private UnverifiedBlockConsumer unverifiedBlockConsumer; // consumer to do "work"
    private VerifiedBlockServer verifiedBlockServer; // verified block server, to manage verified blocks
    private PublicKeyStore publicKeyStore; // to store public keys
    private Queue<BlockchainBlock> blockchainStack; // stack to store full blockchain
    private KeyPair keyPair; // this blockchain node's public and private keys
    private int pid; // process id of this node
    private int verifiedBlockPort; // port number for this node's verified block server
    private int unverifiedBlockPort; // unverified block server port number for this node
    private int publicKeyServerPort; // port number for this nodes public key server

    BlockchainNode(int pid) {
        // set pid of BlockchainNode
        setPid(pid);

        // create blockchainStack to store blockchain
        blockchainStack = new LinkedBlockingDeque<>();

        // get port numbers
        setPorts();

        // tell BlockchainNodeMulticast the number of processes
        BlockchainNodeMulticast.setNumProcesses(numProcesses);
        // create public, private keys
        this.getInstanceKeys();
        // start various servers and consumers for blockchain workflow
        this.startServerandConsumer();
        // if this is process # 2, multicast public keys to other nodes
        if (this.pid == 2) {
            try {
                // sleep for 1 sec.. allow all nodes to start
                Thread.sleep(1000);
            } catch (Exception ex) {
                System.out.println("interrupt exception: " + ex);
            }
            // send pid #2 public key to other nodes
            // this will kick off other nodes sending their keys to all nodes
            new BlockchainNodeMulticast(getPid(), getPublicKey());
        }
    }

    private void getInstanceKeys() {
        // get keypair for this node, store in keyPair
        keyPair = Keys.getInstance().getKeys(this);
    }

    public void startServerandConsumer() {
        // store instance of public key store, unverified block server, and unverified block consumer
        publicKeyStore = new PublicKeyStore(this.getPid(), this);
        // create new unverified block server
        unverifiedBlockServer = new UnverifiedBlockServer(pid, this);
        // create new unverified block consumer
        unverifiedBlockConsumer = new UnverifiedBlockConsumer(Ports.getInstance().getUnverifiedBlockPort(pid), this);
        // create new verified block server
        verifiedBlockServer = new VerifiedBlockServer(this);
        // intialize threads for all servers and consumers
        new Thread(publicKeyStore).start();
        new Thread(unverifiedBlockServer).start();
        new Thread(unverifiedBlockConsumer).start();
        new Thread(verifiedBlockServer).start();
    }

    public void addBlockchainBlock(BlockchainBlock bcBlock) {
        // method to add new block to this nodes copy of the blockchain block
        blockchainStack.add(bcBlock);
        System.out.println("BlockchainStack:" + blockchainStack.toString());
    }

    public void exportBlockchainToFile() {
        // get old file
        File old = new File("./BlockchainLedger.xml");
        // and delete
        old.delete();
        // create new file to write to
        File file = new File("./BlockchainLedger.xml");
        try {
            // new file writer so we can write to file
            FileWriter f = new FileWriter(file, false);
            // write new block to file
            for (BlockchainBlock b : blockchainStack) {
                CreateXml cXml = new CreateXml();
                f.write(cXml.marshalFromBlockchainBlock(b));
                f.write("\n");
            }
            // close the file
            f.close();
        } catch (IOException ex) {
            // catch any exceptions, print to console
            System.out.println("Error printing to disk: " + ex);
            ex.printStackTrace();
        }
    }

    // this method returns hash of the last block in blockchain
    // helper method to add last hash to new unverified block
    public String peekLastHash() {
        // if there are blockchains in stack
        if (blockchainStack.size() > 0) {
            // return the the hash of it
            return CalcHashHelper.calc(blockchainStack.poll());
        } else {
            // value we are looking to match in work hash with random string
            return String.valueOf(0b0000);
        }
    }

    private void setPorts() {
        // get required port numbers, stored in BlockchainNode instance
        unverifiedBlockPort = Ports.getInstance().getUnverifiedBlockPort(pid);
        verifiedBlockPort = Ports.getInstance().getVerifiedBlockPort(pid);
        publicKeyServerPort = Ports.getInstance().getPublicKeyServerPort(pid);
    }

    // method to set pid of this node
    private void setPid(int pnum) {
        pid = pnum;
    }

    // returns public key of this node
    public PublicKey getPublicKey() {
        return keyPair.getPublic();
    }

    // returns private key of this node
    // I realize this is not the most "secure" way of doing this, but its a workaround for now
    public PrivateKey getPrivateKey() {
        return keyPair.getPrivate();
    }

    // reutrn pid of this node
    public int getPid() {
        return pid;
    }

    // toString method
    public String toString() {
        return ("pid of this BlockchainNode: " + pid);
    }
}


// this class parses and creates xml to be marshalled and sent around
class CreateXml {
    // own instance of ParseText, which parses string input
    // TODO: should this be static??
    private static ParseText pt;

    public CreateXml() {
    }

    // method to return signed data
    // takes in data, and the key to sign it with
    private static byte[] signData(byte[] data, PrivateKey key) throws Exception {
        // create new signature using SHA1/RSA
        Signature signer = Signature.getInstance("SHA1withRSA");
        // initialize object to sign, using passed in private key
        signer.initSign(key);
        // update data to be signed- add in data to signature
        signer.update(data);
        // sign data and return from method
        return signer.sign();
    }

    // this method to marshall new *unverified* block - which is sent to method as string
    public String marshalFromString(String input, BlockchainNode originNode) {
        // create new BlockchainBlock object
        // and marshall to XML
        pt = new ParseText(input);
        try {
            BlockchainBlock block = new BlockchainBlock();
            // new instance of JAXB context, using BlockchainBlock class
            // this specified which class we are going to be marshalling
            JAXBContext jaxbContext = JAXBContext.newInstance(BlockchainBlock.class);
            // create a new marshaller using JAXBContext initialized above
            // again, this will use BlockchainBlock class
            Marshaller marshaller = jaxbContext.createMarshaller();
            // new StringWriter to use with marshalling process
            StringWriter sw = new StringWriter();
            // we want nice output on the marshalled data
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            // null string and null signed SHA-256 show this is unverified block
            // previousBlockHash is set in solve() method
            // set randomString to null, to indicate unsolved
            block.setRandomString(null);
            // add create time
            System.out.println(String.valueOf("currenttime: " + System.currentTimeMillis()));
            block.setCreateTime(String.valueOf(System.currentTimeMillis()));
            // add pid of creating process
            block.setCreatingProcessId(String.valueOf(originNode.getPid()));
            // cretae random string to use for block id
            block.setBlockId(new String(UUID.randomUUID().toString()));
            // fill in data from file
            block.setFirstName(pt.firstName);
            block.setLastName(pt.lastName);
            block.setDob(pt.dob);
            block.setSsNum(pt.ssNum);
            block.setDiagnosis(pt.diagnosis);
            block.setTreatment(pt.treatment);
            block.setPrescription(pt.prescription);
            // precaution to set signed hash to null for now
            // this will be included in the data that gets signed
            // so recieving process will need pull out signed hash, save it
            // and set signed hash to null in received block
            // in order to verify properly
            block.setSignedHash(null);
            // lets marshall this block, shall we?
            marshaller.marshal(block, sw);
            // create messageDigest to get sha-256 digest of block (including signed hash == null)
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            // use previously marshalled block, make it a string, and then a byte array
            messageDigest.update(sw.toString().getBytes());
            // create digital signature
            byte[] signature = signData(messageDigest.digest(), originNode.getPrivateKey());
            // encode this in base64, and add to the block
            block.setSignedHash(Base64.getEncoder().encodeToString(signature));
            // helper println of signed hash
            System.out.println("signed hash: " + block.getSignedHash());

            // re-marshall this new block, that includes signature
            JAXBContext jaxbContextFinal = JAXBContext.newInstance(BlockchainBlock.class);
            // create a new marshaller using JAXBContext initialized above
            // again, this will use BlockchainBlock class
            Marshaller marshallerFinal = jaxbContext.createMarshaller();
            // new StringWriter to use with marshalling process
            StringWriter swFinal = new StringWriter();
            // we want nice output on the marshalled data
            marshallerFinal.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshallerFinal.marshal(block, swFinal);
            // return block to calling method
            // this block *includes* the signature
            // again, unmarshalling process will need to set signature to null to verify
            return swFinal.toString();
        } catch (Exception ex) {
            // catch exceptions and print debugging stuff
            System.out.println("CreateXml exception");
            System.out.println(ex);
            ex.printStackTrace();
            return "";
        }
    }

    // this marshalls newly verified block
    public String marshalFromBlockchainBlock(BlockchainBlock newBlock) {
        try {
            // new JAXBContext- to specify we are marshalling BlockchainBlock
            JAXBContext jaxbContext = JAXBContext.newInstance(BlockchainBlock.class);
            // create new marshaller using the context of blockchainblock
            Marshaller marshaller = jaxbContext.createMarshaller();
            // string writer helper
            StringWriter sw = new StringWriter();
            // we want nice output
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            // marshall the block- put into string writer
            marshaller.marshal(newBlock, sw);
            System.out.println("marshalled newly verified block: " + sw.toString());
            // return marshalled string to calling method
            return sw.toString();
        } catch (Exception ex) {
            // exception handling
            System.out.println("CreateXml exception");
            System.out.println(ex);
            ex.printStackTrace();
            return "";
        }

    }

    // method to marhsall public key
    public String marshalPublicKey(int pid, PublicKey pub) {
        // creat XML from public key
        // first, we need to create a new instance of KeyHash
        // which is a class to allow for the marshalling of keys
        // KeyHash will be populated with pid and public key passed in as method args
        KeyHash keyHash = new KeyHash();
        keyHash.setPid(pid);
        keyHash.setPublicKey(pub.getEncoded());
        try {
            // new instance of JAXBContext, this time specifying KeyHash
            JAXBContext jaxbContext = JAXBContext.newInstance(KeyHash.class);
            // new marshaller using the KeyHash context
            Marshaller marshaller = jaxbContext.createMarshaller();
            // string writer helper
            StringWriter sw = new StringWriter();
            // again, we really like nice output
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            // marshal this, please
            marshaller.marshal(keyHash, sw);
            // some pretty debug for command line
            System.out.println("-------------------------------");
            System.out.println("marshalled new block: " + sw.toString());
            // return marshalled data as string to calling method
            return sw.toString();
        } catch (Exception ex) {
            // exception handling
            System.out.println("CreateXml exception");
            System.out.println(ex);
            ex.printStackTrace();
            return "";
        }
    }

    class ParseText {
        // variables to store data in
        private String firstName;
        private String lastName;
        private String dob;
        private String ssNum;
        private String diagnosis;
        private String treatment;
        private String prescription;

        private ParseText(String input) {
            // fancy, very manual string parsing.... I'm sure theres a better way to do this
            // this will pull all the individual data points from the file
            int stringPointer = 0;
            firstName = input.substring(stringPointer, input.indexOf(" "));
            stringPointer = input.indexOf(" ", stringPointer + 1);
            lastName = input.substring(stringPointer, input.indexOf(" ", stringPointer   + 1));
            stringPointer = input.indexOf(" ", stringPointer + 1);
            dob = input.substring(stringPointer, input.indexOf(" ", stringPointer   + 1));
            stringPointer = input.indexOf(" ", stringPointer + 1);
            ssNum = input.substring(stringPointer, input.indexOf(" ", stringPointer   + 1));
            stringPointer = input.indexOf(" ", stringPointer + 1);
            diagnosis = input.substring(stringPointer, input.indexOf(" ", stringPointer   + 1));
            stringPointer = input.indexOf(" ", stringPointer + 1);
            treatment = input.substring(stringPointer, input.indexOf(" ", stringPointer   + 1));
            stringPointer = input.indexOf(" ", stringPointer + 1);
            prescription = input.substring(stringPointer);
        }
    }
}

class BlockchainNodeMulticast {
    // multicast for all blockchain nodes
    // set some class variables
    private static int numProcesses; // store for # processes
    private String serverName = "localhost"; // server name will be localhost
    private int q_len = 6; // length of queue
    private int basePort; // base port to use- dynamically determined based on data sending
    private String newBlock; // place to store string of new block, when passed in
    private String xmlToSend; // this is the xml we are going to multicast
    private BlockchainNode originNode; // originiating node

    // method to start multicast of newly *unverified* blockchain block
    BlockchainNodeMulticast(String input, BlockchainNode bcNode) {
        // received in XML for new Block
        // get base port for unverified blocks
        basePort = Ports.getInstance().getUnverifiedBlockBasePort();
        newBlock = input;
        // set origin node variable to the originating node
        originNode = bcNode;
        // spaw new multicast worker thread with correct args
        // this thread will kick off the multicast party
        new Thread(new MulticastWorker(input, bcNode)).start();
    }

    // method to start multicast of newly verified blockchain block
    BlockchainNodeMulticast(BlockchainBlock newBlockchainBlock) {
        //newBlock = newBlockchainBlock;
        // get base port for verified blocks to be sent on
        basePort = Ports.getInstance().getVerifiedBlockBasePort();
        // start new multicast worker thread to get this multicast party started
        new Thread(new MulticastWorker(newBlockchainBlock)).start();
    }

    // YABMOC - (yet another blockchain multicast overloaded constructor).. this time to send public keys
    BlockchainNodeMulticast(int pid, PublicKey pub) {
        // get base port to send public keys on
        basePort = Ports.getInstance().getPublicKeyServerBasePort();
        // new multicast worker party thread. to handle sending public key
        new Thread(new MulticastWorker(pid, pub)).start();
    }

    // helper method to set number of processes
    public static void setNumProcesses(int num) {
        numProcesses = num;
    }

    // worker class to multicast
    class MulticastWorker implements Runnable {
        // some variables
        private String message; // message we are going to send
        private Socket sock; // to hold socket
        private BlockchainBlock newBlockchainBlock; // blockchain block we are going to send.. if needed

        // overloaded constructor.. to send new blockchian
        private MulticastWorker(String input, BlockchainNode originNode) {
            // pass in XML as 'input', store in 'message'
            message = input;
            // new CreateXML class to create XML
            CreateXml createXml = new CreateXml();
            // create XML for unverified block
            xmlToSend = createXml.marshalFromString(input, originNode);
        }

        // overloaded constructor
        // this constructor takes completed blockchain block, and allows for multicast
        private MulticastWorker(BlockchainBlock newBlock) {
            // store new blockchain in instance variable
            newBlockchainBlock = newBlock;
            // new create xml instance to do the xml creation stuff
            CreateXml createXml = new CreateXml();
            // store this xml to send in instance variable to be read by run method
            xmlToSend = createXml.marshalFromBlockchainBlock(newBlock);
        }

        // another overloaded constructor- to send the public keys
        private MulticastWorker(int pid, PublicKey pub) {
            // new create xml instance
            CreateXml createXml = new CreateXml();
            // get me the xml to be sent for public keys. and store it instance var
            xmlToSend = createXml.marshalPublicKey(pid, pub);
            // print key to send
            System.out.println("public key xml to send: " + xmlToSend);
        }

        public void run() {
            try {
                // for each process - numProcesses was set earlier
                for (int processId = 0; processId < numProcesses; processId++) {
                    // multicast to all blockchain servers
                    // determine port- using the base port
                    int port = basePort + processId;
                    System.out.println("Sending to port: " + port);
                    // get a new socket
                    sock = new Socket(serverName, port);
                    // new printstream, to send xml
                    PrintStream out = new PrintStream(sock.getOutputStream());
                    // send that xml, please
                    out.println(xmlToSend);
                    // i tried to clean up the socket.. think this is all we need?
                    sock.close();
                }
            } catch (IOException ex) {
                // more exception stuff
                System.out.println("multicast worker error");
                System.out.println(ex);
                ex.printStackTrace();
            }
        }
    }
}

// this class verifies the blockchain when called
class VerifyBlockchain {
    // TODO: verifies blockchain
}

// class to receive marshalled public key
// reads in public key
class PublicKeyStore implements Runnable {
    // concurrenthashmap to store mapping of all public keys to pid's
    private static ConcurrentHashMap<Integer, byte[]> pubKeyHashMap;
    private int port; // port to use
    private Socket sock; // socket connection
    int q_len = 6; // length of the queue
    private BlockchainNode blockchainNode; // blockchain node that owns this class

    public PublicKeyStore(int p, BlockchainNode bc) {
        // set up the concurrenthashmap for public key storage
        pubKeyHashMap = new ConcurrentHashMap<>();
        // figure out which port we will look for connection on
        port = Ports.getInstance().getPublicKeyServerPort(p);
        System.out.println("public key server port: " + port);
        // set the blockchian node to the calling process (which passes itself into constructor)
        blockchainNode = bc;
    }

    // return public key for specified pid
    public byte[] getPublicKey(int pid) {
        return pubKeyHashMap.get(pid);
    }

    public void run() {
        try {
            // make a new server socket, using correct port #
            ServerSocket servSock = new ServerSocket(port, q_len);
            while (true) {
                // wait for a connection from client, accept connection when asked
                sock = servSock.accept();
                // spawn new public key store worker when connection started
                new Thread(new PublicKeyStoreWorker(sock)).start();
            }
        } catch (Exception ex) {
            // exception stuff
            System.out.println("PublicKeyStore error: " + ex);
            ex.printStackTrace();
        }
    }

    // worker class that actually unmarshalls and adds received keys to instance hash map
    class PublicKeyStoreWorker implements Runnable {
        // socket we're using
        private Socket socket;

        // constructor.. set the socket
        public PublicKeyStoreWorker(Socket s) {
            socket = s;
        }

        public void run() {
            try {
                // make a new buffered reader to read data sent by client to this process
                BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                // to hold input from client
                String input = "";
                // string builder. to store each line read from client
                StringBuilder sb = new StringBuilder();
                do {
                    // read next line of input
                    input = in.readLine();
                    // if its not null, add it to string builder
                    if (input != null) {
                        sb.append(input);
                    }
                } while (input != null); // stop it when you get a null input

                // create reader object to unmarshal
                StringReader reader = new StringReader(sb.toString());
                // new context for unmarshalling- using KeyHash class
                JAXBContext jaxbContext = JAXBContext.newInstance(KeyHash.class);
                // new unmarshaller using KeyHash context created above
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                // create new KeyHash class by unmarshalling data received from client
                KeyHash pubKeyHash = (KeyHash) unmarshaller.unmarshal(reader);
                // notify user new public key received
                System.out.println("Received public key: " + pubKeyHash.getPid());
                // add this new pid/publickey pair to hash map
                pubKeyHashMap.put(pubKeyHash.getPid(), pubKeyHash.getPublicKey());
                // clean up and close reader
                reader.close();
                // if we received process 2's key, and we are not process 2
                // send our public key to other nodes
                // this ensures all nodes get eachothers keys
                if (pubKeyHash.getPid() == 2 && blockchainNode.getPid() != 2)  {
                    int p = blockchainNode.getPid();
                    PublicKey pub = blockchainNode.getPublicKey();
                    // multicast this nodes' public keys
                    new BlockchainNodeMulticast(p, pub);
                }
            } catch (Exception ex) {
                // exception stuff
                System.out.println("PublicKeyStoreWorker error: " + ex);
                ex.printStackTrace();
            }
        }
    }
}

// class to manage verified blocks
class VerifiedBlockServer implements Runnable {
    private int pid; // process id
    private int port; // port to use
    private int q_len = 6; // queue length
    private Socket sock; // to store socket connection
    private BlockchainNode blockchainNode; // calling blockchain node - passed in constructor

    // constructor
    public VerifiedBlockServer(BlockchainNode bcNode) {
        pid = bcNode.getPid(); // add pid
        port = Ports.getInstance().getVerifiedBlockPort(pid); // get port number to listen on
        blockchainNode = bcNode; // creating node
    }

    public void run() {
        // run method
        try {
            // new server socket created
            ServerSocket servSock = new ServerSocket(port, q_len);
            while (true) {
                // infinite loop- keep waiting for multicast client to connect
                sock = servSock.accept(); // blocks
                // once connected, spawn verifiedblockworker thread to handle
                new Thread(new VerifiedBlockWorker(sock)).start();
            }
        } catch (IOException ex) {
            // exception stuff
            System.out.println(ex);
            ex.printStackTrace();
        }
    }

    // worker class to handle received verified block
    class VerifiedBlockWorker implements Runnable {
        // socket we are connected on
        private Socket sock;

        // constructor
        private VerifiedBlockWorker(Socket s) {
            // set socket connection instance variable
            sock = s;
        }

        public void run() {
            // run method implementation
            try {
                // new buffered reader, to read in data from client
                BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                // keeper of next line input
                String input = "";
                // string builder, to build our input, from multiple lines
                StringBuilder sb = new StringBuilder();
                do {
                    // read next line
                    input = in.readLine();
                    // if its not null, append to the string builder
                    if (input != null) {
                        sb.append(input);
                    }
                } while (input != null); // stop once we read in a null string
                System.out.println("unverified block received: " + sb.toString());
                // create reader object to unmarshal
                StringReader reader = new StringReader(sb.toString());
                // new jaxbcontext, BlockchianBlock since that is what we are unmarshalling to
                JAXBContext jaxbContext = JAXBContext.newInstance(BlockchainBlock.class);
                // new unmarshaller, using blockchainblock context above
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                // create new blockchian block from unmarshalled received data
                BlockchainBlock newBlock = (BlockchainBlock) unmarshaller.unmarshal(reader);

                // clean up
                reader.close();

                // add to unverifiedBlockQueue
                System.out.println("received new solved block");
                System.out.println("newly verified blockchain block: " + newBlock.toString());
                // block has been completed
                // so remove from unverified queue
                UnverifiedBlockConsumer.removeFromUnverifiedQueue(newBlock.getBlockId());
                // and add to new BlockchainBlcok
                blockchainNode.addBlockchainBlock(newBlock);

                // if we are process 0, write new blockchain to disk
                if (blockchainNode.getPid() == 0) {
                    blockchainNode.exportBlockchainToFile();
                }
                System.out.print("verified block queue: ");
            } catch (Exception ex) {
                // exception stuff
                System.out.println("Verified bock worker exception: " + ex);
                ex.printStackTrace();
            }
        }
    }
}

class UnverifiedBlockServer implements Runnable {
    // read data in from text file
    // tell BlockchainNodeList class to multicast to everyone
    // UnverifiedBlockWorker does "work" on new block
    // once verified, UnverifiedBlockWorker tells BlockChainNodeList to multicast
    private int pid; // pid of calling process
    private BlockchainNode originNode; // calling process

    public UnverifiedBlockServer(int p, BlockchainNode blockchainNode) {
        pid = p; // set pid
        originNode = blockchainNode; // add calling blockchainNode to instance variable
    }

    public void run() {
        //run method
        // read data in from text file
        try {
            // string to hold user input
            String input = "";
            // string to hold file name
            String file = "";
            // wait 5sec, while system starts- its a hack, I know...
            Thread.sleep(5000);
            // new buffered reader to read user input
            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
            // print instructions to user
            System.out.print("Enter R <filename> to read file, q to quit> ");
            do {
                // read next input from user
                input = userInput.readLine();
                // if user inputs an R
                if (input.indexOf("R") != -1) {
                    // new buffered reader to read input from specified file
                    BufferedReader fr = new BufferedReader(new FileReader("./" + input.substring(2)));
                    String line = "";
                    do {
                        // read next line from file
                        line = fr.readLine();
                        if (line != null) {
                            // if line is not null, multicast as new unverified block to nodes
                            new BlockchainNodeMulticast(line.toString(), originNode);
                        }
                    } while (line != null); // once you reach null line, youre done reading file
                }
            } while (input.indexOf("quit") == -1);
            // exception stuff
        } catch (IOException ex) {
            System.out.println("File not found.");
        } catch (Exception e) {
            System.out.println("interruped exception " + e);
            e.printStackTrace();
        }
    }

}

// class to handle multicast unverified blocks
class UnverifiedBlockConsumer implements Runnable {
    // SERVER
    // class to do "work" on new block
    private int port; // port we are going to look to receive on
    private Socket sock; // socket connection
    int q_len = 6; // queue length
    private static BlockingQueue<BlockchainBlock> unverifiedQueue; // queue of unverified blocks
    private BlockchainNode blockchainNode; // creating process

    UnverifiedBlockConsumer(int p, BlockchainNode bcNode) {
        // get instance of new SingleThread executor
        port = p; // set port
        unverifiedQueue = new PriorityBlockingQueue<>(); // create new unverified queue of blocks
        blockchainNode = bcNode; // set owning process
    }

    public void run() {
        // run method
        // do work in this thread
        try {
            // new server socket
            ServerSocket servSock = new ServerSocket(port, q_len);
            while (true) {
                // infinite loop- keep waiting for multicast client to connect
                sock = servSock.accept(); // blocks
                // once connected, spawn unverifiedblockworker thread to handle
                new Thread(new UnverifiedBlockWorker(sock)).start();
            }
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }

    // check if blockId is in unverified queue, return boolean
    private boolean isUnverified(String blockId) {
        // new iterator to iterate through unverified blocks
        Iterator<BlockchainBlock> iter = unverifiedQueue.iterator();
        // while there is another unverified block in queue
        while (iter.hasNext()) {
            // get next
            BlockchainBlock b = iter.next();
            // check if id of block equals block id user is asking about
            if (b.getBlockId().equals(blockId)) {
                // if it does, return true
                return true;
            }
        }
        // else no matching block found, so return false
        return false;
    }

    // method to remove a specified block from the unverified queue
    public static void removeFromUnverifiedQueue(String blockId) {
        // iterate through unverifiedQueue, remove when matches blockId
        Iterator<BlockchainBlock> iter = unverifiedQueue.iterator();
        // while there is another unverified block in queue
        while (iter.hasNext()) {
            // get next
            BlockchainBlock b = iter.next();
            // check if this block id matches block id user is asking to remove
            if (b.getBlockId().equals(blockId)) {
                unverifiedQueue.remove(b);
                // break once the correct id is found and removed
                break;
            }
        }
    }

    // method to verify signature
    public static boolean verifySig(byte[] data, PublicKey key, byte[] sig) throws Exception {
        // create new Signature- use SHA1/RSA
        Signature signer = Signature.getInstance("SHA1withRSA");
        // initialize object to verify- using key
        signer.initVerify(key);
        // update data to be verified- (i.e. add in data to key)
        signer.update(data);

        // return result
        return (signer.verify(sig));
    }

    // worker class to handle new unverified block
    class UnverifiedBlockWorker implements Runnable {
        Socket sock; // socket connection

        public UnverifiedBlockWorker(Socket s) {
            sock = s; // set socket connection
        }

        // run method is started by newSingleThreadExecutor()
        // this allows only one thread to execute at at time
        public void run() {
            try {
                // new buffered reader to read in data from clinet
                BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                // string to hold input
                String input = "";
                // new string builder to build full input
                StringBuilder sb = new StringBuilder();
                do {
                    // read next line of input
                    input = in.readLine();
                    if (input != null) {
                        // if its not null, add it to the string builder
                        sb.append(input);
                    }
                } while (input != null); // once you get a null string, youre done reading

                // create reader object to unmarshal
                StringReader reader = new StringReader(sb.toString());
                // new jaxbcontext, using BlockchainBlock
                JAXBContext jaxbContext = JAXBContext.newInstance(BlockchainBlock.class);
                // new unmarshaller to unmarshall to BlockchainBlock class
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                // create new BlockchainBlock from unmarshalled data
                BlockchainBlock newBlock = (BlockchainBlock) unmarshaller.unmarshal(reader);
                // clean up
                reader.close();
                
                // TODO: verify signature on block
                // store block signature
                // remove signature from block (set to null)
                // new messagedigest


                // add to unverifiedBlockQueue
                if (newBlock.getRandomString() == null) {
                    // if null random string, this is a new block
                    // add to unverified queue
                    System.out.println("received new unsolved block");
                    // add to unverified queue
                    unverifiedQueue.add(newBlock);
                    // call solve method on new unverified block- to do work
                    solve(newBlock);
                    return; // we're done here
                }
            } catch (IOException ex) {
                System.out.println(ex);
            } catch (JAXBException e) {
                System.out.println("JAXB unverified block worker exception");
                System.out.println(e);
                e.printStackTrace();
            }
        }

        // solve is a synchronized method, so only one thread can execute at a time
        // i.e. cannot have two threads trying to solve different unverified blocks
        private synchronized void solve(BlockchainBlock newBlock) {
            String randomString; // random string to try and solve
            Boolean unsolved = true; // boolean to tell while loop when to end
            BlockchainBlock workerBlock = newBlock; // the new block we are solving

            // add previous block ID to workerBlock
            workerBlock.setPreviousBlockHash(blockchainNode.peekLastHash());

            while (unsolved) {
                // generate random string to attempt to solve
                randomString = new String(UUID.randomUUID().toString());
                // add the random string to this block
                workerBlock.setRandomString(randomString);
                try {
                    // check to make sure current block is not verified yet
                    if (!isUnverified(workerBlock.getBlockId())) {
                        // return if it is
                        return;
                    }
                    // calculate hash on this block
                    String hex = CalcHashHelper.calc(workerBlock);
                    System.out.println("hex val: " + hex);
                    // our work requirement:
                    // we want the first hex value of the resulting hash to == 16 (F)
                    if (hex.substring(0,1).equals("F")) {
                        // if it does, announce it to the world
                        System.out.println("time: " + System.currentTimeMillis() + "\nWINNER!");
                        // add this process id to the solved process id
                        workerBlock.setSolvedProcessId(String.valueOf(blockchainNode.getPid()));
                        // and tell while loop to please stop
                        unsolved = false;
                    }
                    // sleep for 2 seconds- we're just simulating more work here
                    Thread.sleep(2000);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            // create new multicast to send to all BlockchainNodes
            // this only gets sent *if* this process is the one who solved
            if (!unsolved) {
                new BlockchainNodeMulticast(workerBlock);
            }
        }

        // print queue to screen
        public void printQueue(){
            System.out.println("PRINT QUEUE:");
            System.out.println(unverifiedQueue.toString());
        }
    }
}

class CalcHashHelper {
    public static String calc(BlockchainBlock b) {
        String hexRes = null;
        try {
            // create messagedigest to hash
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            // create byte array of hashed block
            byte[] byteHash = md.digest(b.toString().getBytes("UTF-8"));
            // convert to hex
            hexRes = DatatypeConverter.printHexBinary(byteHash);
        } catch (Exception ex) {
            System.out.println("CalcHash exception");
            ex.printStackTrace();
        }
        // return hex value
        return hexRes;
    }
}

class Keys {
    // singleton
    // provide public key to all clients
    // calculate and return new private key to BlockchainNode
    private static Keys instance = null;

    // private constructor (for singleton)
    private Keys() {
    }

    // getInstance method, for singleton
    public static synchronized Keys getInstance() {
        if (instance == null) {
            instance = new Keys();
        }
        return instance;
    }

    public KeyPair getKeys(BlockchainNode bn) {
        // calculate public and private key here
        // add public key to publicKeyList
        // return private key to BlockchainNode
        byte[] pub = null;
        byte[] priv = null;
        KeyPair keyPair = null;
        try {
            // genrate secure random to use for key pair generation
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
            // create KeyPairGenerator, which will create new public/private keys
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            // initialize and create 512 bit key
            keyGen.initialize(1024, random);
            // generate the key pair
            keyPair = keyGen.generateKeyPair();
            } catch (Exception ex) {
            System.out.println("getPrivateKey exception: " + ex);
            ex.printStackTrace();
        }
        // set BlockchainNode class public and private keys
        return keyPair;
    }
}

// KeyHash class- this is the class used to marshall public keys to all processes
@XmlRootElement
class KeyHash {
    private int pid; // pid of process
    private byte[] publicKey; // public key of process

    public KeyHash() {
    }

    // get methods
    public int getPid() {
        return pid;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    // set methods
    @XmlElement
    public void setPid(int pid) {
        this.pid = pid;
    }

    @XmlElement
    public void setPublicKey(byte[] publicKey) {
        this.publicKey = publicKey;
    }

    @Override
    public String toString() {
        return "pid: " + pid + "\npublic key: " + Base64.getEncoder().encodeToString(publicKey);
    }
}

// Ports class that calculates ports for processes
// also where base ports are set
class Ports {
    // singleton
    private static Ports instance = null;
    private int publicKeyServerBasePort; // base port for public key distribution
    private int unverifiedBlockBasePort; // base port for unverified block distribution
    private int verifiedBlockBasePort; // base port for verified block distribution

    // private constructor, since this is a singleton
    private Ports() {
        publicKeyServerBasePort = 4701; // this is our base public key port
        unverifiedBlockBasePort = 4820; // this is our base unverified block port
        verifiedBlockBasePort = 4930; // this is out base verified block port
    }

    // getInstance method, since this is singleton
    public static synchronized Ports getInstance() {
        if (instance == null) {
            instance = new Ports();
        }
        return instance;
    }

    // return public key server port for given pid
    // (base port + processId)
    public int getPublicKeyServerPort(int pid) {
        return publicKeyServerBasePort + pid;
    }

    public int getPublicKeyServerBasePort() {
        return publicKeyServerBasePort;
    }

    // return unverifed block port for given pid
    // (base port + processId)
    public int getUnverifiedBlockPort(int pid) {
        return unverifiedBlockBasePort + pid;
    }

    public int getUnverifiedBlockBasePort() {
        return unverifiedBlockBasePort;
    }

    // return verified block port for given pid
    // (base port + processId)
    public int getVerifiedBlockPort(int pid) {
        return verifiedBlockBasePort + pid;
    }

    public int getVerifiedBlockBasePort() {
        return verifiedBlockBasePort;
    }
}

// this class is for marshalling blockchain blocks
// used as basis for all blockchainblocks
@XmlRootElement
class BlockchainBlock implements Comparable<BlockchainBlock> {
    private String signedHash;
    private String createTime;
    private String previousBlockHash;
    private String randomString;
    private String blockId;
    private String solvedProcessId;
    private String creatingProcessId;
    private String firstName;
    private String lastName;
    private String dob;
    private String ssNum;
    private String diagnosis;
    private String treatment;
    private String prescription;

    // compareTo method- compares blockId and returns value 0 if equivalent, 1 otherwise
    public int compareTo(BlockchainBlock other) {
        if (this.blockId.equals(other.blockId) == true) {
            return 0;
        } else {
            return 1;
        }
    }

    // getters and setters are below
    public String getSignedHash() {
        return signedHash;
    }

    @XmlElement
    public void setSignedHash(String signedHash) {
        this.signedHash = String.valueOf(signedHash);
    }

    public String getCreateTime() {
        return createTime;
    }

    @XmlElement
    public void setCreateTime(String createTime) {
        this.createTime = String.valueOf(createTime);
    }

    public String getPreviousBlockHash() {
        return previousBlockHash;
    }

    @XmlElement
    public void setPreviousBlockHash(String prevBlockHash) {
        this.previousBlockHash = prevBlockHash;
    }

    public String getRandomString() {
        return randomString;
    }

    @XmlElement
    public void setRandomString(String randomString) {
        this.randomString = randomString;
    }

    public String getBlockId() {
        return blockId;
    }

    @XmlElement
    public void setBlockId(String blockId) {
        this.blockId = blockId;
    }

    public String getSolvedProcessId() {
        return solvedProcessId;
    }

    @XmlElement
    public void setSolvedProcessId(String solvedProcessId) {
        this.solvedProcessId = solvedProcessId;
    }

    public String getCreatingProcessId() {
        return creatingProcessId;
    }

    @XmlElement
    public void setCreatingProcessId(String creatingProcessId) {
        this.creatingProcessId = creatingProcessId;
    }

    public String getFirstName() {
        return firstName;
    }

    @XmlElement
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    @XmlElement
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getDob() {
        return dob;
    }

    @XmlElement
    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getSsNum() {
        return ssNum;
    }

    @XmlElement
    public void setSsNum(String ssNum) {
        this.ssNum = ssNum;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    @XmlElement
    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getTreatment() {
        return treatment;
    }

    @XmlElement
    public void setTreatment(String treatment) {
        this.treatment = treatment;
    }

    public String getPrescription() {
        return prescription;
    }

    @XmlElement
    public void setPrescription(String prescription) {
        this.prescription = prescription;
    }

    @Override
    public String toString() {
        return "BlockchainBlock [createTime=" + String.valueOf(createTime) + "PreviousBlockHash=" + previousBlockHash + ", RandomString=" + randomString + ", blockId="
                + blockId + ", solvedProcesId=" + solvedProcessId + ", creatingProcessId="
                + creatingProcessId + ", firstName=" + firstName + ", lastName=" + lastName
                + ", dob=" + dob + ", ssNum=" + ssNum + ", diagnosis=" + diagnosis + ", treatment=" + treatment
                + ", prescription=" + prescription + "]";
    }

}
