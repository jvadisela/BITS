package com.bits.dc;

import com.bits.dc.model.Node;

/**
 * Represents different states the node can be in
 *
 * @see Node
 */
public enum NodeState {

    /**
     * Node is currently operational
     */
    CONNECTED,

    /**
     * Node is currently NOT operational
     */
    DISCONNECTED
}
