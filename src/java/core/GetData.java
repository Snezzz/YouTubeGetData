package core;

import bronkerbosh.Clique;
import clusters.Clusters;

import com.google.common.base.Utf8;
import main.GraphCreater;

import main.PreviewScetch;
import org.gephi.appearance.api.*;
import org.gephi.appearance.plugin.RankingElementColorTransformer;
import org.gephi.appearance.plugin.RankingNodeSizeTransformer;
import org.gephi.graph.api.*;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.EdgeDirectionDefault;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.preview.api.*;
import org.gephi.preview.types.EdgeColor;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;

import javax.swing.*;
import javax.xml.stream.XMLStreamException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.*;
import java.net.URISyntaxException;
import java.sql.*;
import java.util.*;
import java.util.List;


public class GetData {
    public static ProjectController pc;
    public static Workspace workspace;
    private static final String DELIMITER = ";";
    private static final String NEW_LINE_SEPARATOR = "\n";
    private static String FILE_HEADER = "node_id;cores";
    private static Map<String, Set<String>> nodeCores;
    public static Map <String, String> nodeId;

    public void run(final GraphCreater graphCreater) throws SQLException, IOException, XMLStreamException, InterruptedException {

        pc = Lookup.getDefault().lookup(ProjectController.class);
        JFrame menu=new JFrame("Constructor");
        final JPanel panel=new JPanel();
        panel.setBackground(new Color(232,255,224));

        JLabel label2 = new JLabel();
        label2.setText("k:");

        final JTextField k = new JTextField(2);
        k.setText("20");

        JButton coreButton = new JButton("Получить ядра");
        coreButton.setBackground(new Color(189,234,152));
        coreButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int degree =  Integer.valueOf(k.getText());
                try {
                    Cores cores = new Cores();
                    cores.findCores(degree);

                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                } catch (IOException e1) {
                    e1.printStackTrace();
                } catch (URISyntaxException e1) {
                    e1.printStackTrace();
                }

            }
        });

        JButton cliqueButton = new JButton("Получить клики");
        cliqueButton.setBackground(new Color(189,234,152));
        cliqueButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new bronkerbosh.Clique(25);
            }
        });
        JLabel label3 = new JLabel();
        label3.setText("k:");

        final JTextField clustersCount = new JTextField();
        clustersCount.setText("100");

        JButton clustersButton = new JButton("Получить кластера");
        clustersButton.setBackground(new Color(189,234,152));
        clustersButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int k =  Integer.valueOf(clustersCount.getText());
                new Clusters();
            }
        });

        panel.add(label2);
        panel.add(k);

        panel.add(coreButton);
        panel.add(cliqueButton);
        panel.add(label3);
        panel.add(clustersCount);
        panel.add(clustersButton);

        menu.add(panel);

        menu.add(panel);
        menu.setSize(220,180);
        menu.setLocation(400,150);
        menu.setVisible(true);


    }


    public static void filter(GraphModel graphModel){
        AppearanceController ac = Lookup.getDefault().lookup(AppearanceController.class); //создаем контроллер по отображению
        AppearanceModel appearanceModel = ac.getModel(workspace); // создаем модель

        Column coreColumn = graphModel.getNodeTable().getColumn("Cores");



        Function sizeRanking = appearanceModel
                .getNodeFunction(graphModel.getDirectedGraph(), coreColumn, RankingNodeSizeTransformer.class);

        RankingNodeSizeTransformer sizeTransformer = sizeRanking.getTransformer();
        sizeTransformer.setMinSize(1);
        sizeTransformer.setMaxSize(20);
        //применяем изменение внешнего вида для нашей модели
        ac.transform(sizeRanking);

        Function colorRanking = appearanceModel.getNodeFunction(graphModel.getDirectedGraph(),
                coreColumn, RankingElementColorTransformer.class);
        RankingElementColorTransformer rankingElementColorTransformer = colorRanking.getTransformer();
        int len = rankingElementColorTransformer.getColors().length;
        ac.transform(colorRanking);
    }


    public static void coreStatistics(String fileName, Map<String, Set<String>> nodeCores) throws IOException {
        FileWriter file = null;
        try {
            file = new FileWriter("results/" + fileName + ".csv");
            file.append("node id, cores count");
            file.append(NEW_LINE_SEPARATOR);
            Iterator<Map.Entry<String, Set<String>>> entries = nodeCores.entrySet().iterator();

            while (entries.hasNext()) {

                Map.Entry<String, Set<String>> node = entries.next();
                file.append(Cores.nodeId.get(node.getKey().toString()));
                file.append(DELIMITER);
                file.append(String.valueOf(node.getValue().size()));
                file.append(NEW_LINE_SEPARATOR);
            }
        } catch (Exception e) {
            System.out.println("Error in CsvFileWriter !!!");
            e.printStackTrace();
        }
        file.close();


    }

    private static void setCoresCount(GraphModel graphModel, Map<String, Set<String>> nodeCores){
        Column modCol = graphModel.getNodeTable().addColumn("cores", "Cores", Integer.class, new Integer(0));

        Iterator<Map.Entry<String, Set<String>>> entries = nodeCores.entrySet().iterator();

        while (entries.hasNext()) {
            Map.Entry<String, Set<String>> node = entries.next();
            int coresCount = node.getValue().size();
            graphModel.getDirectedGraph().getNodes().toArray()[Integer.valueOf(node.getKey())].setAttribute(modCol, coresCount);
        }
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

    private static String findMaxCore(Map<String, Set<Integer>> cores){
        int max = 0;
        String idOfMaxCore = null;
        Iterator<Map.Entry<String, Set<Integer>>> entries = cores.entrySet().iterator();
        while(entries.hasNext()) {
            Map.Entry<String, Set<Integer>> entry = entries.next();
            int size = entry.getValue().size();
            if (size > max) {
                idOfMaxCore = entry.getKey();
                max = size;
            }
        }
        return idOfMaxCore;

}

    public static void createCoreCSV(Map<String, Set<String>> from, String coresClass) throws IOException {
        Iterator<Map.Entry<String, Set<String>>> entries = from.entrySet().iterator();
        FileWriter file = null;
        try {
            file = new FileWriter("data/" + coresClass + ".csv");
            file.append(FILE_HEADER.toString());
            file.append(NEW_LINE_SEPARATOR);

            while (entries.hasNext()) {
                Map.Entry<String, Set<String>> entry = entries.next();
                String nodeId = core.Cores.directedGraph.getNodes().toArray()
                        [Integer.valueOf(entry.getKey())].getId().toString();

                file.append(nodeId);
                file.append(DELIMITER);
                file.append(entry.getValue().toString());
                file.append(NEW_LINE_SEPARATOR);
            }
        } catch (Exception e) {
            System.out.println("Error in CsvFileWriter !!!");
            e.printStackTrace();
        }
        file.close();


    }
    public static void createCliqueCSV(Map<String, Set<String>> from, String cliqueClass) throws IOException {
        Iterator<Map.Entry<String, Set<String>>> entries = from.entrySet().iterator();
        FileWriter file = null;
        FILE_HEADER = "nodeId;cliques";
        try {
            file = new FileWriter("data/" +  cliqueClass + ".csv");
            file.append(FILE_HEADER.toString());
            file.append(NEW_LINE_SEPARATOR);

            while (entries.hasNext()) {
                Map.Entry<String, Set<String>> entry = entries.next();
                String nodeId = Clique.directedGraph.getNodes().toArray()
                        [Integer.valueOf(entry.getKey())].getId().toString();

                file.append(nodeId);
                file.append(DELIMITER);
                file.append(entry.getValue().toString());
                file.append(NEW_LINE_SEPARATOR);
            }
        } catch (Exception e) {
            System.out.println("Error in CsvFileWriter !!!");
            e.printStackTrace();
        }
        file.close();
    }

    public static void createClusterCSV(Map<String, Set<String>> from, String clusterClass) throws IOException {
        Iterator<Map.Entry<String, Set<String>>> entries = from.entrySet().iterator();
        FileWriter file = null;
        FILE_HEADER = "cluster;nodes";
        try {
            file = new FileWriter("data/" +  clusterClass + ".csv");
            file.append(FILE_HEADER.toString());
            file.append(NEW_LINE_SEPARATOR);

            while (entries.hasNext()) {
                Map.Entry<String, Set<String>> entry = entries.next();
                String clusterId = entry.getKey();
                file.append(clusterId);
                file.append(DELIMITER);
                file.append(entry.getValue().toString());
                file.append(NEW_LINE_SEPARATOR);
            }
        } catch (Exception e) {
            System.out.println("Error in CsvFileWriter !!!");
            e.printStackTrace();
        }
        file.close();


    }

    public static void importTable(Map<String, Set<String>> nodesList, String fileName) throws FileNotFoundException {

        Scanner scanner = new Scanner(new File("data/"+fileName+".csv"));
        scanner.useDelimiter("\n");
        scanner.next();
        while(scanner.hasNext()){
            Object [] node = scanner.next().split(";");
            String nodeId = node[0].toString();
            String cores = node[1].toString();
            List<String> coresList = Arrays.asList(cores.substring(1,cores.length()-1).split(","));
            Set<String> coresSet = new TreeSet<String>();

            if(!coresList.get(0).equals("")){
                for (String id: coresList){
                    coresSet.add(id);
                }
            }

            nodesList.put(nodeId, coresSet);
        }
        scanner.close();

    }

    public static void export(String fileName) {
        ExportController ec = Lookup.getDefault().lookup(ExportController.class);
        try {
            ec.exportFile(new File("data/"+fileName + ".gexf"));
        } catch
                (IOException ex) {
            ex.printStackTrace();
            return;
        }

    }
    public static Color [] setColors(Set<Integer> set){
        Color [] colors = new Color[set.size()];
        Random random = new Random();

        for(int k = 0; k < colors.length; k++){
            float r = (float) (random.nextFloat() / 2f + 0.5);
            float g = (float) (random.nextFloat() / 2f + 0.5);
            float b = (float) (random.nextFloat() / 2f + 0.5);
            Color color = new Color(r,g,b);
            colors[k] = color;
        }
        return colors;
    }


}

