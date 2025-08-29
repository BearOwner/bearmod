package com.bearmod.loader.utilities;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import androidx.annotation.ColorRes;
import androidx.annotation.DimenRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

/**
 * ResourceProvider - Clean abstraction for accessing Android resources
 *
 * Provides a testable, dependency-injected way to access Android resources
 * without requiring direct Context coupling in business logic components.
 */
public interface ResourceProvider {
    Context app();                 // application Context only
    Context getContext();          // alias for app()
    Resources res();
    String str(@StringRes int id, Object... args);
    int color(@ColorRes int id);
    float dimen(@DimenRes int id);
    Drawable drawable(@DrawableRes int id);

    // String-based resource lookups
    int id(String name);
    int color(String name);
    int drawable(String name);

    static ResourceProvider from(Context context) {
        final Context app = context.getApplicationContext();
        return new ResourceProvider() {
            @Override public Context app() { return app; }
            @Override public Context getContext() { return app; }
            @Override public Resources res() { return app.getResources(); }
            @Override public String str(int id, Object... args) { return app.getString(id, args); }
            @Override public int color(int id) { return ContextCompat.getColor(app, id); }
            @Override public float dimen(int id) { return app.getResources().getDimension(id); }
            @Override public Drawable drawable(int id) { return ContextCompat.getDrawable(app, id); }

            @Override public int id(String name) {
                return app.getResources().getIdentifier(name, "id", app.getPackageName());
            }

            @Override public int color(String name) {
                int id = app.getResources().getIdentifier(name, "color", app.getPackageName());
                return id != 0 ? ContextCompat.getColor(app, id) : 0;
            }

            @Override public int drawable(String name) {
                return app.getResources().getIdentifier(name, "drawable", app.getPackageName());
            }
        };
    }
}