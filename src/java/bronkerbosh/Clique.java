package bronkerbosh;

import core.GetData;
import main.GraphCreater;
import main.PreviewScetch;
import org.apache.commons.collections4.list.TreeList;
import org.gephi.appearance.AppearanceModelImpl;
import org.gephi.appearance.api.AppearanceController;
import org.gephi.appearance.api.AppearanceModel;
import org.gephi.appearance.api.Function;
import org.gephi.appearance.plugin.RankingElementColorTransformer;
import org.gephi.appearance.plugin.RankingNodeSizeTransformer;
import org.gephi.graph.api.*;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.EdgeDirectionDefault;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.preview.api.G2DTarget;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.List;

public class Clique {
    public static DirectedGraph directedGraph;
    public static ProjectController pc;
    public static Workspace workspace;
    public static Map <String, String> nodeId;
    private static Map<String, Set<String>> cliquesMap;

    public JFrame graphFrame;
    public JFrame infoFrame;

    public Clique(int k) {
        pc = Lookup.getDefault().lookup(ProjectController.class);
    }

    public void run(int k) throws FileNotFoundException, URISyntaxException {


        File file = new File("data/"+k+"-clique.gexf");
        pc.newProject();
        workspace = pc.getCurrentWorkspace();

        ImportController importController = Lookup.getDefault().lookup(ImportController.class);
        Container container;

        boolean exists = false;
        if(file.exists()){
            container = importController.importFile(file);
            container.getLoader().setEdgeDefault(EdgeDirectionDefault.DIRECTED);
            importController.process(container, new DefaultProcessor(), workspace);
            final GraphModel graphModel = Lookup.getDefault()
                    .lookup(GraphController.class).getGraphModel();
            directedGraph = graphModel.getDirectedGraph();
            exists = true;
            cliquesMap = new HashMap<>();
            core.GetData.importTable(cliquesMap, k+"-cliques");

        }
        else {
            double startTime = System.currentTimeMillis();

            getGraph(k);

            BronKerbosh bronKerbosh = new BronKerbosh(directedGraph);
            bronKerbosh.base();

            ArrayList<ArrayList<Node>> cliques = bronKerbosh.getCliques();

            ArrayList<ArrayList<Node>> maxCliques = findMaxCliques(cliques);

            for (Node node : directedGraph.getNodes().toArray()) {
                node.setColor(Color.BLACK);
            }

            //node id - cliques list
            Map<String, Set<String>> cliquesList = getCliquesList(directedGraph.getNodes().toArray(), cliques);

            Set<Integer> cliquesCount = setCliquesCount(cliquesList);

            System.out.println("Количество вершин:"+ directedGraph.getNodeCount());
            System.out.println("Количество ребер:"+ directedGraph.getEdgeCount());
            System.out.println("Затрачено:"+(System.currentTimeMillis() - startTime)/1000+" c.");


            filter(directedGraph.getModel(),cliquesCount);
            GetData.export(k+"-clique");
            cliquesMap = cliquesList;
            try {
                GetData.createCliqueCSV(cliquesMap,k+"-cliques");
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        JFrame [] frames = GraphCreater.build(workspace,"Cliques",cliquesMap, false);

       graphFrame = frames[0];

       infoFrame = frames[1];
    }
    private void getGraph(int k) throws URISyntaxException {
        ImportController importController = Lookup.getDefault().lookup(ImportController.class);
        Container container;
        File file = null;
        try{
           file = new File(getClass().getResource("/results/"+k+".gexf").toURI());
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
        if(file!=null) {
            try {

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
    }

    private static ArrayList<ArrayList<Node>> findMaxCliques(ArrayList<ArrayList<Node>> list){
        int maxValue = 0;

        for (ArrayList<Node> nodes: list){
            int size = nodes.size();
            if (size > maxValue)
                maxValue = size;
        }
        ArrayList<ArrayList<Node>> finalList = new ArrayList<ArrayList<Node>>();
        for (ArrayList<Node> nodes: list){
            if (nodes.size() == maxValue)
                finalList.add(nodes);
        }
        return finalList;
    }

    private static Map<String, Set<String>> getCliquesList(Node[] nodes,    ArrayList<ArrayList<Node>> cliques){
        Map<String, Set<String>> nodesList =  new HashMap<String, Set<String>>();

        for (Node node: nodes){
            Set<String> cliquesList = new TreeSet<String>();
            int i = 0;
            for (ArrayList<Node> clique: cliques) {
                i++;
                if (clique.contains(node))
                    cliquesList.add("Clique "+i);
            }

            nodesList.put(node.getAttributes()[1].toString(), cliquesList);
        }

        return nodesList;
    }

    private static Set<Integer> setCliquesCount(Map<String, Set<String>> cliques) {
        Column modCol = directedGraph.getModel().getNodeTable().addColumn("cliques", "Cliques", Integer.class, new Integer(0));

        Iterator<Map.Entry<String, Set<String>>> entries = cliques.entrySet().iterator();
        Set<Integer> cliquesCountSet = new HashSet<>();
        while (entries.hasNext()) {
            Map.Entry<String, Set<String>> node = entries.next();
            int cliquesCount = node.getValue().size();
            cliquesCountSet.add(cliquesCount);
             directedGraph.getNodes().toArray()[Integer.valueOf(node.getKey())].setAttribute(modCol, cliquesCount);
        }
        return cliquesCountSet;
    }

    private void filter(GraphModel graphModel, Set<Integer> cliquesCount){

        System.out.println(cliquesCount);
           Node [] nodes = graphModel.getDirectedGraph().getNodes().toArray();


           Map<Integer, Color> colorClassification = new HashMap<>();

            Color [] colors = core.GetData.setColors(cliquesCount);
            int i = 0;
            Iterator<Integer> it = cliquesCount.iterator();
            while(it.hasNext()){
                colorClassification.put(it.next(), colors[i]);
                i++;
            }

           for (Node node : nodes){
               int clCount = Integer.valueOf(node.getAttribute("Cliques").toString());
               node.setColor(colorClassification.get(clCount));
               node.setSize(clCount+1); // если 0 - 1
           }

    }

}
