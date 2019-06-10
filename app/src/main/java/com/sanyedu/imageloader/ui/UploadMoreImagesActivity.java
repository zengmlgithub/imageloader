package com.sanyedu.imageloader.ui;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.lzy.imagepicker.ImagePicker;
import com.lzy.imagepicker.bean.ImageItem;
import com.lzy.imagepicker.ui.ImageGridActivity;
import com.lzy.imagepicker.ui.ImagePreviewDelActivity;

import com.nanchen.compresshelper.CompressHelper;
import com.sanyedu.imageloader.R;
import com.sanyedu.imageloader.adapter.ImagePickerAdapter;
import com.sanyedu.imageloader.views.dialog.PictureChooseDialog;
import com.sanyedu.imageloader.views.glideTransform.GlideImageLoader;
import com.yanzhenjie.nohttp.NoHttp;
import com.yanzhenjie.nohttp.RequestMethod;
import com.yanzhenjie.nohttp.rest.OnResponseListener;
import com.yanzhenjie.nohttp.rest.Request;
import com.yanzhenjie.nohttp.rest.RequestQueue;
import com.yanzhenjie.nohttp.rest.Response;
import com.yanzhenjie.nohttp.rest.SimpleResponseListener;
import com.yanzhenjie.nohttp.rest.StringRequest;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionNo;
import com.yanzhenjie.permission.PermissionYes;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class UploadMoreImagesActivity extends AppCompatActivity implements ImagePickerAdapter.OnRecyclerViewItemClickListener {

    private EditText contentEt;
    private TextView leftTv;
    private RecyclerView recyclerview;
    private ArrayList<ImageItem> selImageList; //当前选择的所有图片
    private int maxImgCount = 3;
    public static final int IMAGE_ITEM_ADD = -1;
    public static final int REQUEST_CODE_SELECT = 100;
    public static final int REQUEST_CODE_PREVIEW = 101;
    private ImagePickerAdapter adapter;
    public RequestQueue requestQueue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_more_images);
        requestQueue = NoHttp.newRequestQueue();
        findViewById(R.id.publish).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //上传到服务器
//                upLoadImages();
            }
        });
        initImagePickerMulti();
        initView();
    }

    /**
     * 上传图片到服务器
     */
    private void upLoadImages() {
        StringRequest request = new StringRequest("rul", RequestMethod.POST);
        request.addHeader("", "");
        request.add("content",contentEt.getText().toString());
        for (int i = 0; i < selImageList.size(); i++) {
            ImageItem imageItem = selImageList.get(i);
            String newPath = imageItem.path;
            File oldFile = new File(newPath);
            File newFile = CompressHelper.getDefault(getApplicationContext()).compressToFile(oldFile); //文件的压缩
            request.add("imgs"+(i+1),newFile);
        }
        requestData(request, new SimpleResponseListener<String>() {
            @Override
            public void onStart(int what) {
                super.onStart(what);
            }

            @Override
            public void onSucceed(int what, Response<String> response) {
                super.onSucceed(what, response);
                //上传成功
            }
        });
    }

    private void initImagePickerMulti() {
        ImagePicker imagePicker = ImagePicker.getInstance();
        imagePicker.setImageLoader(new GlideImageLoader());   //设置图片加载器
        imagePicker.setShowCamera(true);                      //显示拍照按钮
        imagePicker.setCrop(false);                            //允许裁剪（单选才有效）
        imagePicker.setSaveRectangle(true);                   //是否按矩形区域保存
        imagePicker.setSelectLimit(maxImgCount);              //选中数量限制
        imagePicker.setMultiMode(true);                      //多选
//        imagePicker.setStyle(CropImageView.Style.RECTANGLE);  //裁剪框的形状
//        imagePicker.setFocusWidth(800);                       //裁剪框的宽度。单位像素（圆形自动取宽高最小值）
//        imagePicker.setFocusHeight(800);                      //裁剪框的高度。单位像素（圆形自动取宽高最小值）
//        imagePicker.setOutPutX(1000);                         //保存文件的宽度。单位像素
//        imagePicker.setOutPutY(1000);                         //保存文件的高度。单位像素
    }
    private void initView() {
        contentEt = ((EditText) findViewById(R.id.contentEt));
        leftTv = ((TextView) findViewById(R.id.leftTv));

        contentEt.setSelection(contentEt.getText().length());
        leftTv.setText(contentEt.getText().toString().length()+"/100");
        contentEt.setMaxLines(100);
        contentEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                int length = contentEt.getText().toString().length();
                if(length==100){
                    leftTv.setText(length+"/100");
                    Toast.makeText(UploadMoreImagesActivity.this,"已达到最大数", Toast.LENGTH_SHORT).show();
                }else{
                    leftTv.setText(length+"/100");
                }
            }
        });

        recyclerview = ((RecyclerView) findViewById(R.id.recyclerView));
        recyclerview.setHasFixedSize(true);
        recyclerview.setLayoutManager(new GridLayoutManager(this,4));
        selImageList = new ArrayList<>();
        adapter = new ImagePickerAdapter(this, selImageList, maxImgCount);
        adapter.setOnItemClickListener(this);
        adapter.setOnItemRemoveClick(new ImagePickerAdapter.OnItemRemoveClick() {
            @Override
            public void onItemRemoveClick() {
                adapter.setImages(adapter.getImages());
                adapter.notifyDataSetChanged();
                selImageList.clear();
                selImageList.addAll(adapter.getImages());
            }
        });
        recyclerview.setAdapter(adapter);
    }
    private Object sign = new Object();
    @Override
    protected void onDestroy() {
        requestQueue.cancelBySign(sign);
        requestQueue.stop();

        super.onDestroy();
    }
    @Override
    public void onItemClick(View view, int position) {
        switch (position){
            case IMAGE_ITEM_ADD:
                //先请求权限，再进行操作
                AndPermission.with(this)
                        .requestCode(300)
                        .permission(
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE)
                        .callback(this)
                        .start();

                break;
            default:
                //打开预览
                Intent intentPreview = new Intent(this, ImagePreviewDelActivity.class);
                intentPreview.putExtra(ImagePicker.EXTRA_IMAGE_ITEMS, (ArrayList<ImageItem>) adapter.getImages());
                intentPreview.putExtra(ImagePicker.EXTRA_SELECTED_IMAGE_POSITION, position);
                intentPreview.putExtra(ImagePicker.EXTRA_FROM_ITEMS,true);
                startActivityForResult(intentPreview, REQUEST_CODE_PREVIEW);
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
    private void chooseImage(){
        List<String> names = new ArrayList<>();
        names.add("拍照");
        names.add("从相册选择");
        showDialog(new PictureChooseDialog.SelectDialogListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0: // 直接调起相机
                        //打开选择,本次允许选择的数量
                        ImagePicker.getInstance().setSelectLimit(maxImgCount - selImageList.size());
                        Intent intent = new Intent(UploadMoreImagesActivity.this, ImageGridActivity.class);
                        intent.putExtra(ImageGridActivity.EXTRAS_TAKE_PICKERS,true); // 是否是直接打开相机
                        startActivityForResult(intent, REQUEST_CODE_SELECT);
                        break;
                    case 1:
                        //打开选择,本次允许选择的数量
                        ImagePicker.getInstance().setSelectLimit(maxImgCount - selImageList.size());
                        Intent intent1 = new Intent(UploadMoreImagesActivity.this, ImageGridActivity.class);
                        startActivityForResult(intent1, REQUEST_CODE_SELECT);
                        break;
                    default:
                        break;
                }
            }
        }, names);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == ImagePicker.RESULT_CODE_ITEMS) {
            //添加图片返回
            if (data != null && requestCode == REQUEST_CODE_SELECT) {
                ArrayList<ImageItem> images = (ArrayList<ImageItem>) data.getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);
                if (images != null){
                    selImageList.addAll(images);
                    adapter.setImages(selImageList);
                }
            }
        } else if (resultCode == ImagePicker.RESULT_CODE_BACK) {
            //预览图片返回
            if (data != null && requestCode == REQUEST_CODE_PREVIEW) {
                ArrayList<ImageItem> images = (ArrayList<ImageItem>) data.getSerializableExtra(ImagePicker.EXTRA_IMAGE_ITEMS);
                if (images != null){
                    selImageList.clear();
                    selImageList.addAll(images);
                    adapter.setImages(selImageList);
                }
            }
        }
    }

    /**
     * 发起请求
     * @param what          如果多个被同一个Listener接受结果，那么使用what区分结果。
     * @param request       请求对象。
     * @param httpListener  接受请求结果。
     * @param <T>           请求数据类型。
     */
    protected <T> void requestData(int what, Request<T> request, OnResponseListener<T> httpListener) {
        request.setCancelSign(sign);
        requestQueue.add(what, request, httpListener);
    }

    /**
     * 发起请求
     * @param request       请求对象。
     * @param httpListener  接受请求结果。
     * @param <T>           请求数据类型。
     */
    public  <T> void requestData(Request<T> request, OnResponseListener<T> httpListener) {
        request.setCancelSign(sign);
        requestQueue.add(0, request, httpListener);
    }
}
