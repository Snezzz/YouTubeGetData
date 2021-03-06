package clusters;

import org.apache.commons.collections4.list.TreeList;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.graph.impl.EdgeImpl;

import java.util.*;

public class Kraskel {
    int n; //количество вершин
    List<List<Integer>> matrix;
    Map<Integer,String> help;
    List<List<Node>> V_s; // множество вершин
    int k=0;
    int current;
    Node[] nodes;
    Edge[] edges;
    DirectedGraph directedGraph;
    Graph nonDirectedGraph;

    public Kraskel(DirectedGraph graph){
        this.n = graph.getNodeCount();
        matrix = new ArrayList<List<Integer>>();

        this.directedGraph = graph;
        this.nodes = graph.getNodes().toArray();
        this.edges = directedGraph.getEdges().toArray();
        help = new HashMap<Integer, String>();
        V_s = new ArrayList<>();
    }

    public List<List<Integer>> getMatrix() {
        return matrix;
    }

    public List<Edge> newList(){


        Map<Integer,Edge> result = sort(this.edges);
        for(Edge edge: this.edges){
            System.out.println(edge.getId()+":"+ edge.getWeight());
        }

        List<Edge> way = findWay(result);
        return way;
    }



    private Map<Integer,Edge> sort(Edge [] edges){

        mergeSort(this.edges,this.edges.length);

        Map<Integer,Edge> sortedList = new HashMap<Integer, Edge>();
        for (int i = 0; i < this.edges.length; i++){
            sortedList.put(i,edges[i]);
        }
        return sortedList;
    }

    private void mergeSort(Edge[] a, int n) {
        if (n < 2) {
            return;
        }
        int mid = n / 2;
        Edge[] l = new Edge[mid];
        Edge[] r = new Edge[n - mid];

        for (int i = 0; i < mid; i++) {
            l[i] = a[i];
        }
        for (int i = mid; i < n; i++) {
            r[i - mid] = a[i];
        }
        mergeSort(l, mid);
        mergeSort(r, n - mid);

        merge(a, l, r, mid, n - mid);
    }

    private void merge(
            Edge[] a, Edge[] l, Edge[] r, int left, int right) {

        int i = 0, j = 0, k = 0;
        while (i < left && j < right) {
            if (l[i].getWeight() >= r[j].getWeight()) {
                a[k++] = l[i++];
            }
            else {
                a[k++] = r[j++];
            }
        }
        while (i < left) {
            a[k++] = l[i++];
        }
        while (j < right) {
            a[k++] = r[j++];
        }
    }

    boolean find(Node a,Node b,List<List<Node>> V_s){

        for(int i=0; i<V_s.size();i++) {
            if ((V_s.get(i).contains(a)) && (V_s.get(i).contains(b)))
                return true; //в одном множестве

        }
        return false; //в разныъ множествах
    }


    //остовное дерево
    List<Edge> findWay (Map<Integer,Edge> sortedList){
            List<Edge> way = new ArrayList<Edge>(); //список ребер остовного дерева

        //на нулевом шаге в каждом элементе множества V_s по одной вершине
            for(int i=0; i < nodes.length; i++){
                List<Node> list = new ArrayList<Node>();
                list.add(nodes[i]);
                V_s.add(list);
            }
            int k = 0;
            System.out.println(sortedList.size());
            //Перебираем все отсортированные ребра
            for(Map.Entry<Integer,Edge> map : sortedList.entrySet()){
                //(a,b) = Ребро e
                Node a = map.getValue().getSource();
                Node b = map.getValue().getTarget();
                //если не перебрались все ребра и нет связного графа
                if ((V_s.size() > 1)&&(sortedList.size() != 0)) {
                    //если концы ребра принадлежат РАЗНЫМ множествам вершин
                    if (!find(a, b, V_s)) {
                        List<Node> list1 = new ArrayList<Node>(),list2 = new ArrayList<Node>();

                        for (int i=0; i < V_s.size(); i++){
                            for (int j=0; j < V_s.get(i).size(); j++) {

                                if (V_s.get(i).get(j) == a) {
                                    list1 = V_s.get(i);

                                }
                                if (V_s.get(i).get(j) == b) {
                                    list2 = V_s.get(i);
                                }
                            }
                        }
                        //объединяем подмножества во вножество и обновляем множество V_s
                            List<Node> list3 = new ArrayList<Node>(list1.size()+list2.size());
                            list3.addAll(list1);
                            list3.addAll(list2);
                            V_s.remove(list1);
                            V_s.remove(list2);
                            V_s.add(list3);
                        //добавляем одобренную дугу во множество дуг остовного графа
                        way.add(map.getValue());
                    }
                    else{
                        System.out.println("Не подходит ребро ("+a.getLabel()+","+b.getLabel()+")");
                    }
                }
                else
                    break;
            }

        System.out.println("Результат: ребро + вес");
            int sum = 0;
            for (int i = 0; i < way.size(); i++){

                System.out.println("("+way.get(i)+"), вес:"+
                      this.edges[i].getWeight());
                sum += this.edges[i].getWeight();
              }

            System.out.println("Минимальная стоимость:"+sum);
            return way;
        }

}
