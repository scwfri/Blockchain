/*----------------------------------------------------------
* File: Blockchain.java
* Compilation: javac Blockchain.java
* Usage (in different shell window):
* Files needed to run:
*   - Blockchain.java
*
*
* web sources:
*  Reading lines and tokens from a file:
*  http://www.fredosaurus.com/notes-java/data/strings/96string_examples/example_stringToArray.html
*
*  XML validator:
*  https://www.w3schools.com/xml/xml_validator.asp
*
*  XML / Object conversion:
*  https://www.mkyong.com/java/jaxb-hello-world-example/
----------------------------------------------------------*/

// TODO: verify block class/method
// TODO: have pid 0 export to file
// TODO: sign keys with signature

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
        BlockchainNode bc = new BlockchainNode(pid);
        System.out.println("Scott Friedrich's blockchain framework.");
        System.out.println("Using processID: " + pid + "\n");
    }
}

class BlockchainNode {
    private int numProcesses = 3; // number of processes
    private UnverifiedBlockServer unverifiedBlockServer; // server to receive in new block
    private UnverifiedBlockConsumer unverifiedBlockConsumer; // consumer to do "work"
    private VerifiedBlockServer verifiedBlockServer;
    private PublicKeyStore publicKeyStore;
    private Queue<BlockchainBlock> blockchainStack; // stack to store full blockchain
    private KeyPair keyPair;
    private int pid;
    private int verifiedBlockPort;
    private int unverifiedBlockPort;
    private int publicKeyServerPort;
    private ConcurrentHashMap<Integer, PublicKey> publicKeyList;

    BlockchainNode(int pid) {
        // set pid of BlockchainNode
        setPid(pid);

        // create blockchainStack to store blockchain
        blockchainStack = new LinkedBlockingDeque<>();

        // get port numbers
        setPorts();

        // tell BlockchainNodeMulticast the number of processes
        BlockchainNodeMulticast.setNumProcesses(numProcesses);
        this.getInstanceKeys();
        this.startServerandConsumer();
        // if this is process # 2, multicast public keys to other nodes
        if (this.pid == 2) {
            try {
                Thread.sleep(100);
            } catch (Exception ex) {
                System.out.println("interrupt exception: " + ex);
            }
            new BlockchainNodeMulticast(getPid(), keyPair.getPublic());

            // base64 encoded private key
            //priv = keyPair.getPrivate().getEncoded();
            //base64 encoded public key
            //pub = keyPair.getPublic().getEncoded();
        }
    }

    private void getInstanceKeys() {
        keyPair = Keys.getInstance().getKeys(this);
    }

    public void startServerandConsumer() {
        // store instance of public key store, unverified block server, and unverified block consumer
        publicKeyStore = new PublicKeyStore(this.getPid(), this);
        unverifiedBlockServer = new UnverifiedBlockServer(pid, this);
        unverifiedBlockConsumer = new UnverifiedBlockConsumer(Ports.getInstance().getUnverifiedBlockPort(pid), this);
        verifiedBlockServer = new VerifiedBlockServer(this);
        // intialize threads
        new Thread(publicKeyStore).start();
        new Thread(unverifiedBlockServer).start();
        new Thread(unverifiedBlockConsumer).start();
    }

    public void addBlockchainBlock(BlockchainBlock bcBlock) {
        blockchainStack.add(bcBlock);
        System.out.println("BlockchainStack:" + blockchainStack.toString());
    }

    public String peekLastHash() {
        if (blockchainStack.size() > 0) {
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

    private void setPid(int pnum) {
        pid = pnum;
    }

    public PublicKey getPublicKey() {
        return keyPair.getPublic();
    }

    public int getPid() {
        return pid;
    }

    public String toString() {
        return ("pid of this BlockchainNode: " + pid);
    }
}


class CreateXml {
    // class to parse and create XML
    private static ParseText pt;

    public CreateXml() {
    }

    public String marshalFromString(String input, BlockchainNode originNode) {
        // create new BlockchainBlock object
        // and marshall to XML
        pt = new ParseText(input);
        try {
            BlockchainBlock block = new BlockchainBlock();
            JAXBContext jaxbContext = JAXBContext.newInstance(BlockchainBlock.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            StringWriter sw = new StringWriter();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            // null string and null signed SHA-256 show this is unverified block
            // previousBlockHash is set in solve() meString
            // set randomString to null, to indicate unsolved
            block.setRandomString(null);
            System.out.println(String.valueOf("currenttime: " + System.currentTimeMillis()));
            block.setCreateTime(String.valueOf(System.currentTimeMillis()));
            block.setCreatingProcessId(String.valueOf(originNode.getPid()));
            block.setBlockId(new String(UUID.randomUUID().toString()));
            block.setFirstName(pt.firstName);
            block.setLastName(pt.lastName);
            block.setDob(pt.dob);
            block.setSsNum(pt.ssNum);
            block.setDiagnosis(pt.diagnosis);
            block.setTreatment(pt.treatment);
            block.setPrescription(pt.prescription);
            marshaller.marshal(block, sw);
            System.out.println("marshalled: " + sw.toString());
            return sw.toString();
        } catch (Exception ex) {
            System.out.println("CreateXml exception");
            System.out.println(ex);
            ex.printStackTrace();
            return "";
        }
    }

    public String marshalFromBlockchainBlock(BlockchainBlock newBlock) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(BlockchainBlock.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            StringWriter sw = new StringWriter();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(newBlock, sw);
            System.out.println("marshalled new block: " + sw.toString());
            return sw.toString();
        } catch (Exception ex) {
            System.out.println("CreateXml exception");
            System.out.println(ex);
            ex.printStackTrace();
            return "";
        }

    }

    public String marshalPublicKey(int pid, PublicKey pub) {
        // creat XML from public key
        KeyHash keyHash = new KeyHash();
        keyHash.setPid(pid);
        keyHash.setPublicKey(pub.getEncoded());
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(KeyHash.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            StringWriter sw = new StringWriter();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(keyHash, sw);
            System.out.println("-------------------------------");
            System.out.println("marshalled new block: " + sw.toString());
            return sw.toString();
        } catch (Exception ex) {
            System.out.println("CreateXml exception");
            System.out.println(ex);
            ex.printStackTrace();
            return "";
        }
    }

    class ParseText {
        private String firstName;
        private String lastName;
        private String dob;
        private String ssNum;
        private String diagnosis;
        private String treatment;
        private String prescription;

        private ParseText(String input) {
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
    // CLIENT
    // singleton
    // multicast for all blockchain nodes
    private static int numProcesses;
    private String serverName = "localhost";
    private int q_len = 6;
    private int basePort;
    private String newBlock;
    private String xmlToSend;
    private BlockchainNode originNode;

    BlockchainNodeMulticast(String input, BlockchainNode bcNode) {
        // received in XML for new Block
        basePort = Ports.getInstance().getUnverifiedBlockBasePort();
        newBlock = input;
        new Thread(new MulticastWorker(input, bcNode)).start();
        originNode = bcNode;
    }

    BlockchainNodeMulticast(BlockchainBlock newBlockchainBlock) {
        //newBlock = newBlockchainBlock;
        basePort = Ports.getInstance().getUnverifiedBlockBasePort();
        new Thread(new MulticastWorker(newBlockchainBlock)).start();
    }

    BlockchainNodeMulticast(int pid, PublicKey pub) {
        basePort = Ports.getInstance().getPublicKeyServerBasePort();
        new Thread(new MulticastWorker(pid, pub)).start();
    }

    public static void setNumProcesses(int num) {
        numProcesses = num;
    }

    class MulticastWorker implements Runnable {
        private String message;
        private Socket sock;
        private BlockchainBlock newBlockchainBlock;

        private MulticastWorker(String input, BlockchainNode originNode) {
            // pass in XML as 'input', store in 'message'
            message = input;
            // new CreateXML class to create XML
            CreateXml createXml = new CreateXml();
            // create XML for unverified block
            xmlToSend = createXml.marshalFromString(input, originNode);
        }

        private MulticastWorker(BlockchainBlock newBlock) {
            // overloaded constructor
            // this constructor takes completed blockchain block, and allows for multicast
            newBlockchainBlock = newBlock;
            // need to unmarshal data here?
            CreateXml createXml = new CreateXml();
            xmlToSend = createXml.marshalFromBlockchainBlock(newBlock);
        }

        private MulticastWorker(int pid, PublicKey pub) {
            CreateXml createXml = new CreateXml();
            xmlToSend = createXml.marshalPublicKey(pid, pub);
            System.out.println("public key xml to send: " + xmlToSend);
        }

        public void run() {
            try {
                for (int processId = 0; processId < numProcesses; processId++) {
                    // multicast to all blockchain servers
                    // TODO: this is sending verified blocks on unverified port
                    int port = basePort + processId;
                    System.out.println("Sending to public key port: " + port);
                    System.out.println("serverName: " + serverName);
                    sock = new Socket(serverName, port);
                    PrintStream out = new PrintStream(sock.getOutputStream());
                    out.println(xmlToSend);
                    sock.close();
                }
            } catch (IOException ex) {
                System.out.println("multicast worker error");
                System.out.println(ex);
                ex.printStackTrace();
            }
        }
    }
}

class VerifyBlockchain {
    // TODO: verifies blockchain
}

// class to receive marshalled public key
class PublicKeyStore implements Runnable {
    // reads in public key
    private static ConcurrentHashMap<Integer, byte[]> pubKeyHashMap;
    private int port;
    private Socket sock;
    int q_len = 6;
    private BlockchainNode blockchainNode;

    public PublicKeyStore(int p, BlockchainNode bc) {
        pubKeyHashMap = new ConcurrentHashMap<>();
        port = Ports.getInstance().getPublicKeyServerPort(p);
        System.out.println("public key server port: " + port);
        blockchainNode = bc;
    }

    public void run() {
        try {
            ServerSocket servSock = new ServerSocket(port, q_len);
            while (true) {
                sock = servSock.accept();
                new Thread(new PublicKeyStoreWorker(sock)).start();
            }
        } catch (Exception ex) {
            System.out.println("PublicKeyStore error: " + ex);
            ex.printStackTrace();
        }
    }

    class PublicKeyStoreWorker implements Runnable {
        private Socket socket;

        public PublicKeyStoreWorker(Socket s) {
            socket = s;
        }

        public void run() {
            //KeyHash pubKeyHash = new KeyHash();
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                String input = "";
                StringBuilder sb = new StringBuilder();
                do {
                    input = in.readLine();
                    if (input != null) {
                        sb.append(input);
                    }
                } while (input != null);

                // create reader object to unmarshal
                StringReader reader = new StringReader(sb.toString());
                JAXBContext jaxbContext = JAXBContext.newInstance(KeyHash.class);
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                KeyHash pubKeyHash = (KeyHash) unmarshaller.unmarshal(reader);
                System.out.println("Received public key: " + pubKeyHash.toString());
                pubKeyHashMap.put(pubKeyHash.getPid(), pubKeyHash.getPublicKey());
                reader.close();
                // TODO: send public key for this process, if this.pid not in hashmap
                //if (pubKeyHash.getPid() != blockchainNode.getPid()) {
                    //System.out.println("this process pid not in keylist. pid: " + pubKeyHash.getPid());
                    //new BlockchainNodeMulticast(blockchainNode.getPid(), blockchainNode.getPublicKey());
            ////new BlockchainNodeMulticast(getPid(), keyPair.getPublic());
                //}
            } catch (Exception ex) {
                System.out.println("PublicKeyStoreWorker error: " + ex);
                ex.printStackTrace();
            }
        }
    }
}

class VerifiedBlockServer implements Runnable {
    private int pid;
    private int port;
    private int q_len = 6;
    private Socket sock;
    private BlockchainNode blockchainNode;

    public VerifiedBlockServer(BlockchainNode bcNode) {
        // empty constructor for now
        pid = bcNode.getPid();
        port = Ports.getInstance().getVerifiedBlockPort(pid);
        blockchainNode = bcNode;
    }

    public void run() {
        // run method
        try {
            ServerSocket servSock = new ServerSocket(port, q_len);
            while (true) {
                // infinite loop- keep waiting for multicast client to connect
                sock = servSock.accept(); // blocks
                // once connected, spawn unverifiedblockworker thread to handle
                new Thread(new VerifiedBlockWorker(sock)).start();
            }
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }

    class VerifiedBlockWorker implements Runnable {
        private Socket sock;

        private VerifiedBlockWorker(Socket s) {
            sock = s;
        }

        public void run() {
            // run method implementation
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                String input = "";
                StringBuilder sb = new StringBuilder();
                do {
                    input = in.readLine();
                    if (input != null) {
                        sb.append(input);
                    }
                } while (input != null);
                // create reader object to unmarshal
                StringReader reader = new StringReader(sb.toString());
                JAXBContext jaxbContext = JAXBContext.newInstance(BlockchainBlock.class);
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                BlockchainBlock newBlock = (BlockchainBlock) unmarshaller.unmarshal(reader);

                //PrintStream out = new PrintStream(new FileOutputStream("./xmlExample.xml"));
                //out.print(sb.toString());
                reader.close();

                // add to unverifiedBlockQueue
                System.out.println("received new solved block");
                System.out.println("newly verified blockchain block: " + newBlock.toString());
                // block has been completed
                // so remove from unverified queue
                UnverifiedBlockConsumer.removeFromUnverifiedQueue(newBlock.getBlockId());
                // and add to new BlockchainBlcok
                // TODO: check to make sure block is not already added??
                blockchainNode.addBlockchainBlock(newBlock);
                // TODO: verify new block?
                System.out.print("verified block queue: ");
            } catch (Exception ex) {
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
    private int pid;
    private BlockchainNode originNode;

    public UnverifiedBlockServer(int p, BlockchainNode blockchainNode) {
        pid = p;
        originNode = blockchainNode;
    }

    public void run() {
        //run method
        // read data in from text file
        try {
            String input = "";
            String file = "";
            //String file = "./BlockInput" + pid + ".txt";
            Thread.sleep(5);
            BufferedReader userInput;
            userInput = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Enter R <filename> to read file, q to quit> ");
            do {
                input = userInput.readLine();
                if (input.indexOf("R") != -1) {
                    BufferedReader fr = new BufferedReader(new FileReader("./" + input.substring(2)));
                    String line = "";
                    do {
                        line = fr.readLine();
                        new BlockchainNodeMulticast(line.toString(), originNode);
                    } while (line != null);
                }
            } while (input.indexOf("quit") == -1);
        } catch (IOException ex) {
            System.out.println("File not found.");
        } catch (Exception e) {
            System.out.println("interruped exception " + e);
            e.printStackTrace();
        }
    }

}

class UnverifiedBlockConsumer implements Runnable {
    // SERVER
    // class to do "work" on new block
    private int port;
    private Socket sock;
    int q_len = 6;
    private static BlockingQueue<BlockchainBlock> unverifiedQueue; // queue of unverified blocks
    private BlockchainNode blockchainNode;

    UnverifiedBlockConsumer(int p, BlockchainNode bcNode) {
        // get instance of new SingleThread executor
        port = p;
        unverifiedQueue = new PriorityBlockingQueue<>();
        blockchainNode = bcNode;
    }

    public void run() {
        // run method
        // do work in this thread
        try {
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

    private boolean isUnverified(String blockId) {
        Iterator<BlockchainBlock> iter = unverifiedQueue.iterator();
        while (iter.hasNext()) {
            BlockchainBlock b = iter.next();
            if (b.getBlockId().equals(blockId)) {
                return true;
            }
        }
        return false;
    }

    public static void removeFromUnverifiedQueue(String blockId) {
        // iterate through unverifiedQueue, remove when matches blockId
        Iterator<BlockchainBlock> iter = unverifiedQueue.iterator();
        while (iter.hasNext()) {
            BlockchainBlock b = iter.next();
            if (b.getBlockId().equals(blockId)) {
                unverifiedQueue.remove(b);
                System.out.println("\n\n\nremoved " + b.toString() + "\n\n");
                break;
            }
        }
    }

    class UnverifiedBlockWorker implements Runnable {
        Socket sock;

        public UnverifiedBlockWorker(Socket s) {
            sock = s;
        }

        // run method is started by newSingleThreadExecutor()
        // this allows only one thread to execute at at time
        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                String input = "";
                StringBuilder sb = new StringBuilder();
                do {
                    input = in.readLine();
                    if (input != null) {
                        sb.append(input);
                    }
                } while (input != null);

                // create reader object to unmarshal
                StringReader reader = new StringReader(sb.toString());
                JAXBContext jaxbContext = JAXBContext.newInstance(BlockchainBlock.class);
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                BlockchainBlock newBlock = (BlockchainBlock) unmarshaller.unmarshal(reader);

                //PrintStream out = new PrintStream(new FileOutputStream("./xmlExample.xml"));
                //out.print(sb.toString());
                reader.close();

                // add to unverifiedBlockQueue
                if (newBlock.getRandomString() == null) {
                    // if null random string, this is a new block
                    // add to unverified queue
                    System.out.println("received new unsolved block");
                    unverifiedQueue.add(newBlock);
                    solve(newBlock);
                    System.out.print("unverified block queue: ");
                    printQueue();
                    return;
                } else {
                    System.out.println("received new solved block");
                    System.out.println("newly verified blockchain block: " + newBlock.toString());
                    // block has been completed
                    // so remove from unverified queue
                    removeFromUnverifiedQueue(newBlock.getBlockId());
                    // and add to new BlockchainBlcok
                    blockchainNode.addBlockchainBlock(newBlock);
                    System.out.println("unverified queue after remove: " + unverifiedQueue);
                    System.out.print("verified block queue: ");
                    printQueue();
                    return;
                }
            } catch (IOException ex) {
                System.out.println(ex);
            } catch (JAXBException e) {
                System.out.println("JAXB unverified block worker exception");
                System.out.println(e);
            }
        }

        // solve is a synchronized method, so only one thread can execute at a time
        private synchronized void solve(BlockchainBlock newBlock) {
            String randomString;
            Boolean bool = true;
            BlockchainBlock workerBlock = newBlock;

            // add previous block ID to workerBlock
            workerBlock.setPreviousBlockHash(blockchainNode.peekLastHash());

            while (bool) {
                // generate random string to attempt to solve
                randomString = new String(UUID.randomUUID().toString());
                workerBlock.setRandomString(randomString);
                try {
                    // check to make sure current block is not verified yet
                    if (!isUnverified(workerBlock.getBlockId())) {
                        System.out.println("---------CURRENT WORK BLOCK VERIFIED------");
                        bool = false;
                        return;
                    }
                    MessageDigest md = MessageDigest.getInstance("SHA-256");
                    byte[] byteHash = md.digest(workerBlock.toString().getBytes("UTF-8"));
                    String hex = DatatypeConverter.printHexBinary(byteHash);
                    System.out.println("hex val: " + hex);
                    if (hex.substring(0,1).equals("F")) {
                        System.out.println("time: " + System.currentTimeMillis() + "\nWINNER!");
                        workerBlock.setSolvedProcessId(String.valueOf(blockchainNode.getPid()));
                        bool = false;
                    }
                    // sleep for 5 sec -- for syncrhonization
                    Thread.sleep(5000);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                // do not need to store THIS hash
                // only need to store previous hash
            }

            // create new multicast to send to all BlockchainNodes
            new BlockchainNodeMulticast(workerBlock);
        }

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
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] byteHash = md.digest(b.toString().getBytes("UTF-8"));
            hexRes =  DatatypeConverter.printHexBinary(byteHash);
        } catch (Exception ex) {
            System.out.println("CalcHash exception");
            ex.printStackTrace();
        }
        return hexRes;
    }
}

class Keys {
    // singleton
    // provide public key to all clients
    // calculate and return new private key to BlockchainNode
    private static Keys instance = null;

    private Keys() {
    }

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

@XmlRootElement
class KeyHash {
    private int pid;
    private byte[] publicKey;

    public KeyHash() {
    }

    public int getPid() {
        return pid;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    public int getPiid() {
        return pid;
    }

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

class Ports {
    // singleton
    private static Ports instance = null;
    private int publicKeyServerBasePort;
    private int unverifiedBlockBasePort;
    private int verifiedBlockBasePort;

    private Ports() {
        publicKeyServerBasePort = 4701;
        unverifiedBlockBasePort = 4820;
        verifiedBlockBasePort = 4930;
    }

    public static synchronized Ports getInstance() {
        if (instance == null) {
            instance = new Ports();
        }
        return instance;
    }

    public int getPublicKeyServerPort(int pid) {
        return publicKeyServerBasePort + pid;
    }

    public int getPublicKeyServerBasePort() {
        return publicKeyServerBasePort;
    }

    public int getUnverifiedBlockPort(int pid) {
        return unverifiedBlockBasePort + pid;
    }

    public int getUnverifiedBlockBasePort() {
        return unverifiedBlockBasePort;
    }

    public int getVerifiedBlockPort(int pid) {
        return verifiedBlockBasePort + pid;
    }

    public int getVerifiedBlockBasePort() {
        return verifiedBlockBasePort;
    }
}

@XmlRootElement
class BlockchainBlock implements Comparable<BlockchainBlock> {
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

    public int compareTo(BlockchainBlock other) {
        if (this.blockId.equals(other.blockId) == true) {
            return 0;
        } else {
            return 1;
        }
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
