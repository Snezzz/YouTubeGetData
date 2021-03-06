package bronkerbosh;

import org.gephi.algorithms.shortestpath.DijkstraShortestPathAlgorithm;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Node;
import org.gephi.statistics.plugin.EigenvectorCentrality;
import org.gephi.statistics.plugin.GraphDistance;


import java.util.ArrayList;
import java.util.Collection;


public class BronKerbosh {


    private ArrayList<Node> clique, candidates, not;
    private ArrayList<ArrayList<Node>> cliques;
    private DirectedGraph directedGraph;

    public BronKerbosh(DirectedGraph g){
        clique = new ArrayList<Node>(); //M
        not = new ArrayList<Node>(); //P
        cliques = new ArrayList<ArrayList<Node>>();
        this.directedGraph = g;
        candidates = (ArrayList<Node>) g.getNodes().toCollection();


    }

    public ArrayList<ArrayList<Node>> getCliques(){
        return cliques;
    }
    //A
    private boolean haveAnEdge(ArrayList<Node> newNot, ArrayList<Node> newCandidat){
        if ( newNot.isEmpty() )
            return true;
        //для всех вершин
        for ( Node not: newNot ) {
            for ( Node candidat: newCandidat ) {
                if(directedGraph.getEdge(not,candidat) != null)
                   return true;
            }

        }
        return true;
    }

    //удаляем
    private void remove(ArrayList<Node> newNot, ArrayList<Node> newCand, Node s) {
        if ( newCand.contains(s) ) {
            newCand.remove(s);
            newCand.trimToSize();
        }
        if ( newNot.contains(s) ) {
            newNot.remove(s);
            newNot.trimToSize();
        }
    }

    public void base(){
        algorithmBK(candidates,not);
    }

    private void algorithmBK(ArrayList<Node> candidats, ArrayList<Node> not){


         //пока K и P НЕ содержит вершины, СОЕДИНЕННОЙ СО ВСЕМИ вершинами из K,
        while (!candidats.isEmpty() && haveAnEdge(not, candidats)) {

            Node v = candidats.remove(0); //берем текущего кандидата v
            clique.add(v); // добавляем в список рассматриваемых K

            ArrayList<Node> newCand = new ArrayList<Node>();
            ArrayList<Node> newNot = new ArrayList<Node>();

            remove(newNot,newCand,v); //удаляем текущую вершину из всех списков
            newCand.addAll(candidats);
            newNot.addAll(not);

            //Удаляем из K,P все вершины, НЕсмежные с текущей v
            for (int i = newCand.size()-1; i > -1; i--){
                Collection<Node> neighbours = directedGraph.getNeighbors(v).toCollection();
                Node candidate = newCand.get(i);
                if(!neighbours.contains(candidate)){
                    newCand.remove(i);
                }
                //если есть в соседях = не означает, что смежен
                else if (neighbours.contains(candidate)){
                    //НЕ смежны
                    if (directedGraph.getEdge(v,candidate) == null || directedGraph.getEdge(candidate,v) == null){
                        newCand.remove(i);
                    }
                }
            }
            for (int i = newNot.size()-1; i > -1; i--){
                Collection<Node> neighbours = directedGraph.getNeighbors(v).toCollection();
                Node candidate = newNot.get(i);

                if(!neighbours.contains(candidate)){
                    newNot.remove(i);
                }
                //если есть в соседях = не означает, что смежен
                else if (neighbours.contains(candidate)){
                    //НЕ смежны
                    if (directedGraph.getEdge(v,candidate) == null || directedGraph.getEdge(candidate,v) == null){
                        newNot.remove(i);
                    }
                }
            }

            //если K пусто и P пусто
            if ( newCand.isEmpty() && newNot.isEmpty() ) {
                ArrayList<Node> currentclique = (ArrayList<Node>) clique.clone();
                cliques.add(currentclique);
                }

            else {
                algorithmBK(newCand, newNot);
            }

            //Удаляем последний элемент из множества M;
            clique.remove(v);
            clique.trimToSize();
            //Удаляем из множества K текущую вершину
            candidats.remove(v);
            candidats.trimToSize();
            //Добавляем текущую вершину во множество P
            not.add(v);
        }


    }
}
