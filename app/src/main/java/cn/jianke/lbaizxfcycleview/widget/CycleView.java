package cn.jianke.lbaizxfcycleview.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import cn.jianke.lbaizxfcycleview.R;
import cn.jianke.lbaizxfcycleview.adapter.CycleViewAdapter;
import cn.jianke.lbaizxfcycleview.model.CycleModel;
import cn.jianke.lbaizxfcycleview.utils.ImageLoader;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class CycleView extends FrameLayout implements ViewPager.OnPageChangeListener{
    private int delay = 6000;
    private ViewPager mViewPager;
    private TextView mTitle;
    private LinearLayout mIndicatorLy;
    private ImageView[] mIndicators;
    private boolean isCycle = false;
    private boolean isWheel = false;
    private boolean isHasWheel = false;
    private int mCurrentPosition = 0;
    private int mIndicatorSelected = R.mipmap.btn_appraise_selected;
    private int mIndicatorUnselected = R.mipmap.btn_appraise_normal;
    private List<View> mViews = new ArrayList<>();
    private CycleViewListener cycleViewListener;
    private List<CycleModel> mData;
    private CycleViewAdapter mAdapter;
    private Drawable defaultImage = getResources().getDrawable(R.drawable.default_holder);
    private Subscription mSubscription;

    public CycleView(Context context) {
        this(context, null);
    }

    public CycleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CycleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.widget_cycle_view, null);
        mViewPager = (ViewPager) view.findViewById(R.id.vp_cycle);
        mTitle = (TextView) view.findViewById(R.id.tv_cycle_title);
        mIndicatorLy = (LinearLayout) view.findViewById(R.id.ly_cycle_indicator);
        this.addView(view);
    }

    public void setAlignParentRight(int paddingRight, int paddingBottom){
        if (mIndicatorLy == null)
            return;

        mIndicatorLy.setGravity(Gravity.RIGHT);
        mIndicatorLy.setPadding(0,0,paddingRight,paddingBottom);
        mIndicatorLy.requestLayout();
    }

    public void setAlignParentLeft(int paddingLeft, int paddingBottom){
        mIndicatorLy.setGravity(Gravity.LEFT);
        mIndicatorLy.setPadding(paddingLeft, 0, 0, paddingBottom);
        mIndicatorLy.requestLayout();
    }

    public void setAlignParentCenter(int paddingBottom){
        if (mIndicatorLy == null)
            return;
        mIndicatorLy.setGravity(Gravity.CENTER);
        mIndicatorLy.setPadding(0, 0, 0, paddingBottom);
        mIndicatorLy.requestLayout();
    }

    public void setData(List<CycleModel> mData , CycleViewListener listener){
        setData(mData, 0, defaultImage, listener);
    }

    public void setData(List<CycleModel> mData , Drawable defaultImage, CycleViewListener listener){
        setData(mData, 0, defaultImage, listener);
    }

    public void setData(List<CycleModel> mData, int defaultPosition,
                        Drawable defaultImage, CycleViewListener listener){
        this.mData = mData;
        if (mData == null || mData.size() == 0){
            this.setVisibility(View.GONE);
            return;
        }
        int size = mData.size();
        if (defaultPosition >= size)
            defaultPosition = 0;
        if (size == 1)
            isCycle = false;
        mViews.clear();
        if (isCycle) {
            mViews.add(getCycleView(getContext(), mData.get(size - 1).getUrl(), defaultImage));
            for (int i = 0; i < size; i++) {
                mViews.add(getCycleView(getContext(), mData.get(i).getUrl(), defaultImage));
            }
            mViews.add(getCycleView(getContext() , mData.get(0).getUrl(), defaultImage));
        } else {
            for (int i = 0; i < size; i++) {
                mViews.add(getCycleView(getContext(), mData.get(i).getUrl(), defaultImage));
            }
        }
        cycleViewListener = listener;
        initIndicators(size, getContext());
        setIndicator(defaultPosition);
        setAdapter(mViews, cycleViewListener, size);
        cancelSubscription();
        startWheel(size);
    }

    private void setAdapter(List<View> mViews, CycleViewListener cycleViewListener, int size) {
        mAdapter = new CycleViewAdapter(mViews, cycleViewListener, size);
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setOnPageChangeListener(this);
        mViewPager.setAdapter(mAdapter);
        if (isCycle){
            mViewPager.setCurrentItem(1, false);
        }else {
            mViewPager.setCurrentItem(0, false);
        }
    }

    private void initIndicators(int size, Context context) {
        mIndicatorLy.removeAllViews();
        if (size < 2)
            return;
        mIndicators = new ImageView[size];
        for (int i = 0; i < mIndicators.length; i++) {
            mIndicators[i] = new ImageView(context);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(10, 0, 10, 0);
            mIndicators[i].setLayoutParams(lp);
            mIndicatorLy.addView(mIndicators[i]);
        }
    }

    private void setIndicator(int selectedPosition) {
        if (mData == null || mData.size() == 0 || selectedPosition >= mData.size())
            return;
        if (mTitle != null)
            mTitle.setText(mData.get(selectedPosition).getTitle());
        try {
            for (int i = 0; i < mIndicators.length; i++) {
                mIndicators[i].setBackgroundResource(mIndicatorUnselected);
            }
            if (mIndicators.length > selectedPosition)
                mIndicators[selectedPosition].setBackgroundResource(mIndicatorSelected);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setIndicators(int select, int unselect) {
        mIndicatorSelected = select;
        mIndicatorUnselected = unselect;
    }

    private View getCycleView(Context context, String url, Drawable defaultImage) {
        RelativeLayout mRelativeLayout = new RelativeLayout(context);
        ImageView imageView = new ImageView(context);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        imageView.setLayoutParams(layoutParams);
        ImageLoader.load(context,imageView, url, defaultImage);
        ImageView backGround = new ImageView(context);
        backGround.setLayoutParams(layoutParams);
        backGround.setBackgroundResource(R.color.cycle_image_bg);
        mRelativeLayout.addView(imageView);
        mRelativeLayout.addView(backGround);
        return mRelativeLayout;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        int max = mViews.size() - 1;
        mCurrentPosition = position;
        if (isCycle()) {
            if (position == 0) {
                mCurrentPosition = max - 1;
            } else if (position == max) {
                mCurrentPosition = 1;
            }
            position = mCurrentPosition - 1;
        }
        setIndicator(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if (state == 0 && isCycle()) {
            mViewPager.setCurrentItem(mCurrentPosition, false);
        }
    }

    public void setIsHasWheel(boolean isHasWheel){
        this.isHasWheel = isHasWheel;
        if (isHasWheel) {
            isCycle = true;
            isWheel = true;
        }else {
            isWheel = false;
        }
    }

    public void setCycle(boolean isCycle) {
        this.isCycle = isCycle;
    }

    public boolean isCycle() {
        return isCycle;
    }

    private void setWheel(boolean isWheel) {
        this.isWheel = false;
        if (isCycle() && isHasWheel) {
            this.isWheel = isWheel;
        }
    }

    private void startWheel(int size){
        if (size < 2 || !isCycle()){
            setWheel(false);
            return;
        }
        setWheel(true);
        mSubscription = Observable.interval(delay, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        if (isWheel && isHasWheel) {
                            mCurrentPosition++;
                            if (mViewPager != null)
                                mViewPager.setCurrentItem(mCurrentPosition, false);
                        }
                    }
                });
    }

    public void cancelSubscription(){
        if (mSubscription != null){
            mSubscription.unsubscribe();
            mSubscription = null;
        }
    }

    private boolean isWheel() {
        return isWheel;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()){
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_DOWN:
                setWheel(false);
                break;
            case MotionEvent.ACTION_UP:
                setWheel(true);
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    public interface CycleViewListener{
        void onItemClick(int position);
    }
}
