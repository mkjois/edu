/* Kruskal.java */

import graph.*;
import set.*;
import dict.*;
import list.*;

/**
 * The Kruskal class contains the method minSpanTree(), which implements
 * Kruskal's algorithm for computing a minimum spanning tree of a graph.
 */

public class Kruskal {

  /**
   * minSpanTree() returns a WUGraph that represents the minimum spanning tree
   * of the WUGraph g.  The original WUGraph g is NOT changed.
   */
  public static WUGraph minSpanTree(WUGraph g) {
    WUGraph result = new WUGraph();
    Object[] vertices = g.getVertices();
    DList edges = new DList();
    HashTable vertexMap = new HashTable(vertices.length);
    DisjointSets paths = new DisjointSets(vertices.length);
    int i = 0, j = 0;
    Neighbors neighbors = null;
    for (i = 0; i < vertices.length; i++) {
      result.addVertex(vertices[i]);
      neighbors = g.getNeighbors(vertices[i]);
      for (j = 0; j < neighbors.neighborList.length; j++) {
        edges.add(new KruskalEdge(neighbors.weightList[j],
                                  vertices[i],
                                  neighbors.neighborList[j]));
      }
      vertexMap.insert(vertices[i], i);
    }
    sortEdges(edges);
    KruskalEdge ke = null;
    int root1, root2;
    for (Object item : edges) {
      ke = (KruskalEdge) item;
      //System.out.println("KE weight: "+ke.weight);
      root1 = paths.find((int) vertexMap.find(ke.vertex1).value());
      root2 = paths.find((int) vertexMap.find(ke.vertex2).value());
      if (root1 != root2) {
        paths.union(root1, root2);
        result.addEdge(ke.vertex1, ke.vertex2, ke.weight);
      }
    }
    return result;
  }
  private static void sortEdges(DList edges) {
    if (edges.length() <= 1) {
      return;
    }
    DList smaller = new DList();
    DList equal = new DList();
    DList larger = new DList();
    partitionList(edges, (Comparable)  edges.get(0), smaller, equal, larger);
    sortEdges(smaller);
    sortEdges(larger);
    edges.append(smaller);
    edges.append(equal);
    edges.append(larger);
  }
  private static void partitionList(DList in, Comparable pivot,
                                    DList smaller, DList equal, DList larger) {
    Comparable item = null;
    int comparison = 0;
    while (!in.isEmpty()) {
      item = (Comparable) in.pop();
      comparison = item.compareTo(pivot);
      if (comparison < 0) {
        smaller.add(item);
      } else if (comparison > 0) {
        larger.add(item);
      } else {
        equal.add(item);
      }
    }
  }
}
