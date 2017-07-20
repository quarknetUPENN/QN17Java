import java.io.IOException;

import static com.pi4j.io.gpio.PinState.HIGH;
import static com.pi4j.io.gpio.PinState.LOW;


/**
 * Test
 */
public class PinWriteTest {
    final static String TAG = PinWriteTest.class.getSimpleName();
    public static void main(String[] args){
        //make pi4j use the new version of wiringPi on the RPi
        //instead of the old statically linked version
        System.setProperty("pi4j.linking", "dynamic");

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                //try {
                    while (!Thread.interrupted()) {
                        main.FpgaPin.CLK.setHigh();
                        //Speed of 2 microseconds per pulse
                        //Thread.sleep(0, 0);
                        //System.out.println("Clock is now "+ main.FpgaPin.CLK.getState());
                        //System.out.println("Enable is now "+ main.FpgaPin.ENABLE.getState());
                        main.FpgaPin.CLK.setLow();
                        //Thread.sleep(0, 0);
                        //System.out.println("Clock is now "+ main.FpgaPin.CLK.getState());
                        //System.out.println("Enable is now "+ main.FpgaPin.ENABLE.getState());

                    }
                //} //catch (InterruptedException e){
                    //Log.e(TAG,"interrupted while pulsing clock, stopping",e);
                //}

                Log.i(TAG,"Stopping clock pulsing thread");
            }
        });

        thread.start();

        try {
            System.in.read();
        } catch (IOException e) {
            Log.e(TAG, "Reading thread interrupted while waiting; terminating read thread", e);
        }
        thread.interrupt();
        // For some reason, the following line breaks the code
        //      ((GpioPinDigitalOutput)main.FpgaPin.CLK.getPin()).toggle();
        try {
            Thread.sleep(1000, 0);
        } catch (InterruptedException e) {
            Log.e(TAG, "Reading thread interrupted while waiting; terminating read thread", e);
            return;
        }
    }
}
