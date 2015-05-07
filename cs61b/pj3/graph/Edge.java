package graph;

import list.DListNode;

/**
 * Our internal edge representation. Stores 2 internal Vertex objects,
 * the edge's weight, and both references to DListNodes containing
 * one Vertex in the other's adjacency list.
 */
public class Edge {

	private Vertex v1, v2;
	private DListNode V2refToV1, V1refToV2;
	private int weight;
	
	/**
	 * Edge (Vertex, Vertex, int)
     *
	 * Constructs an edge that is used to join internal vertices. 
     *
	 * @param v1 The first vertex.
	 * @param v2 The second vertex.
	 * @param weight The edge's weight.
	 */
	Edge (Vertex v1, Vertex v2, int weight) {
		this.v1 = v1;
		this.v2 = v2;
		this.weight = weight;
		this.V2refToV1 = null;
		this.V1refToV2 = null;
	}

	/**
	 * isSelfEdge () -- boolean
     *
     * Checks to see if the two vertices on this edge are the same object.
     *
	 * @return True if both ends point to the same Vertex, false otherwise.
	 */
	boolean isSelfEdge () {
		return (this.v1 == this.v2);
	}

	/**
	 * weight () -- int
     *
     * Returns the weight of this edge.
     *
	 * @return The weight of this Edge.
	 */
	int weight () {
		return this.weight;
	}
    
    /**
     * setWeight (int)
     *
     * Sets the weight of this edge.
     *
     * @param weight The weight to set.
     */
    void setWeight (int weight) {
        this.weight = weight;
    }

	/**
	 * vertices () -- Vertex[]
     *
     * Returns a Vertex[2] with the 2 vertices incident upon this edge.
     *
	 * @return A Vertex array of the two Vertices connected to this Edge.
	 */
	Vertex[] vertices () {
		Vertex[] vertArray = {this.v1, this.v2}; 
		return vertArray;
	}
	
	/**
	 * adjacentVertex (Vertex) -- Vertex
     *
	 * Given a vertex on this edge, returns the adjacent vertex.
     *
	 * @param v One of the two Vertices on this Edge.
	 * @return The other Vertex on this Edge.
	 */
	Vertex adjacentVertex (Vertex v) {
		if (v == this.v1) {
			return this.v2;
		} else {
			return this.v1;
		}
	}
	
    /**
     * adjacentListRef (Vertex) -- DListNode
     *
     * Given a vertex on this edge, returns the reference to the node containing
     * that vertex in the other vertex's adjacency list.
     *
     * @param v One of the two Vertices on this Edge.
     * @return The DListNode in the other Vertex's adjacency list that stores
     *     the given Vertex.
     */
	DListNode adjacentListRef (Vertex v) {
		if (v == this.v1) {
			return this.V2refToV1;
		} else {
            return this.V1refToV2;
        }
	}

	/**
	 * setRefToAdjacent (Vertex, DListNode)
     *
     * Sets the reference one vertex has to the other in its adjacency list.
     * If this is a self-edge, both references are set to the same node.
     *
	 * @param v One of the two Vertices connected by this Edge.
	 * @param ref The DListNode in the given Vertex's adjacency list that stores
     *     the other Vertex.
	 */
	void setRefToAdjacent (Vertex v, DListNode ref) {
        if (v == this.v1 && v == this.v2) {
            this.V1refToV2 = ref;
            this.V2refToV1 = ref;
        } else if (v == this.v1) {
			this.V1refToV2 = ref;
		} else if (v == this.v2) {
            this.V2refToV1 = ref;
        }
	}

	/**
	 * reset ()
     *
	 * Sets all fields of this edge to null to improve memory usage.
	 */
	void reset () {
		this.v1 = null;
		this.v2 = null;
		this.V2refToV1 = null;
		this.V1refToV2 = null;
	}
}
