package com.dji.ui.sample;

import android.content.Context;
import android.support.annotation.IntRange;
import android.support.annotation.Keep;
import android.support.annotation.MainThread;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import dji.keysdk.AirLinkKey;
import dji.keysdk.DJIKey;
import dji.ui.d.a;
import android.widget.FrameLayout;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.FrameLayout;
import android.widget.TextView;
import dji.thirdparty.rx.subscriptions.CompositeSubscription;
/**
 * Created by zhaoyong on 11/13/17.
 * 这个Widget主要用于显示与RTMapper的PC端连接的状态，点击Widget显示IP地址和端口信息等其他转发相关状态
 */

public class RTMapperTransferWidget extends FrameLayout {
    private ImageView hdSignal;
    private int signalValue;
    private int currentHDSignalResId;
    private AirLinkKey videoSignalStrengthKey;
    private AirLinkKey ocuVideoSignalStrengthKey;

    public RTMapperTransferWidget(Context var1, AttributeSet var2) {
        super(var1, var2, 0);
        a.b();
        initView(var1,var2,0);
    }

    public void initView(Context var1, AttributeSet var2, int var3) {
    }

    @MainThread
    @Keep
    public void onVideoSignalStrengthChange(@IntRange(from = 0L,to = 100L) int var1) {
        this.hdSignal.setImageResource(this.currentHDSignalResId);
    }

    private void updateResourceBasedOnValue(int var1) {
        if(var1 <= 0) {
            this.currentHDSignalResId = dji.ui.R.drawable.ic_topbar_signal_level_0;
        } else if(var1 > 0 && var1 <= 20) {
            this.currentHDSignalResId = dji.ui.R.drawable.ic_topbar_signal_level_1;
        } else if(var1 > 20 && var1 <= 40) {
            this.currentHDSignalResId = dji.ui.R.drawable.ic_topbar_signal_level_2;
        } else if(var1 > 40 && var1 <= 60) {
            this.currentHDSignalResId = dji.ui.R.drawable.ic_topbar_signal_level_3;
        } else if(var1 > 60 && var1 <= 80) {
            this.currentHDSignalResId = dji.ui.R.drawable.ic_topbar_signal_level_4;
        } else {
            this.currentHDSignalResId = dji.ui.R.drawable.ic_topbar_signal_level_5;
        }

    }

    public void transformValue(Object var1, DJIKey var2) {
        if(var2.equals(this.videoSignalStrengthKey)) {
            this.signalValue = ((Integer)var1).intValue();
            this.updateResourceBasedOnValue(this.signalValue);
        } else if(var2.equals(this.ocuVideoSignalStrengthKey)) {
            this.signalValue = ((Integer)var1).intValue();
            this.updateResourceBasedOnValue(this.signalValue);
        }

    }

    public void updateWidget(DJIKey var1) {
        this.onVideoSignalStrengthChange(this.signalValue);
    }
}
