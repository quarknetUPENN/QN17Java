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

    native static int readPins();

    static boolean[] readDecodePins(){
        boolean[] result = new boolean[16];
        String data = Integer.toBinaryString(RpiPinReader.readPins());

        Log.v(TAG,"Read string: "+data);
        for (int i = 0; i <= 15; i++) {
            result[i] = data.charAt(i) == '1';
        }
        return result;
    }
}

//compile the java file by generating .class file;
//javac RpiPinReader.java

//make the .h header file from .class, but don't add .class to the end of the file name
//javah RpiPinReader

//(make sure to #include the .h in the .c file)

//compile the .c file into a .so library
//gcc RpiPinReader.c -shared -std=c99 -lwiringPi -I"/usr/lib/jvm/jdk-8-oracle-arm32-vfp-hflt/include" -I"/usr/lib/jvm/jdk-8-oracle-arm32-vfp-hflt/include/linux/" -o librpipinreader.so -Wl,--kill-at
//-shared       : make a shared library, .so on *nix or .dll on windows
//-std=c99      : compile against the version of C released in 1999
//-lwiringPi    : location to go find wiringPi.h
//-I (#1)       : location to go find jni.h $JAVA_HOME/include
//-I (#2)       : location to find dependencies of jni.h $JAVA_HOME/include/$OS
//-o            : specify output filename
//-Wl,--kill-at : remove compiler names from functions.  not doing this causes UnsatisfiedLinkErrors because of the name mismatch

//move the .so library into lib/
//mv [appropriate args]

//done