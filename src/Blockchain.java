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

// XML libraries
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

class Blockchain {
    public static void main(String[] args) {
        int q_len = 6; // queue length
        int pid = ((args.length < 1) ? 0 : Integer.parseInt(args[0]));
        System.out.println("pid: " + pid);
        new BlockchainNode(pid);
        System.out.println("Scott Friedrich's blockchain framework.");
        System.out.println("Using processID: " + pid + "\n");
    }
}

class BlockchainNode {
    private UnverifiedBlockServer unverifiedBlockServer; // server to receive in new block
    private UnverifiedBlockConsumer unverifiedBlockConsumer; // consumer to do "work"
    private Stack<BlockchainBlock> blockchainStack; // stack to store full blockchain
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
        unverifiedBlockServer = new UnverifiedBlockServer(pid);
        unverifiedBlockConsumer = new UnverifiedBlockConsumer(Ports.getInstance().getUnverifiedBlockPort(pid), this);
        blockchainStack = new Stack<>();

        // intialize threads
        new Thread(unverifiedBlockServer).start();
        new Thread(unverifiedBlockConsumer).start();

        // get port numbers
        setPorts();

        // tell BlockchainNodeMulticast the number of processes
        BlockchainNodeMulticast.setNumProcesses(numProcesses);
    }

    public void addBlockchainBlock(BlockchainBlock bcBlock) {
        blockchainStack.push(bcBlock);
        System.out.println("BlockchainStack:" + blockchainStack.toString());
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
}

@XmlRootElement
class BlockchainBlock implements Comparable<BlockchainBlock> {
    private String previousBlockHash;
    private String signedSHA256;
    private String randomString;
    private String blockId;
    private String verificationProcessId;
    private String creatingProcessId;
    private String prevHash;
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

    public String getSignedSHA256() {
        return signedSHA256;
    }

    @XmlElement
    public void setSignedSHA256(String signedSHA256) {
        this.signedSHA256 = signedSHA256;
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

    public String getVerificationProcessId() {
        return verificationProcessId;
    }

    @XmlElement
    public void setVerificationProcessId(String verificationProcessId) {
        this.verificationProcessId = verificationProcessId;
    }

    public String getCreatingProcessId() {
        return creatingProcessId;
    }

    @XmlElement
    public void setCreatingProcessId(String creatingProcessId) {
        this.creatingProcessId = creatingProcessId;
    }

    public String getPrevHash() {
        return prevHash;
    }

    @XmlElement
    public void setPrevHash(String prevHash) {
        this.prevHash = prevHash;
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
        return "BlockchainBlock [PreviousBlockHash=" + previousBlockHash + ", signedSHA256=" + signedSHA256 + ", RandomString=" + randomString + ", blockId="
                + blockId + ", verificationProcessId=" + verificationProcessId + ", creatingProcessId="
                + creatingProcessId + ", prevHash=" + prevHash + ", firstName=" + firstName + ", lastName=" + lastName
                + ", dob=" + dob + ", ssNum=" + ssNum + ", diagnosis=" + diagnosis + ", treatment=" + treatment
                + ", prescription=" + prescription + "]";
    }

}

class CreateXml {
    // class to parse and create XML
    private static ParseText pt;

    public CreateXml() {
    }

    public String create(String input) {
        pt = new ParseText(input);
        try {
            BlockchainBlock block = new BlockchainBlock();
            JAXBContext jaxbContext = JAXBContext.newInstance(BlockchainBlock.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            StringWriter sw = new StringWriter();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            // null string and null signed SHA-256 show this is unverified block
            // TODO: do we need to set these 3?
            //block.setPreviousBlockHash(null);
            //block.setSignedSHA256(null);
            //block.setRandomString(null);
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
    private String xml;

    public BlockchainNodeMulticast(String input) {
        newBlock = input;
        new Thread(new MulticastWorker(input)).start();
    }

    public static void setNumProcesses(int num) {
        numProcesses = num;
    }

    class MulticastWorker implements Runnable {
        private String message;
        private Socket sock;
        private int port;

        private MulticastWorker(String input) {
            message = input;
            CreateXml createXml = new CreateXml();
            xml = createXml.create(input);
        }

        public void run() {
            try {
                for (int i = 0; i < numProcesses; i++) {
                    // multicast to all blockchain servers
                    port = Ports.getInstance().getUnverifiedBlockPort(i);
                    System.out.println("multicastworker port = " + port);
                    System.out.println("num processes: " + numProcesses);
                    sock = new Socket(serverName, port);
                    PrintStream out = new PrintStream(sock.getOutputStream());
                    out.println(xml);
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

    public UnverifiedBlockServer(int p) {
        pid = p;
    }

    public void run() {
        //run method
        System.out.println("hello from unverifiedBlockServer");
        // read data in from text file
        StringBuilder sb = new StringBuilder();
        try {
            String input = "";
            String file = "./BlockInput" + pid + ".txt";
            Thread.sleep(5);
            BufferedReader userInput;
            userInput = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Enter R to read file, q to quit> ");
            BufferedReader fr = new BufferedReader(new FileReader(file));
            do {
                input = userInput.readLine();
                if (input.equals("R")) {
                    String line = fr.readLine();
                    sb.append(line);
                    new BlockchainNodeMulticast(line.toString());
                }
            } while (input.indexOf("quit") == -1);
        } catch (IOException ex) {
            System.out.println("File not found.");
        } catch (Exception e) {
            System.out.println("interruped exception " + e);
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
        port = p;
        unverifiedQueue = new PriorityBlockingQueue<>();
        System.out.println("starting unverified block consumer");
        blockchainNode = bcNode;
    }

    public void run() {
        // run method
        // do work in this thread
        try {
            System.out.println("unverified block consumer port: " + port);
            ServerSocket servSock = new ServerSocket(port, q_len);
            while (true) {
                // infinite loop- keep waiting for multicast client to connect
                sock = servSock.accept(); // blocks
                // once connected, spawn unverifiedblockworker thread to handle
                new Thread(new UnverifiedBlockWorker(sock, unverifiedQueue)).start();
            }
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }

    class UnverifiedBlockWorker implements Runnable {
        Socket sock;
        private BlockingQueue<BlockchainBlock> unverifiedQueue; // queue of unverified blocks

        public UnverifiedBlockWorker(Socket s, BlockingQueue<BlockchainBlock> queue) {
            sock = s;
            unverifiedQueue = queue;
        }

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
                if (newBlock.getSignedSHA256() == null) {
                    // if null SHA-256 string (i.e. block not solved yet)
                    // add to unverified queue
                    unverifiedQueue.add(newBlock);
                    solve(newBlock);
                } else {
                    // block has been completed
                    // so remove from unverified queue
                    unverifiedQueue.remove(newBlock);
                    // and add to Blockchain
                    // TODO: Add to blockchain
                    // TODO: verify new block?
                    // TODO: stop work on block
                }

                printQueue();
                System.out.println("unverified block worker: " + sb.toString());
            } catch (IOException ex) {
                System.out.println(ex);
            } catch (JAXBException e) {
                System.out.println("JAXB exception");
                System.out.println(e);
            }
        }

        private void solve(BlockchainBlock newBlock) {
            // TODO: do work to solve puzzle
            // TODO: multicast updated block (will add to blockchain)
            String randomString;
            Boolean bool = true;

            // TODO; change to "while unverified"

            while (bool) {
                // generate random string to attempt to solve
                randomString = new String(UUID.randomUUID().toString());
                String newBlockData = newBlock.toString();
                StringBuilder sb = new StringBuilder();
                sb.append(newBlockData);
                sb.append(randomString);
                // TODO: hash and check value
                // TODO: hash and check if last 3 bits are "0"
                String resHash= "";
                bool = false;
            }

            blockchainNode.addBlockchainBlock(newBlock);
        }

        private void printQueue(){
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
