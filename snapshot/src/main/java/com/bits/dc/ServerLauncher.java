package com.bits.dc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.bits.dc.model.Node;
import com.bits.dc.rmi.NodeRemote;
import com.bits.dc.utils.InputUtil;
import com.bits.dc.utils.NetworkUtil;
import com.bits.dc.utils.RemoteUtil;
import com.bits.dc.utils.StorageUtil;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Simulates server node (bank) in the graph for distributed snapshot
 *
 * @see Node
 * @see ServiceConfiguration
 */
public final class ServerLauncher {

    private static final Logger logger = LogManager.getLogger();

    private static final int RMI_PORT = ServiceConfiguration.getRmiPort();

    @Nullable
    private static Node node;

    private static NodeState nodeState = NodeState.DISCONNECTED;

    /**
     * Thread pool scheduler of N threads for money transfer and snapshot taking
     */
    private static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    /**
     * Description: method name,node host,node id,existing node host,existing node id
     * Example: join,localhost,10,none,0
     * Example: join,localhost,15,localhost,10
     * Example: join,localhost,20,localhost,15
     * Example: join,localhost,25,localhost,20
     * Example: join,localhost,20,localhost,25
     * Example: view
     * Example: cut
     */
    public static void main(String[] args) {
        System.out.println("You can change service configuration parameters in " + ServiceConfiguration.CONFIGURATION_FILE);
        System.out.println("Service configuration: RMI port=" + RMI_PORT);
        System.out.println("Service configuration: BankTransfer MIN_AMOUNT=" + Constants.MIN_AMOUNT + ", MAX_AMOUNT=" + Constants.MAX_AMOUNT + ", INITIAL_BALANCE=" + Constants.INITIAL_BALANCE);
        System.out.println("Service configuration: BankTransfer TIMEOUT_FREQUENCY=" + Constants.TIMEOUT_FREQUENCY + ", TIMEOUT_UNIT=" + Constants.TIMEOUT_UNIT);
        if (Constants.MIN_AMOUNT >= Constants.MAX_AMOUNT || Constants.MAX_AMOUNT >= Constants.INITIAL_BALANCE) {
            logger.warn("Bank transfer properties must maintain formula [ MIN_AMOUNT < MAX_AMOUNT < INITIAL_BALANCE ] !");
            return;
        }
        System.out.println("Type in: method name,node host,node id,existing node host,existing node id");
        System.out.println("Example: create,localhost,10");
        System.out.println("Example: join,localhost,15,localhost,10");
        System.out.println("Example: join,localhost,20,localhost,15");
        System.out.println("Example: join,localhost,25,localhost,20");
        System.out.println("Example: join,localhost,30,localhost,25");
        System.out.println("Example: view");
        System.out.println("Example: cut");
        StorageUtil.init();
        NetworkUtil.printMachineIPv4();
        System.out.println("Bank is ready for request >");
        InputUtil.readInput(ServerLauncher.class.getName());
    }

    /**
     * Signals current node to create the graph
     *
     * @param nodeHost host for new current node
     * @param nodeId   id for new current node
     */
    public static void create(@NotNull String nodeHost, int nodeId) throws Exception {
        if (nodeState != NodeState.DISCONNECTED) {
            logger.warn("Must be DISCONNECTED to create! Current nodeState=" + nodeState);
            return;
        }
        if (nodeId <= 0) {
            logger.warn("Node id must be positive integer [ nodeID > 0 ] !");
            return;
        }
        startRMIRegistry();
        System.out.println("NodeId=" + nodeId + " is the first bank in the graph");
        node = register(nodeId, nodeHost);
        System.out.println("NodeId=" + nodeId + " is connected as first node=" + node);
        nodeState = NodeState.CONNECTED;
        startMoneyTransferring();
    }

    /**
     * Signals current node to join the graph:
     * - accumulate the graph structure of all available banks from the existing node
     * - start randomly sending/accepting money transfers
     * <p>
     * Existing node MUST be operational!
     *
     * @param nodeHost         host for new current node
     * @param nodeId           id for new current node
     * @param existingNodeHost of node in the graph to fetch data from
     * @param existingNodeId   of node in the graph to fetch data from
     */
    public static void join(@NotNull String nodeHost, int nodeId, @NotNull String existingNodeHost, int existingNodeId) throws Exception {
        if (nodeState != NodeState.DISCONNECTED) {
            logger.warn("Must be DISCONNECTED to join! Current nodeState=" + nodeState);
            return;
        }
        if (nodeId <= 0) {
            logger.warn("Node id must be positive integer [ nodeID > 0 ] !");
            return;
        }
        startRMIRegistry();
        System.out.println("NodeId=" + nodeId + " connects to existing nodeId=" + existingNodeId);
        Node existingNode = RemoteUtil.getRemoteNode(existingNodeId, existingNodeHost).getNode();
        if (existingNode.getNodes().isEmpty()) {
            logger.warn("Existing node must be operational!");
            return;
        }
        if (existingNode.getNodes().containsKey(nodeId)) {
            logger.warn("Cannot join as nodeId=" + nodeId + " already taken!");
            return;
        }
        node = register(nodeId, nodeHost);
        node.putNodes(existingNode.getNodes());
        announceJoin();
        System.out.println("NodeId=" + nodeId + " connected as node=" + node + " from existingNode=" + existingNode);
        nodeState = NodeState.CONNECTED;
        startMoneyTransferring();
    }

    /**
     * View the graph topology aka all the banks in connected component
     */
    public static void view() throws RemoteException {
        if (nodeState != NodeState.CONNECTED) {
            logger.warn("Must be CONNECTED to view topology! Current nodeState=" + nodeState);
            return;
        }
        System.out.println("Viewing topology from node=" + node);
        node.getNodes().entrySet().forEach(n -> {
            try {
                RemoteUtil.getRemoteNode(n.getKey(), n.getValue()).getNode();
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Initiate distributed snapshot to all known nodes (all nodes are interconnected as a digraph)
     */
    public static void cut() throws RemoteException {
        if (nodeState != NodeState.CONNECTED) {
            logger.warn("Must be CONNECTED to initiate the distributed snapshot! Current nodeState=" + nodeState);
            return;
        }
        System.out.println("Starting distributed snapshot from node=" + node);
        RemoteUtil.getRemoteNode(node).receiveMarker(node.getId());
    }

    /**
     * Registers RMI for new node, initializes node object
     *
     * @param id   of the new node
     * @param host of the new node
     */
    @NotNull
    private static Node register(int id, @NotNull String host) throws Exception {
        System.setProperty("java.rmi.server.hostname", host);
        Node node = new Node(id, host);
        Naming.bind("rmi://" + node.getHost() + "/NodeRemote" + node.getId(), new NodeRemote(node));
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                System.out.println("Auto-leaving process initiated...");
                try {
                    if (nodeState == NodeState.CONNECTED) {
                        leave();
                    }
                } catch (Exception e) {
                    logger.error("Failed to leave node", e);
                }
            }
        });
        return node;
    }

    /**
     * Signals current node to leave the graph
     */
    private static void leave() throws Exception {
        System.out.println("NodeId=" + node.getId() + " is disconnecting from the graph...");
        Naming.unbind("rmi://" + node.getHost() + "/NodeRemote" + node.getId());
        StorageUtil.removeFile(node.getId());
        System.out.println("NodeId=" + node.getId() + " disconnected");
        node = null;
        nodeState = NodeState.DISCONNECTED;
    }

    /**
     * Announce JOIN operation to the nodes in the graph
     */
    private static void announceJoin() throws RemoteException {
        System.out.println("Announcing join to nodes=" + Arrays.toString(node.getNodes().entrySet().toArray()));
        node.getNodes().entrySet().parallelStream().filter(n -> n.getKey() != node.getId()).forEach(n -> {
            try {
                RemoteUtil.getRemoteNode(n.getKey(), n.getValue()).addNode(node.getId(), node.getHost());
                System.out.println("Announced join to nodeId=" + n.getKey());
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static void startMoneyTransferring() {
        executor.scheduleAtFixedRate((Runnable) () -> {
            try {
                if (node != null && node.getNodes().size() > 1) {
                    Node randomNode = getRandomNode();
                    int randomAmount = new Random().nextInt(Constants.MAX_AMOUNT + 1) + Constants.MIN_AMOUNT;
                    RemoteUtil.getRemoteNode(node).transferMoney(randomNode.getId(), randomAmount);
                }
            } catch (RemoteException e) {
                logger.error("Failed to transfer to random node!", e);
            }
        }, 0, Constants.TIMEOUT_FREQUENCY, TimeUnit.valueOf(Constants.TIMEOUT_UNIT));
    }

    /**
     * Gets node given nodeId
     *
     * @return currentNode if nodeId is the same, remote node otherwise
     */
    private static Node getRandomNode() {
        int index = new Random().nextInt(node.getNodes().size() - 1);
        int nodeId = node.getNodes().keySet().parallelStream().filter(n -> n != node.getId()).collect(Collectors.toList()).get(index);
        return new Node(nodeId, node.getNodes().get(nodeId));
    }

    /**
     * Starts RMI registry on default port if not started already
     */
    private static void startRMIRegistry() {
        try {
            LocateRegistry.createRegistry(RMI_PORT);
        } catch (RemoteException e) {
            // already started
        }
    }
}
