package clusters;

import core.GetData;
import main.GraphCreater;
import org.apache.commons.collections4.Get;
import org.apache.commons.collections4.list.TreeList;
import org.gephi.graph.api.*;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.EdgeDirectionDefault;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class Clusters {
    public static DirectedGraph directedGraph;
    public static ProjectController pc;
    public static Workspace workspace;
    public static Map <String, String> nodeId;
    private List<Node> visited;
    public JFrame graphFrame;
    public JFrame infoFrame;

    public Clusters(){
        pc = Lookup.getDefault().lookup(ProjectController.class);
    }

    public void run(int k) throws FileNotFoundException {

        File file = new File("data/"+k+"-cluster.gexf");
        pc.newProject();
        workspace = pc.getCurrentWorkspace();

        ImportController importController = Lookup.getDefault().lookup(ImportController.class);
        Container container;
        Map<String, Set<String>> clustersMap = new HashMap<>();

        boolean exists = false;
        if(file.exists()){
            container = importController.importFile(file);
            container.getLoader().setEdgeDefault(EdgeDirectionDefault.DIRECTED);
            importController.process(container, new DefaultProcessor(), workspace);
            final GraphModel graphModel = Lookup.getDefault()
                    .lookup(GraphController.class).getGraphModel();
            directedGraph = graphModel.getDirectedGraph();
            core.GetData.importTable(clustersMap, k+"-clusters");
            exists = true;
        }
        else {
            getGraph(k);
            System.out.println(directedGraph.getEdges().toArray().length);
            graphFilter();
            System.out.println(directedGraph.getEdges().toArray().length);

            double startTime = System.currentTimeMillis();

            Kraskel kraskel = new Kraskel(directedGraph);
            List<Edge> treeEdges = kraskel.newList();
            directedGraph.removeAllEdges(directedGraph.getEdges().toCollection());
            directedGraph.addAllEdges(treeEdges);

            List<List<Node>> clusters = findClusters(treeEdges);
            System.out.println("Затрачено:"+(System.currentTimeMillis() - startTime)/1000+" c.");

            List<List<Node>> importantClusters = new ArrayList<>();

            int minClustersSize = 2;
            for (List<Node> list : clusters) {
                if (list.size() >=minClustersSize)
                    importantClusters.add(list);
            }
            System.out.println(importantClusters.size());

            System.out.println("Количество вершин:"+ directedGraph.getNodeCount());
            System.out.println("Количество ребер:"+ directedGraph.getEdgeCount());
            //делается для восстановления исходного k-графа (восстанавливаются ребра)
            getGraph(k);

            importantClusters.sort(new Comparator<List>() {
                public int compare(List o1, List o2) {
                    Integer i1 = o1.size();
                    Integer i2 = o2.size();
                    return (i1 < i2 ? -1 : (i1 == i2 ? 0 : 1));
                }
            });
            visualize(importantClusters, directedGraph.getModel());

            GetData.export(k+"-cluster");

            int i = 1;
            for(List<Node> nodes:importantClusters){
                Set<String> clusterSet = new HashSet<>();
                for(Node node: nodes){
                    clusterSet.add(node.getId().toString());
                }
                clustersMap.put("Cluster "+i,clusterSet);
                i++;
            }
            try {
                GetData.createClusterCSV(clustersMap, k+"-clusters");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        JFrame [] frames = GraphCreater.build(workspace,"Clusters",clustersMap, exists);
        graphFrame = frames[0];
        infoFrame = frames[1];
    }

    private static void getNodes(int k){
        boolean end;

        List<Node> nodesList = new TreeList<Node>();

        for (Node n : directedGraph.getNodes().toArray()){
            int count = directedGraph.getOutDegree(n)+directedGraph.getInDegree(n);

            if (count >= k) {
                nodesList.add(n);
            }
        }
        do {
            end = true;

            for (Node n : directedGraph.getNodes().toArray()) {
                if((!nodesList.contains(n))&&(directedGraph.contains(n))){
                    directedGraph.removeNode(n);
                    end = false;

                }
            }
        }
        while(!end);

        nodeId = new HashMap<String, String>();
        int i = 0;
        for (Node n : directedGraph.getNodes().toArray()) {
            n.setLabel(String.valueOf(i));
            nodeId.put(String.valueOf(i),n.getId().toString());
            i++;
        }
        System.out.println(directedGraph.getNodeCount());
        export(String.valueOf(k));
    }

    public static void getGraph(int k) throws FileNotFoundException {
        pc.newProject();
        workspace = pc.getCurrentWorkspace();

        ImportController importController = Lookup.getDefault().lookup(ImportController.class);
        Container container;
        File file = null;
        try{
            file = new File("graphs/"+k+".gexf");
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
        if(file.exists()){
            container = importController.importFile(file);
            importController.process(container, new DefaultProcessor(), workspace);
            final GraphModel graphModel = Lookup.getDefault()
                    .lookup(GraphController.class).getGraphModel();

            directedGraph = graphModel.getDirectedGraph();
        }
        else{
            try {

                file = new File(Clusters.class.getResource("/results/final_2.gexf").toURI());
                container = importController.importFile(file);
                container.getLoader().setEdgeDefault(EdgeDirectionDefault.DIRECTED);

            } catch (Exception ex) {
                ex.printStackTrace();
                return;
            }
            importController.process(container, new DefaultProcessor(), workspace);
            final GraphModel graphModel = Lookup.getDefault()
                    .lookup(GraphController.class).getGraphModel();

            directedGraph = graphModel.getDirectedGraph();
            getNodes(k);
        }

    }

    private void graphFilter(){
        Edge[] edges = directedGraph.getEdges().toArray();
        List<Edge> toRemove = new ArrayList<Edge>();

        for (Edge edge: edges){
            Node sourse = edge.getSource();
            Node target = edge.getTarget();
            double weight = edge.getWeight();

            Edge currentEdge = directedGraph.getEdge(target,sourse); //нашли исходящее
            if ((currentEdge!= null) && !toRemove.contains(currentEdge) && (!toRemove.contains(edge))) {
                double currengtWeight = currentEdge.getWeight();
                edge.setWeight(weight+currengtWeight);
                toRemove.add(currentEdge);
            }
            else if (currentEdge == null){
                toRemove.add(edge);
            }
        }
        directedGraph.removeAllEdges(toRemove);
    }

    private List<List<Node>> findClusters(List<Edge> treeEdges){

        List<List<Node>> clusters = null;
         clusters = new ArrayList<>();
         boolean end = false;
         while (!end) {
             Edge edge = treeEdges.get(treeEdges.size()-1);
             Node sourse = edge.getSource();
             Node target = edge.getTarget();

             findAndRemove(clusters,edge);
             directedGraph.removeEdge(edge);
             treeEdges.remove(edge);

             List<Node> cluster2 = new ArrayList<>();
             List<Node> cluster1 = new ArrayList<>();


             Node neighbour = sourse;
             visited = new ArrayList<>();
             DFS(neighbour);
             cluster1 = (List<Node>) visited;

             neighbour = target;
             visited = new ArrayList<>();
             DFS(neighbour);

             cluster2 = (List<Node>) visited;


             clusters.add(cluster1);
             clusters.add(cluster2);

             for (List<Node> list: clusters){
                 if(list.size() > 10 && list.size() < 12)
                     end = true;

         }

         }

        return clusters;
     }

    private void DFS(Node node)
    {
         visited.add(node);

        List<Node> neighbours = (List<Node>) directedGraph.getNeighbors(node).toCollection();
        for(Node neighbour:neighbours)
        {
            if (!visited.contains(neighbour))
                DFS(neighbour);
        }
    }

    private void findAndRemove( List<List<Node>> clusters, Edge edge) {
        if (clusters.size() != 0) {

            for (int i = clusters.size() - 1; i > -1; i--) {
                Node source = edge.getSource();
                Node target = edge.getTarget();
                if (clusters.get(i).contains(source) && (clusters.get(i).contains(target)))
                    clusters.remove(i);
            }
        }
    }

    private void visualize(List<List<Node>> importantClusters, GraphModel graphModel){

        Column modCol = graphModel.getNodeTable().addColumn("clusters", "Clusters",
                Integer.class, new Integer(0));

        for (Node node: graphModel.getDirectedGraph().getNodes()){
            node.setColor(Color.gray);
            node.setSize(2.0f);
            node.setAttribute(modCol,-1);
        }

        Color[] colors = new Color[importantClusters.size()];
        Random rand = new Random();
        for(int k = 0; k < colors.length; k++){
            float r = (float) (rand.nextFloat() / 2f + 0.5);
            float g = (float) (rand.nextFloat() / 2f + 0.5);
            float b = (float) (rand.nextFloat() / 2f + 0.5);
            Color color = new Color(r,g,b);
            colors[k] = color;
        }

        Set<Integer> sizes = new TreeSet<>();
        int minSize = 10;
        int i = 0;
        int step = 5;
        Map <Integer, Integer> sizesMap = new HashMap<>();

        for (List<Node> nodesList: importantClusters) {
            sizes.add(nodesList.size());
        }

        for (Integer size: sizes){
            int newSize = minSize + step;
            sizesMap.put(size, newSize);
            step = step + 5;
            i++;
        }

        i = 0;
        for (List<Node> nodesList: importantClusters) {
            Color color = colors[i];
            int count = nodesList.size();
            int size = sizesMap.get(count);
            for (Node node : nodesList) {
                Node nodeModel = graphModel.getDirectedGraph().getNode(node.getId());
                nodeModel.setColor(color);
                nodeModel.setSize(size);
                nodeModel.setAttribute(modCol, i);
            }

            i++;
        }
    }
        public static Comparator<List> listComparator = new Comparator<List>() {

        @Override
        public int compare(List list1, List list2) {
            return (int) (list1.size() - list2.size());
        }
    };

    public static void export(String fileName) {
        ExportController ec = Lookup.getDefault().lookup(ExportController.class);
        try {
            ec.exportFile(new File("graphs/"+fileName + ".gexf"));
        } catch
                (IOException ex) {
            ex.printStackTrace();
            return;
        }

    }
}
