import java.util.Arrays;

/**
 * Created by Jonathan on 7/6/2017.
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

    private String findGonLine(boolean[] tubeStates)
    {
        String gonLine = "";
        gonLine += binaryDecode(Arrays.copyOfRange(tubeStates,0,4)); //tube level
        gonLine += tubeStates[4] ? "A" : "B";                                  //tube sublevel
        gonLine += binaryDecode(Arrays.copyOfRange(tubeStates,5,8)); //tube number
        gonLine += ";";                                                        //.gon seperator
        gonLine += binaryDecode(Arrays.copyOfRange(tubeStates,8,16));//tube radius in clock pulses
        return gonLine;
    }

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
