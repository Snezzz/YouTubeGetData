package core;

import main.GraphCreater;
import org.apache.commons.collections4.list.TreeList;
import org.gephi.graph.api.*;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.EdgeDirectionDefault;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.preview.api.G2DTarget;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;
import sun.security.util.BitArray;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.List;

public class Cores {
    public static Map <String, String> nodeId;
    private static Map<String, Set<String>> nodeCores;
    public static DirectedGraph directedGraph;
    public static ProjectController pc;
    public static Workspace workspace;
    public JFrame graphFrame;
    public JFrame infoFrame;
    boolean coresFound = false;

    public Cores(){
                pc = Lookup.getDefault().lookup(ProjectController.class);
            }

            public void run(int k) throws IOException, InterruptedException {

                pc.newProject();
                workspace = pc.getCurrentWorkspace();
                File file = new File("data/"+k+"-core.gexf");

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
                    nodeCores = new HashMap<>();

                    core.GetData.importTable(nodeCores, k+"-cores");
                    exists = true;
                    coresFound = true;
                }
                else{
                    try {
                        double startTime = System.currentTimeMillis();

                        coresFound = findCores(k);
                        System.out.println("Затрачено:"+(System.currentTimeMillis() - startTime)/1000+" c.");

                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                }

        if (coresFound) {
            JFrame[] frames = GraphCreater.build(workspace, "Cores", nodeCores, exists);
            graphFrame = frames[0];
            infoFrame = frames[1];
        }
        else{
            infoFrame = new JFrame("Info");
            infoFrame.setSize(200,100);
            infoFrame.setLocation(500,200);
            JLabel jLabel = new JLabel("There are no cores");
            infoFrame.add(jLabel);

        }


    }

private void getGraph(int k) throws URISyntaxException {
    //  pc.newProject();
    ImportController importController = Lookup.getDefault().lookup(ImportController.class);
    Container container;
    File file = null;
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

    public boolean findCores(int k) throws InterruptedException, IOException, URISyntaxException {
        getGraph(k);

        System.out.println("Количество вершин:" + directedGraph.getNodeCount());
        System.out.println("Количество ребер:" + directedGraph.getEdgeCount());
        int nodeCount = directedGraph.getNodeCount();

        Map<String,Set<Integer>> cores = null;
        int i;

        if (nodeCount > 3) {
            Magician m = new Magician(nodeCount);
            i = 0;
            for (Node v : directedGraph.getNodes()) {

                Node[] neighbours = directedGraph.getSuccessors(v).toArray();
                int neighbours_count = neighbours.length;
                int[] neighbours_arr = new int[neighbours_count];
                int j = 0;
                for (Node neighbour : neighbours) {
                    neighbours_arr[j] = Integer.valueOf(neighbour.getAttributes()[1].toString());
                    j++;
                }
                m.setNeighbours(i, neighbours_arr);
                i++;
            }

            ArrayList<BitArray> internal = m.getExpression(m.matrix, false);
            ArrayList<BitArray> external = m.getExpression(m.matrix, true);

            List<Set<Integer>> A = getMap(internal);

            List<Set<Integer>> B = getMap(external);

            //список ядер графа
            cores = m.intersection(A, B);


            if (cores.size() == 0){
                System.out.println("Ядра не найдены");
                return false;
            }
            else {
                System.out.println("Найдено " + cores.size() + " ядер");

                ImportController importController = Lookup.getDefault().lookup(ImportController.class);
                Container container;


                        try {
                            File file = new File(GetData.class.getResource("data/final_2.gexf").toURI());
                            container = importController.importFile(file);
                            container.getLoader().setEdgeDefault(EdgeDirectionDefault.DIRECTED);

                        } catch (Exception ex) {
                            ex.printStackTrace();
                            return false;
                        }
                pc.newProject();
                workspace = pc.getCurrentWorkspace();
                        importController.process(container, new DefaultProcessor(), workspace);
                        final GraphModel graphModel = Lookup.getDefault()
                                .lookup(GraphController.class).getGraphModel();



                        i = 1;

                        nodeCores = getCoresList(directedGraph.getNodes().toArray(), cores);
                        Set<Integer> coresCount = setCoresCount(graphModel, nodeCores);

                        int minValue = findMinValue(nodeCores);
                        int maxValue = findMaxValue(nodeCores);

                        for (Node node : graphModel.getDirectedGraph().getNodes()) {
                            node.setSize(1.0f);
                        }
                        Iterator<Map.Entry<String, Set<String>>> entries = nodeCores.entrySet().iterator();

                        while (entries.hasNext()) {
                            Map.Entry<String, Set<String>> node = entries.next();
                            Node currentNode = graphModel.getDirectedGraph().getNodes().toArray()[Integer.valueOf(node.getKey())];
                            int size = Integer.valueOf(currentNode.getAttribute("Cores").toString());

                            if (maxValue > 80) {
                                currentNode.setSize(Integer.valueOf(currentNode.getAttribute("Cores").toString()) / 5);
                            } else if (maxValue < 10) {
                                currentNode.setSize(Integer.valueOf(currentNode.getAttribute("Cores").toString()) * 25);
                            } else {
                                currentNode.setSize(Integer.valueOf(currentNode.getAttribute("Cores").toString()));
                            }
                            if (size == 0)
                                currentNode.setSize(1);

                        }
                GetData.createCoreCSV(nodeCores, k + "-cores");
                filter(graphModel,coresCount);
                GetData.export(k + "-core");
            }
        return true;
        }
        else{
            System.out.println("слишком большая степень!");
            return false;
        }
    }

    private List<Set<Integer>> getMap(ArrayList<BitArray> array){
        List<Set<Integer>> finalNodes = new TreeList<Set<Integer>>();
        for (Iterator<BitArray> it1 = array.iterator(); it1.hasNext(); ) {
            Set<Integer> nodes = new TreeSet<Integer>();
            BitArray current = it1.next();
            for (int j = 0; j < current.toBooleanArray().length; j++) {
                if (current.get(j)) {
                    nodes.add(j);
                }
            }
            finalNodes.add(nodes);
        }
        return finalNodes;
    }

    private static Map<String, Set<String>> getCoresList(Node[] nodes, Map<String, Set<Integer>> cores){
        Map<String, Set<String>> nodesList =  new HashMap<String, Set<String>>();

        for (Node node: nodes){
            Iterator<Map.Entry<String, Set<Integer>>> entries = cores.entrySet().iterator();
            Set<String> coresList = new TreeSet<String>();
            while (entries.hasNext()){
                Map.Entry<String, Set<Integer>> core = entries.next();
                if (core.getValue().contains(Integer.valueOf(node.getAttributes()[1].toString())))
                    coresList.add(core.getKey());
            }
            nodesList.put(node.getAttributes()[1].toString(), coresList);
        }

        return nodesList;
    }

    private static int findMaxValue(Map<String, Set<String>> map){
        int maxValue = 0;
        Iterator<Map.Entry<String, Set<String>>> entries = map.entrySet().iterator();

        while (entries.hasNext()) {
            Map.Entry<String, Set<String>> node = entries.next();
            int len = node.getValue().size();
            if(len > maxValue){
                maxValue = len;
            }
        }
        return maxValue;
    }

    private static int findMinValue(Map<String, Set<String>> map){
        int minValue = 10000000;
        Iterator<Map.Entry<String, Set<String>>> entries = map.entrySet().iterator();

        while (entries.hasNext()) {
            Map.Entry<String, Set<String>> node = entries.next();
            int len = node.getValue().size();
            if(len < minValue){
                minValue = len;
            }
        }
        return minValue;
    }

    private static  Set<Integer> setCoresCount(GraphModel graphModel, Map<String, Set<String>> nodeCores){
        Column modCol = graphModel.getNodeTable().addColumn("cores", "Cores", Integer.class, new Integer(0));

        Iterator<Map.Entry<String, Set<String>>> entries = nodeCores.entrySet().iterator();
        Set<Integer> coresCountSet = new HashSet<>();
        while (entries.hasNext()) {
            Map.Entry<String, Set<String>> node = entries.next();
            int coresCount = node.getValue().size();
            coresCountSet.add(coresCount);
            graphModel.getDirectedGraph().getNodes().toArray()[Integer.valueOf(node.getKey())].setAttribute(modCol, coresCount);
        }
        return coresCountSet;
    }

    private List<Node> getPopularNodes(Map<String, Set<String>> nodeCores){
        List<Node> nodes = new TreeList<Node>();
        int max = 0;
        Iterator<Map.Entry<String, Set<String>>> entries = nodeCores.entrySet().iterator();

        while (entries.hasNext()) {
            Map.Entry<String, Set<String>> node = entries.next();
            int coresCount = node.getValue().size();
            if (coresCount > max) {
                max = coresCount;
            }
        }
        entries = nodeCores.entrySet().iterator();

        while (entries.hasNext()) {
            Map.Entry<String, Set<String>> node = entries.next();
            int coresCount = node.getValue().size();
            if (coresCount == max){
                nodes.add(directedGraph.getNodes().toArray()[Integer.valueOf(node.getKey())]);
            }
        }
        return null;

    }

    private void filter(GraphModel graphModel, Set<Integer> coresCount){

        Node [] nodes = graphModel.getDirectedGraph().getNodes().toArray();


        Map<Integer, Color> colorClassification = new HashMap<>();
        Color [] colors = core.GetData.setColors(coresCount);
        int i = 0;
        Iterator<Integer> it = coresCount.iterator();
        while(it.hasNext()){
            colorClassification.put(it.next(), colors[i]);
            i++;
        }
        for (Node node : nodes){
            int clCount = -1;
            try {
                clCount = Integer.valueOf(node.getAttribute("Cores").toString());
            }
            catch (Exception e){
                System.out.println(e.getMessage());
            }
            if(clCount == -1) {
                node.setColor(Color.BLACK);
            }
            else{
                node.setColor(colorClassification.get(clCount));

            }
        }
    }
}
