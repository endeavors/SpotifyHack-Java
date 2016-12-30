import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by gurkiratsingh on 7/22/16.
 */
public class Redeemer {
    public static final long START_QUEUE = 6880000000L;
    /*S:INC6880717758 E:INC6880763671 */
    public static final long END_QUEUE = 6890000000L;
    public static final String USERNAME = "";
    public static final String PASSWORD = "";
    public static final int NUM_THREADS = 40;
    public static final String USER_AGENT_FILE = "./user_agent.txt";
    public static final String PHANTOM_EXE = "./phantomjs1.9.8";

    private BlockingQueue<String> queue = new ArrayBlockingQueue<String>((int) (END_QUEUE - START_QUEUE));
    private Worker[] workers = new Worker[NUM_THREADS];

    public static void main(String[] args){
        Redeemer redeemer = new Redeemer();
        redeemer.addToQueue();
        redeemer.spawnWorkers();
        redeemer.startThreads();

    }

    private void addToQueue(){
        String prefix = "INC";
        try{
            for (long i = START_QUEUE; i < END_QUEUE; i++){
                String guess = prefix + String.valueOf(i);
                queue.put(guess);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void spawnWorkers(){
        for (int i = 0; i < workers.length; i++){
            Worker worker = new Worker(queue);
            worker.setDaemon(true);
            worker.setPriority(Thread.MAX_PRIORITY);
            workers[i] = worker;
            worker.start();
        }
    }

    private void startThreads(){

        for (int i = 0; i < workers.length; i++){
            try{
                workers[i].join();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}
