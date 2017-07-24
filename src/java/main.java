import org.apache.commons.io.FileUtils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Created by Jonathan on 7/5/2017.
 */


public class main {
    final static String TAG = main.class.getSimpleName();

    public static void main(String[] args) {
        initGpio();

        //create the directory to store all the data from this run in
        File dataDir = createTimeStampedDataDir();
        if(dataDir == null){
            Log.e(TAG,"FATAL: Could not make data directory");
            return;
        }

        //create a new thread to handle receiving all the data and sending it to GonWriters to record
        Thread pinRecorder = new Thread(new DataRecorder(dataDir));

        try {
            System.in.read();
        } catch (IOException e) {
            Log.e(TAG, "Interrupted while running, will shut down");
        }
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
     * Initializes the GPIO pins by initializing wiringPi
     * Note that setup.sh still needs to be run as sudo to export the pins
     */
    static void initGpio(){
/*        try{
            Runtime.getRuntime().exec("sudo ./setup.sh");
        } catch (IOException e) {
            Log.e(TAG,"Could not export pins, the thing might not work",e);
        }*/

        //make wiringpi use BCM pin numbering
        if (com.pi4j.wiringpi.Gpio.wiringPiSetupGpio() == -1) {
            Log.e(TAG, "FATAL: failed to set up GPIO");
            throw new RuntimeException("WiringPi failed to properly set up GPIO for some reason.  Are you running as sudo?");
        } else
            Log.i(TAG,"Successfully set up GPIO");

        //set up the outputs
        FpgaPin.ENABLE.setHigh();
        FpgaPin.CLK.setLow();
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
        ENABLE(2, GpioDirection.OUT),
        VALID(8, GpioDirection.IN),
        EMPTY(9, GpioDirection.IN),
        CLK(11, GpioDirection.OUT),

        TUBELEVEL_0(18, GpioDirection.IN),
        TUBELEVEL_1(23, GpioDirection.IN),
        TUBELEVEL_2(24, GpioDirection.IN),
        TUBELEVEL_3(25, GpioDirection.IN),
        TUBESUBLEVEL(7, GpioDirection.IN),
        TUBENUM_0(16, GpioDirection.IN),
        TUBENUM_1(20, GpioDirection.IN),
        TUBENUM_2(21, GpioDirection.IN),

        RAD_0(26, GpioDirection.IN),
        RAD_1(13, GpioDirection.IN),
        RAD_2(5, GpioDirection.IN),
        RAD_3(10, GpioDirection.IN),
        RAD_4(4, GpioDirection.IN),
        RAD_5(17, GpioDirection.IN),
        RAD_6(27, GpioDirection.IN),
        RAD_7(22, GpioDirection.IN);

        private int pinCode;
        private int direction;
        private Writer writer;
        private RandomAccessFile file;


        FpgaPin(int pinCode, int direction) {
            this.pinCode = pinCode;
            this.direction = direction;


            //TODO: this is dumb, the write capacity should also depend on the RAF
            if (direction == GpioDirection.OUT) {
                try {
                    writer = new OutputStreamWriter(new FileOutputStream("/sys/class/gpio/gpio"+pinCode+"/value"));
                } catch (FileNotFoundException e){
                    Log.e(TAG, "Pin "+pinCode+" not exported, try running setup.sh",e);
                }
            }
            if (direction == GpioDirection.IN) {
                try{
                    file = new RandomAccessFile("/sys/class/gpio/gpio"+pinCode+"/value","r");
                } catch (FileNotFoundException e) {
                    Log.e(TAG, "Pin "+pinCode+" not exported, try running setup.sh",e);
                }
            }
        }


        /**
         * Sets the current state of the pin to a given value
         *
         * @param state the state to set the pin to; false for low, true for high
         */
        public void setState(boolean state) {
            if (direction == GpioDirection.OUT) {
                try {
                    if(state)
                        writer.write("1\n");
                    else
                        writer.write("0\n");
                    writer.flush();
                } catch (IOException e) {
                    Log.e(TAG, "IO Exception while writing to file :( ", e);
                }
            }
        }


        /**
         * Gets the current pin state, unless the pin is not digital I/O, then returns null
         *
         * @return the current pin state; true if high, false if low
         */
        public boolean getState() {
            try {
                file.seek(0);
                return file.readLine().equals("1");
            } catch (IOException e) {
                Log.e(TAG,"Could not read from pin "+pinCode+" did you export the pin with setup.sh?",e);
            }
            return false;
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

    class GpioDirection {
        public static final int IN = 0;
        public static final int OUT = 1;
    }
}
