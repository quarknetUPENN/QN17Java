import com.pi4j.io.gpio.*;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.pi4j.io.gpio.PinState.LOW;
import static com.pi4j.io.gpio.PinState.HIGH;


/**
 * Created by Jonathan on 7/5/2017.
 */


public class main {
    final static String TAG = main.class.getSimpleName();
    final static GpioController gpioController = GpioFactory.getInstance();

    public static void main(String[] args) {
        //initialize rd pins with the FPGA
        FpgaPin.ENABLE.setState(LOW);
        FpgaPin.CLOCK.setState(LOW); //should this pulse instead?


        //create the directory to store all the data from this run in
        final File dataDir = createTimeStampedDataDir();
        if(dataDir == null)
            return;

        //create a new thread to handle receiving all the data and sending it to GonWriters to record
        Thread pinRecorder = new Thread(new Runnable() {
            @Override
            public void run() {
                //counts how many events have been received
                int eventN = 1;
                //if we are not asked to stop, keep reading data
                while (!Thread.interrupted())
                {
                    //this is filled with data from one event, ie, 32 tubes
                    //each sublist is one tube
                    boolean[][] eventTubeStates = new boolean[][]{};

                    //a counter variable for each event.  it counts the number of tubes that have passed
                    int tubeCounter = 0;

                    //if there is data, go get it
                    if (FpgaPin.EMPTY.getState() == LOW)
                    {
                        //temporary timing
                        long validNanotime;
                        long recordNanotime;
                        long finalNanotime;
                        //keep reading tubes until there are no more tubes, or you run out for some reason
                        while (!isStopFlag() || FpgaPin.EMPTY.getState() == LOW) {
                            //cycle the clock for a moment.  note that this assumes it takes 0 time to record data
                            //what happens FPGA-side if something is interrupted for some reason over here?
                            FpgaPin.CLOCK.setState(HIGH);
                            try {
                                Thread.sleep(0, 100);
                            } catch (InterruptedException e) {
                                Log.e(TAG, "Reading thread interrupted while waiting; terminating read thread", e);
                                break;
                            }
                            FpgaPin.CLOCK.setState(LOW);
                            try {
                                Thread.sleep(0, 100);
                            } catch (InterruptedException e) {
                                Log.e(TAG, "Reading thread interrupted while waiting; terminating read thread", e);
                                break;
                            }

                            validNanotime = System.nanoTime();
                            //wait for the valid pin to go high from the FPGA, ensuring there is new data on the bus
                            while (FpgaPin.VALID.getState() == LOW) ;

                            recordNanotime = System.nanoTime();
                            //record the data for the tube into eventTubeStates
                            eventTubeStates[tubeCounter] = recordCurrentInputs();
                            finalNanotime = System.nanoTime();
                            tubeCounter++;

                            Log.i(TAG,"Valid: "+Long.toString(finalNanotime-validNanotime)+", Record: "+
                                    Long.toString(finalNanotime-recordNanotime));
                        }

                        //send the event data over to a GonWriter to have it recorded to a file
                        new Thread(new GonWriter(new File(dataDir, "event" + Integer.toString(eventN) + ".gon"), eventTubeStates)).start();
                        eventN++;
                    } else {
                        Log.i(TAG, "No data to get");
                    }
                }
                Log.i(TAG,"Interrupt received, ending pin listener thread");
            }
        });


        //start the thread to read data, and don't end until user input
        pinRecorder.start();
        try {
            System.in.read();
        } catch (IOException e) {
            Log.e(TAG, "Interrupted while running, will shut down");
        }
        pinRecorder.interrupt();
    }

    /**
     * Detects if the FPGA is sending a stop flag with all bits high, indicating the end of the event (all
     * data for each tube has been sent)
     *
     * @return whether or not the FPGA is currently sending a stop flag
     */
    static boolean isStopFlag() {
        boolean isStop = true;
        //iterate through every current pin.  if they are all high, then return true
        for(boolean pin : recordCurrentInputs()){
            isStop = isStop && pin;
        }
        return isStop;
    }

    /**
     * Checks the levels of all the data pins connected to the FPGA and returns them as booleans in a list
     * @return a boolean list with the current input on each input pin
     */
    static boolean[] recordCurrentInputs(){
        return new boolean[]{
                FpgaPin.TUBELEVEL_0.getState()  ==     HIGH,
                FpgaPin.TUBELEVEL_1.getState()  ==     HIGH,
                FpgaPin.TUBELEVEL_2.getState()  ==     HIGH,
                FpgaPin.TUBELEVEL_3.getState()  ==     HIGH,
                FpgaPin.TUBESUBLEVEL.getState() ==     HIGH,
                FpgaPin.TUBENUM_0.getState()    ==     HIGH,
                FpgaPin.TUBENUM_1.getState()    ==     HIGH,
                FpgaPin.TUBENUM_2.getState()    ==     HIGH,
                FpgaPin.RAD_0.getState()        ==     HIGH,
                FpgaPin.RAD_1.getState()        ==     HIGH,
                FpgaPin.RAD_2.getState()        ==     HIGH,
                FpgaPin.RAD_3.getState()        ==     HIGH,
                FpgaPin.RAD_4.getState()        ==     HIGH,
                FpgaPin.RAD_5.getState()        ==     HIGH,
                FpgaPin.RAD_6.getState()        ==     HIGH,
                FpgaPin.RAD_7.getState()        ==     HIGH};
    }


    /**
     * Makes a new folder in the current working dir, with a time stamp added to "data"
     * Will overwrite the exist folder by recursive deletion of everything inside it
     * @return a File object representing the newly created or cleaned directory
     */
    static File createTimeStampedDataDir(){
        SimpleDateFormat dateFormatter = new SimpleDateFormat("MM-dd-yy_hh-mm");
        File dataDir = new File(dateFormatter.format(new Date()) + "data");
        try {
            if (dataDir.exists())
                FileUtils.cleanDirectory(dataDir);
            else {
                if (!dataDir.mkdirs())
                    throw new IOException("Failed to make data directory");
            }
        } catch (IOException e) {
            Log.e(TAG, "Failed to create data directory, returning null.  Invalid dir name?", e);
            return null;
        }
        return dataDir;
    }

    /**
     * Enumerates every pin that connects to the FPGA, and contains some basic methods
     * Includes the RPi pin, the pin mode, and some basic get/set functions for each
     * pin as appropriate
     */
    enum FpgaPin {
        ENABLE(RaspiBcmPin.GPIO_11, PinMode.DIGITAL_OUTPUT),
        VALID(RaspiBcmPin.GPIO_08, PinMode.DIGITAL_INPUT),
        EMPTY(RaspiBcmPin.GPIO_09, PinMode.DIGITAL_INPUT),
        CLOCK(RaspiBcmPin.GPIO_06, PinMode.DIGITAL_OUTPUT),

        TUBELEVEL_0(RaspiBcmPin.GPIO_18, PinMode.DIGITAL_INPUT),
        TUBELEVEL_1(RaspiBcmPin.GPIO_23, PinMode.DIGITAL_INPUT),
        TUBELEVEL_2(RaspiBcmPin.GPIO_24, PinMode.DIGITAL_INPUT),
        TUBELEVEL_3(RaspiBcmPin.GPIO_25, PinMode.DIGITAL_INPUT),
        TUBESUBLEVEL(RaspiBcmPin.GPIO_12, PinMode.DIGITAL_INPUT),
        TUBENUM_0(RaspiBcmPin.GPIO_16, PinMode.DIGITAL_INPUT),
        TUBENUM_1(RaspiBcmPin.GPIO_20, PinMode.DIGITAL_INPUT),
        TUBENUM_2(RaspiBcmPin.GPIO_21, PinMode.DIGITAL_INPUT),

        RAD_0(RaspiBcmPin.GPIO_26, PinMode.DIGITAL_INPUT),
        RAD_1(RaspiBcmPin.GPIO_13, PinMode.DIGITAL_INPUT),
        RAD_2(RaspiBcmPin.GPIO_05, PinMode.DIGITAL_INPUT),
        RAD_3(RaspiBcmPin.GPIO_10, PinMode.DIGITAL_INPUT),
        RAD_4(RaspiBcmPin.GPIO_04, PinMode.DIGITAL_INPUT),
        RAD_5(RaspiBcmPin.GPIO_17, PinMode.DIGITAL_INPUT),
        RAD_6(RaspiBcmPin.GPIO_27, PinMode.DIGITAL_INPUT),
        RAD_7(RaspiBcmPin.GPIO_22, PinMode.DIGITAL_INPUT);

        private Pin pinCode;
        private PinMode mode;

        private String badModeWarn;

        private GpioPinDigital pin;


        FpgaPin(Pin pinCode, PinMode mode) {
            this.pinCode = pinCode;
            this.mode = mode;

            badModeWarn = "Pin " + pinCode.getName() + " not configured as digital I/O" +
                    " instead configured as " + mode.getName() + " and will not function";

            //provision the pin as either input or output
            switch (mode) {
                case DIGITAL_INPUT:
                    pin = gpioController.provisionDigitalInputPin(pinCode);
                    break;
                case DIGITAL_OUTPUT:
                    pin = gpioController.provisionDigitalOutputPin(pinCode);
                    break;
                default:
                    Log.w(TAG, badModeWarn);
            }
        }


        /**
         * Sets the current state of the pin if it is a DIGITAL_OUTPUT pin
         *
         * @param state the state to set the pin to; low or high
         */
        public void setState(PinState state) {
            switch (mode) {
                case DIGITAL_INPUT:
                    Log.w(TAG, "Attempted state set on pin " + pinCode.getName() + " not configured as digital output" +
                            " instead configured as " + mode.getName() + ".  Will ignore instruction");
                    break;
                case DIGITAL_OUTPUT:
                    ((GpioPinDigitalOutput) pin).setState(state);
                    break;
                default:
                    Log.w(TAG, badModeWarn);
            }
        }


        /**
         * Gets the current pin state, unless the pin is not digital I/O, then returns null
         *
         * @return the current pin state
         */
        public PinState getState() {
            switch (mode) {
                case DIGITAL_INPUT:
                case DIGITAL_OUTPUT:
                    return pin.getState();
                default:
                    Log.w(TAG, badModeWarn);
                    return null;
            }
        }

        /**
         * Logs the current state of the pin in the terminal
         */
        public void logState(){
            if(getState() == null)
                Log.v(TAG,"Null value for pin state, invalid pin configuration");
            else
                Log.v(TAG,pinCode.getName()+" is currently "+getState().getName()+" at nanotime "+Long.toString(System.nanoTime()));
        }


        /**
         * Gets the current pin object  If not digital I/O, returns null
         *
         * @return the digital pin object
         */
        public GpioPinDigital getPin() {
            switch (mode) {
                case DIGITAL_INPUT:
                case DIGITAL_OUTPUT:
                    return pin;
                default:
                    Log.w(TAG, badModeWarn);
                    return null;
            }
        }

        /**
         * Gets the current pin mode.  If not digital I/O, returns null
         *
         * @return the mode of the current pin; either DIGITAL_INPUT or DIGITAL_OUTPUT
         */
        public PinMode getMode() {
            switch (mode) {
                case DIGITAL_INPUT:
                case DIGITAL_OUTPUT:
                    return mode;
                default:
                    Log.w(TAG, badModeWarn);
                    return null;
            }
        }
    }
}
