package com.sanyedu.imageloader.ui;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.lzy.imagepicker.ImagePicker;
import com.lzy.imagepicker.bean.ImageItem;
import com.lzy.imagepicker.ui.ImageGridActivity;
import com.lzy.imagepicker.view.CropImageView;
import com.sanyedu.imageloader.R;
import com.sanyedu.imageloader.views.dialog.PictureChooseDialog;
import com.sanyedu.imageloader.views.glideTransform.GlideImageLoader;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionNo;
import com.yanzhenjie.permission.PermissionYes;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView headImg;
    private int maxImgCount = 1;
    private ArrayList<ImageItem> selImageList; //当前选择的所有图片
    public static final int REQUEST_CODE_SELECT = 100;
    public static final int REQUEST_CODE_PREVIEW = 101;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        selImageList = new ArrayList<>();
        initView();
    }

    private void initView() {
        headImg = ((ImageView) findViewById(R.id.headImg));
        findViewById(R.id.changeHeadLinear).setOnClickListener(this);
        findViewById(R.id.btn).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.changeHeadLinear:
                //先请求权限，再进行操作
                AndPermission.with(MainActivity.this)
                        .requestCode(300)
                        .permission(
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE)
                        .callback(this)
                        .start();
                break;
            case R.id.btn:
                startActivity(new Intent(MainActivity.this,UploadMoreImagesActivity.class));
                break;
        }
    }


    // 成功回调的方法，用注解即可，这里的300就是请求时的requestCode。
    @PermissionYes(300)
    private void getPermissionYes(List<String> grantedPermissions) {
        // TODO 申请权限成功。
        if(AndPermission.hasPermission(this,grantedPermissions)) {
            // TODO 执行拥有权限时的下一步。
            chooseImage();
        } else {
            // 使用AndPermission提供的默认设置dialog，用户点击确定后会打开App的设置页面让用户授权。
            AndPermission.defaultSettingDialog(this, 300).show();
            // 建议：自定义这个Dialog，提示具体需要开启什么权限，自定义Dialog具体实现上面有示例代码。
        }
    }

    @PermissionNo(300)
    private void getPermissionNo(List<String> deniedPermissions) {
        // TODO 申请权限失败。
        if(AndPermission.hasPermission(this,deniedPermissions)) {
            // TODO 执行拥有权限时的下一步。
        } else {
            // 使用AndPermission提供的默认设置dialog，用户点击确定后会打开App的设置页面让用户授权。
            AndPermission.defaultSettingDialog(this, 300).show();
            // 建议：自定义这个Dialog，提示具体需要开启什么权限，自定义Dialog具体实现上面有示例代码。
        }
    }
    private PictureChooseDialog showDialog(PictureChooseDialog.SelectDialogListener listener, List<String> names) {
        PictureChooseDialog dialog = new PictureChooseDialog(this, R.style.transparentFrameWindowStyle, listener, names);
        if (!this.isFinishing()) {
            dialog.show();
        }
        return dialog;
    }
    private void chooseImage() {
        List<String> names = new ArrayList<>();
        names.add("拍照");
        names.add("从相册选择");
        showDialog(new PictureChooseDialog.SelectDialogListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0: // 直接调起相机
                        //打开选择,本次允许选择的数量
                        getImagePickerSquare().setSelectLimit(maxImgCount - selImageList.size());
                        Intent intent = new Intent(MainActivity.this, ImageGridActivity.class);
                        intent.putExtra(ImageGridActivity.EXTRAS_TAKE_PICKERS,true); // 是否是直接打开相机
                        startActivityForResult(intent, REQUEST_CODE_SELECT);
                        break;
                    case 1:
                        //打开选择,本次允许选择的数量
                        getImagePickerSquare().setSelectLimit(maxImgCount - selImageList.size());
                        Intent intent1 = new Intent(MainActivity.this, ImageGridActivity.class);
                        startActivityForResult(intent1, REQUEST_CODE_SELECT);
                        break;
                    default:
                        break;
                }
            }
        }, names);
    }
    private ImagePicker getImagePickerSquare() {//正方形
        ImagePicker imagePicker = ImagePicker.getInstance();
        imagePicker.setImageLoader(new GlideImageLoader());   //设置图片加载器
        imagePicker.setShowCamera(true);                      //显示拍照按钮
        imagePicker.setCrop(true);                            //允许裁剪（单选才有效）
        imagePicker.setSaveRectangle(true);                   //是否按矩形区域保存
        imagePicker.setMultiMode(false);                      //多选
        imagePicker.setStyle(CropImageView.Style.RECTANGLE);  //裁剪框的形状
        imagePicker.setFocusWidth(800);                       //裁剪框的宽度。单位像素（圆形自动取宽高最小值）
        imagePicker.setFocusHeight(800);                      //裁剪框的高度。单位像素（圆形自动取宽高最小值）
        imagePicker.setOutPutX(1000);                         //保存文件的宽度。单位像素
        imagePicker.setOutPutY(1000);                         //保存文件的高度。单位像素
        return imagePicker;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == ImagePicker.RESULT_CODE_ITEMS) {
            //添加图片返回
            if (data != null && requestCode == REQUEST_CODE_SELECT) {
                ArrayList<ImageItem> images = (ArrayList<ImageItem>) data.getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);
                if (images != null){
                    ImageItem imageItem = images.get(0);
                    String newPathHead = imageItem.path;
                    updateMineMessage(newPathHead);
                }
            }
        } else if (resultCode == ImagePicker.RESULT_CODE_BACK) {
            //预览图片返回
            if (data != null && requestCode == REQUEST_CODE_PREVIEW) {
                ArrayList<ImageItem> images = (ArrayList<ImageItem>) data.getSerializableExtra(ImagePicker.EXTRA_IMAGE_ITEMS);
                if (images != null){
                    ImageItem imageItem = images.get(0);
//                    String newPathHead = BitmapUtils.compressImageUpload(imageItem.path);
                    String newPathHead = imageItem.path;
                    updateMineMessage(newPathHead);
                }
            }
        }
    }

    private void updateMineMessage(String newPathHead) {
        //在这里将图片上传至服务器
        Glide.with(this).load(new File(newPathHead)).into(headImg);
    }

}
