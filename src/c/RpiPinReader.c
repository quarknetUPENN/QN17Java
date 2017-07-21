#include <stdio.h>
#include <wiringPi.h>
#include <jni.h>
#include "RpiPinReader.h"

JNIEXPORT jint JNICALL Java_RpiPinReader_readPins(JNIEnv *env, jobject obj) {
 printf("it worked");
 return (jint)1;
}
