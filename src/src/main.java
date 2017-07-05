import com.pi4j.io.gpio.*;

/**
 * Created by Matthew on 7/5/2017.
 */


public class main {
    public static void main(String[] args) {
        System.out.println("thing");

        final GpioController gpioController = GpioFactory.getInstance();

        GpioPinDigitalInput myButton = gpioController.provisionDigitalInputPin(RaspiPin.GPIO_02);

        System.out.println(myButton.isHigh());
    }

    enum FPGAPINS{
        ENABLE,
        VALID,
        EMPTY,
        CLOCK,
        LEVEL_0,
        LEVEL_1,
        LEVEL_2,
        LEVEL_3,
        SUBLEVEL,
        TUBENUM_0,
        TUBERAD_1,
        TUBERAD_2,
        TUBERAD_3,
        TUBERAD_4,
        TUBERAD_5,
        TUBERAD_6,
        TUBERAD_7
    }
}
