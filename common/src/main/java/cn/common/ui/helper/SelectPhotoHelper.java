
package cn.common.ui.helper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 描述：用于按比例选取图片通过相册和拍照
 * 
 * @author jakechen
 */
public class SelectPhotoHelper {

    private static final int RESULT_CAMERA = 3211;

    private static final int RESULT_LOAD_IMAGE = 3212;

    private static final int PHOTO_REQUEST_CUT = 3213;

    private File tempFile = getTempFile();

    private Activity mActivity;

    private String pictureSavaPath = "/selectphoto";

    private boolean isSavaPhoto = false;

    private int cutWidth = 100;

    private int cutHeight = 100;

    private Callback mCallback;

    public SelectPhotoHelper(Activity activity, Callback callback) {
        mActivity = activity;
        mCallback = callback;
    }

    private File getTempFile() {
        File dir = new File(Environment.getExternalStorageDirectory() + pictureSavaPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir, getPhotoFileName());
        return file;
    }

    // 删除SD卡拍的图片，保留剪辑的图片
    private void deleteFile(File file) {
        if (file != null) {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                if (file.exists()) {
                    if (file.isFile()) {
                        file.delete();
                    }
                    // 如果它是一个目录
                    else if (file.isDirectory()) {
                        // 声明目录下所有的文件 files[];
                        File files[] = file.listFiles();
                        for (int i = 0; i < files.length; i++) { // 遍历目录下所有的文件
                            deleteFile(files[i]); // 把每个文件 用这个方法进行迭代
                        }
                    }
                    file.delete();
                }
            }
            tempFile = null;
        }
    }

    private void savePhoto(Bitmap photo) {
        ByteArrayOutputStream baos = null;
        tempFile = getTempFile();
        try {
            FileOutputStream fos = new FileOutputStream(tempFile);
            baos = new ByteArrayOutputStream();
            photo.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] photodata = baos.toByteArray();
            fos.write(photodata);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.getStackTrace();
        } finally {
            if (baos != null) {
                try {
                    baos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void startPhotoZoom(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // crop为true是设置在开启的intent中设置显示的view可以剪裁
        intent.putExtra("crop", "true");

        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX,outputY 是剪裁图片的宽高
        intent.putExtra("outputX", cutWidth);
        intent.putExtra("outputY", cutHeight);
        intent.putExtra("return-data", true);
        intent.putExtra("noFaceDetection", true);
        mActivity.startActivityForResult(intent, PHOTO_REQUEST_CUT);
    }

    // 使用系统当前日期加以调整作为照片的名称
    @SuppressLint("SimpleDateFormat")
    private String getPhotoFileName() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat("'IMG'_yyyyMMdd_HHmmss");
        return dateFormat.format(date) + ".png";
    }

    /**
     * 拍照
     */
    public void takePhoto() {
        tempFile = getTempFile();
        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // 指定调用相机拍照后照片的储存路径
        i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tempFile));
        mActivity.startActivityForResult(i, RESULT_CAMERA);
    }

    /**
     * 从相册选择头像
     */
    public void selectPhotoFromSystem() {
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        mActivity.startActivityForResult(i, RESULT_LOAD_IMAGE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RESULT_CAMERA:// 当选择拍照时调用
                if (resultCode == mActivity.RESULT_OK) {
                    startPhotoZoom(Uri.fromFile(tempFile));
                }
                break;
            case RESULT_LOAD_IMAGE:// 当选择从本地获取图片时
                // 做非空判断，当我们觉得不满意想重新剪裁的时候便不会报异常，下同
                if (resultCode == mActivity.RESULT_OK) {
                    if (data != null) {
                        startPhotoZoom(data.getData());
                    }
                }
                break;
            case PHOTO_REQUEST_CUT:// 返回的结果
                if (resultCode == mActivity.RESULT_OK) {
                    if (data != null) {
                        deleteFile(tempFile);
                        Bundle bundle = data.getExtras();
                        if (bundle != null) {
                            Bitmap photo = bundle.getParcelable("data");
                            if (mCallback != null) {
                                mCallback.handleResult(photo);
                            }
                            if (isSavaPhoto) {
                                savePhoto(photo);
                            }
                            photo = null;
                        }
                    }
                }

                break;
        }
    }

    public int getCutHeight() {
        return cutHeight;
    }

    public void setCutHeight(int cutHeight) {
        this.cutHeight = cutHeight;
    }

    public int getCutWidth() {
        return cutWidth;
    }

    public void setCutWidth(int cutWidth) {
        this.cutWidth = cutWidth;
    }

    public boolean isSavaPhoto() {
        return isSavaPhoto;
    }

    public void setIsSavaPhoto(boolean isSavaPhoto) {
        this.isSavaPhoto = isSavaPhoto;
    }

    public String getPictureSavaPath() {
        return pictureSavaPath;
    }

    public void setPictureSavaPath(String pictureSavaPath) {
        this.pictureSavaPath = pictureSavaPath;
    }

    public static interface Callback {
        void handleResult(Bitmap bitmap);
    }

}
