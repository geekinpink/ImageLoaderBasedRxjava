package io.geekinpink.imageloader;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


public class ImageAdapter extends RecyclerView.Adapter{
    private Context context;
    private ImageLoader imageLoader;
    private static final String[] URL_LIST = {
            "http://ww3.sinaimg.cn/large/610dc034jw1f71bezmt3tj20u00k0757.jpg"
            ,"http://ww4.sinaimg.cn/large/610dc034jw1f76axy6xcsj20u00yqq49.jpg"
            , "http://ww3.sinaimg.cn/large/610dc034jw1f6xsqw8057j20dw0kugpf.jpg"
            ,"http://ww2.sinaimg.cn/large/610dc034jw1f6vyy5a99ej20u011gq87.jpg"
            ,"http://ww4.sinaimg.cn/large/610dc034jw1f6uv5gbsa9j20u00qxjt6.jpg"
            ,"http://ww1.sinaimg.cn/large/610dc034jw1f6u4boc0k2j20u00u0gni.jpg"
            ,"http://ww3.sinaimg.cn/large/610dc034jw1f6qsn74e3yj20u011htc6.jpg"
            , "http://ww2.sinaimg.cn/large/610dc034jw1f6pnw6i7lqj20u00u0tbr.jpg"
            ,"http://ww2.sinaimg.cn/large/610dc034jw1f6ofd28kr6j20dw0kudgx.jpg"
            ,"http://ww1.sinaimg.cn/large/610dc034jw1f6nbm78pplj20dw0i2djy.jpg"
            ,"http://ww3.sinaimg.cn/large/610dc034jw1f71bezmt3tj20u00k0757.jpg"
            ,"http://ww4.sinaimg.cn/large/610dc034jw1f76axy6xcsj20u00yqq49.jpg"
            , "http://ww3.sinaimg.cn/large/610dc034jw1f6xsqw8057j20dw0kugpf.jpg"
            ,"http://ww2.sinaimg.cn/large/610dc034jw1f6vyy5a99ej20u011gq87.jpg"
            ,"http://ww4.sinaimg.cn/large/610dc034jw1f6uv5gbsa9j20u00qxjt6.jpg"
            ,"http://ww1.sinaimg.cn/large/610dc034jw1f6u4boc0k2j20u00u0gni.jpg"
            ,"http://ww3.sinaimg.cn/large/610dc034jw1f6qsn74e3yj20u011htc6.jpg"
            , "http://ww2.sinaimg.cn/large/610dc034jw1f6pnw6i7lqj20u00u0tbr.jpg"
            ,"http://ww2.sinaimg.cn/large/610dc034jw1f6ofd28kr6j20dw0kudgx.jpg"
            ,"http://ww1.sinaimg.cn/large/610dc034jw1f6nbm78pplj20dw0i2djy.jpg"
            ,"http://ww3.sinaimg.cn/large/610dc034jw1f71bezmt3tj20u00k0757.jpg"
            ,"http://ww4.sinaimg.cn/large/610dc034jw1f76axy6xcsj20u00yqq49.jpg"
            , "http://ww3.sinaimg.cn/large/610dc034jw1f6xsqw8057j20dw0kugpf.jpg"
            ,"http://ww2.sinaimg.cn/large/610dc034jw1f6vyy5a99ej20u011gq87.jpg"
            ,"http://ww4.sinaimg.cn/large/610dc034jw1f6uv5gbsa9j20u00qxjt6.jpg"
            ,"http://ww1.sinaimg.cn/large/610dc034jw1f6u4boc0k2j20u00u0gni.jpg"
            ,"http://ww3.sinaimg.cn/large/610dc034jw1f6qsn74e3yj20u011htc6.jpg"
            , "http://ww2.sinaimg.cn/large/610dc034jw1f6pnw6i7lqj20u00u0tbr.jpg"
            ,"http://ww2.sinaimg.cn/large/610dc034jw1f6ofd28kr6j20dw0kudgx.jpg"
            ,"http://ww1.sinaimg.cn/large/610dc034jw1f6nbm78pplj20dw0i2djy.jpg"
            ,"http://ww3.sinaimg.cn/large/610dc034jw1f71bezmt3tj20u00k0757.jpg"
            ,"http://ww4.sinaimg.cn/large/610dc034jw1f76axy6xcsj20u00yqq49.jpg"
            , "http://ww3.sinaimg.cn/large/610dc034jw1f6xsqw8057j20dw0kugpf.jpg"
            ,"http://ww2.sinaimg.cn/large/610dc034jw1f6vyy5a99ej20u011gq87.jpg"
            ,"http://ww4.sinaimg.cn/large/610dc034jw1f6uv5gbsa9j20u00qxjt6.jpg"
            ,"http://ww1.sinaimg.cn/large/610dc034jw1f6u4boc0k2j20u00u0gni.jpg"
            ,"http://ww3.sinaimg.cn/large/610dc034jw1f6qsn74e3yj20u011htc6.jpg"
            , "http://ww2.sinaimg.cn/large/610dc034jw1f6pnw6i7lqj20u00u0tbr.jpg"
            ,"http://ww2.sinaimg.cn/large/610dc034jw1f6ofd28kr6j20dw0kudgx.jpg"
            ,"http://ww1.sinaimg.cn/large/610dc034jw1f6nbm78pplj20dw0i2djy.jpg"};


    public ImageAdapter(Context context) {
        this.context = context;
        this.imageLoader = ImageLoader.getInstance(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ImageView imageView = (ImageView) LayoutInflater.from(context).inflate(R.layout.item_image, null);
        return new ImageVH(imageView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((ImageVH) holder).bind(imageLoader, URL_LIST[position]);
    }

    @Override
    public int getItemCount() {
        return URL_LIST.length;
    }

    static class ImageVH extends RecyclerView.ViewHolder {

        public ImageVH(View itemView) {
            super(itemView);
        }

        public void bind(ImageLoader imageLoader, String url) {
            imageLoader.bindImage((ImageView) itemView, url, 200, 200);
        }
    }
}
