import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * Given a boolean[][] - a list of lists containing raw tube data from gpio - writes it all to a gon file
 * in the current working directory
 *
 * Designed to be run on a separate thread
 */
public class GonWriter implements Runnable {
    final static String TAG = GonWriter.class.getSimpleName();
    private boolean[][] eventTubeStates;
    private File targetDir;
    private String fileName;

    GonWriter(File targetDir, String fileName, boolean[][] eventTubeStates){
        this.targetDir = targetDir;
        this.fileName = fileName;
        this.eventTubeStates = eventTubeStates;
    }

    @Override
    public void run() {
        boolean isBroken = false;
        //go through every tube in the received event array, and write them all to the specified file

        for(boolean[] tubeState : eventTubeStates) {
            String gonLine = findGonLine(tubeState);
            //If the data contains "0A0", something went wrong.
            //Flag this data so that it can get filtered out
            if (gonLine.substring(0, 3).equals("0A0")) {
                isBroken = true;
            }

        }

        for(boolean[] tubeState : eventTubeStates)
        {
            //attempt to append on the tube currently being read into the gon file
            try {
                String gonLine = findGonLine(tubeState);
                //If the data contains "0A0", put that file in a folder called "broken"
                //so that it's not analyzed later by the Python
                //If it doesn't, then it's real data; write it to a regular .gon file
                if (isBroken)
                    FileUtils.writeStringToFile(new File(targetDir, "broken/" + fileName), gonLine,(Charset) null,true);
                else
                    FileUtils.writeStringToFile(new File(targetDir, fileName),gonLine,(Charset) null,true);
            } catch (IOException e) {
                Log.e(TAG,"Failed to write line to gon file "+ targetDir.getPath()+" will skip this line",e);
            }
        }
    }

    /**
     * Decodes the states of the tube data received to get a string that is one line in a .gon file
     * @param tubeStates an array of all the pin data received for one particular tube
     * @return a string that is a line in a .gon file describing that one tube
     */
    private String findGonLine(boolean[] tubeStates) {
        String gonLine = "";
        gonLine += binaryDecode(Arrays.copyOfRange(tubeStates,0,4)); //tube level
        gonLine += tubeStates[4] ? "B" : "A";                                  //tube sublevel
        gonLine += binaryDecode(Arrays.copyOfRange(tubeStates,5,8)); //tube number
        gonLine += ";";                                                        //.gon seperator
        gonLine += binaryDecode(Arrays.copyOfRange(tubeStates,8,16));//tube radius in clock pulses
        gonLine += "\n";
        return gonLine;
    }


    /**
     * Interprets the input as a binary number and returns it as an int
     * @param bin an array sorted from LSB to MSB representing a binary number
     * @return the int represented by the whole input array
     */
    private int binaryDecode(boolean[] bin){
        int i = 1;
        int sum = 0;
        for(boolean b : bin){
            if(b) sum += i;
            i *= 2;
        }
        return sum;
    }
}
