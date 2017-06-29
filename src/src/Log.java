/**
 * Created by root on 6/28/17.
 */
public class Log {
    public static void e(String TAG, String msg, Exception e){
        e(TAG,msg);
        e.printStackTrace();
    }
    public static void e(String TAG, String msg){
        System.out.println(TAG+"/e: "+msg);
    }

    public static void w(String TAG, String msg){
        System.out.println(TAG+"/w: "+msg);
    }

    public static void i(String TAG, String msg){
        System.out.println(TAG+"/i: "+msg);
    }
}
