package core;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedAbstractActor;
import akka.routing.RoundRobinPool;
import sun.security.util.BitArray;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class CounterAkka {


    boolean finish = false;
    public static BlockingQueue<ArrayList <BitArray> >  disjunctionsList;

    CounterAkka(BlockingQueue<ArrayList <BitArray> > disjunctions){
        this.disjunctionsList = disjunctions;
        final ActorSystem system =  ActorSystem.create("MySystem");
        ActorRef master = system.actorOf(MasterActor.props(),"master");

        //размер четен
        if(disjunctions.size()%2 == 0)
            master.tell(new Start(disjunctions,0),null);
        //размер нечетен
        else if(disjunctions.size()%2 != 0)
            master.tell(new Start(disjunctions,1),null);

        while(!master.isTerminated()){
           finish = false;
        }
        System.out.println(disjunctionsList);
    }



   public static class DownloaderActor extends UntypedAbstractActor {

       public static Props props() {
           return Props.create(DownloaderActor.class);
       }
        @Override
        public void onReceive(Object message) throws GeneralSecurityException, XMLStreamException, IOException, NoSuchFieldException {

           //получили пару
            if(message.getClass() ==  Vector.class){
                Vector <ArrayList <BitArray>  > data = (Vector)message;
                ArrayList <BitArray>  str1 = data.get(0);
                ArrayList <BitArray>  str2 = data.get(1);
                System.out.println("строка 1:"+str1);
                System.out.println("строка 2:"+str2);

                ArrayList<BitArray> head = Magician.express(str1, str2); //перемножаем
                head = Magician.find(head);
                int prev = head.size();
                int current = 0;

                while(prev!=current){
                    current = head.size();
                    head = Magician.find(head);
                    prev = head.size();
                }

                Add res = new Add(head);
                sender().tell(res,self());
            }
        }
    }



    public static class MasterActor extends UntypedAbstractActor{


      final ActorRef worker = getContext().actorOf((new RoundRobinPool(10))
                .props(DownloaderActor.props()), "downloader");

        public static Props props() {
            return Props.create(MasterActor.class);
        }

        BlockingQueue<ArrayList <BitArray>  > disjunctions;
        BlockingQueue<ArrayList <BitArray> > next_arr = new LinkedBlockingQueue<ArrayList <BitArray>>();
        Counter counter;
        ArrayList <BitArray>   last;
        int size;

        public void onReceive(Object message) throws Throwable {


            if(message.getClass() == String.class){
                context().system().terminate();
            }
            else if(message.getClass() == CounterAkka.Start.class){
                System.out.println("here");
                counter = new Counter(0);
                next_arr = new LinkedBlockingQueue<ArrayList<BitArray>>(); //очищаем
                Class<?> cls = message.getClass();
                Field field = cls.getField("disjunctions");
                Field variant = cls.getField("variant");
                disjunctions = ((BlockingQueue<ArrayList <BitArray>  >)field.get(message));
                System.out.println(disjunctions.size());


                if(disjunctions.size() == 1){
                    //нечетный случай
                    if(last!=null){

                        ArrayList <BitArray>  first = disjunctions.take();

                        Vector <ArrayList <BitArray>  > data = new Vector<ArrayList <BitArray>  >();
                        data.add(first);
                        data.add(last);
                        worker.tell(data, self());
                        last = null;
                    }

                }

                //четное
                if((Integer)variant.get(message) == 0){
                    size = disjunctions.size()/2;

                    while(disjunctions.size() > 0) {
                      //  System.out.println(disjunctions.size());
                        ArrayList <BitArray>   first = disjunctions.take();
                        ArrayList <BitArray>   second = disjunctions.take();
                        Vector <ArrayList <BitArray>  > data = new Vector<ArrayList <BitArray>  >();
                        data.add(first);
                        data.add(second);
                        worker.tell(data, self());
                    }
                }
                //нечетное
                else if ((Integer)variant.get(message) == 1){
                    last = (ArrayList <BitArray> )disjunctions.toArray()[disjunctions.size()-1];
                    size = (disjunctions.size()-1)/2;

                    while(disjunctions.size() > 1) {
                       // System.out.println(disjunctions.size());
                        ArrayList <BitArray>   first = disjunctions.take();
                        ArrayList <BitArray>   second = disjunctions.take();
                        Vector <ArrayList <BitArray>  > data = new Vector<ArrayList <BitArray>  >();
                        data.add(first);
                        data.add(second);
                        worker.tell(data, self());
                    }

                }


             }
             else if (message.getClass() == CounterAkka.Add.class){
                System.out.println("обработано:"+counter.count);
                Class<?> cls = message.getClass();
                Field field = cls.getField("result");
                next_arr.add((ArrayList <BitArray>)field.get(message));
                counter.append(); //готовы
                //Обработано все
                if(counter.getCount() == size){
                    System.out.println(size);
                    if(size == 1){
                        ArrayList<BitArray> result  = (ArrayList <BitArray>)field.get(message);
                        result = Magician.find(result);       //упрощаем
                        next_arr = new LinkedBlockingQueue<ArrayList<BitArray>>(); //очищаем
                        next_arr.add(result);

                        disjunctionsList = next_arr;
                        if(last == null)
                            self().tell("finish",self()); //передаем на новую фазу
                        else{
                            next_arr.add(last);
                            last = null;
                            self().tell(new Start(next_arr, 0), self()); //передаем на новую фазу

                        }
                    }
                    else {
                        System.out.println("обработал " + size + " пары");
                        if (size % 2 == 0)
                            self().tell(new Start(next_arr, 0), self()); //передаем на новую фазу
                        else
                            self().tell(new Start(next_arr, 1), self()); //передаем на новую фазу

                    }
                }

            }

        }

    }

    public static class Start{

        public BlockingQueue<ArrayList <BitArray> > disjunctions;
        public int variant;
        public Start(BlockingQueue<ArrayList <BitArray> >  disjunctions,
                     int variant
                     )
        {
            this.disjunctions = disjunctions;
            this.variant = variant;
        }
    }
    public static class Add{

        public   ArrayList<BitArray>  result;
        public Add(  ArrayList<BitArray>  result)
        {
            this.result = result;
        }
    }

    public static class Counter{
        private int count;
        Counter(int count){
            this.count = count;
        }
        public void append(){
            this.count+=1;
        }

        public int getCount(){
            return this.count;
        }
    }

}


