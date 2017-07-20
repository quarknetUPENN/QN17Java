import com.pi4j.wiringpi.Gpio;
import com.pi4j.wiringpi.GpioUtil;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Created by Jonathan on 7/5/2017.
 */


public class main {
    final static String TAG = main.class.getSimpleName();

    public static void main(String[] args) {
        //make pi4j use the new version of wiringPi on the RPi
        //instead of the old statically linked version
        System.setProperty("pi4j.linking", "dynamic");


        //initialize rd pins with the FPGA
        FpgaPin.ENABLE.setHigh();
        FpgaPin.CLK.setLow();


        //create the directory to store all the data from this run in
        final File dataDir = createTimeStampedDataDir();
        if(dataDir == null){
            Log.e(TAG,"FATAL: Could not make data directory");
            return;
        }

        //create a new thread to handle receiving all the data and sending it to GonWriters to record
        Thread pinRecorder = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.v(TAG, "Pin recorder thread started");
                //counts how many events have been received
                int eventN = 1;

                //if we are not asked to stop, keep reading data
                while (!Thread.interrupted()) {
                    //this is filled with data from one event, ie, 32 tubes
                    //each sublist is one tube
                    boolean[][] eventTubeStates = new boolean[32][16];

                    //a counter variable for each event.  it counts the number of tubes that have passed
                    int tubeCounter = 0;
                    boolean[] currentInput;

                    //if there is data, go get it
                    if (FpgaPin.EMPTY.isLow()) {
                        //Log.v(TAG, "Going to read data");
                        //keep reading tubes until there are no more tubes, or you run out for some reason
                        try {
                            while (FpgaPin.EMPTY.isLow()) {
                                //pulse the clock
                                FpgaPin.CLK.setHigh();
                                Thread.sleep(10, 1);
                                FpgaPin.CLK.setLow();
                                Thread.sleep(10, 1);

                                //record the data for the tube into eventTubeStates
                                try {
                                    currentInput = recordCurrentInputs();
                                    //stop if we are supposed to, otherwise, add the tube to the array
                                    if(isStopFlag(currentInput)) {
                                        break;
                                    } else {
                                        eventTubeStates[tubeCounter] = currentInput;
                                    }
                                } catch (ArrayIndexOutOfBoundsException e) {
                                    Log.w(TAG, "Stop flag not encountered on 33rd data point; will move on to next file");
                                    break;
                                }

                                tubeCounter++;
                            }
                        } catch (InterruptedException e) {
                            Log.e(TAG, "Reading thread interrupted while waiting; terminating read thread", e);
                            break;
                        }
                        //send the event data over to a GonWriter to have it recorded to a file
                        new Thread(new GonWriter(new File(dataDir, "event" + Integer.toString(eventN) + ".gon"), eventTubeStates)).start();
                        eventN++;
                    } else {
                        Log.i(TAG, "No data to get");
                        try {
                            Thread.sleep(10);
                        } catch(InterruptedException e){
                            Log.i(TAG,"Interrupted while waiting for data");
                        }
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
    static boolean isStopFlag(boolean[] inputs) {

        boolean isStop = true;
        //iterate through every current pin.  if they are all high, then return true
        for(boolean pin : inputs){
            isStop = isStop && pin;
        }
        if(isStop)
            Log.v(TAG,"Stop flag reached");
        return isStop;
    }

    /**
     * Checks the levels of all the data pins connected to the FPGA and returns them as booleans in a list
     * @return a boolean list with the current input on each input pin
     */
    static boolean[] recordCurrentInputs(){
        return new boolean[]{
                FpgaPin.TUBELEVEL_0.getState(),
                FpgaPin.TUBELEVEL_1.getState(),
                FpgaPin.TUBELEVEL_2.getState(),
                FpgaPin.TUBELEVEL_3.getState(),
                FpgaPin.TUBESUBLEVEL.getState(),
                FpgaPin.TUBENUM_0.getState(),
                FpgaPin.TUBENUM_1.getState(),
                FpgaPin.TUBENUM_2.getState(),
                FpgaPin.RAD_0.getState(),
                FpgaPin.RAD_1.getState(),
                FpgaPin.RAD_2.getState(),
                FpgaPin.RAD_3.getState(),
                FpgaPin.RAD_4.getState(),
                FpgaPin.RAD_5.getState(),
                FpgaPin.RAD_6.getState(),
                FpgaPin.RAD_7.getState()};
    }


    /**
     * Makes a new folder in the current working dir, with a time stamp added to "data"
     * Will overwrite the exist folder by recursive deletion of everything inside it
     * @return a File object representing the newly created or cleaned directory
     */
    static File createTimeStampedDataDir(){
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HHmm");
        File dataDir = new File("data_" + dateFormatter.format(new Date()));
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
     * Includes the RPi pin, the pin direction, and some basic get/set functions for each
     * pin as appropriate
     */
    enum FpgaPin {
        ENABLE      (11,        GpioUtil.DIRECTION_OUT),
        VALID       (8,         GpioUtil.DIRECTION_IN),
        EMPTY       (9,         GpioUtil.DIRECTION_IN),
        CLK         (2,         GpioUtil.DIRECTION_OUT),

        TUBELEVEL_0 (18,        GpioUtil.DIRECTION_IN),
        TUBELEVEL_1 (23,        GpioUtil.DIRECTION_IN),
        TUBELEVEL_2 (24,        GpioUtil.DIRECTION_IN),
        TUBELEVEL_3 (25,        GpioUtil.DIRECTION_IN),
        TUBESUBLEVEL(7,         GpioUtil.DIRECTION_IN),
        TUBENUM_0   (16,        GpioUtil.DIRECTION_IN),
        TUBENUM_1   (20,        GpioUtil.DIRECTION_IN),
        TUBENUM_2   (21,        GpioUtil.DIRECTION_IN),

        RAD_0       (26,        GpioUtil.DIRECTION_IN),
        RAD_1       (13,        GpioUtil.DIRECTION_IN),
        RAD_2       (5,         GpioUtil.DIRECTION_IN),
        RAD_3       (10,        GpioUtil.DIRECTION_IN),
        RAD_4       (4,         GpioUtil.DIRECTION_IN),
        RAD_5       (17,        GpioUtil.DIRECTION_IN),
        RAD_6       (27,        GpioUtil.DIRECTION_IN),
        RAD_7       (22,        GpioUtil.DIRECTION_IN);

        private int pinCode;
        private int direction;



        //use BCM numbering
        static {
            if (com.pi4j.wiringpi.Gpio.wiringPiSetupGpio() == -1)
                Log.e(TAG,"Failed to set up GPIO");
        }

        FpgaPin(int pinCode, int direction) {
            this.pinCode = pinCode;
            this.direction = direction;

            //provision the pin as either input or output
            GpioUtil.export(pinCode,direction);
        }


        /**
         * Sets the current state of the pin to a given value
         *
         * @param state the state to set the pin to; false for low, true for high
         */
        public void setState(boolean state) {
            Gpio.digitalWrite(pinCode,state);
        }


        /**
         * Gets the current pin state, unless the pin is not digital I/O, then returns null
         *
         * @return the current pin state; true if high, false if low
         */
        public boolean getState() {
            return Gpio.digitalRead(pinCode) == 1;
        }

        public boolean isHigh() {
            return getState();
        }

        public boolean isLow() {
            return !getState();
        }

        public void setLow(){
            setState(false);
        }

        public void setHigh(){
            setState(true);
        }

        /**
         * Logs the current state of the pin in the terminal
         */
        public void logState(){
            Log.v(TAG,"Pin "+pinCode+" is currently "+getState()+" at nanotime "+Long.toString(System.nanoTime()));
        }


        /**
         * Gets the current pin direction
         *
         * @return the direction of the current pin encoded as an int; see GpioUtils from wiringPi to decode it
         */
        public int getDirection() {
            return direction;
        }
    }
}
