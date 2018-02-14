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

import java.util.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.security.MessageDigest;
import java.math.BigInteger;

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
    private UnverifiedBlockServer unverifiedBlockServer; // server to receive in new block
    private UnverifiedBlockConsumer unverifiedBlockConsumer; // consumer to do "work"
    private Queue<BlockchainBlock> blockchainStack; // stack to store full blockchain
    private int numProcesses = 3; // number of processes
    private int privateKey; // private key for server
    private int pid;
    private int updatedBlockchainPort;
    private int unverifiedBlockPort;
    private int publicKeyServerPort;

    BlockchainNode(int pid) {
        // set pid of BlockchainNode
        setPid(pid);

        // privateKey = Keys.getInstance.getPrivateKey();
        blockchainStack = new LinkedBlockingDeque<>();

        // get port numbers
        setPorts();

        // tell BlockchainNodeMulticast the number of processes
        BlockchainNodeMulticast.setNumProcesses(numProcesses);
        this.startServerandConsumer();
    }

    public void startServerandConsumer() {
        unverifiedBlockServer = new UnverifiedBlockServer(pid, this);
        unverifiedBlockConsumer = new UnverifiedBlockConsumer(Ports.getInstance().getUnverifiedBlockPort(pid), this);
        // intialize threads
        new Thread(unverifiedBlockServer).start();
        new Thread(unverifiedBlockConsumer).start();
    }

    public void addBlockchainBlock(BlockchainBlock bcBlock) {
        blockchainStack.add(bcBlock);
        System.out.println("BlockchainStack:" + blockchainStack.toString());
    }

    public String peekLastHash() {
        if (blockchainStack.size() > 0) {
            return blockchainStack.poll().getPreviousBlockHash();
        } else {
            // value we are looking to match in work hash with random string
            return String.valueOf(0b0000);
        }
    }

    private void setPorts() {
        // get required port numbers, stored in BlockchainNode instance
        unverifiedBlockPort = Ports.getInstance().getUnverifiedBlockPort(pid);
        updatedBlockchainPort = Ports.getInstance().getUpdatedBlockchainPort(pid);
        publicKeyServerPort = Ports.getInstance().getPublicKeyServerPort(pid);
    }

    private void setPid(int pnum) {
        pid = pnum;
    }

    public int getPid() {
        return pid;
    }

    public String toString() {
        return ("pid of this BlockchainNode: " + pid);
    }
}

@XmlRootElement
class BlockchainBlock implements Comparable<BlockchainBlock> {
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

    public String getPreviousBlockHash() {
        return previousBlockHash;
    }

    @XmlElement
    public void setPreviousBlockHash(String prevBlockHash) {
        previousBlockHash = prevBlockHash;
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
        return "BlockchainBlock [PreviousBlockHash=" + previousBlockHash + ", RandomString=" + randomString + ", blockId="
                + blockId + ", solvedProcesId=" + solvedProcessId + ", creatingProcessId="
                + creatingProcessId + ", prevHash=" + firstName + ", lastName=" + lastName
                + ", dob=" + dob + ", ssNum=" + ssNum + ", diagnosis=" + diagnosis + ", treatment=" + treatment
                + ", prescription=" + prescription + "]";
    }

}

class CreateXml {
    // class to parse and create XML
    private static ParseText pt;

    public CreateXml() {
    }

    public String marshalFromString(String input, BlockchainNode originNode) {
        pt = new ParseText(input);
        try {
            BlockchainBlock block = new BlockchainBlock();
            JAXBContext jaxbContext = JAXBContext.newInstance(BlockchainBlock.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            StringWriter sw = new StringWriter();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            // null string and null signed SHA-256 show this is unverified block
            // previousBlockHash is set in solve() method
            // set randomString to null, to indicate unsolved
            block.setRandomString(null);
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
            BlockchainBlock block = newBlock;
            JAXBContext jaxbContext = JAXBContext.newInstance(BlockchainBlock.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            StringWriter sw = new StringWriter();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(block, sw);
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
    private String newBlock;
    private String xmlToSend;
    private BlockchainNode originNode;

    BlockchainNodeMulticast(String input, BlockchainNode bcNode) {
        // received in XML for new Block
        newBlock = input;
        new Thread(new MulticastWorker(input, bcNode)).start();
        originNode = bcNode;
    }

    BlockchainNodeMulticast(BlockchainBlock newBlockchainBlock) {
        //newBlock = newBlockchainBlock;
        new Thread(new MulticastWorker(newBlockchainBlock)).start();
    }

        public static void setNumProcesses(int num) {
            numProcesses = num;
        }

    class MulticastWorker implements Runnable {
        private String message;
        private Socket sock;
        private int port;
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

        public void run() {
            try {
                for (int i = 0; i < numProcesses; i++) {
                    // multicast to all blockchain servers
                    port = Ports.getInstance().getUnverifiedBlockPort(i);
                    sock = new Socket(serverName, port);
                    PrintStream out = new PrintStream(sock.getOutputStream());
                    out.println(xmlToSend);
                    sock.close();
                }
            } catch (IOException ex) {
                System.out.println("multicast worker error");
                System.out.println(ex);
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
        StringBuilder sb = new StringBuilder();
        try {
            String input = "";
            String file = "./BlockInput" + pid + ".txt";
            Thread.sleep(5);
            BufferedReader userInput;
            userInput = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Enter R to read file, q to quit> ");
            BufferedReader fr = new BufferedReader(new FileReader(file));
            do {
                input = userInput.readLine();
                if (input.equals("R")) {
                    String line = fr.readLine();
                    sb.append(line);
                    new BlockchainNodeMulticast(line.toString(), originNode);
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
        System.out.println("unverifiedblockconsumer: " + bcNode.toString());
        // get instance of new SingleThread executor
        port = p;
        unverifiedQueue = new PriorityBlockingQueue<>();
        System.out.println("starting unverified block consumer");
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

    private void removeFromUnverifiedQueue(String blockId) {
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

        // TODO: can only work on one block at a time
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
                    removeFromUnverifiedQueue(newBlock.getBlockId());
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
                    System.out.println("unverified queue after remove: " + unverifiedQueue);
                    // and add to Blockchain
                    // TODO: verify new block?
                    System.out.print("verified block queue: ");
                    printQueue();
                    return;
                }
            } catch (IOException ex) {
                System.out.println(ex);
            } catch (JAXBException e) {
                System.out.println("JAXB exception");
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

            blockchainNode.addBlockchainBlock(workerBlock);
            new BlockchainNodeMulticast(workerBlock);
        }

        private void printQueue(){
            System.out.println("PRINT QUEUE:");
            System.out.println(unverifiedQueue.toString());
        }
    }
}

class Keys {
    // singleton
    // provide public key to all clients
    // calculate and return new private key to BlockchainNode
    private static Keys instance = null;
    private ArrayList<String> publicKeyList;

    private Keys() {
        publicKeyList = new ArrayList<>();
    }

    public static synchronized Keys getInstance() {
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
    private static Ports instance = null;
    private int publicKeyServerBasePort;
    private int unverifiedBlockBasePort;
    private int updatedBlockchainBasePort;

    private Ports() {
        publicKeyServerBasePort = 4701;
        unverifiedBlockBasePort = 4820;
        updatedBlockchainBasePort = 4930;
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

    public int getUnverifiedBlockPort(int pid) {
        return unverifiedBlockBasePort + pid;
    }

    public int getUpdatedBlockchainPort(int pid) {
        return updatedBlockchainBasePort + pid;
    }
}
