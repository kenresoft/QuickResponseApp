package com.kixfobby.security.camera;

import android.app.Activity;

interface InternalPhotographer extends Photographer {

    void initWithViewfinder(Activity activity, CameraView preview);
}
