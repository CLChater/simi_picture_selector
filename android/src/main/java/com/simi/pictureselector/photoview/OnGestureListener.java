
package com.simi.pictureselector.photoview;

interface OnGestureListener {

    void onDrag(float dx, float dy);

    void onFling(float startX, float startY, float velocityX,
                 float velocityY);

    void onScale(float scaleFactor, float focusX, float focusY);

    void onScale(float scaleFactor, float focusX, float focusY, float dx, float dy);
}