import java.io.IOException;
import java.util.Arrays;

/**
 * A class that tests the pin reading by logging the states of all the pins
 */
public class PinReadTest {
    final static String TAG = PinReadTest.class.getSimpleName();

    public static void main(String[] args){
        Thread pinReadThread = new Thread(new Runnable() {
            @Override
            public void run() {
                boolean[] result = new boolean[16];

                //Calls the C function to read each pin
                String unpadded = Integer.toBinaryString(1);

                //Pad the leading edge of this int with zeroes until it's 16 characters long
                while(unpadded.length() <= 15)
                    unpadded = "0" + unpadded;
                Log.v(TAG, "Unpadded (this is backwards): " + unpadded);

                //Assign each array value true or false based on the value of each character
                //1 = true and 0 = false
                for (int i = unpadded.length()-1; i >= 0; i--)
                    result[i] = (Character.getNumericValue(unpadded.charAt(i))) == 1;

                //The array is backwards, so we flip it
                for (int j = 0; j < result.length/2; j++) {
                    boolean temp = result[j];
                    result[j] = result[result.length - 1 - j];
                    result[result.length - 1 - j] = temp;
                }

                //Done
                Log.v(TAG, Arrays.toString(result));

                /*                while(!Thread.interrupted())
                {
                    main.FpgaPin.ENABLE.logState();
                    main.FpgaPin.VALID.logState();
                    main.FpgaPin.EMPTY.logState();
                    main.FpgaPin.CLK.logState();
                    main.FpgaPin.TUBELEVEL_0.logState();
                    main.FpgaPin.TUBELEVEL_1.logState();
                    main.FpgaPin.TUBELEVEL_2.logState();
                    main.FpgaPin.TUBELEVEL_3.logState();
                    main.FpgaPin.TUBESUBLEVEL.logState();
                    main.FpgaPin.TUBENUM_0.logState();
                    main.FpgaPin.TUBENUM_1.logState();
                    main.FpgaPin.TUBENUM_2.logState();
                    main.FpgaPin.RAD_0.logState();
                    main.FpgaPin.RAD_1.logState();
                    main.FpgaPin.RAD_2.logState();
                    main.FpgaPin.RAD_3.logState();
                    main.FpgaPin.RAD_4.logState();
                    main.FpgaPin.RAD_5.logState();
                    main.FpgaPin.RAD_6.logState();
                    main.FpgaPin.RAD_7.logState();
                }*/

            }
        });

        pinReadThread.start();
        try {
            System.in.read();
        } catch (IOException e) {
            Log.e(TAG,"Interrupted while trying to read pin values, ending thread",e);
        }
        pinReadThread.interrupt();

    }

}
