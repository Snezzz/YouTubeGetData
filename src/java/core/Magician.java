package core;

import org.apache.commons.collections4.list.TreeList;
import sun.security.util.BitArray;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Magician {
    BitArray  arr;
    BitArray [] matrix;
    public static BlockingQueue<ArrayList <BitArray> >  disjunctionList;
    Magician(int length){
        this.arr = new BitArray(length);
        this.matrix = new BitArray[length];
    }

    //по матрице смежности
    //для каждой строки строим конъюнкцию
    //x_i или все ее соседи
    //neighbours - Индексы соседей
    //i - индекс текущей вершины
    void setNeighbours(int i, int[] neighbours){
        BitArray bitArray = new BitArray(this.arr.length());

        for(int neighbour: neighbours){
            bitArray.set(neighbour,true);
        }

        this.matrix[i] = bitArray;
    }

    //строим слагаемое
    //1 - код x_i
    //2 - код его соседей
    ArrayList<BitArray> getTerm(int i, boolean external){

        ArrayList<BitArray> term =  new ArrayList<BitArray>();
        BitArray bitArray = new BitArray(this.arr.length());
        bitArray.set(i,true);

        term.add(0, bitArray);
        if (!external) {
            term.add(1, this.matrix[i]);
        }
        else {
            BitArray b = this.matrix[i];

            for (int j = 0; j < b.length(); j++){
                if (b.get(j)){
                    BitArray a = new BitArray(b.length());
                    a.set(j,true);
                    term.add(a);
                }
            }
        }

        return term;
    }

    public ArrayList<BitArray> getExpression(BitArray [] matrix, boolean external) throws InterruptedException {


        BlockingQueue<ArrayList <BitArray>> arr = new LinkedBlockingQueue<ArrayList <BitArray>>();

            for (int i = 0; i < matrix.length; i++) {
                if (!external)
                    arr.add(getTerm(i,false));
                else arr.add(getTerm(i, true));
            }


        CounterAkka counterAkka = new CounterAkka(arr);
        BlockingQueue<ArrayList <BitArray>>  result = counterAkka.disjunctionsList; //упростили
        ArrayList<BitArray> ult = Magician.find(result.take());

        int prev = ult.size();
        int current = 0;

        while(prev!=current){
            current = ult.size();
            ult = Magician.find(ult);
            prev = ult.size();
        }
        if (!external){
            ArrayList<BitArray> finalResult = new ArrayList<BitArray>(matrix.length);
            for (int j = 0; j < ult.toArray().length; j++){
                BitArray currentElement = ult.get(j);
                BitArray addition = new BitArray(matrix.length);
                for (int i = 0; i < currentElement.length(); i++){
                    if (!currentElement.get(i))
                        addition.set(i,true);
                }
                finalResult.add(j,addition);
            }
            return finalResult;
        }
        return ult;
    }

    //1 - код x_i
    //2 - код его соседей
    static ArrayList<BitArray>  express(ArrayList<BitArray> a, ArrayList<BitArray> b){

        ArrayList<BitArray> result = new ArrayList<BitArray>(a.size()*b.size());
        int k = 0;

        for (int i = 0; i < a.size(); i++){
            BitArray a_i = a.get(i);
            for(int j = 0; j< b.size(); j++){
                //System.out.println("Считаю дизъюнкцию");
                BitArray bitArray = disjunction(a_i, b.get(j));
                result.add(k,bitArray);
                k++;
            }
        }


        BitArray [] arr = new BitArray[result.size()];
        return result;


    }
    static int getCountTrue(boolean[] a) {
        int count = 0;
        for (boolean i : a) {
            if (i)
                count++;
        }
        return count;
    }

    //метод упрощения
    static ArrayList<BitArray> find(ArrayList<BitArray> elements){
        ArrayList<BitArray> newElements = new ArrayList<BitArray>();
        List<Integer> indexes = new TreeList<Integer>();

        //убираем повторяющиеся элементы
        Set<BitArray> set = new HashSet<BitArray>(elements);
        elements.clear();
        elements.addAll(set);
        ArrayList<BitArray> b = (ArrayList<BitArray>)elements.clone();

        System.out.println("start");
        System.out.println("размер массива:"+elements.size());
        //смотрим на 1 слагаемое и упрощаем его
        for(int i=0; i < elements.size(); i++){
            BitArray current = elements.get(i); //берем элемент
            int count1 = getCountTrue(current.toBooleanArray()); //считаем количество 1

            for(int j = b.size()-1; j > -1 ; j--) {
                    int count2 = getCountTrue(b.get(j).toBooleanArray()); //считаем количество 1 у второго
                    //если у 1го меньше 1, чем у 2го
                    if (count1 <= count2) {
                        BitArray result = absorption(current, b.get(j)); //упрощаем по поглощению
                        if (result!=null && (!indexes.contains(j))) {
                            indexes.add(j);         //добавляем упрощенный вариант
                            b.remove(j);
                        }
                    }
                }
        }


        System.out.println("end");

        for(Integer index: indexes){
            elements.set(index,null);
        }
        for(BitArray bitArray:elements){
            if(bitArray!=null)
                newElements.add(bitArray);
        }

        return  b;
    }



    int findMin(int index, ArrayList<BitArray> list){
        int maxCount = 1000000000;
        int helpIndex = -1;

        for(int i=0; i<list.size(); i++){
            int count = getCountTrue(list.get(i).toBooleanArray());
            if(i == index)
                continue;
            if(count < maxCount) {
                helpIndex = i;
                maxCount = count;
            }
        }
        return helpIndex;
    }

    int getCount(boolean[]a, int maxCount){
        int count = 0;
        for(boolean i: a){
            if (i)
                count++;
        }
        if (count > maxCount)
            maxCount = count;
        return maxCount;
    }

    //сложение
    static BitArray disjunction(BitArray a, BitArray b){
        BitArray finalArray = new BitArray(a.length());
        for (int i = 0; i< a.length(); i++){
            boolean a_i = a.toBooleanArray()[i];
            boolean b_i = b.toBooleanArray()[i];
            finalArray.set(i, a_i || b_i);
        }
        return finalArray;
    }

    //поглощение
    //в search количество 1 меньше, чем в where
    static BitArray absorption(BitArray search, BitArray where){

        boolean [] from = search.toBooleanArray();
        int count = 0;
        int find_count = 0;
        //считаем количество 1
        for (int i = 0; i < search.length(); i++){
            if(from[i]){
                count++;
            }
        }

        for (int i = 0; i < search.length(); i++){
            if((search.get(i))&&(where.get(i))){
                 find_count++;
            }
        }
        //нашли
        if (find_count == count)
            return search;

        return null;
    }

    public static Map<String,Set<Integer>> intersection(List<Set<Integer>> A, List<Set<Integer>> B){
        List<Set<Integer>> list = new ArrayList<Set<Integer>>();
        Map<String,Set<Integer>> result = new TreeMap<String, Set<Integer>>();
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

        if (A.size() == 1 || B.size() == 1){
            if(A.get(0).size() == 0 || B.get(0).size() == 0)
                return result;
        }


        for (Set<Integer> extrList: A){
            if(B.contains(extrList)){
                list.add(extrList);
            }
        }
        for (int i = 0; i < list.size(); i++){
            result.put(String.valueOf(i), list.get(i));
        }

        return result;
    }
}
