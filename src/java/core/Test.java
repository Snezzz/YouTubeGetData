package core;

import core.Magician;
import sun.security.util.BitArray;

import java.util.ArrayList;

public class Test {


    public static void main(String[]args){
        ArrayList<BitArray> testArray = new ArrayList<BitArray>();
        BitArray bitArray = new BitArray(4);

        /*
        bitArray.set(0,true);
        bitArray.set(1,true);
        bitArray.set(2,true);
        bitArray.set(3,false);
        testArray.add(bitArray);

        bitArray = new BitArray(4);
        bitArray.set(0,true);
        bitArray.set(1,true);
        bitArray.set(2,false);
        bitArray.set(3,true);
        testArray.add(bitArray);

        bitArray = new BitArray(4);
        bitArray.set(0,true);
        bitArray.set(1,false);
        bitArray.set(2,true);
        bitArray.set(3,true);
        testArray.add(bitArray);

        bitArray = new BitArray(4);
        bitArray.set(0,true);
        bitArray.set(1,false);
        bitArray.set(2,true);
        bitArray.set(3,true);
        testArray.add(bitArray);

        bitArray = new BitArray(4);
        bitArray.set(0,false);
        bitArray.set(1,true);
        bitArray.set(2,true);
        bitArray.set(3,false);
        testArray.add(bitArray);

        bitArray = new BitArray(4);
        bitArray.set(0,false);
        bitArray.set(1,true);
        bitArray.set(2,true);
        bitArray.set(3,true);
        testArray.add(bitArray);

        bitArray = new BitArray(4);
        bitArray.set(0,false);
        bitArray.set(1,true);
        bitArray.set(2,true);
        bitArray.set(3,true);
        testArray.add(bitArray);

        bitArray = new BitArray(4);
        bitArray.set(0,false);
        bitArray.set(1,true);
        bitArray.set(2,true);
        bitArray.set(3,true);
        testArray.add(bitArray);

*/
        bitArray = new BitArray(4);
        bitArray.set(0,true);
        bitArray.set(1,false);
        bitArray.set(2,true);
        bitArray.set(3,false);
        testArray.add(bitArray);


        bitArray = new BitArray(4);
        bitArray.set(0,true);
        bitArray.set(1,true);
        bitArray.set(2,false);
        bitArray.set(3,false);
        testArray.add(bitArray);

        bitArray = new BitArray(4);
        bitArray.set(0,false);
        bitArray.set(1,true);
        bitArray.set(2,true);
        bitArray.set(3,true);
        testArray.add(bitArray);

        bitArray = new BitArray(4);
        bitArray.set(0,false);
        bitArray.set(1,true);
        bitArray.set(2,true);
        bitArray.set(3,true);
        testArray.add(bitArray);





        Magician m = new Magician(4);
        m.find(testArray);
      // core.CounterAkka counterAkka = new core.CounterAkka(testArray);
    }
}
