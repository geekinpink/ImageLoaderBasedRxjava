package io.geekinpink.imageloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class ImageLoader {
    private static final String TAG = "ImageLoader";

    private static final int MAX_MEMO = (int) Runtime.getRuntime().maxMemory();
    ;
    private static final int LRU_MAX_MEMO = MAX_MEMO / 1024 / 8; //LruCache设置的最大内存
    private static final int MAX_DISK_CACHE_SIZE = 100 * 1024 * 1024; //100MB
    private static final int IO_BUFFER_SIZE = 1024 * 8;

    private LruCache<String, Bitmap> mLruCache;
    private DiskLruCache mDiskLruCache;
    private static ImageLoader instance;

    private ImageLoader(Context context) {

        mLruCache = new LruCache<String, Bitmap>(LRU_MAX_MEMO) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight() / 1024;
            }
        };
        Log.d(TAG, "ImageLoader: LruCache init");

        try {
            mDiskLruCache = DiskLruCache.open(getDiskCacheDir(context, "bitmap"), 1, 1, MAX_DISK_CACHE_SIZE);
            Log.d(TAG, "ImageLoader: DiskLruCache init");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ImageLoader getInstance(Context context) {
        synchronized (ImageLoader.class) {
            if (instance == null) {
                instance = new ImageLoader(context);
            }
            return instance;
        }
    }

    public void bindImage(ImageView imageView, String url, int reqWidth, int reqHeight) {
        String key = hashKeyFormUrl(url);
        Log.d(TAG, "bindImageKey: " + key);
        try {
            if (mLruCache.get(key) != null) {
                bindImageFromMemo(imageView, key);
            }
            if (mDiskLruCache.get(key) != null) {
                bindImageFromDisk(imageView, key, reqWidth, reqHeight);
            } else {
                bindImageFromHttp(imageView, url, reqWidth, reqHeight);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void bindImageFromDisk(final ImageView imageView, final String key, final int reqWidth, final int reqHeight) {
        imageView.setTag(R.id.imageloader_id, key);
        Observable.just(key)
                .flatMap(new Func1<String, Observable<Bitmap>>() {
                    @Override
                    public Observable<Bitmap> call(String s) {
                        Bitmap bitmap = getBitmapFromDisk(s, reqWidth, reqHeight);
                        saveBitmapToMemo(s, bitmap);
                        return Observable.just(bitmap);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Bitmap>() {
                    @Override
                    public void call(Bitmap bitmap) {
                        if (imageView.getTag(R.id.imageloader_id) == key) {
                            imageView.setImageBitmap(bitmap);
                        }
                    }
                });
    }

    private void saveBitmapToMemo(final String key, final Bitmap bitmap) {
        if (mLruCache.get(key) == null) {
            mLruCache.put(key, bitmap);
        }
        Log.d(TAG, "saveBitmapToMemo: ");
    }

    private void bindImageFromMemo(ImageView imageView, final String key) {
        imageView.setImageBitmap(mLruCache.get(key));
        Log.d(TAG, "bindImageFromMemo: ");
    }

    private void bindImageFromHttp(final ImageView imageView, final String url, final int reqWidth, final int reqHeight) {
        imageView.setTag(R.id.imageloader_id, url);
        Observable.just(url)
                .flatMap(new Func1<String, Observable<Bitmap>>() {
                    @Override
                    public Observable<Bitmap> call(String s) {
                        InputStream inputStream = null;
                        try {
                            inputStream = new URL(s).openStream();
                            saveBitmapToDisk(inputStream, hashKeyFormUrl(url));
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            return Observable.just(getBitmapFromDisk(hashKeyFormUrl(url), reqWidth, reqHeight));
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Bitmap>() {
                    @Override
                    public void call(Bitmap bitmap) {
                        if (imageView.getTag(R.id.imageloader_id) == url) {
                            imageView.setImageBitmap(bitmap);
                        }
                    }
                });
    }

    private Bitmap getBitmapFromDisk(String key, int reqWidth, int reqHeight) {
        FileInputStream is = null;
        DiskLruCache.Snapshot snapshot = null;
        FileDescriptor fd = null;
        Bitmap bitmap = null;

        try {
            snapshot = mDiskLruCache.get(key);
            if (snapshot != null) {
                is = (FileInputStream) snapshot.getInputStream(0);
            }
            fd = is.getFD();
            bitmap = ImageResizer.decodeSampledBitmapFromFD(fd, reqWidth, reqHeight);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return bitmap;
        }
    }

    private void saveBitmapToDisk(InputStream inputStream, String key) {
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        DiskLruCache.Editor editor = null;

        try {
            bis = new BufferedInputStream(inputStream, IO_BUFFER_SIZE);
            editor = mDiskLruCache.edit(key);
            if (editor == null) {
                return;
            }
            bos = new BufferedOutputStream(editor.newOutputStream(0), IO_BUFFER_SIZE);
            int a;
            while ((a = bis.read()) != -1) {
                bos.write(a);
            }
            editor.commit();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bis != null) {
                    bis.close();
                }
                if (bos != null) {
                    bos.close();
                }
                mDiskLruCache.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private String hashKeyFormUrl(String url) {
        String cacheKey;
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(url.getBytes());
            cacheKey = bytesToHexString(messageDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(url.hashCode());
        }
        return cacheKey;
    }

    private String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xff & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    private File getDiskCacheDir(Context context, String name) {
        File file;
        if (Environment.isExternalStorageEmulated()) {
            file = new File(context.getExternalCacheDir().getPath() + File.separator + name);
        } else {
            file = new File(context.getCacheDir().getPath() + File.separator + name);
        }
        return file;
    }

}
