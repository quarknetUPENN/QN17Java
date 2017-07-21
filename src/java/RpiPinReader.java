import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;

/**
 * Created by root on 7/20/17.
 */
public class RpiPinReader {
    final static String TAG = RpiPinReader.class.getSimpleName();

    static {
        //load the compiled c code by absolute path.  first get absolute path to working dir, then go into the libs
        //folder and get the appropriate .so file
        System.load(Paths.get("").toAbsolutePath().toString()+"/lib/librpipinreader.so");
    }

    native static int readPins();

    static boolean[] readDecodePins(){
        boolean[] result = new boolean[16];

        //Calls the C function to read each pin
        String unpadded = Integer.toBinaryString(RpiPinReader.readPins());

        //Pad the leading edge of this int with zeroes until it's 16 characters long
        while(unpadded.length() <= 15)
            unpadded = "0" + unpadded; //Now padded (don't let the name mislead you)
        //Log.v(TAG, "Unpadded (this is backwards): " + unpadded);

        //Assign each array value true or false based on the value of each character
        //1 = true and 0 = false
        for (int i = unpadded.length()-1; i >= 0; i--)
            result[i] = (Character.getNumericValue(unpadded.charAt(i))) == 1;

        //The array is backwards, so we flip it
        for (int j = 0; j < result.length/2; j++) {
            boolean temp = result[j];
            result[j] = result[result.length - 1 - j];
            result[result.length - 1 - j] = temp;
        }

        //Done
        //Log.v(TAG, Arrays.toString(result));
        return result;
    }
}

//compile the java file by generating .class file;
//javac RpiPinReader.java

//make the .h header file from .class, but don't add .class to the end of the file name
//javah RpiPinReader

//(make sure to #include the .h in the .c file)

//compile the .c file into a .so library
//gcc RpiPinReader.c -shared -std=c99 -lwiringPi -I"/usr/lib/jvm/jdk-8-oracle-arm32-vfp-hflt/include" -I"/usr/lib/jvm/jdk-8-oracle-arm32-vfp-hflt/include/linux/" -o librpipinreader.so
//-shared       : make a shared library, .so on *nix or .dll on windows
//-std=c99      : compile against the version of C released in 1999
//-lwiringPi    : location to go find wiringPi.h
//-I (#1)       : location to go find jni.h $JAVA_HOME/include
//-I (#2)       : location to find dependencies of jni.h $JAVA_HOME/include/$OS
//-o            : specify output filename
//(windows only?? gcc 4.8 which is on the RPi doesn't recognize this, but compiles fine without it)
//-Wl,--kill-at : remove compiler names from functions.  not doing this causes UnsatisfiedLinkErrors because of the name mismatch

//move the .so library into lib/
//mv [appropriate args]

//done