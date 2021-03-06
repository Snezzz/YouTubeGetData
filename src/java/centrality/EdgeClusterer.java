package centrality;

import com.google.common.base.Function;
import edu.uci.ics.jung.algorithms.cluster.WeakComponentClusterer;
import edu.uci.ics.jung.algorithms.scoring.BetweennessCentrality;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Pair;
import org.apache.commons.collections15.Transformer;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class EdgeClusterer<V, E> implements Function<Graph<V, E>, Set<Set<V>>> {
    private int mNumEdgesToRemove;
    private Map<E, Pair<V>> edges_removed;

    public EdgeClusterer(int numEdgesToRemove) {
        this.mNumEdgesToRemove = numEdgesToRemove;
        this.edges_removed = new LinkedHashMap();
    }

    public Set<Set<V>> apply(Graph<V, E> graph) {
        if (this.mNumEdgesToRemove >= 0 && this.mNumEdgesToRemove <= graph.getEdgeCount()) {
            this.edges_removed.clear();

            for(int k = 0; k < this.mNumEdgesToRemove; ++k) {
                BetweennessCentrality<V, E> bc =
                        new BetweennessCentrality<V, E>(graph);
                E to_remove = null;
                double score = 0.0D;
                Iterator var7 = graph.getEdges().iterator();
                while(var7.hasNext()) {
                    E e = (E) var7.next();
                    System.out.println(bc.getEdgeScore(e));

                    if (Math.abs(bc.getEdgeScore(e)) > Math.abs(score)) {
                        to_remove = e;
                        score = Math.abs(bc.getEdgeScore(e));
                    }
                }

                this.edges_removed.put(to_remove, graph.getEndpoints(to_remove));
                graph.removeEdge(to_remove);
            }

            WeakComponentClusterer<V, E> wcSearch = new WeakComponentClusterer();
            Set<Set<V>> clusterSet = wcSearch.transform(graph);
            Iterator var11 = this.edges_removed.entrySet().iterator();

            while(var11.hasNext()) {
                Entry<E, Pair<V>> entry = (Entry)var11.next();
                Pair<V> endpoints = (Pair)entry.getValue();
                graph.addEdge(entry.getKey(), endpoints.getFirst(), endpoints.getSecond());
            }

            return clusterSet;
        } else {
            throw new IllegalArgumentException("Invalid number of edges passed in.");
        }
    }

    public List<E> getEdgesRemoved() {
        return new ArrayList(this.edges_removed.keySet());
    }
}
