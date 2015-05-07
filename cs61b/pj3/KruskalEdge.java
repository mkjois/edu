public class KruskalEdge implements Comparable<KruskalEdge> {
  int weight;
  Object vertex1;
  Object vertex2;

  public KruskalEdge(int weight, Object vertex1, Object vertex2) {
    this.weight = weight;
    this.vertex1 = vertex1;
    this.vertex2 = vertex2;
  }

  public boolean includesVertex(Object vertex) {
    return vertex == vertex1 || vertex == vertex2;
  }

  public boolean equals(KruskalEdge ke) {
    if (weight == ke.weight) {
      if (ke.vertex1 == vertex1) {
        return ke.vertex2 == vertex2;
      } else if (ke.vertex2 == vertex1) {
        return ke.vertex1 == vertex2;
      } else {
        return false;
      }
    } else {
      return false;
    }
  }

  public int compareTo(KruskalEdge ke) {
    return weight - ke.weight;
  }
}
