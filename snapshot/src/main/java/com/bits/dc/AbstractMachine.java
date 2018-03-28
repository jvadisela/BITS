package com.bits.dc;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import com.bits.dc.model.Node;
import com.bits.dc.rmi.RemoteNode;
import com.bits.dc.utils.RMIUtils;
import com.bits.dc.utils.StorageUtil;

public class AbstractMachine {

	static final int RMI_PORT = ServiceConfiguration.getRmiPort();

	@Nullable
	private static Node node;

	private static NodeState nodeState = NodeState.DISCONNECTED;

	/**
	 * Thread pool scheduler of N threads for money transfer and snapshot taking
	 */
	private static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();


	/**
	 * Signals current node to create the graph
	 *
	 * @param nodeHost
	 *            host for new current node
	 * @param nodeId
	 *            id for new current node
	 */
	public static void create(String nodeHost, int nodeId) throws Exception {
		if (nodeState != NodeState.DISCONNECTED) {
			System.out.println("Must be DISCONNECTED to create! Current nodeState=" + nodeState);
			return;
		}
		if (nodeId <= 0) {
			System.out.println("Node id must be positive integer [ nodeID > 0 ] !");
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
	 * Signals current node to join the graph: - accumulate the graph structure
	 * of all available banks from the existing node - start randomly
	 * sending/accepting money transfers
	 * <p>
	 * Existing node MUST be operational!
	 *
	 * @param nodeHost
	 *            host for new current node
	 * @param nodeId
	 *            id for new current node
	 * @param existingNodeHost
	 *            of node in the graph to fetch data from
	 * @param existingNodeId
	 *            of node in the graph to fetch data from
	 */
	public static void join(String nodeHost, int nodeId, String existingNodeHost, int existingNodeId)
			throws Exception {
		if (nodeState != NodeState.DISCONNECTED) {
			System.out.println("Must be DISCONNECTED to join! Current nodeState=" + nodeState);
			return;
		}
		if (nodeId <= 0) {
			System.out.println("Node id must be positive integer [ nodeID > 0 ] !");
			return;
		}
		startRMIRegistry();
		System.out.println("NodeId=" + nodeId + " connects to existing nodeId=" + existingNodeId);
		Node existingNode = RMIUtils.getRemoteNode(existingNodeId, existingNodeHost).getNode();
		if (existingNode.getNodes().isEmpty()) {
			System.out.println("Existing node must be operational!");
			return;
		}
		if (existingNode.getNodes().containsKey(nodeId)) {
			System.out.println("Cannot join as nodeId=" + nodeId + " already taken!");
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
			System.out.println("Must be CONNECTED to view topology! Current nodeState=" + nodeState);
			return;
		}
		System.out.println("Viewing topology from node=" + node);
		node.getNodes().entrySet().forEach(n -> {
			try {
				RMIUtils.getRemoteNode(n.getKey(), n.getValue()).getNode();
			} catch (RemoteException e) {
				throw new RuntimeException(e);
			}
		});
	}

	/**
	 * Initiate distributed snapshot to all known nodes (all nodes are
	 * interconnected as a digraph)
	 */
	public static void cut() throws RemoteException {
		if (nodeState != NodeState.CONNECTED) {
			System.out.println("Must be CONNECTED to initiate the distributed snapshot! Current nodeState=" + nodeState);
			return;
		}
		System.out.println("Starting distributed snapshot from node=" + node);
		RMIUtils.getRemoteNode(node).receiveMarker(node.getId());
	}

	/**
	 * Registers RMI for new node, initializes node object
	 *
	 * @param id
	 *            of the new node
	 * @param host
	 *            of the new node
	 */
	
	private static Node register(int id, String host) throws Exception {
		System.setProperty("java.rmi.server.hostname", host);
		Node node = new Node(id, host);
		Naming.bind("rmi://" + node.getHost() + "/NodeRemote" + node.getId(), new RemoteNode(node));
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				System.out.println("Auto-leaving process initiated...");
				try {
					if (nodeState == NodeState.CONNECTED) {
						leave();
					}
				} catch (Exception e) {
					System.out.println("Failed to leave node");
					e.printStackTrace();
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
				RMIUtils.getRemoteNode(n.getKey(), n.getValue()).addNode(node.getId(), node.getHost());
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
					RMIUtils.getRemoteNode(node).transferMoney(randomNode.getId(), randomAmount);
				}
			} catch (RemoteException e) {
				System.out.println("Failed to transfer to random node!");
				e.printStackTrace();
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
		int nodeId = node.getNodes().keySet().parallelStream().filter(n -> n != node.getId())
				.collect(Collectors.toList()).get(index);
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
