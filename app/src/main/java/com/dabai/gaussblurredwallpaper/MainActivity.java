package com.dabai.gaussblurredwallpaper;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.WallpaperManager;
import android.content.ContentUris;
import android.content.Context;
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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

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
                Intent intent = new Intent("android.intent.action.GET_CONTENT");
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_SYSTEM_PIC);//打开系统相册
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

    public void toast(Object o) {
        Toast.makeText(this, "" + o, Toast.LENGTH_SHORT).show();
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SYSTEM_PIC && resultCode == RESULT_OK && null != data) {
            if (Build.VERSION.SDK_INT >= 19) {
                handleImageOnKitkat(data);
            } else {
                handleImageBeforeKitkat(data);
            }
        }

        if (requestCode == CROP_PICTURE && resultCode == RESULT_OK && null != data) {

            try {
                Bitmap headShot = BitmapFactory.decodeStream(getContentResolver().openInputStream(cropImageUri));
                iv.setImageBitmap(headShot);
                tmp_bm = ((BitmapDrawable) ((ImageView) iv).getDrawable()).getBitmap();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }

    }

    @TargetApi(19)
    private void handleImageOnKitkat(Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        if (DocumentsContract.isDocumentUri(this, uri)) {
            //如果是document类型的uri，则通过document id处理
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content:" +
                        "//downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            //如果是content类型的uri，则使用普通方式处理
            imagePath = getImagePath(uri, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            //如果是File类型的uri，直接获取图片路径即可
            imagePath = uri.getPath();
        }

        //displayImage(imagePath);//根据图片路径显示图片
        startPhotoZoom(uri);


    }

    private void handleImageBeforeKitkat(Intent data) {
        Uri uri = data.getData();
        String imagePath = getImagePath(uri, null);

        //displayImage(imagePath);
        startPhotoZoom(uri);


    }

    private String getImagePath(Uri uri, String selection) {
        String path = null;
        //通过uri和selection来获取真实的图片路径
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    private void displayImage(String imagePath) {
        if (imagePath != null) {

            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            iv.setImageBitmap(bitmap);

        } else {
            Toast.makeText(this, "failed to get image", Toast.LENGTH_SHORT).show();
        }
    }


    public void startPhotoZoom(Uri uri) {


        File CropPhoto = new File(getExternalCacheDir(), "crop_image.jpg");
        try {
            if (CropPhoto.exists()) {
                CropPhoto.delete();
            }
            CropPhoto.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        cropImageUri = Uri.fromFile(CropPhoto);
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); //添加这一句表示对目标应用临时授权该Uri所代表的文件
        }
        // 下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
        intent.putExtra("crop", "true");
        intent.putExtra("scale", true);

        intent.putExtra("aspectX", 9);
        intent.putExtra("aspectY", 18);

        intent.putExtra("outputX", getImageWidthHeight(CropPhoto.getAbsolutePath())[0]);
        intent.putExtra("outputY", getImageWidthHeight(CropPhoto.getAbsolutePath())[1]);

        intent.putExtra("return-data", false);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cropImageUri);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true); // no face detection
        startActivityForResult(intent, CROP_PICTURE);
    }


    public static int[] getImageWidthHeight(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();

        /**
         * 最关键在此，把options.inJustDecodeBounds = true;
         * 这里再decodeFile()，返回的bitmap为空，但此时调用options.outHeight时，已经包含了图片的高了
         */
        options.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options); // 此时返回的bitmap为null
        /**
         *options.outHeight为原始图片的高
         */
        return new int[]{options.outWidth, options.outHeight};
    }


    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void setWap(View view) {
        try {
            //设置壁纸

            wallpaperManager.setBitmap(getBitmapByView(iv));

            sendNotification("2", "设置壁纸提示", "提示", "设置壁纸成功");

        } catch (Exception e) {

            sendNotification("2", "设置壁纸提示", "提示", "程序异常" + e.getMessage());
        }
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
                        sendNotification("1", "保存壁纸通知", "提示", "壁纸保存成功 " + file.getAbsolutePath());
                    }
                });

            }
        }).start();

    }


    @Override
    public void onBackPressed() {
        Intent i = new Intent(Intent.ACTION_MAIN);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addCategory(Intent.CATEGORY_HOME);
        startActivity(i);

    }

    @TargetApi(Build.VERSION_CODES.O)
    public void sendNotification(String channelID, String channelName, String title, String text) {

        NotificationChannel channel = new NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_HIGH);

        manager.createNotificationChannel(channel);

        Notification.Builder builder = new Notification.Builder(context);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle(title);
        builder.setContentText(text);
        builder.setColor(Color.parseColor("#E91E63"));
        builder.setWhen(System.currentTimeMillis());//设置创建时间
        builder.setPriority(Notification.PRIORITY_MAX);

        //创建通知时指定channelID
        builder.setChannelId(channelID);
        Notification notification = builder.build();

        notification.defaults = Notification.DEFAULT_VIBRATE;

        manager.notify(noi, notification);
        noi++;

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

