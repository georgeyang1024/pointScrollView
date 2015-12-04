package me.georgeyang.pointscrollview;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;


/**
 * 可以缩放的地图view
 * Created by george.yang on 2015/12/1.
 */
public class PointScrollView extends ViewGroup {
    private static class MapInfo {
        //控件位置缩放信息
        int x, y;
        float scale = 1;
        //地图位置缩放信息
        float mapScale = 1;
        int mapZeroX,mapZeroY;
    }

    //json File:http://www.zivixgroup.com/dev/avayaapi/getAmenities.json
    //assets File:amenities.json
    private Bitmap map;
    private ImageView mapView;
    private MapInfo info = new MapInfo();

    public PointScrollView(Context context) {
        super(context);
        init(context);
    }

    public PointScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PointScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PointScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    @Override
    public void removeAllViews() {
        for (int i=0;i<getChildCount();i++) {
            View view = getChildAt(1);
            if (view!=mapView) {
                removeView(view);
            }
        }
    }

    public void drawPoint(int x, int y, @DrawableRes int resId, Object object) {
        ImageView pointView = new ImageView(getContext());
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lp.leftMargin = x;
        lp.topMargin = y;
        pointView.setLayoutParams(lp);
        pointView.setImageResource(resId);
        pointView.setTag(object);
        pointView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener!=null) {
                    mListener.onCick(v,v.getTag());
                }
            }
        });
        addView(pointView);
    }
    private OnPointClickListener mListener;
    public void setOnPointClickListener(OnPointClickListener onPointClickListener) {
        mListener = onPointClickListener;
    }

    public interface OnPointClickListener {
        void onCick(View pointView, Object point);
    }

    private void init(Context context) {
        setBackgroundColor(Color.WHITE);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int cCount = getChildCount();
        int cWidth = 0;
        int cHeight = 0;
        //遍历所有childView根据其宽和高，以及margin进行布局
        for (int i = 0; i < cCount; i++) {
            View childView = getChildAt(i);
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) childView.getLayoutParams();
            cWidth = childView.getMeasuredWidth();
            cHeight = childView.getMeasuredHeight();

            int startX,startY,endX,endY;
            if (i==0) {
                startX = info.x;
                startY = info.y;
                endX = (int) (info.x + cWidth * info.scale);
                endY = (int) (info.y + cHeight * info.scale);
            } else {
                startX = (int) (info.x + (lp.leftMargin*info.mapScale + info.mapZeroX)*info.scale - cWidth/2.0f);
                startY = (int) (info.y + (lp.topMargin*info.mapScale + info.mapZeroY)*info.scale - cHeight/2.0f);
                endX = startX+cWidth;
                endY = startY+cHeight;
            }

            childView.layout(startX,startY,endX,endY);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int sizeWidth = MeasureSpec.getSize(widthMeasureSpec);
        int sizeHeight = MeasureSpec.getSize(heightMeasureSpec);
        //必须是：EXACTLY,充满屏幕
        //        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        //        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(sizeWidth, sizeHeight);

        //TODO if size has change on your view,delete this line
        if (info.mapZeroX == 0 && info.mapZeroY==0) {
            float scaleH = getMeasuredHeight() * 1f / map.getHeight();
            float scaleW = getMeasuredWidth() * 1f / map.getWidth();
            float scale = Math.min(scaleH, scaleW);
            if (scale == scaleW) {
                //左右铺满
                info.mapZeroX = 0;
                info.mapZeroY = (int) (getMeasuredHeight()*0.5f-map.getHeight()*scale*0.5f);
            } else {
                //左右铺满
                info.mapZeroX = (int) (getMeasuredWidth()*0.5f-map.getWidth()*scale*0.5f);
                info.mapZeroY = 0;
            }
            info.mapScale = scale;
        }
    }

    private float[][] pointLastXY = new float[2][2];//[第n个手指][x,y]
    private boolean hasZoom;

    public boolean onTouchEvent(MotionEvent event) {
        int handIndex = event.getPointerId(event.getActionIndex());
        if (handIndex > 1) {
            return true;
        }
        int currPointerCount = event.getPointerCount();
        //move的时候，获取第几个手指永远都是1，呵呵
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                hasZoom = false;
            case MotionEvent.ACTION_POINTER_2_DOWN:
                pointLastXY[handIndex][0] = event.getX(handIndex);
                pointLastXY[handIndex][1] = event.getY(handIndex);
                break;
            case MotionEvent.ACTION_MOVE:
                boolean needUpdate = true;
                if (currPointerCount >= 2) {
                    hasZoom = true;
                    double downDis = Math.sqrt(Math.pow((pointLastXY[0][0] - pointLastXY[1][0]), 2) + Math.pow((pointLastXY[0][1] - pointLastXY[1][1]), 2));
                    double moveDis = Math.sqrt(Math.pow(event.getX(0) - event.getX(1), 2) + Math.pow(event.getY(0) - event.getY(1), 2));
                    float daltaScale = (float) (moveDis / downDis);
                    float[] center = new float[2];
                    center[0] = (pointLastXY[0][0] + pointLastXY[1][0])/2;
                    center[1] = (pointLastXY[0][1] + pointLastXY[1][1])/2;
                    float[] zeroDis = new float[2];
                    zeroDis[0]=center[0] - info.x;
                    zeroDis[1]=center[1] - info.y;
                    info.x = (int) (center[0]  -zeroDis[0]*daltaScale);
                    info.y = (int) (center[1]  -zeroDis[1]*daltaScale);
                    info.scale = info.scale * daltaScale;
                } else if (!hasZoom) {
                    int daltaX = (int) (event.getX(0) - pointLastXY[0][0]);
                    int daltaY = (int) (event.getY(0) - pointLastXY[0][1]);
                    info.x += daltaX;
                    info.y += daltaY;
                } else {
                    needUpdate = false;
                }
                if (needUpdate) {
                    requestLayout();
                }

                pointLastXY[0][0] = event.getX();
                pointLastXY[0][1] = event.getY();
                if (currPointerCount >= 2) {
                    pointLastXY[1][0] = event.getX(1);
                    pointLastXY[1][1] = event.getY(1);
                }
                break;
            default:
                break;
        }
        return true;
    }

    /**
     * 设置地图背景，必须保持地图size不变
     * @param mapFile
     */
    public void setMap(String mapFile) {
        if (mapView!=null) {
            removeView(mapView);
        }
        mapView = new ImageView(getContext());
        mapView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        mapView.setLayoutParams(lp);
        addView(mapView);
        try {
            map = BitmapFactory.decodeStream(getContext().getAssets().open(mapFile));
        } catch (Exception e) {
            e.printStackTrace();
        }
        mapView.setImageBitmap(map);
    }
}
