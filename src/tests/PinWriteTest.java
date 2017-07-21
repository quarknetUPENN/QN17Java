import java.io.IOException;


/**
 * Test
 */
public class PinWriteTest {
    final static String TAG = PinWriteTest.class.getSimpleName();
    public static void main(String[] args){
        main.initGpio();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(!Thread.interrupted()) {
                    main.FpgaPin.ENABLE.setState(true);
                    main.FpgaPin.ENABLE.setState(false);
                }
                Log.i(TAG,"Stopping clock pulsing thread");
            }
        });

        thread.start();

        try {
            System.in.read();
        } catch (IOException e) {
            Log.e(TAG, "Reading thread interrupted while waiting; terminating read thread", e);
            return;
        }
        thread.interrupt();
        // For some reason, the following line breaks the code
        //      ((GpioPinDigitalOutput)main.FpgaPin.CLK.getPin()).toggle();
        try {
            Thread.sleep(1000, 0);
        } catch (InterruptedException e) {
            Log.e(TAG, "Reading thread interrupted while waiting; terminating read thread", e);
        }
    }
}
