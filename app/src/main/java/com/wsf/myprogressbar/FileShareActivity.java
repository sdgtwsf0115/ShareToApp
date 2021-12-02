package com.wsf.myprogressbar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class FileShareActivity extends Activity {
    private Context mContext;
    private TextView tvFileName;
    private TextView tvFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_share);
        mContext = FileShareActivity.this;
        tvFileName = findViewById(R.id.fileName);
        tvFilePath = findViewById(R.id.filePath);
        tvFileName.setText("文件名称:");
        tvFilePath.setText("文件路径:");
    }


    @Override
    protected void onResume() {
        super.onResume();
        shareFile();

    }

    private void shareFile() {
        try {
            ///storage/sdcard0/DCIM/Camera/IMG_20180111_104731.jpg
            //content://media/external/images/media/26549
//            if (userId == null || "".equals(userId)){
//                startActivity(new Intent(mContext,LaucherActivity.class));
//            }
            Intent intent = getIntent();
            String action = intent.getAction();
            if (action == null) {
                return;
            }
            Log.e("action:", action);
            Uri imgUri = null;
            String url = "";
            if ("android.intent.action.SEND".equals(action)) {
                Bundle extras = intent.getExtras();
                imgUri = (Uri) extras.getParcelable(Intent.EXTRA_STREAM);
                if (imgUri != null)
                    url = imgUri.toString();
            } else {
                imgUri = intent.getData();
                url = intent.getDataString();
            }
            if (!"".equals(url)) {
                url = Uri.decode(url);
                Log.e("要上传的文件地址", url);
                //文件管理路径:content://media/external/file/85139
                //微信文件路径:content://com.tencent.mm.external.fileprovider/external/tencent/MicroMsg/Download/111.doc
                //QQ文件路径:file:///storage/emulated/0/tencent/MicroMsg/Download/111.doc
                if (url.contains("content://")) { //文件管理和微信路径都是content://开头,QQ是file:
                    if (!url.contains(".fileprovider/")) {//微信路径都是用fileprovider提供的,所以如果有fileprovider就是微信,其他是文件管理
                        url = FileUtil.getFilePathFromContentUri(imgUri, FileShareActivity.this);
                    } else {
                        url = FileUtil.getFPUriToPath(mContext, imgUri);
                    }
                }
                Log.e("要上传的文件地址222", url);
                tvFileName.setText("文件名称:" + FileUtil.getFileName(url));
                tvFilePath.setText("文件路径:" + url);
//                File file = new File(listData.get(i).getFilePath());
            }
        } catch (Exception e) {
            Log.e("FileShareActivity", "fail to get file from shared", e);
        }
    }



}
