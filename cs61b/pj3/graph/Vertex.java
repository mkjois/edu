package graph;

import list.*;
import java.util.*;

/**
 * Our internal vertex representation. Stores the application vertex object,
 * the adjacency list of all edges incident upon the vertex, and the reference
 * to the DListNode containing the vertex in its graph's vertex list.
 */
public class Vertex implements Iterable<Edge> {
	
	/**
	 * EdgeIterator
     *
     * The iterator of Edge objects over a Vertex.
	 */
	private class EdgeIterator implements Iterator<Edge> {

        private Iterator<Object> iter;

	    private EdgeIterator (Vertex vertex) {
            this.iter = vertex.edgeList.iterator();
	    }

	    public boolean hasNext () {
            return this.iter.hasNext();
	    }

	    public Edge next () {
            return (Edge) this.iter.next();
	    }

	    public void remove () {
	    }
	}
	
	private Object item;
	private DList edgeList;
	private DListNode graphRef;
	
	/**
	 * Vertex (Object, DListNode)
     *
	 * Constructs an internal vertex object. 
     *
	 * @param item The application vertex object to store.
	 * @param graphRef The reference to the DListNode containing the stored
     *     object in the graph's list of vertices.
	 */
	Vertex (Object item, DListNode graphRef) {
		this.item = item;
		this.edgeList = new DList();
		this.graphRef = graphRef;
	}
	
	/**
	 * item () -- Object
     *
     * Returns the application object this vertex stores.
     *
	 * @return The stored object.
	 */
	Object item () {
		return this.item;
	}
	
	/**
	 * degree () -- int
     *
	 * Returns the degree of this vertex: the number of other vertices connected
     * to this vertex by an edge.
     *
	 * @return The number of Vertices connected to this Vertex.
	 */
	int degree () {
		return this.edgeList.length();
	}

	/**
	 * graphRef () -- DListNode
     *
     * Returns the DListNode in the graph's vertex list that stores this vertex.
     *
	 * @return The reference to this vertex in the graph's vertex list.
	 */
	DListNode graphRef () {
		return this.graphRef;
	}

	/**
	 * addEdge (Edge)
     *
	 * Adds an incident edge to this vertex's adjacency list and sets the
     * appropriate references.
     *
	 * @param edge The Edge (which contains this Vertex) to add.
	 */
	void addEdge (Edge edge) {
		DListNode refToAdjacent = this.edgeList.addNode(edge);
		edge.setRefToAdjacent(this, refToAdjacent);
	}
	
	/**
	 * removeEdge (DListNode)
     *
     * Removes the edge stored in the given node from this vertex's
     * adjacency list.
     *
	 * @param node The DListNode containing the Edge to remove.
	 */
	void removeEdge (DListNode node) {
		this.edgeList.removeNode(node);
	}

	/**
	 * iterator () -- Iterator<Edge>
     *
	 * Returns an iterator for edges in this vertex's adjacency list.
     *
	 * @return An iterator of Edges over this Vertex.
	 */
	public Iterator<Edge> iterator () {
		return new EdgeIterator(this);
	}

	/**
	 * reset ()
     *
	 * Sets all fields of this vertex to null to improve memory usage.
	 */
	void reset () {
		this.item = null;
		this.edgeList = null;
		this.graphRef = null;
	}
}
