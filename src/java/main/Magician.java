package main;

import org.apache.commons.collections4.list.TreeList;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Node;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Magician {

    public static List<String> resultsList;

    public static Map <String, List<String>> readData(String filePath) {
        Map <String, List<String>> data = new TreeMap<String, List<String>>();
        try {
            FileReader fr = new FileReader(filePath);
            BufferedReader br = new BufferedReader(fr);
            String line;
            int a = 0;
            while ((line = br.readLine()) != null) {
                String [] values = line.split(",");
                List<String> neighbours = null;
                //уже рассматривался
                if(data.containsKey(values[1]))
                    neighbours = data.get(values[1]);
                else
                    neighbours = new ArrayList<String>();

                neighbours.add(values[1]);
                data.put(values[0],neighbours);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }



    Magician(DirectedGraph graph){

        finalVariant(graph);

    }


    public static List<Set<Node>> intersection(List<Set<Node>> A,List<Set<Node>> B){
        List<Set<Node>> result = new ArrayList<Set<Node>>();
        Iterator external = A.listIterator();
        Iterator internal = B.listIterator();

        System.out.println("внешне независимые множества вершин");
        while (external.hasNext()){
            System.out.println(external.next());
        }

        System.out.println("внутренне независимые множества вершин");
        while (internal.hasNext()){
            System.out.println(internal.next());
        }

        for (Set<Node> extrList: A){
            if(B.contains(extrList)){
                result.add(extrList);
            }
        }
        return result;
    }


    public static Set<Node> findInside(String vertexes,DirectedGraph graph, String type){

        Set<Node> result1 = new LinkedHashSet<Node>();
        Set<Node> result2 = new LinkedHashSet<Node>();
        for (Node node: graph.getNodes()) {
                if(type.equals("external")) {
                    if (vertexes.contains(node.getId().toString())) {
                        result1.add(node);
                    }
                }
                else if(type.equals("internal")){
                if (!vertexes.contains(node.getId().toString())) {
                    result2.add(node);
                }
            }
        }
         if(type.equals("external"))
            return result1;
         return result2;
    };


    public static String createDisjunction(String a, String b){
        System.out.println(a+","+b);
            return "(" + a + "+" + b + ")";
    }

    public static String createСonjunction(String a,String b){
        return a+"*"+b;
    }


    public static void finalVariant(DirectedGraph directedGraph) {
        BlockingQueue<String> disjunctions = new LinkedBlockingQueue<String>();
        List<String> disjunctions2 = new ArrayList<String>();


        //перебираем вершины
        for (Node n : directedGraph.getNodes().toArray()) {
            List<String> dis = new ArrayList<String>();
            String n_id = GetData.node_number.get(n.getId().toString());
            Node[] neighbours = directedGraph.getNeighbors(n).toArray();
            String value = null;
            //перебираем смежные вершины
            for (Node neighbour: neighbours) {
                String neighbour_id = GetData.node_number.get(neighbour.getId().toString());

                //создаем дизъюнкцию и добавляем в список для внутренней
                disjunctions.add(createDisjunction(n_id, neighbour_id));

                if (value == null) {
                    value = createDisjunction(n_id, neighbour_id);
                } else {
                    //     String lastValue = dis.get(((ArrayList) dis).size() - 1);
                    String currentValue = value.substring(1, value.length() - 1);
                    //     System.out.println(lastValue.toString());
                    //      System.out.println(neighbour_id);
                    value = createDisjunction(currentValue,
                            neighbour_id);
                }
            }
            //для внешней
                disjunctions2.add(value);
            }

          //  core.CounterAkka ca = new core.CounterAkka(disjunctions);
        while(resultsList.size() == 0){
            System.out.println("I am working...");
        }
        Iterator it1 = resultsList.iterator();
           // String res1 =  createConjunction(disjunctions);
         //   String res2 = createConjunction(disjunctions);
    //   List<Set<String>> finalList1 = new ArrayList<Set<String>>();
        List<Set<Node>> finalList1 = new TreeList<Set<Node>>();
      //  Set<String > result1 = simplify(res1.toString());
            //Iterator it1 = result1.iterator();

        while (it1.hasNext()){
            finalList1.add(findInside(it1.next().toString(),directedGraph,"external"));
        }
    //    List<Set<Node>> finalList2 = new TreeList<Set<Node>>();
      //  Set<String > result2 = simplify(res2.toString());
        //Iterator it2 = result2.iterator();

        //while (it2.hasNext()){
          //  finalList2.add(findInside(it2.next().toString(),directedGraph,"internal"));
        //}

       // List<Set<Node>> result = intersection(finalList1,finalList2);
        System.out.println("");

    }

    public static String createConjunction( List<String> disjunctions) {
        String conjunction = disjunctions.get(0);
        for (int i = 1; i< disjunctions.size(); i++){
            conjunction = createСonjunction(conjunction,disjunctions.get(i));
        }
        return conjunction;
    }


    public static String my (String expression){
        String [] parts = expression.split("[*]");
        Set <String> result = new LinkedHashSet<String>();

        result.add(conjunction(parts[0].substring(1,parts[0].length()-1),
                parts[1].substring(1,parts[1].length()-1)));

        //O(количество частей)
        for (int i = 2; i < parts.length; i++){
            String res = result.toArray()[result.size()-1].toString();

            String sortedValues = sortValues(conjunction(res.substring(1,res.length()-1),
                    parts[i].substring(1,parts[i].length()-1)));
            result = search(sortedValues,result);
        }
        return result.toArray()[result.size()-1].toString(); //(a+b)
    }

    public static List<String> second(String res){

        System.out.println(res);
        Set<String> newValues = new LinkedHashSet<String>();

        Set<String> finalValues = search(res.substring(1,res.length()-1),newValues);
        String substr = null;
        if(finalValues.size() > 1)
            substr = finalValues.toArray()[1].toString();
        else
            substr = finalValues.toArray()[0].toString();
        String [] data = substr.substring(1,substr.length()-1).split("[+]");
        List<String> answer = new LinkedList<String>();

        answer.addAll(Arrays.asList(data));

        return answer;
    }

    public static List<String> simplify(String expression){
        String [] parts = expression.split("[*]");
        Set <String> result = new LinkedHashSet<String>();

        result.add(conjunction(parts[0].substring(1,parts[0].length()-1),
                        parts[1].substring(1,parts[1].length()-1)));

        //O(количество частей)
        for (int i = 2; i < parts.length; i++){
            String res = result.toArray()[result.size()-1].toString();

            String sortedValues = sortValues(conjunction(res.substring(1,res.length()-1),
                    parts[i].substring(1,parts[i].length()-1)));
            result = search(sortedValues,result);
        }
        String res = result.toArray()[result.size()-1].toString();

        Set<String> newValues = new LinkedHashSet<String>();

        Set<String> finalValues = search(res.substring(1,res.length()-1),newValues);
        String substr = null;
        if(finalValues.size() > 1)
            substr = finalValues.toArray()[1].toString();
        else
            substr = finalValues.toArray()[0].toString();
        String [] data = substr.substring(1,substr.length()-1).split("[+]");
        List<String> answer = new LinkedList<String>();

        answer.addAll(Arrays.asList(data));

        return answer;

    }

    static Set <String> search(String sortedValues, Set<String> result){


        String [] forSearch = sortedValues.split("[+]");
        Set<String> values = new LinkedHashSet<String>();
        values.addAll(Arrays.asList(forSearch));
        Iterator iterator = values.iterator();
        Set<String> results = values;

        //ДОЛГО
        //ищем эти шаблоны в нащих слагаемых
        while (iterator.hasNext()){
            String current = iterator.next().toString();
            results = find(current,results,getIndex(results,current));
            Object [] fin = results.toArray();
            String finR="(";
            for(int k=0; k<fin.length; k++){
                if(k!=fin.length-1){
                    finR+=fin[k].toString()+"+";
                }
                else
                    finR+=fin[k].toString()+")";
            }
            result.add(finR);
        }

    //    System.out.println(result);
        return result;
    }


    public static int getIndex(Set<? extends Object> set, Object value) {
        int result = 0;
        for (Object entry:set) {
            if (entry.equals(value)) return result;
            result++;
        }
        return -1;
    }

    public static Set<String> find(String val, Set <String> set,int ind){
      ;

        String [] values = val.split("[*]");

        String regExp = "("+val+"|";
        for(int i = 0; i < values.length; i++){
            regExp+=values[i]+"[0-9]*";
        }
        regExp+=")";
        Object [] parts= set.toArray();

        Pattern r = Pattern.compile(regExp);

        for(int i=0;i<parts.length;i++){
            Matcher m = r.matcher(parts[i].toString());
            if((r.matcher(parts[i].toString()).find())&&(i!=ind)){
                parts[i] = null;
            }
        }
        Set<String> result = new HashSet<String>();
        for(int i=0;i<parts.length;i++){
            if(parts[i]!=null){
                result.add(parts[i].toString());
            }
        }

        return result;

    }
    public static String sortValues(String word){
        String [] parts = word.substring(1,word.length()-1).split("[+]");
        for(int i = 0; i < parts.length; i++){
            for(int j = 0; j < parts.length-i-1; j++){
                if(parts[j].length()>parts[j+1].length()){
                    String help = parts[j];
                    parts[j] = parts[j+1];
                    parts[j+1] = help;
                }

            }
        }
        String finalResult="";
        for(int i = 0; i < parts.length; i++){
            if(i!=parts.length-1)
                finalResult+=parts[i]+"+";
            else
                finalResult+=parts[i];
        }
        return finalResult;
    }
    public static String sort(String word){
        String [] parts = word.split("[*]");
        for(int i = 0; i < parts.length; i++){
            for(int j = 0; j < parts.length-i-1; j++){
                int valueA = Integer.valueOf(parts[j]);
                int valueB = Integer.valueOf(parts[j+1]);
                if(valueA>valueB){
                    String help = parts[j];
                    parts[j] = parts[j+1];
                    parts[j+1] = help;
                }

            }
        }
        String finalResult="";
        for(int i = 0; i < parts.length; i++){
            if (i == 0)
                finalResult+=parts[i];
            else
                finalResult+="*"+parts[i];
        }
        return finalResult;
    }
    //раскрывание скобок
    public static String conjunction(String a,String b){
        String [] partsA = a.split("[+]");
        String [] partsB = b.split("[+]");
        List<String> result = new ArrayList<String>();
        String conj = "";
        for(int i = 0; i < partsA.length; i++ ){
            for(int j = 0; j < partsB.length; j++ ){
                if(partsA[i].contains(partsB[j])||(partsB[j].contains(partsA[i]))){
                    result.add(partsA[i]);
                }
                else{
                    String data = sort( partsA[i]+"*"+partsB[j]);

                    result.add(data);
                }
                result.add("+");
            }

        }
        for(String word:result){
            conj+=word;
        }

        return "("+conj.substring(0,conj.length()-1)+")";
    }



}
