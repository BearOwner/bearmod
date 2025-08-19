#include "Helper/Includes.h"

JNIEnv *env;

class JField {
public:
    jobject field;

    void setAccessible(bool b) {
        auto fieldClass = env->FindClass("java/lang/reflect/Field");
        auto setAccessibleMethod = env->GetMethodID(fieldClass, "setAccessible", "(Z)V");

        env->CallVoidMethod(field, setAccessibleMethod, b);
    }

    jobject get(jobject obj) {
        auto fieldClass = env->FindClass("java/lang/reflect/Field");
        auto getMethod = env->GetMethodID(fieldClass, "get", "(Ljava/lang/Object;)Ljava/lang/Object;");

        return env->CallObjectMethod(field, getMethod, obj);
    }

    void set(jobject obj, jobject val) {
        auto fieldClass = env->FindClass("java/lang/reflect/Field");
        auto setMethod = env->GetMethodID(fieldClass, "set", "(Ljava/lang/Object;Ljava/lang/Object;)V");

        env->CallVoidMethod(field, setMethod, obj, val);
    }
};

class JClass {
public:
    jobject clazz;

    static JClass *forName(const char *s) {
        auto str = env->NewStringUTF(s);

        auto classClass = env->FindClass("java/lang/Class");
        auto forNameMethod = env->GetStaticMethodID(classClass, "forName", "(Ljava/lang/String;)Ljava/lang/Class;");

        auto clazz = new JClass();
        clazz->clazz = env->CallStaticObjectMethod(classClass, forNameMethod, str);

        return clazz;
    }

    JField *getDeclaredField(const char *s) {
        auto str = env->NewStringUTF(s);

        auto classClass = env->FindClass("java/lang/Class");
        auto getDeclaredFieldMethod = env->GetMethodID(classClass, "getDeclaredField", "(Ljava/lang/String;)Ljava/lang/reflect/Field;");

        auto field = new JField();
        field->field = env->CallObjectMethod(clazz, getDeclaredFieldMethod, str);

        return field;
    }
};

string apk_path = "";
string apk_sign = "";

void native_StartKill(JNIEnv *env, jclass clazz, jobject contextObj) {
    ::env = env;

    auto contextClass = env->FindClass("android/content/Context");
    auto getAssetsMethod = env->GetMethodID(contextClass, "getAssets", "()Landroid/content/res/AssetManager;");
    auto getPackageNameMethod = env->GetMethodID(contextClass, "getPackageName", "()Ljava/lang/String;");

    auto assetMgr = AAssetManager_fromJava(env, env->CallObjectMethod(contextObj, getAssetsMethod));
    auto apkFile = AAssetManager_open(assetMgr, "__tpcfinfo.tsb", AASSET_MODE_UNKNOWN);

    auto gamePackage = env->GetStringUTFChars((jstring) env->CallObjectMethod(contextObj, getPackageNameMethod), 0);

    char apkPath[512];
    sprintf(apkPath, "/data/data/%s/files/Kuro.dat", gamePackage);

    char apkDir[512];
    sprintf(apkDir, "mkdir /data/data/%s/files", gamePackage);
    system(apkDir);

    apk_path = apkPath;

    FILE *f = fopen(apkPath, "wb");
    if (f) {
        uint8_t buf[512];
        int n;
        while ((n = AAsset_read(apkFile, buf, sizeof(buf))) > 0) {
            fwrite(buf, sizeof(uint8_t), n, f);
        }
        fclose(f);
    }

    AAsset_close(apkFile);

    auto contextImplClass = JClass::forName("android.app.ContextImpl");
    auto mPackageInfoField = contextImplClass->getDeclaredField("mPackageInfo");
    mPackageInfoField->setAccessible(true);

    jobject mPackageInfo = mPackageInfoField->get(contextObj);

    auto loadedApkClass = JClass::forName("android.app.LoadedApk");
    auto mAppDirField = loadedApkClass->getDeclaredField("mAppDir");
    mAppDirField->setAccessible(true);

    auto mResDirField = loadedApkClass->getDeclaredField("mResDir");
    mResDirField->setAccessible(true);

    auto mApplicationInfoField = loadedApkClass->getDeclaredField("mApplicationInfo");
    mApplicationInfoField->setAccessible(true);
    jobject mApplicationInfo = mApplicationInfoField->get(mPackageInfo);

    auto applicationInfoClass = JClass::forName("android.content.pm.ApplicationInfo");

    auto sourceDirField = applicationInfoClass->getDeclaredField("sourceDir");
    sourceDirField->setAccessible(true);

    auto publicSourceDirField = applicationInfoClass->getDeclaredField("publicSourceDir");
    publicSourceDirField->setAccessible(true);

    auto newApk = env->NewStringUTF(apkPath);

    mAppDirField->set(mPackageInfo, newApk);
    mResDirField->set(mPackageInfo, newApk);
    sourceDirField->set(mApplicationInfo, newApk);
    publicSourceDirField->set(mApplicationInfo, newApk);

    if (!strcmp(gamePackage, "com.pubg.imobile")) { // BGMI
        apk_sign = "AQAAA2UwggNhMIICSaADAgECAgRvSDmYMA0GCSqGSIb3DQEBCwUAMGExCzAJBgNVBAYTAmtyMQ4wDAYDVQQIEwVTZW91bDEOMAwGA1UEBxMFU2VvdWwxDTALBgNVBAoTBFBVQkcxDzANBgNVBAsTBktSIEJpejESMBAGA1UEAxMJSnNvbiBDaG9pMB4XDTIwMDgwNDA0MTMwMloXDTQ1MDcyOTA0MTMwMlowYTELMAkGA1UEBhMCa3IxDjAMBgNVBAgTBVNlb3VsMQ4wDAYDVQQHEwVTZW91bDENMAsGA1UEChMEUFVCRzEPMA0GA1UECxMGS1IgQml6MRIwEAYDVQQDEwlKc29uIENob2kwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDfasN86hVc/OrFUnr3I6nWBOf0R5QR8QM3jYyr9v+WOpPz6T9PsHbTnMaXTcY3WDCoQGiyoIuqKMAQlUPaE6NDH7GoOTOF6u4SNf2hR8T0+Lbcl5z625AFdf6KcL9Mr3o3Ffq3N+mkwOsnlJpe0WdebVOTL1+CDjpobhrMiZBlsW5ukul+P1923X3RpslQe3hsD/E/wcEITzSTrE99KqdeKT50AQqsCpU6M3BqgethV70n+hlkryJSqTAPlXlCZD3ofNFn1STFxRtGS0lvA6z4THaQa99mPFaleBxX7tMi8iKwtK8ynMvUbXu1dh6Lukfo6x6LH8IdgoQBpIJVv+MNAgMBAAGjITAfMB0GA1UdDgQWBBTlRHrvIcYfNYpQ1SlV3I563mV7HjANBgkqhkiG9w0BAQsFAAOCAQEAgTuIerIMga9hyjWX1qmPhUzl9BYUY8pqtq/X40d7eYVBk3qFG4FpsKg3dLBWull7L2q5hpCfec003nhKnwpgLTZORxO4KlyvaCE186zvfLVykO6oWFXJCzJVGNnMSeH1cNprZMcmOZp0Q0PB07TpQf5wEpb/uiP1trNAiok3li2/3GQCIiEwfU7HFjr2sn4HFmzPJHW2oVx4M61LRm4zp3FE697JWZ+iH2rGz1XDyiHWq2zAp2iumkBfJN/qG7dI6fE95aGBkqLQQnFbIwon5C6jPFWkqJ2G2EEsZNT+noygyJkDJzFnrtMW7oYANcrxVdwIMDFtrIIt2jt+VuZVLA==";
    } else if (!strcmp(gamePackage, "com.pubg.krmobile")) { // KOREA
        apk_sign = "AQAAA2UwggNhMIICSaADAgECAgRGrEN5MA0GCSqGSIb3DQEBCwUAMGExCzAJBgNVBAYTAmtyMQ4wDAYDVQQIEwVTZW91bDEOMAwGA1UEBxMFU2VvdWwxDTALBgNVBAoTBFBVQkcxDzANBgNVBAsTBktSIEJpejESMBAGA1UEAxMJSnNvbiBDaG9pMB4XDTE4MDMzMDA1MDk1M1oXDTQzMDMyNDA1MDk1M1owYTELMAkGA1UEBhMCa3IxDjAMBgNVBAgTBVNlb3VsMQ4wDAYDVQQHEwVTZW91bDENMAsGA1UEChMEUFVCRzEPMA0GA1UECxMGS1IgQml6MRIwEAYDVQQDEwlKc29uIENob2kwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCf987Etd/fj7AgjeZ002BghE9IEfaYKvUi+7SN9jByXh4ujtsb+GSakoTl3+eDNbZ6TkLVhHLnab2gtcYJMsX3l7xdtzQt6rjUeZlhquGHlJd1NIuOHoB3nQS51DQA4GqsKxT/5Gm83UdhPvQ6Lz1IzTK5k1GRoHt16G3emWe5AgPyJJZG8Rkq5d0SHyGwQbeuH1KyAwes7L+E/qCNoDn8ed50ixzVx0YuCnbLSqDrGbGIfUVPAe6b1Pb7VBJ/r0Fin8+1mwLs1y+W0uA8qVEqDm3ZWz5l6muJ7xeSfiWRt9+imBK8S+vni22VBcp6FzDSpJPxK4JfyqvkPlKdMZpxAgMBAAGjITAfMB0GA1UdDgQWBBRNoRhsqQ1dHYCBa6l7Wim2zOOPxTANBgkqhkiG9w0BAQsFAAOCAQEAUnF8V9x8hnlqwgTVzXMt1mnbPlbxsSyEbzDACuFsyAQ/XGqd7whVPpiFua0nmOuyemGqzO0uMUJsMzaKtEdANSrGb3RiiN6GstGY0+OnexXZplQEdy2DxEs7k4IRNOWgfUjgTQkzA9aGRReDm3GMMCCto0or40HLNGzNafNRNPfvbmFDN34P3/fapAWgzUeszEDbkj9KjkPWlSMJfte8xGw43n6+AiHX+gFYskrJLWe8bEl3YJRfzJ2309a1OcWiHMt6W2T7hr8xyoo+4sgTwAoiVc+bQiRFLRBuzMg5JG7jgkjWzckQ2o0+RQ3dspjz6sWgfN0tCW34gIHwl3q4Sw==";
    } else if (!strcmp(gamePackage, "com.rekoo.pubgm")) { // TAIWAN
        apk_sign = "AQAAA4EwggN9MIICZaADAgECAgR3sEeBMA0GCSqGSIb3DQEBCwUAMG4xCzAJBgNVBAYTAjg2MRMwEQYDVQQIEwpHdWFuZyBEb25nMRIwEAYDVQQHEwlTaGVuIFpoZW4xEDAOBgNVBAoTB1RlbmNlbnQxEDAOBgNVBAsTB1RlbmNlbnQxEjAQBgNVBAMTCUhlY3RvciBMdTAgFw0xOTAyMTUxMzE5MDhaGA8yMjkyMTEyOTEzMTkwOFowbjELMAkGA1UEBhMCODYxEzARBgNVBAgTCkd1YW5nIERvbmcxEjAQBgNVBAcTCVNoZW4gWmhlbjEQMA4GA1UEChMHVGVuY2VudDEQMA4GA1UECxMHVGVuY2VudDESMBAGA1UEAxMJSGVjdG9yIEx1MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArGPsxZm30XySTRem8shDXM1MSk+3PVQ+BybaXEIQAZ9IuiHnCrniff3f/LAl4TOIM+3wUMgFQX4Pmkx00FunBc+AiwIZi7lcMPy63Z654glgGokKIQm4onvFhgPxDUNqpNeEumObiMycTIrQ1G671li91+Bb0Lyc5pP7tach3OgQbmDxP6kAOYJf2Eb/ovkaQSxT+Q5rc1DBR/jw4Oa4jgOLDdCdhn6lQ3mc9NiqDYWvA4MGz6QTdgFHUmoCTy2dyRcc4KvgPgHnFdzN2bLunPe3QNCzbpozDY31jm0Mfot+cF+VHcojVa1SJ7oBWUZj0O8srORLUVwdsRqbEvRysQIDAQABoyEwHzAdBgNVHQ4EFgQU9E5bbj3qMdz0974HLPtvKd06N2EwDQYJKoZIhvcNAQELBQADggEBAKdlbcHxDw7z+NxgepiC+17AKc3hI6jHmky25cbh3xLBqGrKOYO7bKh2YwExIZHyeeW2ZKR/9kNes42YpVYdwBnCG/0BnvzX5tXdlKlzV/uHEkj82Lo2eiIBtEsszD0kEnJfS1+7THoJaFvNFfTLmzdHsM3bZhL70T59YtPQUoLa4dlBMAQg0gc4x9tZdQWnFgyb0e7g3Ta4cOkXPIUDErQS0wMBY0n5hXsnw72vDVhW2NZor5wv6fSNLdImMjSFlo8RmdBPqc+sqvXLk8HCjGkut6vcZXR609LpfYtQH5T1bkMqdUu1PjQRLHWG8jsTtt/Ofd7ype7NHu1lp9H/voc=";
    } else if (!strcmp(gamePackage, "com.vng.pubgmobile")) { // VIETNAM
        apk_sign = "AQAAA4EwggN9MIICZaADAgECAgRvmtqRMA0GCSqGSIb3DQEBCwUAMG8xCzAJBgNVBAYTAlZOMRQwEgYDVQQIEwtIbyBDaGkgTWluaDEUMBIGA1UEBxMLSG8gQ2hpIE1pbmgxEjAQBgNVBAoTCVZORyBHYW1lczESMBAGA1UECxMJVk5HIEdhbWVzMQwwCgYDVQQDEwNWTkcwHhcNMTQxMjI0MTM0ODE0WhcNNDIwNTExMTM0ODE0WjBvMQswCQYDVQQGEwJWTjEUMBIGA1UECBMLSG8gQ2hpIE1pbmgxFDASBgNVBAcTC0hvIENoaSBNaW5oMRIwEAYDVQQKEwlWTkcgR2FtZXMxEjAQBgNVBAsTCVZORyBHYW1lczEMMAoGA1UEAxMDVk5HMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAimd2CrBZ2Dl26SIDsVfp68sSfZMmwsbuD1UAIPVg1XGgxTDCuHzZcp8T6vdDnaWpTjz3l5vs4HMk5ZUt0ejVNy88WLV8ziG5RsnUUS2Lw1PR3cLPI7qvK+x8rINoqRphYQ1AXAJ+UwRoR9VrEkD8SyZjst6ahPGt1ZL5GrfzFiKmcHGhXTHLSac1nFwy1JMFr6TfTyQ1Quw7fH2oDL82t3eV1MyRmcxg1reGhjPPZVu4xuCJdQ1dKHrWPkhc6GkipaVc12nwPRX87fN2Lf0Oy/6c48g4S/jP31rt5afmQfzNcCoWM/XzvZ0DNyr3an+EVqbq9lqE2kjeefULUfaVRwIDAQABoyEwHzAdBgNVHQ4EFgQUV/0zdu870XbeUO4p8oP1fQkzqEQwDQYJKoZIhvcNAQELBQADggEBAHyFPvO5KLEMyd8gPeKVRql36kdMcRwWBt5imulULQnzXyMVqyAK6eoZSxk5szit7zBnTfiyVt80oOBs2PHx6QKHGc6wSInGFg+llbrMXrgJ7o4j8XJh2B5yFsZzP8ue5nQPsSOQvmatD6y8/KnBlAlV7VHMVFAfurxXK8OsD2yqr0U2wu8k2axu+qpTVaio5+tXD4y0yIMfVBg+6J5J4//b3742tGrrqwt9jLttKBmO6gh1SSUm5pNXQSDh49hDN555M4LMUlZkEN9h+Siyg53TFBQhbS61o+reZVyQt84LAS8zhK6UvteODVWwiHz+ac4hTa3A8v8lUu84rbRWQtU=";
    } else if (!strcmp(gamePackage, "com.tencent.ig")) { // GLOBAL
        apk_sign = "AQAAA4EwggN9MIICZaADAgECAgQdzoakMA0GCSqGSIb3DQEBCwUAMG4xCzAJBgNVBAYTAjg2MRMwEQYDVQQIEwpHdWFuZyBEb25nMRIwEAYDVQQHEwlTaGVuIFpoZW4xEDAOBgNVBAoTB1RlbmNlbnQxEDAOBgNVBAsTB1RlbmNlbnQxEjAQBgNVBAMTCUhlY3RvciBMdTAgFw0xNzEyMjgxMTM3MDdaGA8yMTE3MTIwNDExMzcwN1owbjELMAkGA1UEBhMCODYxEzARBgNVBAgTCkd1YW5nIERvbmcxEjAQBgNVBAcTCVNoZW4gWmhlbjEQMA4GA1UEChMHVGVuY2VudDEQMA4GA1UECxMHVGVuY2VudDESMBAGA1UEAxMJSGVjdG9yIEx1MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAoLUkEokic5WdkaoxZsw1kiYwqea6s5TYNBq/6whx5hUl850hZKdIBj8oPtxzNSV2MytKJJo6gxH6o1M1WFuiMXzaCTtEKE8T2wASnxM1BJ1yW7KIZMPPZVGotAcsdhYLD9RxrR0t5kDeeRX2lBVcOhso/gGVP3uuvB2260mqInGr0qfD07pJ2pTMApiHRDMcUQEtyOON2esTPHXlME7mfUBIsAD9WwPLZKNQBcl2QkfWhAj51xZ6bpGxBSHXzOYhcfuHYGkt7SX65mE1T5Oah1ZbN3AVyo1dahE5b7FdfEz91r0B6w0LeP/yW3SwvnDH4CHbMBelsPusR9V07uhONQIDAQABoyEwHzAdBgNVHQ4EFgQUBOHr6O/GDj0JgFVqzRwoaoY3DMIwDQYJKoZIhvcNAQELBQADggEBAAgV8yC0OhdYD4+YnTdtwFlybV3mDKO6NAIM+LatvdrW/qV3s1IOjvUBp8Em6JIrvHITy6NzGPrKPMYX2gMBD8/vjjGzRtpSSzJobiOWWM+3+G0wlTFBzhKLSR5itmGkP4JLvq6//Sb0lDFnK4bDhnfOMjVcqyAI6Es+PhOPyRUDNLEu8XjjpcKIzSvybj5LHRvcEU2Kj9WH8xzzE3QoHpx35jzhcnptErLz36XZqKWuhnNqEuMgyIN214yXwlKErPRK5bOZRr6c93zxwQb6cSbKVrRZbo5HzGWG8xEPSaeYMY8rviisXPtlm6AT7iJmYGhor19kBIR0HI9V3nz06Fs=";
    } else if (!strcmp(gamePackage, "com.tencent.iglite")) { // LITE
            apk_sign = "AQAAA3cwggNzMIICW6ADAgECAgQWIEWSMA0GCSqGSIb3DQEBCwUAMGkxCzAJBgNVBAYTAjg2MRMwEQYDVQQIEwpHdWFuZyBEb25nMRIwEAYDVQQHEwlTaGVuIFpoZW4xEDAOBgNVBAoTB1RlbmNlbnQxEDAOBgNVBAsTB1RlbmNlbnQxDTALBgNVBAMTBGFsZXgwIBcNMTgwNTI5MTIxODAzWhgPMjEyNzEyMDQxMjE4MDNaMGkxCzAJBgNVBAYTAjg2MRMwEQYDVQQIEwpHdWFuZyBEb25nMRIwEAYDVQQHEwlTaGVuIFpoZW4xEDAOBgNVBAoTB1RlbmNlbnQxEDAOBgNVBAsTB1RlbmNlbnQxDTALBgNVBAMTBGFsZXgwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDCNszTwd1M9yE9daM4kdZbcoZ/xPcjBr3HGZtZPyOUo0yZlbJ/I7qucbHyC/h01hlX/VVOc8Vuag04gg28+KnNxL5KRQ/oyIVAuhcFOqb8m333bdKdDTtPNnVhgHLyxXJBOuESLIJs6l84zbvIdMBqNumedHQifKurTQv55hcCjsZcoYKaKyMz6AR4BAPFE3gqJVkmCpNFvWimY0TIFy/uNrjX3SvcOlHfOTf1kvoj/H3AhV46P3rbZBf2FCExF4mGj7LGf5d//vgXL9UyKzNoAFCLFt9/yUxaEfFoi3jHSIdeUzgmO1rwPrL6pOBc0Wl3UWkRtC9KYbwvHWTM9KLPAgMBAAGjITAfMB0GA1UdDgQWBBRJCUaD0oxoWcIqM9V6Y/waujoGSTANBgkqhkiG9w0BAQsFAAOCAQEABOmrQ+0/UD+y860QGYL3NetXETkqYRYzeMRTFqHE70KC43tgC3l10p1z9LR4Wk/i8lomRRvto78GJ/Uo1ZHssTtGmDRVawhuOf5hMEUGTl0sizPZZOiVLfVAtyZU+rW5CxssXHUimnKhLaeFhyEow5Ns8tD1tY93zBoF0ZqrGro5OFCvn1UBacspJmbIaRb0GgaRzG7EPxo1pBdjsRrH5xuor6LJXC3xAgT/qWwrieeHwsW4g8oQs0EOKpTCgiZ3rEFp4MYvmea4vyB4cZfaiEo8r+hKIJEJtV+A67xA2Ye1QW1HQEkmrwsPkITe6rXG3QrD6WoKmXMXz/BnkwHkMQ==";
    } else if (!strcmp(gamePackage, "com.tencent.igce")) { // BETA
            apk_sign = "AQAAA4EwggN9MIICZaADAgECAgQdzoakMA0GCSqGSIb3DQEBCwUAMG4xCzAJBgNVBAYTAjg2MRMwEQYDVQQIEwpHdWFuZyBEb25nMRIwEAYDVQQHEwlTaGVuIFpoZW4xEDAOBgNVBAoTB1RlbmNlbnQxEDAOBgNVBAsTB1RlbmNlbnQxEjAQBgNVBAMTCUhlY3RvciBMdTAgFw0xNzEyMjgxMTM3MDdaGA8yMTE3MTIwNDExMzcwN1owbjELMAkGA1UEBhMCODYxEzARBgNVBAgTCkd1YW5nIERvbmcxEjAQBgNVBAcTCVNoZW4gWmhlbjEQMA4GA1UEChMHVGVuY2VudDEQMA4GA1UECxMHVGVuY2VudDESMBAGA1UEAxMJSGVjdG9yIEx1MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAoLUkEokic5WdkaoxZsw1kiYwqea6s5TYNBq/6whx5hUl850hZKdIBj8oPtxzNSV2MytKJJo6gxH6o1M1WFuiMXzaCTtEKE8T2wASnxM1BJ1yW7KIZMPPZVGotAcsdhYLD9RxrR0t5kDeeRX2lBVcOhso/gGVP3uuvB2260mqInGr0qfD07pJ2pTMApiHRDMcUQEtyOON2esTPHXlME7mfUBIsAD9WwPLZKNQBcl2QkfWhAj51xZ6bpGxBSHXzOYhcfuHYGkt7SX65mE1T5Oah1ZbN3AVyo1dahE5b7FdfEz91r0B6w0LeP/yW3SwvnDH4CHbMBelsPusR9V07uhONQIDAQABoyEwHzAdBgNVHQ4EFgQUBOHr6O/GDj0JgFVqzRwoaoY3DMIwDQYJKoZIhvcNAQELBQADggEBAAgV8yC0OhdYD4+YnTdtwFlybV3mDKO6NAIM+LatvdrW/qV3s1IOjvUBp8Em6JIrvHITy6NzGPrKPMYX2gMBD8/vjjGzRtpSSzJobiOWWM+3+G0wlTFBzhKLSR5itmGkP4JLvq6//Sb0lDFnK4bDhnfOMjVcqyAI6Es+PhOPyRUDNLEu8XjjpcKIzSvybj5LHRvcEU2Kj9WH8xzzE3QoHpx35jzhcnptErLz36XZqKWuhnNqEuMgyIN214yXwlKErPRK5bOZRr6c93zxwQb6cSbKVrRZbo5HzGWG8xEPSaeYMY8rviisXPtlm6AT7iJmYGhor19kBIR0HI9V3nz06Fs=";
    }
}

jstring native_GetAPKPath(JNIEnv *env, jclass clazz) {
    return env->NewStringUTF(apk_path.c_str());
}

jstring native_GetSignature(JNIEnv *env, jclass clazz) {
    return env->NewStringUTF(apk_sign.c_str());
}
