package com.zhihuta.xiaota.ui;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.zhihuta.xiaota.R;
import com.zhihuta.xiaota.adapter.DistanceAdapter;
import com.zhihuta.xiaota.bean.basic.DistanceData;
import com.zhihuta.xiaota.bean.basic.LujingData;
import com.zhihuta.xiaota.common.Constant;
import com.zhihuta.xiaota.net.Network;
import com.zhihuta.xiaota.util.ShowMessage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import static java.nio.file.Paths.get;

public class LujingActivity extends AppCompatActivity {

    private static String TAG = "LujingActivity";
    private DistanceAdapter mDistanceAdapter;
    private ArrayList<DistanceData> mDistanceList = new ArrayList<>();
    private RecyclerView mDistanceRV;

    private Network mNetwork;
    private GetLujingDistanceListHandler getLujingDistanceListHandler;

    private Button mButtonScanToAddXianduan; // 扫码去添加线段
    private Button mButtonRelateDx; // 去关联电缆电线
    private Button mButtonOk; // 完成按钮 (包括： 全新新建、修改、基于旧路径新建)

    private int mRequestCodeFromMain =0 ; //标记, 来自Main界面， 是 全新新建/修改/基于旧的新建
    private static final int REQUEST_CODE_SCAN_QRCODE_START = 1;
    private static final int REQUEST_CODE_RELATEd_DX =2;

    /**
     * 基于旧路径 新建路径, 需要对新路径赋值一次旧路径的间距
     */
    private boolean isDistanceSetted = false; //只要对

//    private LujingData mNewLujing = new LujingData(); //新建的路径
//    private LujingData mLujingDataToBeModified = new LujingData(); //待修改的路径
//    private LujingData mOldBasedNewLujing = new LujingData(); //基于旧路径 新建路径

    private LujingData mLujing;
    private int oldLujingID; ///旧路径的ID，在基于旧路径新建路径时，用到旧路径的间距。

    TextInputEditText lujingNameTv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_lujing);

        mNetwork = Network.Instance(getApplication());
        lujingNameTv = (TextInputEditText) findViewById(R.id.inputEditText_lujingName);

        getLujingDistanceListHandler = new GetLujingDistanceListHandler();
        //返回前页按钮
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        getDataFromPrev();
        showDistanceList();
        initViews();//初始化控件

    }
    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        getGetLujingDistanceList(intent);
    }

    /**
     * 从前一个界面 收集到的数据
     */
    private void getDataFromPrev(){

        Intent intent = getIntent();
        mRequestCodeFromMain = (int) intent.getExtras().getSerializable("requestCode");
        mLujing = (LujingData) intent.getExtras().getSerializable("mLujingToPass");
        //因为路径名称在main就决定了，不可改
        lujingNameTv.setEnabled(false);
        lujingNameTv.setText(mLujing.getName());
        if (mRequestCodeFromMain == Constant.REQUEST_CODE_ADD_TOTAL_NEW_LUJING){
//            mNewLujing
        } else if(mRequestCodeFromMain == Constant.REQUEST_CODE_MODIFY_LUJING ){
            this.setTitle("编辑路径");
            getGetLujingDistanceList(intent);
        }
        // 在基于已有路径 新建路径, 要把旧的间距加到新的路径
        else if(mRequestCodeFromMain == Constant.REQUEST_CODE_ADD_NEW_LUJING_BASE_ON_EXIST ){
            getGetLujingDistanceList(intent);
            oldLujingID = (int) intent.getExtras().getSerializable("oldLujingID");
            Log.i(TAG,"oldLujingID: " + oldLujingID);


        }
    }
    private void initViews() {
        /**
         * 扫码按钮
         */
        mButtonScanToAddXianduan = (Button) findViewById(R.id.button_scan_to_add_xianduan);
        mButtonScanToAddXianduan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LujingActivity.this, ZxingScanActivity.class);
                intent.putExtra("mLujing", (Serializable) mLujing);
                //运行时权限
                if (ContextCompat.checkSelfPermission(LujingActivity.this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(LujingActivity.this,new String[]{Manifest.permission.CAMERA},1);
                }else {
                    startActivityForResult(intent, mRequestCodeFromMain);
                }

//                bundle2.putSerializable("mLujing", (Serializable) mLujing);
//                intent.putExtras(bundle2);

            }
        });

        mButtonRelateDx = (Button) findViewById(R.id.button_relate_dx);
        mButtonRelateDx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LujingActivity.this, RelatedDxActivity.class);
                Bundle bundle2 = new Bundle();

                bundle2.putSerializable("mLujing", (Serializable) mLujing);
                intent.putExtras(bundle2);
                startActivityForResult(intent, REQUEST_CODE_RELATEd_DX);
            }
        });

        mButtonOk = (Button) findViewById(R.id.button_create_lj);
        mButtonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 不用传数据回主界面，每个界面在会自己刷新。--筛选路径界面除外，筛选路径后要把筛选结果返回主界面
                LujingActivity.this.finish();
                }
        });

    }

    boolean isPathNameEmpty(){
        if(lujingNameTv.getText().toString().equals("")){
            return true;
        } else {
            return false;
        }

    }

    // 无论是 修改路径，还是基于已有路径新建路径，都需要获该路径取原有的间距列表
    private void getGetLujingDistanceList(Intent intent) {
        //获取 路径对应的间距列表
        LinkedHashMap<String, String> mPostValue = new LinkedHashMap<>();
        mPostValue.put("account", "z");
        String theUrl;
        //如果是基于旧的路径，还需要把旧路径的间距拿来。
        if(mRequestCodeFromMain == Constant.REQUEST_CODE_ADD_NEW_LUJING_BASE_ON_EXIST) {
            theUrl = Constant.getLujingDistanceListUrl.replace("lujingID", String.valueOf(oldLujingID));
        } else {
            theUrl = Constant.getLujingDistanceListUrl.replace("lujingID", String.valueOf(mLujing.getId()));
        }
        mNetwork.fetchDistanceListOfLujing(theUrl, mPostValue, getLujingDistanceListHandler);

    }
    private void showDistanceList(){
        //间距列表
        mDistanceRV = (RecyclerView) findViewById(R.id.rv_distance);
        LinearLayoutManager manager5 = new LinearLayoutManager(this);
        manager5.setOrientation(LinearLayoutManager.VERTICAL);
        mDistanceRV.setLayoutManager(manager5);
        mDistanceAdapter = new DistanceAdapter(mDistanceList, this);
        mDistanceRV.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        mDistanceRV.setAdapter(mDistanceAdapter);


        // 设置item及item中控件的点击事件
        mDistanceAdapter.setOnItemClickListener(MyItemClickListener);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish(); // back button
                return true;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 所有界面自己刷新，不要传数据
     */
    /**
     * item＋item里的控件点击监听事件
     */
    private DistanceAdapter.OnItemClickListener MyItemClickListener = new DistanceAdapter.OnItemClickListener() {

        @Override
        public void onItemClick(View v, DistanceAdapter.ViewName viewName, int position) {
            //viewName可区分item及item内部控件
            switch (v.getId()) {
                case R.id.button_distance_up:
                    Toast.makeText(LujingActivity.this,"你点击了 Up 按钮"+(position+1),Toast.LENGTH_SHORT).show();
//                    gotoAddNewLujing(Constant.REQUEST_CODE_MODIFY_LUJING, mLujingList.get(position));
                    break;
                case R.id.button_distance_down:
//                    gotoAddNewLujing(Constant.REQUEST_CODE_ADD_NEW_LUJING_BASE_ON_EXIST,null);
                    Toast.makeText(LujingActivity.this,"你点击了 Down 按钮"+(position+1),Toast.LENGTH_SHORT).show();
                    break;
                case R.id.button_distance_delete:
                    Toast.makeText(LujingActivity.this, "你点击了 删除路径按钮" + (position + 1), Toast.LENGTH_SHORT).show();
                    //TODO 警告之后再删除??
                    mDistanceList.remove(position);
                    mDistanceAdapter.notifyDataSetChanged();
                    break;
                default:
                    Toast.makeText(LujingActivity.this, "你点击了item按钮" + (position + 1), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
        @Override
        public void onItemLongClick(View v) {

        }
    };


    @SuppressLint("HandlerLeak")
    class GetLujingDistanceListHandler extends Handler {
        @Override
        public void handleMessage(final Message msg) {
//            if(mLoadingProcessDialog != null && mLoadingProcessDialog.isShowing()) {
//                mLoadingProcessDialog.dismiss();
//            }

            if (msg.what == Network.OK) {
                mDistanceList = (ArrayList<DistanceData>) msg.obj;
                if (mDistanceList == null) {
                    Log.d(TAG, "获取路径: " + "间距获取异常"  );
                } else {
                    Log.d(TAG, "获取路径: size: " + mDistanceList.size());
                    if (mDistanceList.size() == 0) {
                        Toast.makeText(LujingActivity.this, "该路径的间距数量为0！", Toast.LENGTH_SHORT).show();
                    }
                    mDistanceAdapter = new DistanceAdapter(mDistanceList, LujingActivity.this);
                    mDistanceRV.addItemDecoration(new DividerItemDecoration(LujingActivity.this, DividerItemDecoration.VERTICAL));
                    mDistanceRV.setAdapter(mDistanceAdapter);
                    mDistanceAdapter.notifyDataSetChanged();
                    // 设置item及item中控件的点击事件
                    mDistanceAdapter.setOnItemClickListener(MyItemClickListener);

                    /**
                     * 如果是基于旧的新建，还要加间距到新路径中去
                     */
                    if( !isDistanceSetted) {
                        if (mRequestCodeFromMain == Constant.REQUEST_CODE_ADD_NEW_LUJING_BASE_ON_EXIST) {
                            LinkedHashMap<String, String> mPostValue = new LinkedHashMap<>();
                            for (int k = 0; k < mDistanceList.size(); k++) {
                                String url = Constant.putLujingDistanceUrl.replace("lujingID", String.valueOf(oldLujingID));
                                mNetwork.putLujingDistance(url, mPostValue, new PutLujingDistanceHandler());
                            }
                            isDistanceSetted = true;
                        }
                    }

                }
            } else {
                String errorMsg = (String) msg.obj;
                Log.d(TAG, errorMsg);
                Toast.makeText(LujingActivity.this, "获取该路径的 间距列表失败！" + errorMsg, Toast.LENGTH_SHORT).show();
            }
        }
    }
    @SuppressLint("HandlerLeak")
    class PutLujingDistanceHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == Network.OK) {
                ShowMessage.showToast(LujingActivity.this,"添加间距成功！",ShowMessage.MessageDuring.SHORT);
//                //把扫码新加的各个间距加入间距列表
//                mDistanceList.add(list.get(i));
//                mDistanceAdapter.notifyDataSetChanged();

            }else {

                if( msg.obj != null){
                    if( msg.obj.toString().equals("PATH_QRCODE_EXIST")){
                        ShowMessage.showDialog(LujingActivity.this,"异常！该路径已包含了该二维码" ); //
                    } else {
                        ShowMessage.showDialog(LujingActivity.this,"出错！" ); //
                    }
                }
            }
        }
    }

}