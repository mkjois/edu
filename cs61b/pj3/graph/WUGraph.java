/* WUGraph.java */

package graph;

import dict.*;
import list.*;

/**
 * The WUGraph class represents a weighted, undirected graph.  Self-edges are
 * permitted.
 */
public class WUGraph {

  private static final int NONVERTEX = 0, NONEDGE = 0;

  // verts: <Object -> Vertex>
  // edges: <VertexPair -> Edge>
  private HashTable verts, edges;
  private DList objects;

  /**
   * WUGraph() constructs a graph having no vertices or edges.
   *
   * Running time:  O(1).
   */
  public WUGraph() {
      this.verts = new HashTable();
      this.edges = new HashTable();
      this.objects = new DList();
  }

  /**
   * vertexCount() returns the number of vertices in the graph.
   *
   * Running time:  O(1).
   */
  public int vertexCount() {
      return this.verts.size();
  }

  /**
   * edgeCount() returns the number of edges in the graph.
   *
   * Running time:  O(1).
   */
  public int edgeCount() {
      return this.edges.size();
  }

  /**
   * getVertices() returns an array containing all the objects that serve
   * as vertices of the graph.  The array's length is exactly equal to the
   * number of vertices.  If the graph has no vertices, the array has length
   * zero.
   *
   * (NOTE:  Do not return any internal data structure you use to represent
   * vertices!  Return only the same objects that were provided by the
   * calling application in calls to addVertex().)
   *
   * Running time:  O(|V|).
   */
  public Object[] getVertices() {
      Object[] result = new Object[this.objects.length()];
      int index = 0;
      for (Object o : this.objects) {
          result[index] = o;
          index++;
      }
      return result;
  }

  /**
   * addVertex() adds a vertex (with no incident edges) to the graph.  The
   * vertex's "name" is the object provided as the parameter "vertex".
   * If this object is already a vertex of the graph, the graph is unchanged.
   *
   * Running time:  O(1).
   */
  public void addVertex(Object vertex) {
      Entry entry = this.verts.find(vertex);
      if (entry != null) { // already in graph
          return;
      }
      DListNode graphReference = this.objects.addNode(vertex);
      Vertex realVertex = new Vertex(vertex, graphReference);
      this.verts.insert(vertex, realVertex);
  }

  /**
   * removeVertex() removes a vertex from the graph.  All edges incident on the
   * deleted vertex are removed as well.  If the parameter "vertex" does not
   * represent a vertex of the graph, the graph is unchanged.
   *
   * Running time:  O(d), where d is the degree of "vertex".
   */
  public void removeVertex(Object vertex) {
      Entry entry = this.verts.remove(vertex); // remove from graph's vertex table
      if (entry == null) { // vertex not in graph
          return;
      }
      Vertex v = (Vertex) entry.value(), adjacent;
      DListNode graphReference = v.graphRef(), adjacentReference;
      this.objects.removeNode(graphReference); // remove from graph's object list
      VertexPair pair;
      for (Edge edge : v) {
          adjacent = edge.adjacentVertex(v);
          pair = new VertexPair(vertex, adjacent.item());
          this.edges.remove(pair); // remove from graph's edge table
          if (edge.isSelfEdge()) {
              continue;
          } else {
              adjacentReference = edge.adjacentListRef(v);
              adjacent.removeEdge(adjacentReference); // remove from each edge's list
          }
      }
      v.reset();
  }

  /**
   * isVertex() returns true if the parameter "vertex" represents a vertex of
   * the graph.
   *
   * Running time:  O(1).
   */
  public boolean isVertex(Object vertex) {
      return (this.verts.find(vertex) != null);
  }

  /**
   * degree() returns the degree of a vertex.  Self-edges add only one to the
   * degree of a vertex.  If the parameter "vertex" doesn't represent a vertex
   * of the graph, zero is returned.
   *
   * Running time:  O(1).
   */
  public int degree(Object vertex) {
      Entry entry = this.verts.find(vertex);
      if (entry == null) {
          return NONVERTEX;
      }
      return ((Vertex) entry.value()).degree();
  }

  /**
   * getNeighbors() returns a new Neighbors object referencing two arrays.  The
   * Neighbors.neighborList array contains each object that is connected to the
   * input object by an edge.  The Neighbors.weightList array contains the
   * weights of the corresponding edges.  The length of both arrays is equal to
   * the number of edges incident on the input vertex.  If the vertex has
   * degree zero, or if the parameter "vertex" does not represent a vertex of
   * the graph, null is returned (instead of a Neighbors object).
   *
   * The returned Neighbors object, and the two arrays, are both newly created.
   * No previously existing Neighbors object or array is changed.
   *
   * (NOTE:  In the neighborList array, do not return any internal data
   * structure you use to represent vertices!  Return only the same objects
   * that were provided by the calling application in calls to addVertex().)
   *
   * Running time:  O(d), where d is the degree of "vertex".
   */
  public Neighbors getNeighbors(Object vertex) {
      Entry entry = this.verts.find(vertex);
      if (entry == null || ((Vertex) entry.value()).degree() == 0) {
          return null;
      }
      Vertex v = ((Vertex) entry.value()), adjacent;
      Neighbors result = new Neighbors();
      result.neighborList = new Object[v.degree()];
      result.weightList = new int[v.degree()];
      int index = 0;
      for (Edge edge : v) {
          adjacent = edge.adjacentVertex(v);
          result.neighborList[index] = adjacent.item();
          result.weightList[index] = edge.weight();
          index++;
      }
      return result;
  }

  /**
   * addEdge() adds an edge (u, v) to the graph.  If either of the parameters
   * u and v does not represent a vertex of the graph, the graph is unchanged.
   * The edge is assigned a weight of "weight".  If the edge is already
   * contained in the graph, the weight is updated to reflect the new value.
   * Self-edges (where u == v) are allowed.
   *
   * Running time:  O(1).
   */
  public void addEdge(Object u, Object v, int weight) {
      Entry e1 = this.verts.find(u), e2 = this.verts.find(v);
      if (e1 == null || e2 == null) {
          return;
      }
      Vertex v1 = (Vertex) e1.value(), v2 = (Vertex) e2.value();
      VertexPair pair = new VertexPair(u, v);
      Entry e3 = this.edges.find(pair);
      Edge edge;
      if (e3 != null) {
          edge = (Edge) e3.value();
          edge.setWeight(weight);
          return;
      }
      edge = new Edge(v1, v2, weight);
      v1.addEdge(edge);
      if (! edge.isSelfEdge()) {
          v2.addEdge(edge);
      }
      this.edges.insert(pair, edge);
  }

  /**
   * removeEdge() removes an edge (u, v) from the graph.  If either of the
   * parameters u and v does not represent a vertex of the graph, the graph
   * is unchanged.  If (u, v) is not an edge of the graph, the graph is
   * unchanged.
   *
   * Running time:  O(1).
   */
  public void removeEdge(Object u, Object v) {
      VertexPair pair = new VertexPair(u, v);
      Entry entry = this.edges.remove(pair);
      if (entry == null) {
          return;
      }
      Edge edge = (Edge) entry.value();
      Vertex[] vertices = edge.vertices();
      Vertex v1 = vertices[0], v2 = vertices[1];
      DListNode refToV1 = edge.adjacentListRef(v1);
      DListNode refToV2 = edge.adjacentListRef(v2);
      v1.removeEdge(refToV2);
      v2.removeEdge(refToV1);
      edge.reset();
  }

  /**
   * isEdge() returns true if (u, v) is an edge of the graph.  Returns false
   * if (u, v) is not an edge (including the case where either of the
   * parameters u and v does not represent a vertex of the graph).
   *
   * Running time:  O(1).
   */
  public boolean isEdge(Object u, Object v) {
      VertexPair pair = new VertexPair(u, v);
      return (this.edges.find(pair) != null);
  }

  /**
   * weight() returns the weight of (u, v).  Returns zero if (u, v) is not
   * an edge (including the case where either of the parameters u and v does
   * not represent a vertex of the graph).
   *
   * (NOTE:  A well-behaved application should try to avoid calling this
   * method for an edge that is not in the graph, and should certainly not
   * treat the result as if it actually represents an edge with weight zero.
   * However, some sort of default response is necessary for missing edges,
   * so we return zero.  An exception would be more appropriate, but
   * also more annoying.)
   *
   * Running time:  O(1).
   */
  public int weight(Object u, Object v) {
      VertexPair pair = new VertexPair(u, v);
      Entry entry = this.edges.find(pair);
      if (entry == null) {
          return NONEDGE;
      }
      return ((Edge) entry.value()).weight();
  }
}
