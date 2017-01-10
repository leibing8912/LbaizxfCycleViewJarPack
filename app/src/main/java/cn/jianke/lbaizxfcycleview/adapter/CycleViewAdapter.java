package cn.jianke.lbaizxfcycleview.adapter;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import java.util.List;
import cn.jianke.lbaizxfcycleview.widget.CycleView.CycleViewListener;


public class CycleViewAdapter extends PagerAdapter{
    private List<View> mViews;
    private CycleViewListener cycleViewListener;
    private int size;

    public CycleViewAdapter(List<View> mViews, CycleViewListener cycleViewListener, int size){
        this.mViews = mViews;
        this.cycleViewListener = cycleViewListener;
        this.size = size;
    }

    @Override
    public int getCount() {
        return mViews != null? mViews.size():0;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public View instantiateItem(ViewGroup container, final int position) {
        View v = mViews.get(position);
        if (cycleViewListener != null) {
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int vPosition = position;
                    if (vPosition > size)
                        vPosition = vPosition % size;
                    vPosition --;
                    if (vPosition < 0)
                        vPosition = 0;
                    cycleViewListener.onItemClick(vPosition);
                }
            });
        }
        container.addView(v);
        return v;
    }
}
