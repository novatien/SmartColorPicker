package com.nvt.color;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class ColorPickerDialog {

    public interface OnColorPickerListener {

        void onCancel(ColorPickerDialog dialog);

        void onOk(ColorPickerDialog dialog, int color);
    }

    final Dialog dialog;
    private final boolean isAlpha;
    final OnColorPickerListener listener;
    final View viewHue;
    final ColorPickerSquare viewSatVal;
    final ImageView viewCursor;
    final ImageView viewAlphaCursor;
    final View viewOldColor;
    final View viewNewColor;
    final View viewAlphaOverlay;
    final ImageView viewTarget;
    final ImageView viewAlphaCheckered;
    final ViewGroup viewContainerSat;
    final ViewGroup viewContainerHue;
    final ViewGroup viewContainerAlpha;
    final float[] currentColorHsv = new float[3];
    int alpha;

    public ColorPickerDialog(final Context context, int color, boolean isAlpha, OnColorPickerListener listener) {
        this.isAlpha = isAlpha;
        this.listener = listener;

        if (!isAlpha) { // remove alpha if not supported
            color = color | 0xff000000;
        }

        Color.colorToHSV(color, currentColorHsv);
        alpha = Color.alpha(color);

        final View view = LayoutInflater.from(context).inflate(R.layout.color_picker_dialog, null);
        viewHue = view.findViewById(R.id.viewHue);
        viewSatVal = (ColorPickerSquare) view.findViewById(R.id.viewSatVal);
        viewCursor = (ImageView) view.findViewById(R.id.viewCursor);
        viewOldColor = view.findViewById(R.id.viewOldColor);
        viewNewColor = view.findViewById(R.id.viewNewColor);
        viewTarget = (ImageView) view.findViewById(R.id.viewTarget);
        viewContainerAlpha = (ViewGroup) view.findViewById(R.id.viewContainerAlpha);
        viewContainerSat = (ViewGroup) view.findViewById(R.id.viewContainerSat);
        viewContainerHue = (ViewGroup) view.findViewById(R.id.viewContainerHue);
        viewAlphaOverlay = view.findViewById(R.id.viewAlphaOverlay);
        viewAlphaCursor = (ImageView) view.findViewById(R.id.viewAlphaCursor);
        viewAlphaCheckered = (ImageView) view.findViewById(R.id.viewAlphaCheckered);

        { // hide/show alpha
            viewAlphaOverlay.setVisibility(isAlpha ? View.VISIBLE : View.GONE);
            viewAlphaCursor.setVisibility(isAlpha ? View.VISIBLE : View.GONE);
            viewAlphaCheckered.setVisibility(isAlpha ? View.VISIBLE : View.GONE);
        }

        viewSatVal.setHue(getHue());
        viewOldColor.setBackgroundColor(color);
        viewNewColor.setBackgroundColor(color);

        viewHue.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_MOVE
                    || event.getAction() == MotionEvent.ACTION_DOWN
                    || event.getAction() == MotionEvent.ACTION_UP) {

                float y = event.getY();
                if (y < 0.f) y = 0.f;
                if (y > viewHue.getMeasuredHeight()) {
                    y = viewHue.getMeasuredHeight() - 0.001f; // to avoid jumping the cursor from bottom to top.
                }
                float hue = 360.f - 360.f / viewHue.getMeasuredHeight() * y;
                if (hue == 360.f) hue = 0.f;
                setHue(hue);

                // update view
                viewSatVal.setHue(getHue());
                moveCursor();
                viewNewColor.setBackgroundColor(getColor());
                updateAlphaView();
                return true;
            }
            return false;
        });

        if (isAlpha) viewAlphaCheckered.setOnTouchListener((v, event) -> {
            if ((event.getAction() == MotionEvent.ACTION_MOVE)
                    || (event.getAction() == MotionEvent.ACTION_DOWN)
                    || (event.getAction() == MotionEvent.ACTION_UP)) {

                float y = event.getY();
                if (y < 0.f) {
                    y = 0.f;
                }
                if (y > viewAlphaCheckered.getMeasuredHeight()) {
                    y = viewAlphaCheckered.getMeasuredHeight() - 0.001f; // to avoid jumping the cursor from bottom to top.
                }
                final int a = Math.round(255.f - ((255.f / viewAlphaCheckered.getMeasuredHeight()) * y));
                ColorPickerDialog.this.setAlpha(a);

                // update view
                moveAlphaCursor();
                int col = ColorPickerDialog.this.getColor();
                int c = a << 24 | col & 0x00ffffff;
                viewNewColor.setBackgroundColor(c);
                return true;
            }
            return false;
        });
        viewSatVal.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_MOVE
                    || event.getAction() == MotionEvent.ACTION_DOWN
                    || event.getAction() == MotionEvent.ACTION_UP) {

                float x = event.getX(); // touch event are in dp units.
                float y = event.getY();

                if (x < 0.f) x = 0.f;
                if (x > viewSatVal.getMeasuredWidth()) x = viewSatVal.getMeasuredWidth();
                if (y < 0.f) y = 0.f;
                if (y > viewSatVal.getMeasuredHeight()) y = viewSatVal.getMeasuredHeight();

                setSat(1.f / viewSatVal.getMeasuredWidth() * x);
                setVal(1.f - (1.f / viewSatVal.getMeasuredHeight() * y));

                // update view
                moveTarget();
                viewNewColor.setBackgroundColor(getColor());

                return true;
            }
            return false;
        });

        dialog = new Dialog(context);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setContentView(view);
        view.findViewById(R.id.btnOK).setOnClickListener(v -> {
            if (ColorPickerDialog.this.listener != null) {
                ColorPickerDialog.this.listener.onOk(ColorPickerDialog.this, getColor());
            }
            dialog.dismiss();
        });
        view.findViewById(R.id.btnCancel).setOnClickListener(v -> {
            if (ColorPickerDialog.this.listener != null) {
                ColorPickerDialog.this.listener.onCancel(ColorPickerDialog.this);
            }
            dialog.dismiss();
        });

        // move cursor & target on first draw
        ViewTreeObserver vto = view.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                moveCursor();
                if (ColorPickerDialog.this.isAlpha) moveAlphaCursor();
                moveTarget();
                if (ColorPickerDialog.this.isAlpha) updateAlphaView();
                view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });
    }

    protected void moveCursor() {
        float y = viewHue.getMeasuredHeight() - (getHue() * viewHue.getMeasuredHeight() / 360.f);
        if (y == viewHue.getMeasuredHeight()) y = 0.f;
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) viewCursor.getLayoutParams();
        layoutParams.leftMargin = (int) (viewHue.getLeft() - Math.floor(viewCursor.getMeasuredWidth() >> 1) - viewContainerHue.getPaddingLeft());
        layoutParams.topMargin = (int) (viewHue.getTop() + y - Math.floor(viewCursor.getMeasuredHeight() >> 1) - viewContainerHue.getPaddingTop());
        viewCursor.setLayoutParams(layoutParams);
    }

    protected void moveTarget() {
        float x = getSat() * viewSatVal.getMeasuredWidth();
        float y = (1.f - getVal()) * viewSatVal.getMeasuredHeight();
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) viewTarget.getLayoutParams();
        layoutParams.leftMargin = (int) (viewSatVal.getLeft() + x - Math.floor(viewTarget.getMeasuredWidth() >> 1) - viewContainerSat.getPaddingLeft());
        layoutParams.topMargin = (int) (viewSatVal.getTop() + y - Math.floor(viewTarget.getMeasuredHeight() >> 1) - viewContainerSat.getPaddingTop());
        viewTarget.setLayoutParams(layoutParams);
    }

    protected void moveAlphaCursor() {
        final int measuredHeight = this.viewAlphaCheckered.getMeasuredHeight();
        float y = measuredHeight - ((this.getAlpha() * measuredHeight) / 255.f);
        final RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this.viewAlphaCursor.getLayoutParams();
        layoutParams.leftMargin = (int) (this.viewAlphaCheckered.getLeft() - Math.floor(this.viewAlphaCursor.getMeasuredWidth() >> 1) - this.viewContainerAlpha.getPaddingLeft());
        layoutParams.topMargin = (int) ((this.viewAlphaCheckered.getTop() + y) - Math.floor(this.viewAlphaCursor.getMeasuredHeight() >> 1) - this.viewContainerAlpha.getPaddingTop());

        this.viewAlphaCursor.setLayoutParams(layoutParams);
    }

    private int getColor() {
        final int argb = Color.HSVToColor(currentColorHsv);
        return alpha << 24 | (argb & 0x00ffffff);
    }

    private float getHue() {
        return currentColorHsv[0];
    }

    private float getAlpha() {
        return this.alpha;
    }

    private float getSat() {
        return currentColorHsv[1];
    }

    private float getVal() {
        return currentColorHsv[2];
    }

    private void setHue(float hue) {
        currentColorHsv[0] = hue;
    }

    private void setSat(float sat) {
        currentColorHsv[1] = sat;
    }

    private void setAlpha(int alpha) {
        this.alpha = alpha;
    }

    private void setVal(float val) {
        currentColorHsv[2] = val;
    }

    public void show() {
        dialog.show();
    }

    private void updateAlphaView() {
        final GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{
                Color.HSVToColor(currentColorHsv), 0x0
        });
        viewAlphaOverlay.setBackgroundDrawable(gd);
    }
}
