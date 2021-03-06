package main;

import data.Comment;
import data.Video;
import database.DBConnection;
import org.gephi.preview.api.*;
import org.gephi.project.api.ProjectController;
import org.openide.util.Lookup;

import javax.swing.*;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.List;


public class GetData {
    public Connection c;
    Statement stmt;
    String sql;
    private UnicodeXMLStreamWriter out;
    private XMLStreamReader in;
    public  static Map <String,Map <String, String>> nodes;
    public  static Map <String,Integer> comments_count;
    public static ProjectController pc;
    public static  Map <String,String> names;

    public static  Map <String,String> node_number;
    public Map <String, Video> main_map;
    DBConnection db;
    GraphCreater graphCreater;

    public void run() throws SQLException, IOException, XMLStreamException, InterruptedException {
        comments_count=new HashMap<String, Integer>();


        //соединение с БД
        db = new DBConnection();
        db.makeConnection("postgres");
        //создание пулов соединений
        double in = System.currentTimeMillis();
        //получение вершин (запрос 1)
        getNodes();
        get_comments_count();
        System.out.println("время:"+ (System.currentTimeMillis()-in)/1000);
        System.out.println(nodes.size());
      //  get();
       // create_xml(main_map);
        //3.визуализация
        pc = Lookup.getDefault().lookup(ProjectController.class);
        JFrame menu=new JFrame("Constructor");
        final JPanel panel=new JPanel();
        panel.setBackground(new Color(232,255,224));
        JLabel label = new JLabel();
        label.setText("min node degree:");
        JLabel stowageLabel = new JLabel();
        stowageLabel.setText("Тип укладки:");

        final JTextField count = new JTextField(2);
        count.setText("1");



        String[] type = {
                "OpenOrd",
                "Yifan Hu",
                "Force Atlas 2"
        };
        final JComboBox stowage = new JComboBox(type);
        stowage.setBackground(Color.white);
        String[] items = {
                "Default",
                "Betweeness_centrality",
                "Page_rank",
                "Modularity",
                "Filter",
                "Special"
        };
        stowage.setSize(100,20);
        final JComboBox box=new JComboBox(items);
        box.setBackground(Color.white);
        box.setSize(100,20);
        box.setEditable(false);
        stowage.setEditable(false);

        JButton buildGraphbutton=new JButton("Построить граф");
        buildGraphbutton.setBackground(new Color(189,234,152));
        buildGraphbutton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean Default = false;
                boolean betweeness = false;
                boolean page_rank = false;
                boolean modularity = false;
                boolean filter = false;
                boolean special = false;
                int k = Integer.valueOf(count.getText());
                String type=box.getSelectedItem().toString();
                if(type.equals("Betweeness_centrality"))
                    betweeness = true;
                else if(type.equals("Page_rank"))
                    page_rank = true;
                else  if(type.equals("Modularity"))
                    modularity = true;
                else if (type.equals("Filter"))
                    filter = true;
                else if (type.equals("Special"))
                    special = true;

                else{
                    Default = true;
                }
                String stowage_type = stowage.getSelectedItem().toString();

                //строим график
                try {
                    G2DTarget target = null;
                    graphCreater = new GraphCreater(k,"",
                            stowage_type,Default,betweeness,page_rank,modularity,filter,special,target);//создание графа
                } catch (IOException e1) {
                    e1.printStackTrace();
                } catch (CloneNotSupportedException e1) {
                    e1.printStackTrace();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }

            }
        });

        JButton showGraphButton=new JButton("Исследовать граф");
        showGraphButton.setBackground(new Color(189,234,152));
        showGraphButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // try {
                 try {
                     new core.GetData().run(graphCreater);
                 } catch (InterruptedException e1) {
                     e1.printStackTrace();
                 } catch (IOException e1) {
                     e1.printStackTrace();
                 } catch (XMLStreamException e1) {
                     e1.printStackTrace();
                 } catch (SQLException e1) {
                     e1.printStackTrace();
                 }
            }
        });

        panel.add(label);
        panel.add(count);

        panel.add(stowageLabel);
        panel.add(stowage);
        panel.add(box);
        panel.add(buildGraphbutton);
        panel.add(showGraphButton);


        menu.add(panel);

        menu.add(panel);
        menu.setSize(220,180);
        menu.setLocation(400,150);
        menu.setVisible(true);
    }

     private  void getNodes() throws SQLException {

      /*  sql="select parent_id,channel_id,author_id,count(parent_id) from comments\n" +
                "  join videos on comments.video_id = videos.video_id" +
                " group by parent_id,channel_id,author_id";
                */
        String parend_id,author_id, count;
        nodes = new HashMap<String, Map<String, String>>();
        names = new HashMap<String, String>();
        node_number = new HashMap<String, String>();
        sql = "select * from youtube_schema.users";
        ResultSet rs = db.makeQuery(sql);

        rs = db.makeQuery(sql);
        while(rs.next()){
            names.put(rs.getString(3),rs.getString(2));
            node_number.put(rs.getString(1), rs.getString(3));
        }

        sql = "select * from youtube_schema.weight";
        rs = db.makeQuery(sql);


        //обращаемся к каждой строке
        while(rs.next()){
            parend_id = rs.getString(2);
            author_id = rs.getString(1);
            count = rs.getString(3);
            if(nodes.containsKey(parend_id)){
                Map <String, String> old_map = new HashMap<String, String>();
                old_map = nodes.get(parend_id);
                old_map.put(author_id,count);
                nodes.put(parend_id,old_map);
            }
            else{
                Map<String, String> children = new HashMap<String, String>();
                children.put(author_id,count);
                nodes.put(parend_id,children);
            }
        }


    }

    private void get() throws SQLException{
        String sql = "select * from videos inner join " +
                "comments on videos.video_id = comments.video_id order by comment_id";
        ResultSet rs = db.makeQuery(sql);

        Map <String, String> comments = new HashMap<String,String>();
        Map <String, String> answers = new HashMap<String, String>();
        Map <String, Comment> answer = new HashMap<String,Comment>();
        Map <String, Comment> comment = new HashMap<String, Comment>();
        main_map = new HashMap<String, Video>();
        Video video = null;
        while(rs.next()){

            String id = rs.getString("video_id");
            if(!main_map.containsKey(id)){
                video = new Video();
                video.setHeader(rs.getString("video_title"));
                video.setDescription(rs.getString("description"));
                video.setChannel_name(rs.getString("author"));
                video.setChannel_id(rs.getString("channel_id"));
                video.setDate(rs.getString("publication_date"));
                String [] tags= rs.getString("tags").split(",");
                video.setTags(tags);
                video.setLikes(rs.getInt("likes_count"));
                video.setDislikes(rs.getInt("dislikes_count"));
                video.setViews(rs.getInt("view_count"));
                video.setComments_count(rs.getInt("comments_count"));
                main_map.put(id,video);
            }

            String comment_id = rs.getString("comment_id");
            String comment_author_id= rs.getString("author_id");
            String comment_author_name= rs.getString("comment_author");
            String comment_text= rs.getString("comment_text");
            boolean isAnswer = rs.getBoolean("answer");
            //ответ
            if(isAnswer) {
                String parent_id = comment_id.split("\\.")[0];
                answers.put(comment_id, parent_id);
                Comment answer1 = new Comment(comment_id, comment_author_id, comment_author_name, comment_text);
                answer.put(comment_id, answer1);  //id ответа - данные ответа
            }
                 //комментарии
            else {
                String parent_id = rs.getString("channel_id");
                Comment comment1 = new Comment(comment_id,comment_author_id,comment_author_name,comment_text);
                comment.put(comment_id,comment1);
                comments.put(comment_id,parent_id);
                main_map.get(id).addCommentId(comment_id); //складываем все id комментариев
            }
        }
        int count = 0;
        for(Map.Entry<String,String> entry : answers.entrySet()) {
            if (comment.get(entry.getValue()) == null) {
                count++;
            } else {
                comment.get(entry.getValue()).addAnswer(answer.get(entry.getKey()));
            }
        }
        int j=0;

        for(Map.Entry<String,Video> entry3 : main_map.entrySet()) {
            Video current_video = entry3.getValue();
            List<String> comment_ids = current_video.getComment_id();
           //каждый комментарий
            for(String id: comment_ids){
                Comment current_comment = comment.get(id); //комментарий
                List<String> current_answers = current_comment.getAnswers_id();
                //все ответы на комментарий
                for(String answer_id: current_answers){
                    Comment current_answer= answer.get(answer_id);
                    current_comment.addAnswer(current_answer); //добавляем каждый ответ
                }
            current_video.setComments(current_comment); //добавляем комментарий с ответами
            }
            j++;

        }
    }

    private void get_comments_count() throws SQLException {

        ResultSet resultSet=null;
        try {
            String sql = "select * from youtube_schema.weight";
            resultSet=db.makeQuery(sql);
            while (resultSet.next()){
                String who_to_whom=resultSet.getString("author_id")+"!"
                        +resultSet.getString("parent_id");
                if(comments_count.containsKey(who_to_whom)){
                  //  System.out.println("есть");
                    int old_val = comments_count.get(who_to_whom);
                    int new_val = old_val + Integer.valueOf(resultSet.getString("count"));
                    comments_count.put(who_to_whom, new_val);
                }
               else {
                    comments_count.put(who_to_whom,
                            Integer.valueOf(resultSet.getString("count")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

        private void get_tags(String from,Map <String, Object> to){
            if(from.contains(",")) {
                String[] tags = from.substring(1, from.length() - 1).split(",");
                to.put("Tags",tags);
            }
            else{
                to.put("Tags","");
            }

        }
        int global_i;

    public void getUsers(){

    }
public void remake() throws SQLException {
    sql="select distinct id from nodes";
    ResultSet rs = stmt.executeQuery(sql);
    Map <String, Integer> ids = new TreeMap<String, Integer>();
    int i = 1;
    while(rs.next()){
        String old_id = rs.getString(1);
        if(ids.containsKey(old_id)){
            System.out.println("есть!"+old_id);
            continue;
        }
        else {
            ids.put(old_id,i);
            i++;
        }
    }
    //sql = "select author_id as id,comment_author as name,count(comment_id)\n" +
      //      "  from comments group by comment_author,author_id order by comment_author;";
    sql = "select * from edges";
    rs = stmt.executeQuery(sql);
    /*while(rs.next()){
        String user_id = rs.getString(1);
        int id = ids.get(user_id);
        sql = "insert INTO postgres.public.table_name (id, user_id, name, count) values (?,?,?,?)";
        PreparedStatement  preparedStatement = c.prepareStatement(sql);
        preparedStatement.setInt(1, id);
        preparedStatement.setString(2,user_id);
        preparedStatement.setString(3,rs.getString(2));
        preparedStatement.setInt(4,Integer.valueOf(rs.getString(3)));
        preparedStatement.executeUpdate();
    }*/
    while (rs.next()){
        String source = rs.getString(1);
        String target = rs.getString(2);
        sql = "insert INTO postgres.public.new_edges (source, target) values (?,?)";
        PreparedStatement  preparedStatement = c.prepareStatement(sql);
        preparedStatement.setInt(1, ids.get(source));
        preparedStatement.setInt(2, ids.get(target));
        preparedStatement.executeUpdate();
    }
    stmt.close();
    c.commit();
    System.out.println("end");

}
}

