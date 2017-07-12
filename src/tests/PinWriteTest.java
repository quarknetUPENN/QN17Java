import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListener;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

import java.io.File;

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



        main.FpgaPin.CLOCK.setPwm(512);

        //this don't actually work, not sure why
        main.FpgaPin.CLOCK.getPin().addListener(new GpioPinListenerDigital() {
            @Override
            public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
                if(event.getEdge() == PinEdge.RISING)
                    Log.i(TAG,"rising");
            }
        });
        Log.i(TAG,main.FpgaPin.CLOCK.getMode().getName());
        try {
            Thread.sleep(10000, 0);
        } catch (InterruptedException e) {
            Log.e(TAG, "Reading thread interrupted while waiting; terminating read thread", e);
            return;
        }
        /*((GpioPinDigitalOutput)main.FpgaPin.CLOCK.getPin()).toggle();
        Log.i(TAG,main.FpgaPin.CLOCK.getState().getName());
        try {
            Thread.sleep(1000, 0);
        } catch (InterruptedException e) {
            Log.e(TAG, "Reading thread interrupted while waiting; terminating read thread", e);
            return;
        }*/
    }
}
