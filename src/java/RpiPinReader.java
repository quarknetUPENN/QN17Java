import java.nio.file.Paths;

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

    //A function written in native C that reads all 16 pins and returns them packed into an int
    native static int readPins();

    /**
     * Gets the current states of all the pins and decodes it into a boolean array
     * Uses the order from RpiPinReader.c
     *
     * @return the state of all the 16 data pins, with true as high and false as low
     */
    static boolean[] readDecodePins(){
        boolean[] result = new boolean[16];

        //Read the int, get into a binary format, and pad it in the front to 16 characters long
        StringBuilder padded = new StringBuilder(Integer.toBinaryString(readPins()));
        while (padded.length() <= 15)
            padded.insert(0, "0");

        //Assign each array value true or false based on the value of each character - in reverse so as to correct for endianness
        //1 = true and 0 = false
        for (int i = 0; i <= padded.length() - 1; i++)
            result[padded.length() - 1 - i] = (Character.getNumericValue(padded.charAt(i))) == 1;

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