#include <stdio.h>
#include <wiringPi.h>
#include <jni.h>
#include "RpiPinReader.h"

JNIEXPORT jint JNICALL Java_RpiPinReader_readPins(JNIEnv *env, jobject obj) {
    int result = 0;
    result += 1     * digitalRead(18);
    result += 2     * digitalRead(23);
    result += 4     * digitalRead(24);
    result += 8     * digitalRead(25);
    result += 16    * digitalRead(7 );
    result += 32    * digitalRead(16);
    result += 64    * digitalRead(20);
    result += 128   * digitalRead(21);
    result += 256   * digitalRead(26);
    result += 512   * digitalRead(13);
    result += 1024  * digitalRead(5 );
    result += 2048  * digitalRead(10);
    result += 4096  * digitalRead(4 );
    result += 8192  * digitalRead(17);
    result += 16394 * digitalRead(27);
    result += 32768 * digitalRead(22);
    return (jint) result;
}
