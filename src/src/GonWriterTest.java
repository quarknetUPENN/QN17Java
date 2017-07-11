import java.io.File;


/**
 * Tests the GonWriter by writing two lines of fake data to a file
 */
public class GonWriterTest {
    final static String TAG = GonWriterTest.class.getSimpleName();
    public static void main(String[] args){
        //make a new folder from the current working dir, with a time stamp
        File dataDir = main.createTimeStampedDataDir();

        //should create a new folder and put "event1.gon" in it with the following:
        //4A3;20
        //3B4;96
        //
        new Thread(new GonWriter(new File(dataDir,"event1.gon"),new boolean[][]{new boolean[]{false,false,true,false, false, true,true,false, false,false,true,false,true,false,false,false},
            new boolean[]{true,true,false,false, true, false,false,true, false,false,false,false,false,true,true,false}})).start();
    }
}
