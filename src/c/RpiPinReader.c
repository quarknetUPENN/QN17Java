#include <stdio.h>
#include <wiringPi.h>
#include <jni.h>
#include "RpiPinReader.h"

JNIEXPORT jint JNICALL Java_RpiPinReader_readPins(JNIEnv *env, jclass clazz) {
 printf("it worked");
 return 1;
}
