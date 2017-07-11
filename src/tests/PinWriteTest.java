import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.RaspiGpioProvider;
import com.pi4j.io.gpio.RaspiPinNumberingScheme;

import java.io.File;

import static com.pi4j.io.gpio.PinState.HIGH;
import static com.pi4j.io.gpio.PinState.LOW;


/**
 * Tests the GonWriter by writing two lines of fake data to a file
 */
public class PinWriteTest {
    final static String TAG = PinWriteTest.class.getSimpleName();
    public static void main(String[] args){
        //make pi4j use the new version of wiringPi on the RPi
        //instead of the old statically linked version
        System.setProperty("pi4j.linking", "dynamic");

        while(true){
            main.FpgaPin.CLOCK.setState(HIGH);
            Log.i(TAG,"high");
            try {
                Thread.sleep(1000, 0);
            } catch (InterruptedException e) {
                Log.e(TAG, "Reading thread interrupted while waiting; terminating read thread", e);
                break;
            }
            main.FpgaPin.CLOCK.setState(LOW);
            Log.i(TAG,"low");
            try {
                Thread.sleep(1000, 0);
            } catch (InterruptedException e) {
                Log.e(TAG, "Reading thread interrupted while waiting; terminating read thread", e);
                break;
            }
        }
    }
}
