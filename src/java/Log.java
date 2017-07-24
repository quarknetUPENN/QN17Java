/**
 * A class to provide a uniform system logging standard
 */
public class Log {
    public static void e(String TAG, String msg, Exception e){
        e(TAG,msg);
        e.printStackTrace();
    }
    public static final boolean ENABLE = false; //Controls printing "v" statements

    public static void e(String TAG, String msg){
        System.out.println(TAG+"/e: "+msg);
    }

    public static void w(String TAG, String msg){
        System.out.println(TAG+"/w: "+msg);
    }

    public static void i(String TAG, String msg){
        System.out.println(TAG+"/i: "+msg);
    }

    public static void v(String TAG, String msg){
        if (ENABLE){
            System.out.println(TAG+"/v: "+msg);
        }
    }
}
