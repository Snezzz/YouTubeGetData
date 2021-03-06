package centrality;

import clusters.Clusters;
import core.GetData;
import edu.uci.ics.jung.algorithms.cluster.EdgeBetweennessClusterer;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import main.GraphCreater;
import org.apache.commons.collections4.list.TreeList;
import org.gephi.graph.api.*;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.EdgeDirectionDefault;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.layout.api.LayoutModel;
import org.gephi.layout.plugin.scale.Expand;
import org.gephi.layout.plugin.scale.ScaleLayout;
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

public class NewmanAndGirvan {
    public static DirectedGraph directedGraph;
    public static ProjectController pc;
    public static Workspace workspace;
    public static Map<String, String> nodeId;
    public JFrame graphFrame;
    public JFrame infoFrame;

    public NewmanAndGirvan(){
        pc = Lookup.getDefault().lookup(ProjectController.class);
    }

    public static Comparator<Set> listComparator = new Comparator<Set>() {
        @Override
        public int compare(Set list1, Set list2) {
            return (int) (list1.size() - list2.size());
        }
    };

    public void run(int k) throws FileNotFoundException {

        File file = new File("data/"+k+"-NAGClusters.gexf");
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
            core.GetData.importTable(clustersMap, k+"-NAGClusters");
            exists = true;
        }
        else {
            getGraph(k);

            double startTime = System.currentTimeMillis();
            Set<Set<Node>> clusters = findClusters(100);
            System.out.println("Затрачено:"+(System.currentTimeMillis() - startTime)/1000+" c.");

            List<Set<Node>> importantClusters = new ArrayList<>();

            for(Set<Node> set: clusters){
                if (set.size() > 1){
                    importantClusters.add(set);
                }
            }
            System.out.println("clusters size:"+importantClusters.size());

            importantClusters.sort(new Comparator<Set>() {
                public int compare(Set o1, Set o2) {
                    Integer i1 = o1.size();
                    Integer i2 = o2.size();
                    return (i1 < i2 ? -1 : (i1 == i2 ? 0 : 1));
                }
            });

            visualize(importantClusters, directedGraph.getModel());

            GetData.export(k+"-NAGClusters");
            int i = 1;

            for(Set<Node> nodes:importantClusters){
                Set<String> clusterSet = new HashSet<>();
                for(Node node: nodes){
                    clusterSet.add(node.getId().toString());
                }
                clustersMap.put("Cluster "+i,clusterSet);
                i++;
            }
            try {
                GetData.createClusterCSV(clustersMap, k+"-NAGClusters");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Expand expand = new Expand();
        ScaleLayout scaleLayout = expand.buildLayout();
        scaleLayout.setGraphModel(directedGraph.getModel());
        scaleLayout.initAlgo();
        scaleLayout.goAlgo();

        JFrame [] frames = GraphCreater.build(workspace,"NAGClusters",clustersMap, exists);
        graphFrame = frames[0];
        infoFrame = frames[1];
    }

    private void getGraph(int k) throws FileNotFoundException {
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

                file = new File(getClass().getResource("/results/final_2.gexf").toURI());
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

    private void getNodes(int k){
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

    private  Set<Set<Node>> findClusters(int numEdgesToRemove){

        edu.uci.ics.jung.graph.DirectedSparseGraph<Node, Edge> dg = new DirectedSparseGraph<>();


        for (Node node: directedGraph.getNodes().toArray()){
            dg.addVertex(node);
        }
        for (Edge edge: directedGraph.getEdges().toArray()){
            double oldWeight = edge.getWeight();
            double newWeight = -oldWeight;
            edge.setWeight(newWeight);
            if (String.valueOf(oldWeight).length() > 3)
                dg.addEdge(edge, edge.getSource(), edge.getTarget());
        }
        for (Edge edge: dg.getEdges()){
            System.out.println(edge.getWeight());

        }
        System.out.println(dg.getEdges().size());
        System.out.println(dg.getVertexCount());

        int i = 1;
        int edgesCount = dg.getEdgeCount();
        EdgeBetweennessClusterer<Node,Edge> clusterer = new EdgeBetweennessClusterer<>(30);
        Set<Set<Node>> clusters = clusterer.transform(dg);

        return clusters;

    }

    private void visualize(List<Set<Node>> importantClusters, GraphModel graphModel){

        Column modCol = graphModel.getNodeTable().addColumn("NAG-clusters", "NAG-Clusters",
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

        for (Set<Node> nodesList: importantClusters) {
            sizes.add(nodesList.size());
        }

        for (Integer size: sizes){
            int newSize = minSize + step;
            sizesMap.put(size, newSize);
            step = step + 5;
            i++;
        }

        i = 0;

        for (Set<Node> nodesList: importantClusters) {
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
