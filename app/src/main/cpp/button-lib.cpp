#include <jni.h>
#include <string>
#include <iostream>

/* “Java_com_medos_mos_MainActivity_getAPIKey”: represents the Java code with package name “com.medos.mos”
 * [dot(.) must be replaced with underscore(_)],
 * followed by Activity name “MainActivity” where we want to call the native function and;
 * the static method “getFacebookApiKey” to fetch the API key from the native function.
 * So the combination is: {Package Name}_{Activity Name}_{Method Name}
 * */

extern "C" JNIEXPORT jstring

JNICALL
Java_com_medos_mos_ui_login_ForgetPassword_getRSAKey(JNIEnv *env, jobject object) {
    std::string enRsaKey = "E/FPmMYsfF57KKMln3TWqbYknFy5o2krGDZKoPZyx4A=";
    return env->NewStringUTF(enRsaKey.c_str());
}

extern "C" JNIEXPORT jstring

JNICALL
Java_com_medos_mos_AES_getRSA(JNIEnv *env, jobject object) {
    std::string enRsa = "pj7XPR1PZKSfsL7LxNA82H14T61awxfVRsMj7mTQ+elTXjuyRb71Re37I4MzCK0Ylyn0LdJlcIWmZUma/2g3j7JIggKZlsp3O0qKJralF2vIkGmc6VLAmlgrr3cFUGVLZdwr7YEQSeIHQ7A7gSjpPfSwnCHwEFI/yNqpFiwq+ZfLwmJjEgs/lTbd1fTV8hews4qvVuSCi9BwITG1WAC3qWpm0G+aGjvBhegXIPKdmYZ3yofS4E2b6mbwYszLCs7bajJ1iSKxe6No4Rtl0j6ZU+eRsmfNZm6MqytGpDk3Tx/K94X1vfuMVdGtA6rmCPZpYIEWAALxrc1G/wj1eqd51x0kXUf7lqvRrAsDvrc5dQNi74lJEi/Nt82VJxGLCUXzem+pHZf2zZp5h+NvheMrNHq8aec5BGYZuEMOkmr2z4uhZcRxm90SfrM9HegMq5f2XHTIsno3mHkVdhcfVGPtpHkm+llfVxAvTdNnCi/yUlr2Wz4qA41F/ZdBC4q5u/1aOl5sIJMjZxC8xj7R+sQIYTcrR9WOQdXSZFfPjCOzNeetYwDeRQksO4qzYnZzdcjQ1ZChCkHjwzvYuE74PkJHVXg3MM4a9m1tYvytRO+wco9g54SrLV/LoxqW8e6IEkJYiAvZ8AU6BwkqKxUdQv8QmRHll+agc4+udRdAQAB61jtuKKPX+RIbVtfOeHyzdzy7EISNpdNRkAk/uXXIV2AUcyvY8g4UxM4Cr+2yBX0tGV3hfBLbRaxfNTywdQnlCyvODcg3EMwVvFGA5aQD7jiv2a6Nq/RFD+Y87jNHZwJYck1mK3oYxYcvBXA3gJCOJNg4lhx5LdzgBjN0ZVYjxCflA38RO4PoXA+reUhiyp3H8dmYU8NcWyutIii0Y0Tc+BS+hKAOcJMS9sDRRl4zzaH6PcdoSaOoqD04WOdczvQoa7LFvmJjzxBtMBKkneQlmvxgo1UzIGlv9dY2X6rTQGTqjD6NDtmKL2Gbl2vVoIFLn67Arb7HHWPM8pxAS+Bsvz/xjufwahKMHzrnW33ZkDZhrC/upyx147NdPXOauxDX/g+HxGEy1oMI+rwGsCrW17lNJSczT3Vv/AOeZrI0vndIAHb/CwjymMtgS1Frsj3bQJRkOIx7om0z+/xmKaZ0XUj+1BLDkv8OGwvr7erRMxU4jB+VNX7TZ32qnXy8xGx62M0zPiNYnSktJo6sGsblNqhsECeIYtlcJU2PCKAjrVdntOAerEV7HW5zJrxsGKz0nTsWpfr+iBJtZmK5A0RwMqqm4uR1Y4AIoQ1NZH4lwJQZOac7XwhKPZIweHoI0tBD18LH7m+kFuIIPgx5mPfcuBXNsWxG7/8L5/4wdVsSvKC4AfCzDyDmboyrFvv+q6RDBznlBwBniIUBnYtPDk27PUKey2oVsLUJcvvcF5s2bAcbIXGhajrqmGH5fGc34OvlhvP6apK78PCv/w9vncHjU9sd9dVukBQ1Y9Ewemxa9uUUvdtYNh/zZyjwqOkTwEsD7X9pVG6+GR26xE2HiHflxgTJRoV/dkqoVKWRp+j9yPkp0gEHiC1inVcXrDTwceQyWKx71U/hk/oyATBrCWE6bLxa0XnobCmqHlyedSYKFimUuOCe894AYMiixD4pe9AFE1unRRVMr/9uTXaXQZBZNhaATkk1EXf6vHJuFvg33LgfblbpSyMqc+8KDxKR65SElvsjogE7YvO3ZOCX4PTBKHXTuiyLKtmU8flaQMUa77tJFWUD97g4zJxpwUbfDhWmrHUXd4M+YHporzUMLuPHHOJ+jo/XjdfQYr1ur9/DJtWz2fbm1uSfe+JRWcAS2rw+AYil1bdoH0UEcaHhxm2Vm6aZAtlKGXWs9XlXJob6uR0FrmuraDCHomH+wFwUDyDkYK++v/P2e8qh7FR3irGOXM10amgJqCKDwFSJ1ALsdNcm7C7P8GWLQNyCJEHp4xrIthcyPe2Ob0j7j/0M0vmALKI40aw5UDOQn9BA2ue5UmamXpHAAFOGqpOC4LughwmsQgGIJHP0jEkpfUA240N2IfYGNhClni3DXdGViuF4UYF83030eT0ftktfzVAnZbiBvQfv/LN5BtPjDGTKOt5CAeZaZZuctq4y54qO4nHiqeI8p9gQYmNJL9YmlsmGLOp0gauvaKDyMbJJIl82K6vpLEmv5fkyHMPOjkTwQvAeLdbqjPq2E6tt52VfzLYI/ljCMIcX3eiINkvXjL2rCatFAMmX";
    return env->NewStringUTF(enRsa.c_str());
}

extern "C" JNIEXPORT jstring

JNICALL
Java_com_medos_mos_AES_getFKey(JNIEnv *env, jobject object) {
    std::string firstKeyMix2[] = {"y","s","Z","Q","3","W","c","O","W","w","f","6","T","S","f","H","l","b"};
    std::string firstKey;

    for (int i = 0; i < 18; i++) {
        firstKey += firstKeyMix2[i];
    }

    return env->NewStringUTF(firstKey.c_str());
}
