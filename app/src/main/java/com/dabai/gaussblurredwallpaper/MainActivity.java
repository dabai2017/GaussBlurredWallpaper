package com.dabai.gaussblurredwallpaper;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.WallpaperManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.qingmei2.rximagepicker.core.RxImagePicker;
import com.wildma.pictureselector.PictureSelector;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import io.reactivex.functions.Consumer;
import jp.wasabeef.blurry.Blurry;

public class MainActivity extends AppCompatActivity {

    //声明控件
    ImageView iv;
    CardView setting_card;
    SeekBar seek1, seek2, seek3;


    //声明变量
    String TAG = "dabai";
    Context context;
    WallpaperManager wallpaperManager;
    private int REQUEST_SYSTEM_PIC = 0;
    private static final int CROP_PICTURE = 2;//裁剪后图片返回码
    //裁剪图片存放地址的Uri
    private Uri cropImageUri;

    int win_width;
    int win_height;
    private int seek1_pro, seek2_pro;
    private Bitmap tmp_bm;


    boolean whenOk = true;
    private File file;


    private NotificationManager manager;
    private int noi = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        init();
        init_val();


    }

    /**
     * 所有初始化
     */
    private void init() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1); // 动态申请读取权限
        }


        //变量实例化
        context = getApplicationContext();
        wallpaperManager = WallpaperManager.getInstance(this);
        manager = (NotificationManager) this.getSystemService(getApplicationContext().NOTIFICATION_SERVICE);

        //控件实例化
        iv = findViewById(R.id.mainImageView1);
        setting_card = findViewById(R.id.setting_card);
        seek1 = findViewById(R.id.seek1);
        seek2 = findViewById(R.id.seek2);
        seek3 = findViewById(R.id.seek3);

        //透明actionbar
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#33000000")));
        actionBar.setSplitBackgroundDrawable(new ColorDrawable(Color.parseColor("#33000000")));


        DisplayMetrics metrics;
        metrics = getApplicationContext().getResources().getDisplayMetrics();

        win_width = metrics.widthPixels;
        win_height = metrics.heightPixels;

    }


    /**
     * 控件初始化
     */
    private void init_val() {

        tmp_bm = ((BitmapDrawable) ((ImageView) iv).getDrawable()).getBitmap();


        seek1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if (progress != 0) {
                    seek1_pro = progress;
                    setMoreTitle(seek1_pro);
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (whenOk) {

                    whenOk = false;
                    Blurry.with(MainActivity.this)
                            .radius(seek1_pro)
                            .sampling(2)
                            .async(new Blurry.ImageComposer.ImageComposerListener() {
                                @Override
                                public void onImageReady(BitmapDrawable drawable) {
                                    iv.setImageBitmap(drawable2Bitmap(drawable));
                                    whenOk = true;
                                }
                            })
                            .from(tmp_bm)
                            .into(iv);


                } else {
                    Toast.makeText(context, "你能慢点嘛？", Toast.LENGTH_SHORT).show();
                }
            }
        });

        seek2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress != 100) {
                    seek2_pro = progress;
                    setMoreTitle(seek2_pro);
                }

                //颜色转drawable   设置到前景色
                ColorDrawable cd = new ColorDrawable(Color.argb(seek2_pro, 00, 00, 00));

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    iv.setForeground(cd.getCurrent());
                }


            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        seek3.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                changeLight(iv, progress - 100);
                setMoreTitle(progress - 100);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

    }


    /**
     * 菜单加载
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }


    /**
     * 右上角菜单事件
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.get_w:

                Drawable drawable = wallpaperManager.getDrawable();
                Bitmap bitmap = drawable2Bitmap(drawable);
                iv.setImageBitmap(bitmap);
                tmp_bm = ((BitmapDrawable) ((ImageView) iv).getDrawable()).getBitmap();


                break;
            case R.id.choose_img:


                PictureSelector
                        .create(MainActivity.this, PictureSelector.SELECT_REQUEST_CODE)
                        .selectPicture(true, 1080, 2280, 9, 18);

                break;
            case R.id.xg:
                startActivity(new Intent(this, WallpaperActivity.class));
                break;
            case R.id.menu_help:
                startActivity(new Intent(this, HelpActivity.class));

                break;
            case R.id.menu_about:
                startActivity(new Intent(this, AboutActivity.class));

                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PictureSelector.SELECT_REQUEST_CODE) {
            if (data != null) {
                String picturePath = data.getStringExtra(PictureSelector.PICTURE_PATH);
                Bitmap bitmap = getLoacalBitmap(picturePath); //从本地取图片(在cdcard中获取)  //


                iv.setImageBitmap(bitmap);
                tmp_bm = ((BitmapDrawable) ((ImageView) iv).getDrawable()).getBitmap();
                new File("/sdcard/PictureSelector.temp.jpg").delete();

            }
        }

    }

    /**
     * 加载本地图片
     *
     * @param url
     * @return
     */
    public static Bitmap getLoacalBitmap(String url) {
        try {
            FileInputStream fis = new FileInputStream(url);
            return BitmapFactory.decodeStream(fis);  ///把流转化为Bitmap图片

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * drawable转bitmap
     *
     * @param drawable
     * @return
     */
    public Bitmap drawable2Bitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else if (drawable instanceof NinePatchDrawable) {
            Bitmap bitmap = Bitmap
                    .createBitmap(
                            drawable.getIntrinsicWidth(),
                            drawable.getIntrinsicHeight(),
                            drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                                    : Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight());
            drawable.draw(canvas);
            return bitmap;
        } else {
            return null;
        }
    }

    public void setting_ctrl(View view) {
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        int i = actionBar.getHideOffset();

        if (i == 0) {
            actionBar.hide();
            ObjectAnimator.ofFloat(setting_card, "translationY", 0, 700).setDuration(200).start();
        } else {
            actionBar.show();
            ObjectAnimator.ofFloat(setting_card, "translationY", 700, 0).setDuration(200).start();
        }

    }


    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void setWap(View view) {


        new AlertDialog.Builder(this)
                .setTitle("提示")
                .setMessage("确定要设置为壁纸嘛?")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        new Thread(new Runnable() {
                            @Override
                            public void run() {

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            //设置壁纸
                                            wallpaperManager.setBitmap(getBitmapByView(iv));
                                            Toast.makeText(context, "设置壁纸成功", Toast.LENGTH_SHORT).show();

                                        } catch (Exception e) {
                                            Toast.makeText(context, "程序异常", Toast.LENGTH_SHORT).show();

                                        }
                                    }
                                });

                            }
                        }).start();

                    }
                }).show();

    }


    //根据view获取bitmap
    public static Bitmap getBitmapByView(View view) {
        int h = 0;
        Bitmap bitmap = null;
        bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),
                Bitmap.Config.RGB_565);
        final Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    public void save_image(View view) {
        //保存图片

        new AlertDialog.Builder(this)
                .setTitle("提示")
                .setMessage("确定要保存嘛?")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Bitmap bitmap = getBitmapByView(iv);//iv是View
                                int ran = new Random().nextInt(1000);
                                savePhotoToSDCard(bitmap, "/sdcard/高斯模糊处理过的图片", "GaussianBlur_" + ran);
                                file = new File("/sdcard/高斯模糊处理过的图片/GaussianBlur_" + ran + ".png");
                                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(context, "保存壁纸成功", Toast.LENGTH_SHORT).show();
                                    }
                                });

                            }
                        }).start();

                    }
                }).show();

    }


    @Override
    public void onBackPressed() {
        Intent i = new Intent(Intent.ACTION_MAIN);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addCategory(Intent.CATEGORY_HOME);
        startActivity(i);

    }


    //检查sd
    public static boolean checkSDCardAvailable() {
        return android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
    }

    public static void savePhotoToSDCard(Bitmap photoBitmap, String path, String photoName) {
        if (checkSDCardAvailable()) {
            File dir = new File(path);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            File photoFile = new File(path, photoName + ".png");
            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = new FileOutputStream(photoFile);
                if (photoBitmap != null) {
                    if (photoBitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)) {
                        fileOutputStream.flush();
                    }
                }
            } catch (FileNotFoundException e) {
                photoFile.delete();
                e.printStackTrace();
            } catch (IOException e) {
                photoFile.delete();
                e.printStackTrace();
            } finally {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //改变图片的亮度方法 0--原样  >0---调亮  <0---调暗
    private void changeLight(ImageView imageView, int brightness) {
        ColorMatrix cMatrix = new ColorMatrix();
        cMatrix.set(new float[]{1, 0, 0, 0, brightness, 0, 1, 0, 0,
                brightness,// 改变亮度
                0, 0, 1, 0, brightness, 0, 0, 0, 1, 0});
        imageView.setColorFilter(new ColorMatrixColorFilter(cMatrix));
    }


    void setMoreTitle(int title) {
        setTitle("Blur壁纸 : " + title);
    }

}

