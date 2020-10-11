package com.zhihuta.xiaota.ui;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.blankj.utilcode.util.ToastUtils;
import com.zhihuta.xiaota.FrdFragment;
import com.zhihuta.xiaota.R;
import com.zhihuta.xiaota.SettingFragment;
import com.zhihuta.xiaota.WeixinFragment;
import com.zhihuta.xiaota.adapter.QingceAdapter;
import com.zhihuta.xiaota.bean.basic.DianxianQingCeData;

import java.util.ArrayList;

//public class DianxianQingCe extends AppCompatActivity {
public class DianxianQingCe extends FragmentActivity implements View.OnClickListener {
    //声明四个Tab的布局文件
    private LinearLayout mTabWeixin;
    private LinearLayout mTabFrd;
    private LinearLayout mTabAddress;
    private LinearLayout mTabSetting;

    //声明四个Tab的ImageButton
    private ImageButton mWeixinImg;
    private ImageButton mFrdImg;
    private ImageButton mAddressImg;
    private ImageButton mSettingImg;

    //声明四个Tab分别对应的Fragment
    private Fragment mFragWeinxin;
    private Fragment mFragFrd;
    private Fragment mFragAddress;
    private Fragment mFragSetting;

    private LinearLayout mLayoutQingCe;
    private LinearLayout mLayoutOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_dianxian_qing_ce);
        initViews();//初始化控件
        initEvents();//初始化事件
        selectTab(0);//默认选中第一个Tab


    }

    private void initEvents() {
        //初始化四个Tab的点击事件
        mTabWeixin.setOnClickListener(this);
        mTabFrd.setOnClickListener(this);
        mTabAddress.setOnClickListener(this);
        mTabSetting.setOnClickListener(this);
    }

    private void initViews() {
        //初始化四个Tab的布局文件
        mTabWeixin = (LinearLayout) findViewById(R.id.id_tab_weixin);
        mTabFrd = (LinearLayout) findViewById(R.id.id_tab_frd);
        mTabAddress = (LinearLayout) findViewById(R.id.id_tab_address);
        mTabSetting = (LinearLayout) findViewById(R.id.id_tab_setting);

        //初始化四个ImageButton
        mWeixinImg = (ImageButton) findViewById(R.id.id_tab_weixin_img);
        mFrdImg = (ImageButton) findViewById(R.id.id_tab_frd_img);
        mAddressImg = (ImageButton) findViewById(R.id.id_tab_address_img);
        mSettingImg = (ImageButton) findViewById(R.id.id_tab_setting_img);

        //获取传递过来的信息
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        mDianxianQingCeList = (ArrayList<DianxianQingCeData>) bundle.getSerializable("mDianxianQingCeList");
        //列表
        RecyclerView mQingceRV = (RecyclerView) findViewById(R.id.rv_qingce);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        mQingceRV.setLayoutManager(manager);
        mQingceAdapter = new QingceAdapter(mDianxianQingCeList);
        mQingceRV.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        mQingceRV.setAdapter(mQingceAdapter);

        mLayoutQingCe = (LinearLayout)findViewById(R.id.layout_dianxian_qingce_id);
        mLayoutOrder = (LinearLayout)findViewById(R.id.layout_order_id);
    }

    //处理Tab的点击事件
    @Override
    public void onClick(View v) {
        //先将四个ImageButton置为灰色
        resetImgs();
        switch (v.getId()) {
            case R.id.id_tab_weixin:
                selectTab(0);//当点击Tab就选中该的Tab
                break;
            case R.id.id_tab_frd:
                selectTab(1);
                break;
            case R.id.id_tab_address:
                selectTab(2);
                break;
            case R.id.id_tab_setting:
                selectTab(3);
                break;
        }

    }

    //进行选中Tab的处理
    private void selectTab(int i) {
        //获取FragmentManager对象
        FragmentManager manager = getSupportFragmentManager();
        //获取FragmentTransaction对象
        FragmentTransaction transaction = manager.beginTransaction();
        //先隐藏所有的Fragment
        hideFragments(transaction);

//        Button addDianxianQinCeButton = (Button) findViewById(R.id.button5);///
//        Button showDianxianQinCeButton = (Button) findViewById(R.id.button6);///
//        Button addLujinMoxingButton = (Button) findViewById(R.id.button4);///
//        Button showLujingMoxingButton = (Button) findViewById(R.id.button7);///
//        Button calculateDianXianLengthButton = (Button) findViewById(R.id.button8);///
//        Button showDianXianLengthButton = (Button) findViewById(R.id.button9);///
        switch (i) {
            //当选中点击的是微信的Tab时
            case 0:
                //设置微信的ImageButton为绿色
                mWeixinImg.setImageResource(R.mipmap.tab_weixin_pressed);
                //如果微信对应的Fragment没有实例化，则进行实例化，并显示出来
                if (mFragWeinxin == null) {
                    mFragWeinxin = new WeixinFragment();
//                    transaction.add(R.id.layout_dianxian_qingce_id, mFragWeinxin);
                    transaction.add(R.id.layout_dianxian_qingce_id, mFragWeinxin);
                } else {
                    //如果微信对应的Fragment已经实例化，则直接显示出来
                    transaction.show(mFragWeinxin);
                }
                mLayoutQingCe.setVisibility(View.VISIBLE);
                mLayoutOrder.setVisibility(View.GONE);
//                ToastUtils.showShort("按下电线清单");
                Log.d("TAG", " 按下电线清单");
                Toast.makeText(this, "按下电线清单", Toast.LENGTH_SHORT).show();
                break;
            case 1:

                mLayoutQingCe.setVisibility(View.GONE);
                mLayoutOrder.setVisibility(View.VISIBLE);
                mFrdImg.setImageResource(R.mipmap.tab_find_frd_pressed);

                if (mFragFrd == null) {
                    mFragFrd = new FrdFragment();
//                    transaction.add(R.id.layout_dianxian_qingce_id, mFragFrd);
                    transaction.add(R.id.layout_order_id, mFragFrd);

                } else {
                    transaction.show(mFragFrd);
                }
//                ToastUtils.showShort("按下订单中心");
                Log.d("TAG", " 按下订单中心");
                Toast.makeText(this, "按下订单中心", Toast.LENGTH_SHORT).show();
                break;
            case 2:
                mAddressImg.setImageResource(R.mipmap.tab_address_pressed);
                if (mFragAddress == null) {
                    mFragAddress = new AddressFragment();
                    transaction.add(R.id.layout_dianxian_qingce_id, mFragAddress);
                } else {
                    transaction.show(mFragAddress);
                }
//                ToastUtils.showShort("按下路径模型");
                Log.d("TAG", " 按下路径模型");
                Toast.makeText(this, "按下路径模型", Toast.LENGTH_SHORT).show();
                break;
            case 3:
                mSettingImg.setImageResource(R.mipmap.tab_settings_pressed);
                if (mFragSetting == null) {
                    mFragSetting = new SettingFragment();
                    transaction.add(R.id.layout_dianxian_qingce_id, mFragSetting);
                } else {
                    transaction.show(mFragSetting);
                }
//                ToastUtils.showShort("按下路径计算");
                Log.d("TAG", " 按下路径计算");
                Toast.makeText(this, "按下路径计算", Toast.LENGTH_SHORT).show();
//                addDianxianQinCeButton.setVisibility(View.GONE);
//                showDianxianQinCeButton.setVisibility(View.GONE);
//                addLujinMoxingButton.setVisibility(View.GONE);
//                showLujingMoxingButton.setVisibility(View.GONE);
//                calculateDianXianLengthButton.setVisibility(View.GONE);
//                showDianXianLengthButton.setVisibility(View.GONE);
                break;
        }
        //不要忘记提交事务
        transaction.commit();
    }

    //将四个的Fragment隐藏
    private void hideFragments(FragmentTransaction transaction) {
        if (mFragWeinxin != null) {
            transaction.hide(mFragWeinxin);
        }
        if (mFragFrd != null) {
            transaction.hide(mFragFrd);
        }
        if (mFragAddress != null) {
            transaction.hide(mFragAddress);
        }
        if (mFragSetting != null) {
            transaction.hide(mFragSetting);
        }
    }

    //将四个ImageButton置为灰色
    private void resetImgs() {
        mWeixinImg.setImageResource(R.mipmap.tab_weixin_normal);
        mFrdImg.setImageResource(R.mipmap.tab_find_frd_normal);
        mAddressImg.setImageResource(R.mipmap.tab_address_normal);
        mSettingImg.setImageResource(R.mipmap.tab_settings_normal);
    }

//    private InstallActualAdapter mInstallActualAdapter;
//private ArrayList<InstallPlanData> mInstallPlanActualList = new ArrayList<>();
    private QingceAdapter mQingceAdapter;
    private ArrayList<DianxianQingCeData> mDianxianQingCeList = new ArrayList<>();
    private ArrayList<DianxianQingCeData> mDxQingceList = new ArrayList<>();

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
}