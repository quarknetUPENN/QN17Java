import java.io.IOException;

/**
 * A class that tests the pin reading by logging the states of all the pins
 */
public class PinReadTest {
    final static String TAG = PinReadTest.class.getSimpleName();

    public static void main(String[] args){
        Thread pinReadThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(!Thread.interrupted())
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
                }
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
