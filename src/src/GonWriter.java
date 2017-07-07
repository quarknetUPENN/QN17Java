import java.util.Arrays;

/**
 * Given a boolean[][] - a list of lists containing raw tube data from gpio - writes it all to a gon file
 * in the current working directory
 *
 * Designed to be run on a separate thread
 */
public class GonWriter implements Runnable {
    private boolean[][] eventTubeStates;

    public GonWriter(boolean[][] eventTubeStates){
        this.eventTubeStates = eventTubeStates;
    }

    @Override
    public void run() {
        //open file
        for(boolean[] tubeState : eventTubeStates)
        {
            //write all the tubes
            findGonLine(tubeState);
        }
        //close file
    }

    /**
     * Decodes the states of the tube data received to get a string that is one line in a .gon file
     * @param tubeStates an array of all the pin data received for one particular tube
     * @return a string that is a line in a .gon file describing that one tube
     */
    private String findGonLine(boolean[] tubeStates)
    {
        String gonLine = "";
        gonLine += binaryDecode(Arrays.copyOfRange(tubeStates,0,4)); //tube level
        gonLine += tubeStates[4] ? "A" : "B";                                  //tube sublevel
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
            if(b)
                sum += i;
            i *= 2;
        }
        return sum;
    }
}
