package main;

import bronkerbosh.Clique;
import centrality.NewmanAndGirvan;
import clusters.Clusters;
import core.Cores;
import database.DBConnection;
import org.apache.commons.collections4.list.TreeList;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.gephi.appearance.api.*;
import org.gephi.appearance.plugin.PartitionElementColorTransformer;
import org.gephi.appearance.plugin.RankingElementColorTransformer;
import org.gephi.appearance.plugin.RankingNodeSizeTransformer;
import org.gephi.appearance.plugin.palette.Palette;
import org.gephi.appearance.plugin.palette.PaletteManager;
import org.gephi.graph.api.*;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.layout.plugin.force.StepDisplacement;
import org.gephi.layout.plugin.force.yifanHu.YifanHuLayout;
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2;
import org.gephi.layout.plugin.openord.OpenOrdLayout;
import org.gephi.preview.api.*;
import org.gephi.project.api.Workspace;
import org.gephi.statistics.plugin.Degree;
import org.gephi.statistics.plugin.GraphDistance;
import org.gephi.statistics.plugin.Modularity;
import org.gephi.statistics.plugin.PageRank;
import org.openide.util.Lookup;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.List;


public class GraphCreater extends JFrame {


    public static DirectedGraph directedGraph;
    public static List<Node> nodesList;
    public static Map<Integer, Integer> helpMap;
    private Map<String, Node> points;
    private static Map<String, Double> betweeness_centrality;
    private static Map<String, Double> PAGERANK;
    private static Map<String, Integer> InDegree;
    private static Map<String, Integer> OutDegree;
    private static Map<String, Double> DEGREE;
    private static final String COMMA_DELIMITER = ",";
    private static final String NEW_LINE_SEPARATOR = "\n";
    private static final String FILE_HEADER = "node_id,value";
    public static Workspace workspace;
    private static String type;
    private static Map<String, Double> top;
    private static Map<String, Color> colors;
    DBConnection db;

    public GraphCreater(final int n, String title, final String stowageType, boolean Default, boolean betweeness, boolean pagerank, boolean module,
                        boolean filter, boolean special, final G2DTarget target) throws IOException, CloneNotSupportedException, SQLException, InterruptedException {
        super(title);

        //создание нового проекта
        GetData.pc.newProject();
        workspace = GetData.pc.getCurrentWorkspace();
        db = new DBConnection();
        db.makeConnection("postgres");
        //модель графа
        final GraphModel graphModel = Lookup.getDefault()
                .lookup(GraphController.class).getGraphModel();


        directedGraph = graphModel.getDirectedGraph();


        if ((!filter) && (!special)) {
            type = "FullGraph";
            //создание узлов и ребер на directedGraph
            create(n, graphModel, directedGraph, points);

            //анализtColor(Color.gray);
            get_analysis(graphModel);
            //укладка графа по заданному алгоритму
            stowage(stowageType, graphModel);

        }
        //BETWEENESS_CENTRALITY
        if (betweeness) {
            filter(graphModel, GraphDistance.BETWEENNESS, workspace);
            type = "Betweeness";
        } else if (pagerank) {
            //PageRank
            filter(graphModel, PageRank.PAGERANK, workspace);
            type = "PageRank";
        }
        //модулярность
        else if (module) {
            modularity(graphModel, workspace);
            type = "Modularity";
        } else if (filter) {
            JFrame fr = new JFrame();
            JPanel pn = new JPanel();
            String[] items = {
                    "6",
                    "5",
                    "4",
                    "3",
                    "2",
                    "1"
            };
            final JComboBox box = new JComboBox(items);
            JLabel lb = new JLabel();
            lb.setText("Выберите количество каналов:");
            pn.add(lb);
            pn.add(box);
            final JButton button = new JButton("get");

            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int h = Integer.valueOf(box.getSelectedItem().toString());
                    //создание узлов и ребер на directedGraph
                    final GraphModel graphModel = Lookup.getDefault()
                            .lookup(GraphController.class).getGraphModel();
                    directedGraph = graphModel.getDirectedGraph();
                    create(h, graphModel, directedGraph, points);
                    //анализ
                    try {
                        get_analysis(graphModel);
                    } catch (SQLException e1) {
                        e1.printStackTrace();
                    }

                    //укладка графа по заданному алгоритму
                    stowage(stowageType, graphModel);

                    //визуализация
                    try {
                        get_filter(h);
                        filter(graphModel, PageRank.PAGERANK, workspace);
                        type = "PageRank";
                        G2DTarget target = null;
                        build(workspace,type,null, false);
                        export("filter(" + h + ")");
                    } catch (SQLException e1) {
                        e1.printStackTrace();
                    }
                }
            });
            fr.setSize(200, 150);
            fr.setLocation(400, 150);
            pn.add(button);
            fr.add(pn);
            fr.setVisible(true);
        } else if (special) {
            type = "Special";
            create(n, graphModel, directedGraph, points);
            Column modCol = graphModel.getNodeTable().addColumn("class", "Class", String.class, new String(""));

            setColumn(graphModel, "first_channel", "blue", modCol);
            setColumn(graphModel, "second_channel", "green", modCol);
            setColumn(graphModel, "third_channel", "violet", modCol);
            setColumn(graphModel, "forth_channel", "orange", modCol);
            setColumn(graphModel, "fifth_channel", "yellow", modCol);
            setColumn(graphModel, "sixth_channel", "dark blue", modCol);
            setColumn(graphModel, "special_nodes", "red", modCol);
            setColumn(graphModel, "last_nodes", "gray", modCol);
            get_analysis(graphModel);
            //укладка графа по заданному алгоритму
            stowage(stowageType, graphModel);
            export("special");
        }
        //отображение графа на панели
        if (!filter) {
           // build(target);
            System.out.println(type);
            export(type);
        }
    }

    void get_filter(int h) throws SQLException {
        String sql = "with tt as (\n" +
                "    select\n" +
                "      comment_author,\n" +
                "      author_id,\n" +
                "      comment_id,video_id\n" +
                "    from comments\n" +
                "),t as(\n" +
                "    select\n" +
                "      tt.comment_author,\n" +
                "      count(tt.comment_id) as comments_count,\n" +
                "      count(distinct tt.video_id) as video_count\n" +
                "    from tt\n" +
                "    group by tt.comment_author\n" +
                "), t2 as (select distinct tt.video_id,tt.comment_author,tt.author_id,author from tt join videos on tt.video_id=videos.video_id)\n" +
                "--select * from t2\n" +
                "  , t3 as (select distinct t2.comment_author,t2.author_id,count(distinct author) as channels_count,t.comments_count,t.video_count\n" +
                "    as commented_video_count from t2\n" +
                "    join t on t2.comment_author=t.comment_author\n" +
                "  group by t2.comment_author,t2.author_id,t.comments_count,t.video_count order by comments_count desc)\n" +
                "  select comment_author, author_id, channels_count from t3 where channels_count >= " + h;
        ResultSet rs = db.makeQuery(sql);
        List<String> ids = new TreeList<String>();
        while (rs.next()) {
            String id = rs.getString(2);
            ids.add(id);
        }
        List<Node> nodes = Arrays.asList(directedGraph.getNodes().toArray());
        for (int i = 0; i < nodes.size(); i++) {
            String id = nodes.get(i).getAttributes()[0].toString();
            String real_id = nodes.get(i).getId().toString();
            Node current_node = directedGraph.getNode(nodes.get(i).getId());
            //если не принадлежит списку, удаляем
            if (!ids.contains(id)) {
                directedGraph.removeNode(current_node);
            }
        }
    }

    //анализ графа
    public static void get_analysis(GraphModel graphModel) throws SQLException {
        betweeness_centrality = new TreeMap<String, Double>();
        PAGERANK = new TreeMap<String, Double>();
        DEGREE = new TreeMap<String, Double>();
        InDegree = new TreeMap<String, Integer>();
        OutDegree = new TreeMap<String, Integer>();

        //кратчайшие пути
        GraphDistance distance = new GraphDistance();
        distance.setDirected(true);

        distance.setNormalized(true);
        distance.execute(graphModel);
        //pageRank = приоритет узла
        PageRank pageRank = new PageRank();
        pageRank.execute(graphModel);
        //степень вершины
        Degree degree = new Degree();
        degree.execute(graphModel);

    }

    private void setColumn(GraphModel graphModel, String table, String color, Column modCol) throws SQLException {
        System.out.println("table=" + table);
        String sql = "select " + table + ".author_id from " + table;
        ResultSet rs = db.makeQuery(sql);
        while (rs.next()) {
            graphModel.getDirectedGraph().getNode(rs.getString(1)).setAttribute(modCol, table);
        }
    }

    private void set(String channel, NodeIterable nodes) throws SQLException {

        for (Iterator<Node> it1 = nodes.iterator(); it1.hasNext(); ) {
            Node current = it1.next();
            String id = current.getAttributes()[0].toString();
            Integer indegree = Integer.valueOf(current.getAttributes()[3].toString());
            Integer outdegree = Integer.valueOf(current.getAttributes()[4].toString());
            String sql = "UPDATE degrees SET indegree =" + indegree +
                    ", outdegree = " + outdegree + " WHERE degrees.author_id = '" + id + "'";
            db.makeQuery(sql);
        }
    }




    public static void create_XLSlist(HSSFWorkbook workbook, String name, Map<String, Double> from) {

        HSSFSheet sheet = workbook.createSheet(name);
        //sheet.autoSizeColumn(1);
        Cell cell;
        HSSFCellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);

        Row row;
        int rownum = 0;
        row = sheet.createRow(rownum);
        cell = row.createCell(1, CellType.STRING);
        cell.setCellValue("node");
        cell.setCellStyle(style);
        cell = row.createCell(2, CellType.STRING);
        cell.setCellValue("value");
        cell.setCellStyle(style);

        rownum++;
        Iterator<Map.Entry<String, Double>> entries = from.entrySet().iterator();

        while (entries.hasNext()) {
            Map.Entry<String, Double> entry = entries.next();
            row = sheet.createRow(rownum);
            String id = entry.getKey();
            Double value = entry.getValue();
            cell = row.createCell(1, CellType.STRING);
            cell.setCellValue(id);
            cell = row.createCell(2, CellType.STRING);
            cell.setCellValue(value);
            rownum++;
        }
    }

    //укладка графа
    public static void stowage(String type, GraphModel graphModel) {
        double count = Math.pow(directedGraph.getNodes().toArray().length, 2);
        if (type.equals("Yifan Hu")) {

            //YifanHu укладка
            YifanHuLayout layout = new YifanHuLayout(null, new StepDisplacement(1f));
            layout.setGraphModel(graphModel);
            layout.resetPropertiesValues();
            layout.setOptimalDistance(13593.233f);
            //  layout.setRelativeStrength(3000f);
            layout.initAlgo();
            for (int i = 0; i < 100 && layout.canAlgo(); i++) {
                layout.goAlgo();
            }


        } else if (type.equals("OpenOrd")) {
            OpenOrdLayout layout = new OpenOrdLayout(null);
            layout.setGraphModel(graphModel);
            layout.resetPropertiesValues();
            layout.setCooldownStage(100);
            layout.setCrunchStage(0);
            layout.setExpansionStage(1);
            layout.setLiquidStage(2);
            layout.setSimmerStage(1);
            layout.setEdgeCut(1.0f);
            layout.setNumThreads(8);
            layout.initAlgo();
            for (int i = 0; i < 100 && layout.canAlgo(); i++) {
                layout.goAlgo();
            }
        } else if (type.equals("Force Atlas 2")) {
            ForceAtlas2 forceAtlas2 = new ForceAtlas2(null);
            forceAtlas2.setGraphModel(graphModel);
            forceAtlas2.resetPropertiesValues();
            forceAtlas2.initAlgo();
            for (int i = 0; i < 100 && forceAtlas2.canAlgo(); i++) {
                forceAtlas2.goAlgo();
            }

        }
    }


    //создание графа
    public static void create(int degree, GraphModel graphModel, DirectedGraph directedGraph, Map<String, Node> points) {
        Random rand = new Random();
        // Iterator iterator = SetData.nodes_map.entrySet().iterator();
        Iterator iterator = GetData.nodes.entrySet().iterator();
        colors = new LinkedHashMap<String, Color>();
        directedGraph = graphModel.getDirectedGraph();


        points = new TreeMap<String, Node>();

        //перебираем все вершины и их соседей
        while (iterator.hasNext()) {
            Map.Entry pair = (Map.Entry) iterator.next();
            String node_to = pair.getKey().toString();

            int x = rand.nextInt(3000);
            int y = rand.nextInt(1000);
            Node n0;
            if (!points.containsKey(node_to)) {
                n0 = graphModel.factory().newNode(node_to);
                n0.setLabel(GetData.node_number.get(node_to));
                n0.setSize(6);
                n0.setColor(Color.red);
                n0.setX(x);
                n0.setY(y);
                points.put(node_to, n0);
            } else {
                n0 = points.get(node_to);
            }
            directedGraph.addNode(n0);
            //все вершины,ИЗ которых идет дуга на целевые

            Iterator it = GetData.nodes.get(node_to).entrySet().iterator();

            //ссылающиеся вершины
            while (it.hasNext()) {

                Map.Entry pair2 = (Map.Entry) it.next();
                String node_from = pair2.getKey().toString();
                Node n2;
                if (!points.containsKey(node_from)) {
                    n2 = graphModel.factory().newNode(node_from);
                    n2.setLabel(GetData.node_number.get(node_from));

                    n2.setColor(Color.red);
                    n2.setSize(6);
                    int x1 = rand.nextInt(3000);
                    int y1 = rand.nextInt(1000);
                    n2.setX(x1);
                    n2.setY(y1);
                    points.put(node_from, n2);
                } else {
                    n2 = points.get(node_from);
                }
                if (n2 != n0) {
                    //дуга
                    Edge e1 = graphModel.factory().newEdge(n2, n0, 0, 1.0, true);
                    //вес ребра
                    if (GetData.comments_count.containsKey(node_from + "!" + node_to)) {
                        e1.setWeight(GetData.comments_count.get(node_from + "!" + node_to));
                        e1.setLabel(String.valueOf(GetData.comments_count.get(node_from + "!" + node_to)));
                    } else if (GetData.comments_count.containsKey(node_to + "!" + node_from)) {
                        e1.setWeight(GetData.comments_count.get(node_to + "!" + node_from));
                        e1.setLabel(String.valueOf(GetData.comments_count.get(node_to + "!" + node_from)));
                    }
                    //добавляем вершину
                    directedGraph.addNode(n2);
                    //добавляем ребро
                    directedGraph.addEdge(e1);
                }
            }

        }

        boolean end = true;


        if (degree > 1) {
            System.out.println("до:" + directedGraph.getNodeCount());
            nodesList = new ArrayList<Node>();

            for (Node n : directedGraph.getNodes().toArray()) {
                int count = directedGraph.getOutDegree(n) + directedGraph.getInDegree(n);
                if (count >= degree) {
                    nodesList.add(n);
                }
            }

            System.out.println(nodesList.size());
            do {
                end = true;

                for (Node n : directedGraph.getNodes().toArray()) {
                    if ((!nodesList.contains(n)) && (directedGraph.contains(n))) {
                        directedGraph.removeNode(n);
                        end = false;
                        continue;
                    }
                }

                for (Node n : directedGraph.getNodes().toArray()) {
                    if (directedGraph.getNeighbors(n).toArray().length == 0)
                        directedGraph.removeNode(n);
                }
                int i = 0;
                helpMap = new HashMap<Integer, Integer>();

                for (Node n : directedGraph.getNodes().toArray()) {
                    helpMap.put(i, n.getStoreId());
                    i++;
                }

                i = 0;
                for (Node n : directedGraph.getNodes().toArray()) {
                    n.setLabel(String.valueOf(i));
                    i++;
                }

            }
            while (!end);
            System.out.println(directedGraph.getEdges().toArray().length);
            System.out.println("после:" + directedGraph.getNodeCount());
        }

    }

    //appearance(отображение графа)
    private static void filter(GraphModel graphModel, String type, Workspace workspace) {

        //получаем данные нашего графа по модели
        DirectedGraph graph = graphModel.getDirectedGraph();

        AppearanceController ac = Lookup.getDefault().lookup(AppearanceController.class); //создаем контроллер по отображению
        AppearanceModel appearanceModel = ac.getModel(workspace); // создаем модель
        //получаем список вершин(в нашем случае - отображение по вершинам) и определяем тип фильтрации
        Column centralityColumn = graphModel.getNodeTable().getColumn(type);

        Node[] nodes = graph.getNodes().toArray();
        top = find_top(nodes, 2, centralityColumn);
        //применяем функцию к graph - графу, по полученным вершинам - centralityColumn
        //размер вершин
        Function centralityRanking = appearanceModel.getNodeFunction(graph, centralityColumn, RankingNodeSizeTransformer.class);
        //объявляем объект по настройке трансформации и задаем настройки
        RankingNodeSizeTransformer centralityTransformer = centralityRanking.getTransformer();
        centralityTransformer.setMinSize(1);
        centralityTransformer.setMaxSize(20);
        //применяем изменение внешнего вида для нашей модели
        ac.transform(centralityRanking);
        //цвет вершин
        Function centralityRanking2 = appearanceModel.getNodeFunction(graph, centralityColumn, RankingElementColorTransformer.class);
        RankingElementColorTransformer colorTransformer = (RankingElementColorTransformer) centralityRanking2.getTransformer();
        colorTransformer.setColors(new Color[]{Color.gray, Color.YELLOW, Color.orange, Color.red});
        ac.transform(centralityRanking2);
    }


    //модулярность графа (разбиение графа на сообщества)
    private static void modularity(GraphModel graphModel, Workspace workspace) {

        DirectedGraph graph = graphModel.getDirectedGraph();
        Modularity modularity = new Modularity();
        modularity.setUseWeight(true);
        modularity.setRandom(true);
        modularity.setResolution(1.0);
        modularity.execute(graphModel);
        //получили кластеры
        //отображение
        AppearanceController ac = Lookup.getDefault().lookup(AppearanceController.class);
        AppearanceModel appearanceModel = ac.getModel(workspace);
        //цвет вершин
        Column modColumn = graphModel.getNodeTable().getColumn(Modularity.MODULARITY_CLASS);
        Function func2 = appearanceModel.getNodeFunction(graph, modColumn, PartitionElementColorTransformer.class);

        Partition partition2 = ((PartitionFunction) func2).getPartition();
        System.out.println(partition2.size() + " partitions found");
        //настройка цветов вершин
        Palette palette2 = PaletteManager.getInstance().randomPalette(partition2.size());
        partition2.setColors(palette2.getColors());
        top = find_partitions(partition2);
        ac.transform(func2);
        Function func = appearanceModel.getEdgeFunction(graph, AppearanceModel.GraphFunction.EDGE_WEIGHT
                , RankingElementColorTransformer.class);
        ac.transform(func);


    }

    //отображение
    public static JFrame [] build(Workspace workspace, String type, Map<String, Set<String>> list, boolean exists) {
        //создаем контроллер, отвечающий за отображение
        PreviewController previewController =
                Lookup.getDefault().lookup(PreviewController.class);
        PreviewModel previewModel = previewController.getModel(workspace);
        //настройки
        previewModel.getProperties().putValue(PreviewProperty.EDGE_LABEL_FONT, previewModel.getProperties().getFontValue(PreviewProperty.NODE_LABEL_FONT).deriveFont(8));
        previewModel.getProperties().putValue(PreviewProperty.SHOW_EDGE_LABELS, Boolean.FALSE); //отображение id вершин
        previewModel.getProperties().putValue(PreviewProperty.EDGE_LABEL_FONT, new Font("Times New Roman", 2, 2));
        previewModel.getProperties().putValue(PreviewProperty.DIRECTED,
                Boolean.TRUE);
        previewModel.getProperties().putValue(PreviewProperty.NODE_LABEL_PROPORTIONAL_SIZE, Boolean.FALSE);
        previewModel.getProperties().putValue(PreviewProperty.CATEGORY_NODE_LABELS, Boolean.TRUE);
        previewModel.getProperties().putValue(PreviewProperty.BACKGROUND_COLOR, Color.white);
        previewModel.getProperties().putValue(PreviewProperty.SHOW_EDGES, true);
        previewModel.getProperties().putValue(PreviewProperty.EDGE_RESCALE_WEIGHT, Boolean.FALSE);
        previewModel.getProperties().putValue(PreviewProperty.SHOW_EDGE_LABELS, Boolean.FALSE);
        previewModel.getProperties().putValue(PreviewProperty.EDGE_CURVED, Boolean.FALSE);
        previewModel.getProperties().putValue(PreviewProperty.ARROW_SIZE, 0.01);
        previewModel.getProperties().putValue(PreviewProperty.EDGE_THICKNESS, 0.001f);
        previewModel.getProperties().putValue(PreviewProperty.NODE_LABEL_PROPORTIONAL_SIZE, 6);

         G2DTarget target = (G2DTarget) previewController
                .getRenderTarget(RenderTarget.G2D_TARGET);
        //добавляем обработчик событий + paintComponent
        final PreviewScetch previewSketch = new PreviewScetch(target);
        previewController.render(target);
        previewController.refreshPreview();
        //отображение
        JFrame frame = new JFrame(type);
         frame.setLayout(new BorderLayout());
        //сюда накладываем тот объект, что отображается на окне

        frame.add(previewSketch, BorderLayout.CENTER);
        frame.setSize(1000, 700);

        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                previewSketch.resetZoom();
            }
        });
        JFrame help = new JFrame("");
        help.setLocation(600, 100);
        help.setSize(500, 200);
        try {
            JTable table = null;
            JPanel panel = new JPanel();

            panel.setSize(100, 500);
            panel.setBounds(600, 100, 100, 300);

            if (type.equals("Cores")||(type.equals("Clusters")||(type.equals("Cliques"))||(type.equals("NAGClusters")))) {
                table = create_table(list.size(), type, list,exists);
            }

            else {
                int top_size = top.size();
                table = create_table(top_size, type, null,exists);
            }
            for (int i = 0; i < table.getColumnCount(); i++) {
                table.getColumnModel().getColumn(i).setPreferredWidth(150);
            }
            JPanel tablePanel1 = new JPanel(new GridLayout(1,4));
            JPanel tablePanel2 = new JPanel(new GridLayout(1,1));
            JPanel tablePanel3 = new JPanel(new GridLayout(2,0));
            JPanel tablePanel4 = new JPanel(new GridLayout(1,2));
            JTable table2 = null;
            table2 = createNodesTable(type);


           JButton jButton = new JButton("Turn on/off edges view");

            jButton.setVisible(true);
            jButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    boolean edgesVisibility = previewModel.getProperties().getValue(PreviewProperty.SHOW_EDGES);
                    if(edgesVisibility) {
                        previewModel.getProperties().putValue(PreviewProperty.SHOW_EDGES, false);
                    }
                    else{
                        previewModel.getProperties().putValue(PreviewProperty.SHOW_EDGES, true);
                    }
                    previewController.refreshPreview();
                    target.refresh();
                    previewSketch.repaint();
                    }
            });

            JScrollPane scrollPane = new JScrollPane(table);

            JScrollPane scrollPane2 = new JScrollPane(table2);

            tablePanel4.add(scrollPane2);
            tablePanel2.add(jButton, BorderLayout.CENTER);
            tablePanel1.add(scrollPane);

            tablePanel3.add(tablePanel4);
            tablePanel3.add(tablePanel1);
            tablePanel3.add(tablePanel2);
            help.getContentPane().add(tablePanel3);
        } catch (Exception e) {
            System.out.println("Exception:"+e.getMessage());
        }
        target.refresh();
        return new JFrame[]{frame,help};

    }

        //поиск топ-показателей
    private static Map<String, Double> find_top(Node[] nodes, int howMany, Column column) {
        Map<String, Double> results = new LinkedHashMap<String, Double>();
        Node[] new_nodes = new Node[nodes.length];
        double max = 0;
        for (int i = 0; i < nodes.length; i++) {
            for (int j = i + 1; j < nodes.length - 1; j++) {
                if (Double.valueOf(nodes[j].getAttribute(column).toString())
                        > Double.valueOf(nodes[i].getAttribute(column).toString())) {
                    Node node = nodes[i];
                    nodes[i] = nodes[j];
                    nodes[j] = node;
                }
            }
        }
        for (int i = 0; i < howMany; i++) {
            results.put(nodes[i].getAttributes()[0].toString(),
                    Double.valueOf(nodes[i].getAttribute(column).toString()));
        }
        return results;
    }

    private static Map<String, Double> find_partitions(Partition partition) {
        Map<String, Double> result = new LinkedHashMap<String, Double>();

        Collection collection = partition.getSortedValues();
        Iterator it = collection.iterator();
        while (it.hasNext()) {
            Object value = it.next();
            double perc = (double) partition.percentage(value);
            result.put(value.toString(), perc);
            colors.put(value.toString(), partition.getColor(value));
        }
        return result;
    }

    private static JTable create_table(int size, String type, Map<String, Set<String>> list, boolean exists) {
        JTable result = null;
        String[] header = new String[2];
        Object[][] data = new Object[size][header.length];
        if (type.equals("Betweeness") || (type.equals("PageRank"))) {
            header = new String[2];
            header[0] = "UserId";
            header[1] = "Value";
            int i = 0;
            for (Map.Entry<String, Double> entry : top.entrySet()) {
                data[i][0] = entry.getKey().toString();
                data[i][1] = entry.getValue().toString();
                i++;
            }
            result = new JTable(data, header);
        } else if (type.equals("Modularity")) {
            header[0] = "GroapId";
            header[1] = "User percentage(%)";
            int i = 0;
            for (Map.Entry<String, Double> entry : top.entrySet()) {
                data[i][0] = entry.getKey().toString();
                data[i][1] = entry.getValue().toString();
                i++;
            }
            result = new JTable(data, header);
            result.setDefaultRenderer(Object.class, new TableInfoRenderer());
        }
        else if (type.equals("Cores")){
            header = new String[4];
            data = new Object[size][header.length];
            header[0] = "№";
            header[1] = "Node id";
            header[2] = "Cores count";
            header[3] = "Cores list";
            int i = 0;
            for (Map.Entry<String, Set<String>> entry : list.entrySet()) {
                data[i][0] = i+1;
                if(!exists)
                    data[i][1] = core.Cores.nodeId.get(entry.getKey());
                else
                    data[i][1] = entry.getKey();

                data[i][2] = entry.getValue().size();
                data[i][3] = entry.getValue().toString();
                i++;
            }
            result = new JTable(data, header);

            result.setDefaultRenderer(Object.class, new TableInfoRenderer());
        }
        else if (type.equals("Cliques")){
            header = new String[4];
            data = new Object[size][header.length];
            header[0] = "№";
            header[1] = "Node id";
            header[2] = "Cliques count";
            header[3] = "Cliques list";
            int i = 0;
            for (Map.Entry<String, Set<String>> entry : list.entrySet()) {
                data[i][0] = i+1;
                data[i][1] = entry.getKey();
                data[i][2] = entry.getValue().size();
                data[i][3] = entry.getValue().toString();
                i++;
            }
            result = new JTable(data, header);

            result.setDefaultRenderer(Object.class, new TableInfoRenderer());
        }
        else if ((type.equals("Clusters"))||(type.equals("NAGClusters"))){
            header = new String[4];
            data = new Object[size][header.length];
            header[0] = "№";
            header[1] = "Cluster";
            header[2] = "Members";
            header[3] = "Members amount";
            int i = 0;
            for (Map.Entry<String, Set<String>> entry : list.entrySet()) {
                data[i][0] = i+1;
                data[i][1] = entry.getKey();
                if(!exists) {
                        data[i][2] =  entry.getValue().toString();
                }
                else{
                    data[i][2] = entry.getValue().toString();

                }
                data[i][3] = entry.getValue().size();
                i++;
            }
            result = new JTable(data, header);

            result.setDefaultRenderer(Object.class, new TableInfoRenderer());
        }


        return result;
    }

    private static JTable createNodesTable(String type){
        JTable table = null;
        DirectedGraph graph = null;
        if(type.equals("Cores")){
            graph = Cores.directedGraph;
        }
        else if (type.equals("Cliques")){
            graph = Clique.directedGraph;
        }
        else if (type.equals("Clusters")){
            graph = Clusters.directedGraph;
        }
        else if (type.equals("NAGClusters")){
            graph = NewmanAndGirvan.directedGraph;
        }

        String[] header = new String[2];
        Object[][] data = new Object[graph.getNodes().toArray().length][header.length];
        header[0] = "Node number";
        header[1] = "Node id";
        Node [] nodes = graph.getNodes().toArray();
        int i = 0;
        for (Node node: nodes){
            data[i][0] = node.getLabel();
            data[i][1] = node.getId();
            i++;
        }

        table = new JTable(data,header);
        table.setAutoCreateRowSorter(true);
        table.setDefaultRenderer(Object.class, new TableInfoRenderer());

        return table;
    }


    public static class TableInfoRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
            Font font = new Font("Century Gothic", Font.BOLD, 14);
            c.setFont(font);
            return c;
        }
    }

    private static void export(String fileName) {
        ExportController ec = Lookup.getDefault().lookup(ExportController.class);
        try {
            ec.exportFile(new File("src/java/core/"+fileName + ".gexf"));
        } catch
                (IOException ex) {
            ex.printStackTrace();
            return;
        }

    }


    //разбиение на 8 сообществ
    private void partition(GraphModel graphModel) throws SQLException {
        Column modCol = graphModel.getNodeTable().addColumn("class", "Class", String.class, new String(""));

        setColumn(graphModel, "first_channel", "blue", modCol);
        setColumn(graphModel, "second_channel", "green", modCol);
        setColumn(graphModel, "third_channel", "violet", modCol);
        setColumn(graphModel, "forth_channel", "orange", modCol);
        setColumn(graphModel, "fifth_channel", "yellow", modCol);
        setColumn(graphModel, "sixth_channel", "dark blue", modCol);
        setColumn(graphModel, "special_nodes", "red", modCol);
        setColumn(graphModel, "last_nodes", "gray", modCol);


    }
}