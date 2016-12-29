package com.bleu_autom.modules.Controller;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Created by george on 2016/11/9 add FileProvider ContentUri relatives
 */
public class AddPhotoController {
    private static final String LOG_TAG = "AddPhotoController";
    /**
     * FileProvider
     * authorities Must be the same as in AndroidManifest/Application/provider
     */
    public static final String provider_Authorities = "co.foxcat.paking";
    public static final String photoPath = "Parking/images/";
    /**
     * Camera and Album
     */
    public static final int CODE_CAMERA =100;
    public static final int CODE_ALBUM = 99;
    public static final int CODE_PHOTO_CROP = 98;
    public static final String MSG_TAKE_PHOTO = "拍攝相片";
    public static final String MSG_ALBUM_PICK = "選取相簿";
    public static final String MSG_NO_CAMERA = "無系統相機";
    public static final String MSG_NO_EXTERNAL_STORAGE = "無儲存空間";

    private Activity activity=null;
    private Fragment fragment=null;

    private final String[] BUTTON_TEXT = { AddPhotoController.MSG_TAKE_PHOTO, AddPhotoController.MSG_ALBUM_PICK};
    private AlertDialog.Builder builder;
    private String strPhotoName;
    private String strPhotoPath;
    private Uri uriPhoto;
    private PermissionController permissionController;

    public AddPhotoController(@Nullable Activity activity1, @Nullable Fragment fragment1){
        fragment = fragment1;
        if(fragment!=null) {
            activity = fragment.getActivity();
        } else {
            activity = activity1;
        }
        if(activity==null) {
            Log.e(LOG_TAG,"activity is null return false;");
            return;
        }
        this.builder = null;
        this.strPhotoName = "";
        this.permissionController = new PermissionController(this.activity,this.fragment);
    }

    //checkFile
    private void checkFileIsExist(String filePath){
        File file = new File(filePath);
        if(!file.exists()){
            if (!file.mkdirs()){
                Log.e(LOG_TAG,"file.mkdirs() fail filePath="+filePath);
            }
        }
    }

    //add by George on 2016/9/20
    public void start(final AddPhotoListener takePhotoListener){
        if(builder == null){
            builder = new AlertDialog.Builder(activity);
            builder.setItems(BUTTON_TEXT, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    switch (i){
                        case 0://拍攝相片
                            takePhotoListener.onPhoto(showCameraAction());
                            break;
                        case 1://選取相簿
                            doSelectAlbumNew();
                            break;
                        default:
                            break;
                    }
                }
            });
        }
        if(builder != null) builder.show();
    }

    /**
     * Open camera
     */
    public File showCameraAction() {
        File file=null;
        if(!permissionController.checkPermission(Manifest.permission.CAMERA)) return null;
        if(!permissionController.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) return null;

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
            file = getPhotoFile("");
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, getUriFromFile(file));
            if (Build.VERSION.SDK_INT >= 23){
                //give Temp GRANT_READ_URI_PERMISSION && GRANT_WRITE_URI_PERMISSION
                takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }
            Log.d(LOG_TAG,"activity.startActivityForResult AddPhotoController.CODE_CAMERA="+ AddPhotoController.CODE_CAMERA);
            activity.startActivityForResult(takePictureIntent, AddPhotoController.CODE_CAMERA);
        } else {
            Toast.makeText(activity, AddPhotoController.MSG_NO_CAMERA, Toast.LENGTH_SHORT).show();
        }
        return file;
    }
//    private void doSelectAlbum(){
//        if(!checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) return;
//
//        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//        intent.setType("image/*");
//        // 取得相片後返回本畫面
//        activity.startActivityForResult(Intent.createChooser(intent, AddPhotoController.MSG_ALBUM_PICK), AddPhotoController.CODE_ALBUM);
//    }
    private void doSelectAlbumNew(){
        if(!permissionController.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) return;

        final String mimeType = "image/*";
        final PackageManager packageManager = activity.getPackageManager();

        final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);

        intent.setType(mimeType);
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (list.size() > 0) {
            // 如果有可用的Activity

            //20161212
            //Intent picker = new Intent(Intent.ACTION_GET_CONTENT);
            Intent picker = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

            picker.setType(mimeType);
            // 使用Intent Chooser
            Intent destIntent = Intent.createChooser(picker, AddPhotoController.MSG_ALBUM_PICK);
            // 取得相片後返回本畫面
            activity.startActivityForResult(destIntent, AddPhotoController.CODE_ALBUM);
        } else {
            // 沒有可用的Activity
        }
    }

    //add by George on 2016/10/11
    public File cropIntentDataUri(Uri imageUri, double yxRatio){
        File file = getFileFromItentUri(imageUri);
        if (!file.exists() || file.isDirectory()){
            Log.d(LOG_TAG,"getFilesFromUris(activity, new Uri[]{imageUri}, false) is null or isDirectory , imageUri="+ imageUri.toString());
            return null;
        }
        return cropContentFile(file, yxRatio);
    }

    public File cropContentFile(File file, double yxRatio){
        Uri photoURI = getUriFromFile(file);

        // 裁剪圖片意圖
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(photoURI, "image/*");

        if (Build.VERSION.SDK_INT >= 23){
            //grant uri with essential permission the first arg is the The packagename you would like to allow to access the Uri.
            activity.grantUriPermission("com.android.camera", photoURI,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            //give Temp GRANT_READ_URI_PERMISSION && GRANT_WRITE_URI_PERMISSION
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        }

        intent.putExtra("crop", "true");
        //裁剪框的比例 x:y=1:1
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", yxRatio);
        //這則是裁切的照片大小
        intent.putExtra("outputX", 200);
        intent.putExtra("outputY", 200*yxRatio);
        //intent.putExtra("outputFormat", "JPEG");

        //返回路徑
        intent.putExtra("return-data", false);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

        Log.d(LOG_TAG,"activity.startActivityForResult AddPhotoController.CODE_PHOTO_CROP="+ AddPhotoController.CODE_PHOTO_CROP);
        activity.startActivityForResult(intent, AddPhotoController.CODE_PHOTO_CROP);
        return file;
    }


    //add by George on 2016/11/8.
    public Uri getUriFromFile(File file){
        Log.d(LOG_TAG,"getUriFromFile file="+file.getAbsolutePath());
        Uri uri;
        if (Build.VERSION.SDK_INT >= 23){
            uri = FileProvider.getUriForFile(activity,
                    AddPhotoController.provider_Authorities, file);
        } else {
            uri = Uri.fromFile(file);
        }
        Log.d(LOG_TAG,"getUriFromFile uri="+uri.toString());
        return uri;
    }

    //save photo
    public String savePhoto(Bitmap bitmap, final String fileName){
        File file = getPhotoFile(fileName);
        if (file==null||!file.exists()){
            Log.e(LOG_TAG, "savePhoto getPhotoFile fail fileName="+fileName);
            return null;
        }
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        //store image file on media store provider
        addImageToGallery(file.getAbsolutePath());

        // 20161213
        return file.getAbsolutePath();
    }
    //add by George on 2016/9/8.
    public String savePhoto(byte[] resource, String fileName){
        File file = getPhotoFile(fileName);
        if (file==null||!file.exists()){
            Log.e(LOG_TAG, "savePhoto getPhotoFile fail fileName="+fileName);
            return null;
        }
        try {
            BufferedOutputStream s = new BufferedOutputStream(new FileOutputStream(file));
            s.write(resource);
            s.flush();
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        //store image file on media store provider
        addImageToGallery(file.getAbsolutePath());

        // 20161213
        return file.getAbsolutePath();
    }

    public void addImageToGallery(String filePath) {
        //store image file on media store provider
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.MediaColumns.DATA, filePath);
        activity.getContentResolver().insert(isExist_SDCard ? MediaStore.Images.Media.EXTERNAL_CONTENT_URI : MediaStore.Images.Media.INTERNAL_CONTENT_URI, values);
    }
    //判斷sd記憶卡是否存在
    boolean isExist_SDCard = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
    //create photo file
    private File getPhotoFile(String name) {
        if (isExist_SDCard) {
            //File imagePath = new File(activity.getFilesDir(), "TyVillage/images");
            File imagePath = new File(Environment.getExternalStorageDirectory(), AddPhotoController.photoPath);
            if (!imagePath.exists()){
                if (!imagePath.mkdirs()){
                    Log.e(LOG_TAG,"imagePath.mkdirs() fail");
                    return null;
                }
            }
            //get name
            File tempFile=null;
            strPhotoName="";
            int cnt=1;
            do{
                if(cnt>100) return null;//too many filename exist
                String photoName = getStrPhotoName(name);
                if(photoName.equals(strPhotoName)){
                    if (!name.isEmpty()) {
                        photoName = getStrPhotoName( name + "_" + cnt++);
                    } else {
                        photoName = getStrPhotoName( name );
                    }
                }
                strPhotoName=photoName;
                tempFile = createNewFile(imagePath, photoName);//null if file already exist
            } while (tempFile==null);
            strPhotoPath = tempFile.getAbsolutePath();
            Log.d(LOG_TAG, "strPhotoPath =" + strPhotoPath);
            return tempFile;
        }else {
            Toast.makeText(activity, AddPhotoController.MSG_NO_EXTERNAL_STORAGE, Toast.LENGTH_SHORT).show();
        }
        return null;
    }
    private String getStrPhotoName(String name){
        //get name
        if(name.equals("")){
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dayAndTime = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.getDefault());
            String mediaInfo = dayAndTime.format(calendar.getTime());
            return mediaInfo+".jpg";
        }else {
            return name+".jpg";
        }
    }
    private File createNewFile(File pathFile, String strPhotoName){
        File tempFile = new File(pathFile, strPhotoName);
        try {
            if(!tempFile.createNewFile()) return null;//null if file already exist
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("AddPhotoController", "photocheck : " + tempFile.getPath());
        } finally {
            Log.d("AddPhotoController", "createNewFile strPhotoName: " + strPhotoName);
        }
        return tempFile;
    }

    public interface AddPhotoListener {
        void onPhoto(File file);
    }

    public File getFileFromItentUri(Uri intentUri){
        return getFilesFromUris(activity, new Uri[]{intentUri}, false)[0];
    }
    /**
     * 從多個Uri取得File物件。
     *
     * @param context     傳入Context
     * @param uris        傳入Uri陣列
     * @param mustCanRead 傳入Uri所指的路徑是否一定要可以讀取
     * @return 傳回File物件陣列，若File物件無法建立或是檔案路徑無法讀取，則對應的陣列索引位置為null
     */
    public static File[] getFilesFromUris(final Context context, final Uri[] uris, final boolean mustCanRead) {
        if (uris == null) {
            return null;
        }
        final int urisLength = uris.length;
        final File[] files = new File[urisLength];
        for (int i = 0; i < urisLength; ++i) {
            final Uri uri = uris[i];
            files[i] = getFileFromUri(context, uri, mustCanRead);
        }
        return files;
    }
    /**
     * 從Uri取得File物件。
     *
     * @param context     傳入Context
     * @param uri         傳入Uri物件
     * @param mustCanRead 傳入Uri所指的路徑是否一定要可以讀取
     * @return 傳回File物件，若File物件無法建立或是檔案路徑無法讀取，傳回null
     */
    @SuppressLint("NewApi")
    public static File getFileFromUri(final Context context, final Uri uri, final boolean mustCanRead) {
        if (uri == null) {
            return null;
        }

        // 判斷是否為Android 4.4之後的版本
        final boolean after44 = Build.VERSION.SDK_INT >= 19;
        if (after44 && DocumentsContract.isDocumentUri(context, uri)) {
            // 如果是Android 4.4之後的版本，而且屬於文件URI
            final String authority = uri.getAuthority();
            // 判斷Authority是否為本地端檔案所使用的
            if ("com.android.externalstorage.documents".equals(authority)) {
                // 外部儲存空間
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] divide = docId.split(":");
                final String type = divide[0];
                if ("primary".equals(type)) {
                    String path = Environment.getExternalStorageDirectory().getAbsolutePath().concat("/").concat(divide[1]);
                    return createFileObjFromPath(path, mustCanRead);
                } else {
                    String path = "/storage/".concat(type).concat("/").concat(divide[1]);
                    return createFileObjFromPath(path, mustCanRead);
                }
            } else if ("com.android.providers.downloads.documents".equals(authority)) {
                // 下載目錄
                final String docId = DocumentsContract.getDocumentId(uri);
                final Uri downloadUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.parseLong(docId));
                String path = queryAbsolutePath(context, downloadUri);
                return createFileObjFromPath(path, mustCanRead);
            } else if ("com.android.providers.media.documents".equals(authority)) {
                // 圖片、影音檔案
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] divide = docId.split(":");
                final String type = divide[0];
                Uri mediaUri = null;
                if ("image".equals(type)) {
                    mediaUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    mediaUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    mediaUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                } else {
                    return null;
                }
                mediaUri = ContentUris.withAppendedId(mediaUri, Long.parseLong(divide[1]));
                String path = queryAbsolutePath(context, mediaUri);
                return createFileObjFromPath(path, mustCanRead);
            }
        } else {
            // 如果是一般的URI
            final String scheme = uri.getScheme();
            String path = null;
            if ("content".equals(scheme)) {
                // 內容URI
                path = queryAbsolutePath(context, uri);
            } else if ("file".equals(scheme)) {
                // 檔案URI
                path = uri.getPath();
            }
            return createFileObjFromPath(path, mustCanRead);
        }
        return null;
    }
    /**
     * 將路徑轉成File物件。
     *
     * @param path 傳入檔案路徑
     * @return 傳回File物件，若File物件無法建立，傳回null。
     */
    public static File createFileObjFromPath(final String path) {
        return createFileObjFromPath(path, false);
    }

    /**
     * 將路徑轉成File物件。
     *
     * @param path        傳入檔案路徑
     * @param mustCanRead 傳入檔案路徑是否一定要可以讀取
     * @return 傳回File物件，若File物件無法建立或是檔案路徑無法讀取，傳回null
     */
    public static File createFileObjFromPath(final String path, final boolean mustCanRead) {
        if (path != null) {
            try {
                File file = new File(path);
                if (mustCanRead) {
                    file.setReadable(true);
                    if (!file.canRead()) {
                        return null;
                    }
                }
                return file.getAbsoluteFile();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }
    /**
     * 查詢MediaStroe Uri對應的絕對路徑。
     *
     * @param context 傳入Context
     * @param uri     傳入MediaStore Uri
     * @return 傳回絕對路徑
     */
    public static String queryAbsolutePath(final Context context, final Uri uri) {
        final String[] projection = {MediaStore.MediaColumns.DATA};
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                String imagePath = cursor.getString(index); // returns null
                cursor.close();
                Log.d(LOG_TAG,"queryAbsolutePath imagePath="+imagePath);
                return imagePath;
            }
        } catch (final Exception ex) {
            ex.printStackTrace();
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }
}
