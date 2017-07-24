import java.io.File;

/**
 * A Runnable designed to run on a seperate thread that gets data from the RPi pins and records it
 */
public class DataRecorder implements Runnable {
    final static String TAG = DataRecorder.class.getSimpleName();

    /**
     * The folder to write data into
     */
    final private File dataDir;

    /**
     * The maximum number of milliseconds to wait for a valid flag before moving on
     */
    private int maxWait = 100;

    DataRecorder(File dataDir) {
        this(dataDir, 100);
    }

    DataRecorder(File dataDir, int maxWait) {
        this.dataDir = dataDir;
        this.maxWait = maxWait;
    }

    @Override
    public void run() {
        Log.v(TAG, "Data Recorder thread started; saving data to " + dataDir.getAbsolutePath());

        //counts how many events have been received.  Never resets
        int eventN = 1;
        //counts the number of times the pins have been read from.  Never resets
        int readN = 0;
        //counts how many tubes have been recorded for the current event, from 0-31.  Resets to 0 at the start of every event
        int tubeN;

        //an array to hold all the data from one event; each sublist represents one tube's 16 bits of data
        //since each event has 32 tubes, it's 32 levels deep
        boolean[][] eventTubeStates;

        //a list to hold the input received from the set of 16 data pins for any one given tube.  thus, is 16 wide
        boolean[] currentInput;

        //if this is false, it means that we won't write down whatever data we just received into a gon file
        //currently, this is used to prevent the creation of empty gon files if the FPGA sends consecutive stop flags
        boolean eventWriteFlag;

        //the main loop.  if not interrupted, then keep reading or trying to read data
        //executes once for each event
        while (!Thread.interrupted()) {
            //reset everything for the new event
            eventTubeStates = new boolean[32][16];
            eventWriteFlag = false;
            tubeN = 0;

            //Read all the data for one event
            //Loop through all the tubes in one event.  note that this is event driven, not merely counting 32 tubes
            while (true) {
                //if empty goes high for some reason, break out
                if (main.FpgaPin.EMPTY.isHigh()) {
                    Log.w(TAG, "FPGA buffer emptied out in the middle of sending an event?  Moving on");
                    break;
                }

                //send a leading edge to the FPGA, requesting data
                main.FpgaPin.ENABLE.setHigh();
                main.FpgaPin.CLK.setLow();
                main.FpgaPin.CLK.setHigh();

                //wait for the valid pin to go high, or for the timeout to elapse
                //if it is interrupted while waiting, then break out
                if (!waitToValidate(maxWait))
                    break;

                //Read the data pins all at once
                currentInput = RpiPinReader.readDecodePins();
                readN++;
                Log.v(TAG, readN + " read times");

                //if it's a stop flag, then stop reading.  Otherwise, add the tube to the array
                if (isStopFlag(currentInput))
                    break;
                else {
                    eventWriteFlag = true;
                    try {
                        eventTubeStates[tubeN] = currentInput;
                    } catch (ArrayIndexOutOfBoundsException e) {
                        Log.w(TAG, "Stop flag not encountered on 33rd data point; will move on to next file");
                        break;
                    }
                }

                //we've read one tube, so mark that
                tubeN++;
            }
            //finished recording all data for one event

            //if something we want to record happened, then send the data to a GonWriter in a different thread to record
            //otherwise, wait a little and then check for data again
            if (eventWriteFlag) {
                new Thread(new GonWriter(new File(dataDir, "event" + Integer.toString(eventN) + ".gon"), eventTubeStates)).start();
                eventN++;
            } else {
                Log.i(TAG, "No data to get, waiting 10ms");
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Log.i(TAG, "Interrupted while waiting for data");
                    e.printStackTrace();
                }
            }

            //finished writing all data for one event.  Go do it again!
        }
        Log.i(TAG, "Ending pin listener thread");
    }

    /**
     * Detects if the FPGA is sending a stop flag with all bits high, indicating the end of the event (all
     * data for each tube has been sent for that given event)
     *
     * @param inputs a boolean list of all the current data inputs from the FPGA
     * @return whether or not the FPGA is currently sending a stop flag
     */
    private static boolean isStopFlag(boolean[] inputs) {

        boolean isStop = true;
        //iterate through every current pin.  if they are all high, then return true
        for (boolean pin : inputs) {
            isStop = isStop && pin;
        }
        if (isStop)
            Log.v(TAG, "Stop flag reached");
        return isStop;
    }

    /**
     * Waits until the VALID pin goes high
     *
     * @param maxWait the maximum number of milliseconds to wait before returning
     * @return true if successful, false if interrupted
     */
    private static boolean waitToValidate(int maxWait) {
        boolean goodExit = false;
        for (int k = 0; k < maxWait; k++) {
            if (main.FpgaPin.VALID.isLow()) {
                try {
                    Thread.sleep(1, 0);
                } catch (InterruptedException e) {
                    Log.i(TAG, "Interrupted while waiting for the valid pin to go high");
                    e.printStackTrace();
                    return false;
                }
            } else {
                goodExit = true;
                break;
            }
        }
        if (!goodExit)
            Log.w(TAG, "Waited " + maxWait + " for VALID, did not see it.  Moving on");
        return true;
    }
}
