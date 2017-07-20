import com.pi4j.util.NativeLibraryLoader;

/**
 * Created by root on 7/20/17.
 */
public class RpiPinReader {
    static {
        System.loadLibrary("librpipinreader.so");
    }

    public native static int readPins();
}

//gcc RpiPinReader.c -shared -std=c99 -lwiringPi -I"/usr/lib/jvm/jdk-8-oracle-arm32-vfp-hflt/include" -I"/usr/lib/jvm/jdk-8-oracle-arm32-vfp-hflt/include/linux/" -o librpipinreader.so
