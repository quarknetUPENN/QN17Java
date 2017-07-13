import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListener;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

import java.io.File;
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
                GpioPinDigitalOutput CLOCK = GpioFactory.getInstance().provisionDigitalOutputPin(RaspiBcmPin.GPIO_19);
                CLOCK.setState(HIGH);
                try {
                    while (!Thread.interrupted()) {
                        CLOCK.setState(HIGH);
                        Thread.sleep(100, 0);
                        System.out.println("1 "+ CLOCK.getState());
                        CLOCK.setState(LOW);
                        Thread.sleep(100, 0);
                        System.out.println("2 "+ CLOCK.getState());
                    }
                } catch (InterruptedException e){
                    Log.e(TAG,"interrupted while pulsing clock, stopping",e);
                }

                Log.i(TAG,"Stopping clock pulsing thread");
            }
        });

        thread.start();

        //this don't actually work, not sure why
/*        main.FpgaPin.CLOCK.getPin().addListener(new GpioPinListenerDigital() {
            @Override
            public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
                if(event.getEdge() == PinEdge.RISING)
                    Log.i(TAG,"Rising edge");
                else
                    Log.i(TAG,"Not rising edge");
            }
        });*/

    //    Log.i(TAG,main.FpgaPin.CLOCK.getMode().getName());
        try {
            System.in.read();
        } catch (IOException e) {
            Log.e(TAG, "Reading thread interrupted while waiting; terminating read thread", e);
        }
        thread.interrupt();
  //      ((GpioPinDigitalOutput)main.FpgaPin.CLOCK.getPin()).toggle();
        Log.i(TAG,main.FpgaPin.CLOCK.getState().getName());
        try {
            Thread.sleep(1000, 0);
        } catch (InterruptedException e) {
            Log.e(TAG, "Reading thread interrupted while waiting; terminating read thread", e);
            return;
        }
    }
}
