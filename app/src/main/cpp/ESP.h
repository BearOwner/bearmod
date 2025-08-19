#ifndef ESP_H
#define ESP_H


class Paint *m_TextPaint;
    class Paint *m_LinePaint;
    class Paint *m_FillPaint;
    class Paint *m_Paint;
/*#include "struct.h"
#include "SDK.hpp"
#include "Vector3.hpp"

using namespace SDK;
*/
#include "SDK.hpp"
using namespace SDK;



class ESP {
private:
    JNIEnv *_env;
    jobject _cvsView;
    jobject _cvs;
    jclass canvasView;
    jmethodID drawrect;

public:
    ESP() {
        _env = nullptr;
        _cvsView = nullptr;
        _cvs = nullptr;
        drawrect = nullptr;
        canvasView = nullptr;
    }
    // Other methods and members...
//};

/*
class ESP {
private:
    JNIEnv *_env;
    jobject _cvsView;
    jobject _cvs;
    jclass canvasView;
jmethodID drawrect;
   class Paint *m_FillPaint;

public:
    ESP() {
        _env = nullptr;
        _cvsView = nullptr;
        _cvs = nullptr;
        drawrect = nullptr;
        canvasView = nullptr;
    }
*/
    ESP(JNIEnv *env, jobject cvsView, jobject cvs) {
        this->_env = env;
        this->_cvsView = cvsView;
        this->_cvs = cvs;
        canvasView = _env->GetObjectClass(_cvsView);
        drawrect = _env->GetMethodID(canvasView, "DrawRect",
                                     "(Landroid/graphics/Canvas;IIIIFFFFF)V");
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
    /*
    
    void DrawFilledTriangle(Point a, Point b, Point c, int color) {
    Paint *paint = this->m_FillPaint;

    paint->setColor(color);

    Path *path = this->m_Path;
    path->reset();
    path->moveTo(a.x, a.y);
    path->lineTo(b.x, b.y);
    path->lineTo(c.x, c.y);
    path->lineTo(a.x, a.y);
    path->close();

    env->CallVoidMethod(this->m_CanvasObj, this->drawPathId, path->pathObj, paint->paintObj);
}
    */
    void DrawText(Color color, const char *txt, FVector2D pos, float size) {
        if (isValid()) {
            jclass canvasView = _env->GetObjectClass(_cvsView);
            jmethodID drawtext = _env->GetMethodID(canvasView, "NRG_DrawText",
                                                   "(Landroid/graphics/Canvas;IIIILjava/lang/String;FFF)V");
        //    jstring s = _env->NewStringUTF(txt);
            jstring s = _env->NewStringUTF(txt);
            _env->CallVoidMethod(_cvsView, drawtext, _cvs, (int) color.a, (int) color.r,
                                 (int) color.g, (int) color.b,
                                 s, pos.X, pos.Y, size);
            _env->DeleteLocalRef(s);
        }
    }
    
    
    void DrawText(Color color, const wchar_t *txt, FVector2D pos, float size) {
        if (isValid()) {
            jclass canvasView = _env->GetObjectClass(_cvsView);
            jmethodID drawtext = _env->GetMethodID(canvasView, "NRG_DrawText",
                                                   "(Landroid/graphics/Canvas;IIIILjava/lang/String;FFF)V");
        //    jstring s = _env->NewStringUTF(txt);
          //  jstring s = _env->NewStringUTF(txt);
            jstring s = wcstojstr(_env, txt);
            _env->CallVoidMethod(_cvsView, drawtext, _cvs, (int) color.a, (int) color.r,
                                 (int) color.g, (int) color.b,
                                 s, pos.X, pos.Y, size);
            _env->DeleteLocalRef(s);
        }
    }
    
    
    
    
    
    
    void DrawWeaponIcon(int wid,FVector2D pos) {
        if (isValid()) {
            jclass canvasView = _env->GetObjectClass(_cvsView);
            jmethodID drawoth = _env->GetMethodID(canvasView, "NRG_DrawWeaponIcon",
                                                   "(Landroid/graphics/Canvas;IFF)V");
            _env->CallVoidMethod(_cvsView, drawoth, _cvs, wid, pos.X, pos.Y);
        }
    }
  void DrawCircle(Color color, FVector2D pos, float radius,float thickness) {
        if (isValid()) {
            jclass canvasView = _env->GetObjectClass(_cvsView);
            jmethodID drawcircle = _env->GetMethodID(canvasView, "NRG_DrawCircle",
                                                           "(Landroid/graphics/Canvas;IIIIFFFF)V");

            _env->CallVoidMethod(_cvsView, drawcircle, _cvs, (int) color.a, (int) color.r,
                                 (int) color.g, (int) color.b, pos.X, pos.Y, radius,thickness);
        }
    }
    
    
void DrawLine2(Color color, float thickness, FVector2D start, FVector2D end) {
        if (isValid()) {
            jclass canvasView = _env->GetObjectClass(_cvsView);
            jmethodID drawline = _env->GetMethodID(canvasView, "NRG_DrawLine", "(Landroid/graphics/Canvas;IIIIFFFFF)V");
        //    drawline = env->GetMethodID(canvasView, "drawLine", "(FFFFLandroid/graphics/Paint;)V");

            _env->CallVoidMethod(_cvsView, drawline, _cvs, (int) color.a, (int) color.r,
                                 (int) color.g, (int) color.b,
                                 thickness,
                                 start.X, start.Y, end.X, end.Y);
        }
    }
void DrawFilledRect(Color color, FVector2D start, FVector2D end) {
        if (isValid()) {
            jclass canvasView = _env->GetObjectClass(_cvsView);
            jmethodID drawline = _env->GetMethodID(canvasView, "NRG_DrawFilledRect",
                                                   "(Landroid/graphics/Canvas;IIIIFFFF)V");
          //  _env->CallVoidMethod(_cvsView, drawline, _cvs, (int) color.a, (int) color.r,  (int) color.g, (int) color.b, start.X, start.Y, end.X, end.Y);
            _env->CallVoidMethod(_cvsView, drawline, _cvs, (int) color.a, (int) color.r,  (int) color.g, (int) color.b, start.X, start.Y, start.X + end.X, start.Y + end.Y);

        }
    }
    
    
    
    void DrawOTH(FVector2D start, int num) {
        if (isValid()) {
            jclass canvasView = _env->GetObjectClass(_cvsView);
            jmethodID drawline = _env->GetMethodID(canvasView, "NRG_DrawOTH",
                                                   "(Landroid/graphics/Canvas;IFF)V");
            _env->CallVoidMethod(_cvsView, drawline, _cvs, num, start.X, start.Y);
        }
    }
    
    
    /*
void DrawRect(Color color, int thickness, Rect2 rect) {
        if (isValid()) {
            jclass canvasView = _env->GetObjectClass(_cvsView);
            jmethodID drawrect = _env->GetMethodID(canvasView, "DrawRect",
                                                   "(Landroid/graphics/Canvas;IIIIIFFFF)V");
            _env->CallVoidMethod(_cvsView, drawrect, _cvs, (int) color.a, (int) color.r,
                                 (int) color.g, (int) color.b, thickness,
                                 rect.x, rect.y, rect.width, rect.height);
        }
    }
    */
    
    void DrawRect(Color color, float thickness, FVector2D start, FVector2D end) {
        if (isValid()) {
            _env->CallVoidMethod(_cvsView, drawrect, _cvs, (int) color.a, (int) color.r,
                                 (int) color.g, (int) color.b,
                                 thickness,
                                 start.X, start.Y, start.X + end.X, start.Y + end.Y);
        }
    }
    /*
    void DrawRect(Color color, float thickness, FVector2D start, FVector2D end) {
        if (isValid()) {
            jclass canvasView = _env->GetObjectClass(_cvsView);
            jmethodID drawrect = _env->GetMethodID(canvasView, "NRG_DrawRect",
                                                   "(Landroid/graphics/Canvas;IIIIIFFFF)V");
            _env->CallVoidMethod(_cvsView, drawrect, _cvs, (int) color.a, (int) color.r,
                                 (int) color.g, (int) color.b,
                                 thickness,
                                 start.X, start.Y, end.X, end.Y);
        }
    }
    */
void DrawBox4Line(int x, int y, int w, int h, Color color, float thickness) {
 int iw = w / 4;
 int ih = h / 4;
 // top
 DrawLine2(color,thickness,FVector2D(x, y),FVector2D(x + iw, y));
 DrawLine2(color,thickness,FVector2D(x + w - iw, y),FVector2D(x + w, y));  
 DrawLine2(color,thickness,FVector2D(x, y),FVector2D(x, y + ih));  
 DrawLine2(color,thickness,FVector2D(x + w - 1, y),FVector2D(x + w - 1, y + ih)); 
              // bottom
 DrawLine2(color,thickness,FVector2D(x, y + h),FVector2D(x + iw, y + h)); 
 DrawLine2(color,thickness,FVector2D(x + w - iw, y + h),FVector2D(x + w, y + h));
 DrawLine2(color,thickness,FVector2D(x, y + h - ih), FVector2D(x, y + h));
 DrawLine2(color,thickness,FVector2D(x + w - 1, y + h - ih), FVector2D(x + w - 1, y + h));
}
                
	
	




static jstring wcstojstr(JNIEnv *env, const wchar_t *input) {
        jobject bb = env->NewDirectByteBuffer((void *)input, wcslen(input) * sizeof(wchar_t));
        jstring UTF32LE = env->NewStringUTF("UTF-32LE");

        jclass charsetClass = env->FindClass("java/nio/charset/Charset");
        jmethodID forNameMethod = env->GetStaticMethodID(charsetClass, "forName", "(Ljava/lang/String;)Ljava/nio/charset/Charset;");
        jobject charset = env->CallStaticObjectMethod(charsetClass, forNameMethod, UTF32LE);

        jmethodID decodeMethod = env->GetMethodID(charsetClass, "decode", "(Ljava/nio/ByteBuffer;)Ljava/nio/CharBuffer;");
        jobject cb = env->CallObjectMethod(charset, decodeMethod, bb);

        jclass charBufferClass = env->FindClass("java/nio/CharBuffer");
        jmethodID toStringMethod = env->GetMethodID(charBufferClass, "toString", "()Ljava/lang/String;");
        jstring ret = (jstring)env->CallObjectMethod(cb, toStringMethod);

        env->DeleteLocalRef(bb);
        env->DeleteLocalRef(UTF32LE);
        env->DeleteLocalRef(charsetClass);
        env->DeleteLocalRef(charset);
        env->DeleteLocalRef(cb);
        env->DeleteLocalRef(charBufferClass);

        return ret;
    }
    
    
 


	



};


#endif //DESI_ESP_IMPORTANT_ESP_H
