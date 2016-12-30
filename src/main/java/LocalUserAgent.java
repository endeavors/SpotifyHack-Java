import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by gurkiratsingh on 7/8/16.
 */
public class LocalUserAgent {
    private static int NUM_USR_AGENT_LINES = 0;
    private static ArrayList<Integer> rand_nums = new ArrayList<Integer>();
    private static String file;

    public LocalUserAgent(){
        file = new String(Redeemer.USER_AGENT_FILE);
    }

    public static String getUserAgent(){

        if (NUM_USR_AGENT_LINES == 0){
            try{
                LineNumberReader linereader = new LineNumberReader(new FileReader(file));
                linereader.skip(Long.MAX_VALUE);
                NUM_USR_AGENT_LINES = linereader.getLineNumber() + 1;
            }catch (Exception e){
                e.printStackTrace();
            }
        }else{
            int rand_line_num = getRandInt();
            while (rand_nums.contains(rand_line_num)){
                rand_line_num = getRandInt();
            }
            rand_nums.add(rand_line_num);

            try{
                BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                String curr_line;
                int curr_line_num = 0;

                while ((curr_line = bufferedReader.readLine()) != null){
                    curr_line_num++;
                    if (curr_line_num == rand_line_num){
                        return curr_line;
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return "";
    }

    private static int getRandInt(){
        Random random = new Random();
        int max = NUM_USR_AGENT_LINES;
        int min = 1;
        int rand_num = random.nextInt((max - min) + 1) + min;
        return rand_num;
    }
}
