#ifndef ESP_H
#define ESP_H

#include "Helper/struct.h"

class ESP {
private:
    JNIEnv *_env;
    jobject _cvsView;
    jobject _cvs;

public:
    ESP() {
        _env = nullptr;
        _cvsView = nullptr;
        _cvs = nullptr;
    }

    ESP(JNIEnv *env, jobject cvsView, jobject cvs) {
        this->_env = env;
        this->_cvsView = cvsView;
        this->_cvs = cvs;
    }

    bool isValid() const {
        return (_env != nullptr && _cvsView != nullptr && _cvs != nullptr);
    }

    int getWidth() const {
        if (isValid()) {
            jclass canvas = _env->GetObjectClass(_cvs);
            jmethodID width = _env->GetMethodID(canvas, "getWidth", "()I");
            return _env->CallIntMethod(_cvs, width);
        }
        return 0;
    }

    int getHeight() const {
        if (isValid()) {
            jclass canvas = _env->GetObjectClass(_cvs);
            jmethodID width = _env->GetMethodID(canvas, "getHeight", "()I");
            return _env->CallIntMethod(_cvs, width);
        }
        return 0;
    }

    void DebugText(char *s) {
        jclass cls = _env->GetObjectClass(_cvsView);
        jmethodID mid = _env->GetMethodID(cls, "DebugText", "(Ljava/lang/String;)V");
        jstring name = _env->NewStringUTF(s);
        _env->CallVoidMethod(_cvsView, mid, name);
        _env->DeleteLocalRef(name);
    }
};

#endif //DESI_ESP_IMPORTANT_ESP_H
