package com.bearmod;

import android.annotation.SuppressLint;
import android.os.Build;
import android.text.TextUtils;
import android.view.WindowManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;

import android.view.View;
import android.content.Context;
import android.util.Log;

public class RecorderFakeUtils {

    private static final String ROM_FLYME = "Flyme";
    private static final String ROM_MIUI = "MIUI";
    private static final String ROM_BLACKSHARK = "Blackshark";
    private static final String ROM_ONEPLUS = "OnePlus";
    private static final String ROM_ROG = "ROG";
    private static final String ROM_EMUI = "EMUI";
    private static final String ROM_OPPO = "OPPO";
    private static final String ROM_SMARTISAN = "SMARTISAN";
    private static final String ROM_VIVO = "VIVO";
    private static final String ROM_NUBIAUI = "NUBIAUI";
    private static final String ROM_SAMSUNG = "ONEUI";

    private static final String KEY_VERSION_MIUI = "ro.miui.ui.version.name";
    private static final String KEY_VERSION_EMUI = "ro.build.version.emui";
    private static final String KEY_VERSION_OPPO = "ro.build.version.opporom";
    private static final String KEY_VERSION_SMARTISAN = "ro.smartisan.version";
    private static final String KEY_VERSION_VIVO = "ro.vivo.os.version";
    private static final String KEY_VERSION_NUBIA = "ro.build.nubia.rom.name";
    private static final String KEY_VERSION_ONEPLIS = "ro.build.ota.versionname";
    private static final String KEY_VERSION_SAMSUNG = "ro.channel.officehubrow";
    private static final String KEY_VERSION_BLACKSHARK = "ro.blackshark.rom";
    private static final String KEY_VERSION_ROG = "ro.build.fota.version";
    private static String sName;
    private static int flagValue;

    //华为
    public static boolean isEmui() {
        return check(ROM_EMUI);
    }

    //小米
    public static boolean isMiui() {
        return check(ROM_MIUI);
    }

    //vivo
    public static boolean isVivo() {
        return check(ROM_VIVO);
    }

    //oppo
    public static boolean isOppo() {
        return check(ROM_OPPO);
    }

    //魅族
    public static boolean isFlyme() {
        return check(ROM_FLYME);
    }

    //红魔
    public static boolean isNubia() {
        return check(ROM_NUBIAUI);
    }

    //一加
    public static boolean isOnePlus() {
        return check(ROM_ONEPLUS);
    }

    //三星
    public static boolean isSanSung() {
        return check(ROM_SAMSUNG);
    }

    //黑鲨
    public static boolean isBLACKSHARK() {
        return check(ROM_BLACKSHARK);
    }

    //ROG
    // public static boolean isRog() {
    //   return check(ROM_ROG);
    // }

    public static boolean isActivice() { return false;}


    private static WindowManager.LayoutParams sMainLayoutParams;
    private static WindowManager.LayoutParams sIconLayoutParams;
    private static WindowManager.LayoutParams sCanvasLayoutParams;
    private static WindowManager sWindowManager;
    private static View sMainView;
    private static View sIconView;
    private static View sCanvasView;
    private static Context sContext;




    public static void setFakeRecorderWindowLayoutParams(WindowManager.LayoutParams mainViewLayoutParams, WindowManager.LayoutParams iconViewLayoutParams, WindowManager.LayoutParams canvasViewLayoutParams, WindowManager manager, View mainView, View iconView, View canvasView, Context context) {
        sMainLayoutParams = mainViewLayoutParams;
        sIconLayoutParams = iconViewLayoutParams;
        sWindowManager = manager;
        sCanvasLayoutParams = canvasViewLayoutParams;
        sMainView = mainView;
        sIconView = iconView;
        sCanvasView = canvasView;
        sContext = context;

        try {
            sMainLayoutParams.setTitle(RecorderFakeUtils.getFakeRecordWindowTitle());
            if (check(ROM_FLYME)) {
                if (setMeizuParams(sMainLayoutParams)) {
                    if (isActivice()) {
                        setMeizuParams_new(sMainLayoutParams); //最新魅族
                    }
                }
            } else if (check(ROM_MIUI) || check(ROM_BLACKSHARK)) {
                setXiaomiParams(sMainLayoutParams);
                //  setXiaomiParams(sMainLayoutParams);
            } else if (check(ROM_ONEPLUS) && (isActivice() || Build.VERSION.SDK_INT == 35)) {
                @SuppressLint("SoonBlockedPrivateApi") Field cornersOverlayFlag = sMainLayoutParams.getClass().getDeclaredField("PRIVATE_FLAG_IS_ROUNDED_CORNERS_OVERLAY");
                cornersOverlayFlag.setAccessible(true);
                setOnePlusParams(sMainLayoutParams, (int) cornersOverlayFlag.get(sMainLayoutParams.getClass()));
            } else if (isSanSung()) {
                setSamsungFlags(sMainLayoutParams);
            } else if (check(ROM_ROG)) {
                //  sMainLayoutParams.memoryType |= 0x10000000;
            }

            sIconLayoutParams.setTitle(RecorderFakeUtils.getFakeRecordWindowTitle());
            if (check(ROM_FLYME)) {
                if (setMeizuParams(sIconLayoutParams)) {
                    if (isActivice()) {
                        setMeizuParams_new(sIconLayoutParams); //最新魅族
                    }
                }
            } else if (check(ROM_MIUI) || check(ROM_BLACKSHARK)) {
                setXiaomiParams(sIconLayoutParams);
            } else if (check(ROM_ONEPLUS) && (isActivice() || Build.VERSION.SDK_INT == 35)) {
                @SuppressLint("SoonBlockedPrivateApi") Field cornersOverlayFlag = sIconLayoutParams.getClass().getDeclaredField("PRIVATE_FLAG_IS_ROUNDED_CORNERS_OVERLAY");
                cornersOverlayFlag.setAccessible(true);
                setOnePlusParams(sIconLayoutParams, (int) cornersOverlayFlag.get(sIconLayoutParams.getClass()));
            } else if (isSanSung()) {
                setSamsungFlags(sIconLayoutParams);
            } else if (check(ROM_ROG)) {
                // sIconLayoutParams.memoryType |= 0x10000000;
            }

            sCanvasLayoutParams.setTitle(RecorderFakeUtils.getFakeRecordWindowTitle());
            if (check(ROM_FLYME)) {
                if (setMeizuParams(sCanvasLayoutParams)) {
                    if (isActivice()) {
                        setMeizuParams_new(sCanvasLayoutParams); //最新魅族
                    }
                }
            } else if (check(ROM_MIUI) || check(ROM_BLACKSHARK)) {
                setXiaomiParams(sCanvasLayoutParams);
            } else if (check(ROM_ONEPLUS) && (isActivice() || Build.VERSION.SDK_INT == 35)) {
                @SuppressLint("SoonBlockedPrivateApi") Field cornersOverlayFlag = sCanvasLayoutParams.getClass().getDeclaredField("PRIVATE_FLAG_IS_ROUNDED_CORNERS_OVERLAY");
                cornersOverlayFlag.setAccessible(true);
                setOnePlusParams(sCanvasLayoutParams, (int) cornersOverlayFlag.get(sCanvasLayoutParams.getClass()));
            } else if (isSanSung()) {
                setSamsungFlags(sCanvasLayoutParams);
            } else if (check(ROM_ROG)) {
                //  sCanvasLayoutParams.memoryType |= 0x10000000;
            }
        }catch (Exception exception){
            exception.printStackTrace();
        }



        updateViewLayout(sMainView, sMainLayoutParams);
        updateViewLayout(sIconView, sIconLayoutParams);
        updateViewLayout(sCanvasView, sCanvasLayoutParams);
        //Toast.makeText(sContext, "Fake Recorder Window Layout Params set", Toast.LENGTH_SHORT).show();
    }

    public static void unsetFakeRecorderWindowLayoutParams(WindowManager.LayoutParams mainParams, WindowManager.LayoutParams iconParams,WindowManager.LayoutParams canvasParams, WindowManager windowManager, View mainView, View iconView, View canvasView, Context context) {
        sMainLayoutParams = mainParams;
        sIconLayoutParams = iconParams;
        sWindowManager = windowManager;
        sCanvasLayoutParams = canvasParams;

        sMainView = mainView;
        sIconView = iconView;
        sCanvasView = canvasView;
        sContext = context;

        try {

            sMainLayoutParams.setTitle(RecorderFakeUtils.getFakeRecordWindowTitle());
            if (check(ROM_FLYME)) {
                if (unsetMeizuParams(sMainLayoutParams)) {
                    if (isActivice()) {
                        unsetMeizuParams_new(sMainLayoutParams); //最新魅族
                    }
                }
            } else if (check(ROM_MIUI) || check(ROM_BLACKSHARK)) {
                unsetXiaomiParams(sMainLayoutParams, 6666);
            } else if (check(ROM_ONEPLUS) && (isActivice() || Build.VERSION.SDK_INT == 35)) {
                @SuppressLint("SoonBlockedPrivateApi") Field privateflagField = sMainLayoutParams.getClass().getDeclaredField("PRIVATE_FLAG_IS_ROUNDED_CORNERS_OVERLAY");
                privateflagField.setAccessible(true);
                unsetOnePlusParams(sMainLayoutParams, (int) privateflagField.get(sMainLayoutParams.getClass()));
            } else if (isSanSung()) {
                unsetSamsungFlags(sMainLayoutParams);
            } else if (check(ROM_ROG)) {
                // sMainLayoutParams.memoryType &= ~0x10000000;
            } else if (check(ROM_EMUI)) {
                unsetHuaweiParams(sMainLayoutParams);
            } else if (check(ROM_OPPO)) {
                unsetOppoParams(sMainLayoutParams);
            } else if (check(ROM_VIVO)) {
                unsetVivoParams(sMainLayoutParams);
            }

            sCanvasLayoutParams.setTitle(RecorderFakeUtils.getFakeRecordWindowTitle());
            if (check(ROM_FLYME)) {
                if (unsetMeizuParams(sCanvasLayoutParams)) {
                    if (isActivice()) {
                        unsetMeizuParams_new(sCanvasLayoutParams); //最新魅族
                    }
                }
            } else if (check(ROM_MIUI) || check(ROM_BLACKSHARK)) {
                unsetXiaomiParams(sCanvasLayoutParams, 6666);
            } else if (check(ROM_ONEPLUS) && (isActivice() || Build.VERSION.SDK_INT == 35)) {
                @SuppressLint("SoonBlockedPrivateApi") Field privateflagField = sCanvasLayoutParams.getClass().getDeclaredField("PRIVATE_FLAG_IS_ROUNDED_CORNERS_OVERLAY");
                privateflagField.setAccessible(true);
                unsetOnePlusParams(sCanvasLayoutParams, (int) privateflagField.get(sCanvasLayoutParams.getClass()));
            } else if (isSanSung()) {
                unsetSamsungFlags(sCanvasLayoutParams);
            } else if (check(ROM_ROG)) {
                //  sCanvasLayoutParams.memoryType &= ~0x10000000;
            } else if (check(ROM_EMUI)) {
                unsetHuaweiParams(sCanvasLayoutParams);
            } else if (check(ROM_OPPO)) {
                unsetOppoParams(sCanvasLayoutParams);
            } else if (check(ROM_VIVO)) {
                unsetVivoParams(sCanvasLayoutParams);
            }

            sIconLayoutParams.setTitle(RecorderFakeUtils.getFakeRecordWindowTitle());
            if (check(ROM_FLYME)) {
                if (unsetMeizuParams(sIconLayoutParams)) {
                    if (isActivice()) {
                        unsetMeizuParams_new(sIconLayoutParams); //最新魅族
                    }
                }
            } else if (check(ROM_MIUI) || check(ROM_BLACKSHARK)) {
                unsetXiaomiParams(sIconLayoutParams, 6666);
            } else if (check(ROM_ONEPLUS) && (isActivice() || Build.VERSION.SDK_INT == 35)) {
                @SuppressLint("SoonBlockedPrivateApi") Field privateflagField = sIconLayoutParams.getClass().getDeclaredField("PRIVATE_FLAG_IS_ROUNDED_CORNERS_OVERLAY");
                privateflagField.setAccessible(true);
                unsetOnePlusParams(sIconLayoutParams, (int) privateflagField.get(sIconLayoutParams.getClass()));
            } else if (isSanSung()) {
                unsetSamsungFlags(sIconLayoutParams);
            } else if (check(ROM_ROG)) {
                // sIconLayoutParams.memoryType &= ~0x10000000;
            } else if (check(ROM_EMUI)) {
                unsetHuaweiParams(sIconLayoutParams);
            } else if (check(ROM_OPPO)) {
                unsetOppoParams(sIconLayoutParams);
            } else if (check(ROM_VIVO)) {
                unsetVivoParams(sIconLayoutParams);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        updateViewLayout(sMainView, sMainLayoutParams);
        updateViewLayout(sIconView, sIconLayoutParams);
        updateViewLayout(sCanvasView, sCanvasLayoutParams);
        //Toast.makeText(sContext, "Fake Recorder Window Layout Params removed", Toast.LENGTH_SHORT).show();
    }

    private static void updateViewLayout(View view, WindowManager.LayoutParams layoutParams) {
        if (sWindowManager != null && view != null) {
            sWindowManager.removeView(view); // Remove the old view
            sWindowManager.addView(view, layoutParams); // Add it again with updated parameters
        }
    }

    private static void setXiaomiParams(WindowManager.LayoutParams params) {
        RecorderFakeUtils.flagValue = 6666;
        try {
            // FLAG_DITHER is deprecated and not needed for overlays on modern Android
            // params.flags = params.flags | WindowManager.LayoutParams.FLAG_DITHER;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static void unsetXiaomiParams(WindowManager.LayoutParams params, int flagValue) {
        try {
            // FLAG_DITHER is deprecated and not needed for overlays on modern Android
            // params.flags = params.flags & ~WindowManager.LayoutParams.FLAG_DITHER;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void unsetHuaweiParams(WindowManager.LayoutParams params) {
        // Example: remove deprecated flag
        try {
            // params.flags &= ~WindowManager.LayoutParams.FLAG_DITHER;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static void unsetOppoParams(WindowManager.LayoutParams params) {
        try {
            // params.flags &= ~WindowManager.LayoutParams.FLAG_DITHER;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static void unsetVivoParams(WindowManager.LayoutParams params) {
        try {
            // params.flags &= ~WindowManager.LayoutParams.FLAG_DITHER;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @SuppressLint("PrivateApi")
    private static boolean setMeizuParams(WindowManager.LayoutParams params) {
        try {
            Class<?> MeizuParamsClass = Class.forName("android.view.MeizuLayoutParams");
            Field flagField = MeizuParamsClass.getDeclaredField("flags");
            flagField.setAccessible(true);
            Object MeizuParams = MeizuParamsClass.getDeclaredConstructor().newInstance(); // Modern replacement for newInstance()
            flagField.setInt(MeizuParams, 8192);
            Field mzParamsField = params.getClass().getField("meizuParams");
            mzParamsField.set(params, MeizuParams);
            return false;
        } catch (IllegalAccessException | ClassNotFoundException | NoSuchFieldException | InstantiationException | java.lang.reflect.InvocationTargetException | NoSuchMethodException e) {
            return true;
        }
    }
    @SuppressLint("PrivateApi")
    private static boolean unsetMeizuParams(WindowManager.LayoutParams params) {
        try {
            Class<?> MeizuParamsClass = Class.forName("android.view.MeizuLayoutParams");
            Field flagField = MeizuParamsClass.getDeclaredField("flags");
            flagField.setAccessible(true);
            Object MeizuParams = MeizuParamsClass.getDeclaredConstructor().newInstance(); // Modern replacement for newInstance()
            int currentFlags = flagField.getInt(MeizuParams);
            // Remove flag
            flagField.setInt(MeizuParams, currentFlags & ~8192);
            Field mzParamsField = params.getClass().getField("meizuParams");
            mzParamsField.set(params, MeizuParams);
            return false;
        } catch (IllegalAccessException | ClassNotFoundException | NoSuchFieldException | InstantiationException | java.lang.reflect.InvocationTargetException | NoSuchMethodException e) {
            return true;
        }
    }

    private static void setMeizuParams_new(WindowManager.LayoutParams params) {
        try {
            Field mzParamsField = params.getClass().getDeclaredField("meizuFlags");
            mzParamsField.setAccessible(true);
            mzParamsField.setInt(params, 1024);
        } catch (Exception e) {
        }
    }

    private static void unsetMeizuParams_new(WindowManager.LayoutParams params) {
        try {
            Field mzParamsField = params.getClass().getDeclaredField("meizuFlags");
            mzParamsField.setAccessible(true);
            int currentFlags = mzParamsField.getInt(params);
            // Удаление флага
            mzParamsField.setInt(params, currentFlags & ~1024);
        } catch (Exception e) {
        }
    }

    private static void setOnePlusParams(WindowManager.LayoutParams params, int flagValue) {
        try {
            Field flagField = params.getClass().getDeclaredField("privateFlags");
            flagField.setAccessible(true);
            flagField.set(params, flagValue);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static void unsetOnePlusParams(WindowManager.LayoutParams params, int flagValue) {
        try {
            Field flagField = params.getClass().getDeclaredField("privateFlags");
            flagField.setAccessible(true);
            int currentFlags = flagField.getInt(params);
            // Удаление флага
            flagField.set(params, currentFlags & ~flagValue);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void setSamsungFlags(WindowManager.LayoutParams params) {
        try {
            Method semAddExtensionFlags = params.getClass().getMethod("semAddExtensionFlags", Integer.TYPE);
            Method semAddPrivateFlags = params.getClass().getMethod("semAddPrivateFlags", Integer.TYPE);
            semAddExtensionFlags.invoke(params, -2147352576);
            semAddPrivateFlags.invoke(params, params.flags);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static void unsetSamsungFlags(WindowManager.LayoutParams params) {
        try {
            Method semRemoveExtensionFlags = params.getClass().getMethod("semRemoveExtensionFlags", Integer.TYPE);
            Method semRemovePrivateFlags = params.getClass().getMethod("semRemovePrivateFlags", Integer.TYPE);
            semRemoveExtensionFlags.invoke(params, -2147352576);
            semRemovePrivateFlags.invoke(params, params.flags);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    /**
     * 总结下可以操作的是 华为，魅族，OPPO，VIVO，红魔，小米，一加 ORG
     *
     * @return
     */
    private static String getFakeRecordWindowTitle() {
        if (sName == null) {
            check("");
        }
        if (sName == null) {
            return "";
        }
        return switch (sName) {
            case ROM_MIUI -> "com.miui.screenrecorder";
            case ROM_EMUI -> "ScreenRecoderTimer";
            case ROM_OPPO -> "com.coloros.screenrecorder.FloatView";
            case ROM_VIVO -> "screen_record_menu";
            case ROM_ONEPLUS -> "op_screenrecord";
            case ROM_FLYME -> "SysScreenRecorder";
            case ROM_NUBIAUI -> "NubiaScreenDecorOverlay";
            case ROM_BLACKSHARK -> "com.blackshark.screenrecorder";
            case ROM_ROG -> "com.asus.force.layer.transparent.SR.floatingpanel";
            default -> "";
        };
    }

    public static boolean check(String rom) {
        if (sName != null) {
            return sName.equals(rom);
        }

        if (!TextUtils.isEmpty(getProp(KEY_VERSION_MIUI))) {
            sName = ROM_MIUI;
        } else if (!TextUtils.isEmpty(getProp(KEY_VERSION_BLACKSHARK))) {
            sName = ROM_BLACKSHARK;
        } else if (!TextUtils.isEmpty(getProp(KEY_VERSION_EMUI))) {
            sName = ROM_EMUI;
        } else if (!TextUtils.isEmpty(getProp(KEY_VERSION_OPPO))) {
            sName = ROM_OPPO;
        } else if (!TextUtils.isEmpty(getProp(KEY_VERSION_VIVO))) {
            sName = ROM_VIVO;
        } else if (!TextUtils.isEmpty(getProp(KEY_VERSION_SMARTISAN))) {
            sName = ROM_SMARTISAN;
        } else if (!TextUtils.isEmpty(getProp(KEY_VERSION_NUBIA))) {
            sName = ROM_NUBIAUI;
        } else if (!TextUtils.isEmpty(getProp(KEY_VERSION_ONEPLIS)) && Objects.requireNonNull(getProp(KEY_VERSION_ONEPLIS)).toLowerCase().contains("hydrogen")) {
            sName = ROM_ONEPLUS;
        } else if (!TextUtils.isEmpty(getProp(KEY_VERSION_ROG)) && Objects.requireNonNull(getProp(KEY_VERSION_ROG)).toLowerCase().contains("CN_Phone")) {
            sName = ROM_ROG;
        } else if (!TextUtils.isEmpty(getProp(KEY_VERSION_SAMSUNG))) {
            sName = ROM_SAMSUNG;
        } else {
            String sVersion = Build.DISPLAY;
            if (sVersion.toUpperCase().contains(ROM_FLYME)) {
                sName = ROM_FLYME;
            } else {
                sName = Build.MANUFACTURER.toUpperCase();
            }
        }
        return sName.equals(rom);
    }

    private static String getProp(String name) {
        String line = null;
        BufferedReader input = null;
        try {
            Process p = Runtime.getRuntime().exec("getprop " + name);
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            input.close();
        } catch (IOException ex) {
            return null;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return line;
    }

    private static void safeUpdateView(View view, WindowManager.LayoutParams params) {
        params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;

        try {
            sWindowManager.updateViewLayout(view, params);
        } catch (IllegalArgumentException e) {
            Log.e("WindowUtils", "Error updating view layout", e);
            // Handle stale view references
            sWindowManager.removeView(view);
            try {
                sWindowManager.addView(view, params);
            } catch (Exception ex) {
                Log.e("WindowUtils", "Failed to re-add view", ex);
            }
        }
    }

}
