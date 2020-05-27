package com.example.rssi_measure;

import android.Manifest;
import android.Manifest.permission;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.LoggingMXBean;

import jxl.Workbook;
import jxl.write.Number;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

import static android.Manifest.permission.*;
import static com.example.rssi_measure.Address.mAdress_7D19;
import static com.example.rssi_measure.Address.mAdress_A7F3;
import static com.example.rssi_measure.Address.mAdress_AD9F;
import static com.example.rssi_measure.Address.mAdress_ADD7;
import static com.example.rssi_measure.R.*;

public class MainActivity extends AppCompatActivity {
    //测试Android studio上传github
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            permission.WRITE_EXTERNAL_STORAGE,
            permission.READ_EXTERNAL_STORAGE,
            permission.ACCESS_FINE_LOCATION,
            permission.BLUETOOTH_ADMIN,
            permission.BLUETOOTH
    };

    private static boolean flag_start = false;

    MyHanlder myHanlder = new MyHanlder();

    BluetoothAdapter mBluetoothAdapter;
    private static final int request_enabled = 1;//定义一个int resultCode

    private static String TAG = "Test";
    ImageView imageView;
    TextView tv_times,tv_data;
    EditText edit_x,edit_y,edit_times;
    Button btn_start,btn_delete;
    private static WritableSheet sheet;//创建工作表
    private static OutputStream os;//创建输出流
    private static WritableWorkbook wwb;//创建Excel工作簿
    private static String x,y;
    private static int remaining_times_a,remaining_times_b,remaining_times_c,remaining_times_d,total_times,remaining_times;
    File file;




    /**
      *
     检查应用程序是否允许写入存储设备
     如果应用程序不允许那么会提示用户授予权限
      * @param activity
      */
    public static void verifyStoragePermissions(Activity activity) {
    // Check if we have write permission
     int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

     if (permission != PackageManager.PERMISSION_GRANTED) {
        // We don't have permission so prompt the user
         ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
         Log.i(TAG, "verifyStoragePermissions: 成功获取权限");
          }
          else{
         Log.i(TAG, "verifyStoragePermissions: 获取失败");
     }
      }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.layout_measure);
        verifyStoragePermissions(this);
        imageView = findViewById(id.imageView);
        edit_x = findViewById(id.edit_x);
        edit_y = findViewById(id.edit_y);
        edit_times = findViewById(id.edit_times);
        tv_times = findViewById(id.tv_remaining_times);
        tv_data = findViewById(id.tv_data);
        tv_data.setMovementMethod(new ScrollingMovementMethod());
        btn_start = findViewById(id.btn_start);
        btn_delete = findViewById(id.btn_delete);
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flag_start = !flag_start;

                Message msg = Message.obtain();
                msg.what = 2;
                myHanlder.sendMessage(msg);

                btn_start.setClickable(false);
                if(!TextUtils.isEmpty(edit_times.getText()) && !TextUtils.isEmpty(edit_x.getText())
                && !TextUtils.isEmpty(edit_y.getText()) && flag_start )
                {
                    total_times = Integer.parseInt(edit_times.getText().toString());
                    remaining_times = Integer.parseInt(edit_times.getText().toString());
                    remaining_times_a = Integer.parseInt(edit_times.getText().toString());
                    remaining_times_b = Integer.parseInt(edit_times.getText().toString());
                    remaining_times_c = Integer.parseInt(edit_times.getText().toString());
                    remaining_times_d = Integer.parseInt(edit_times.getText().toString());

                    Message msg2 = Message.obtain();
                    msg2.what = 1;
                    msg2.arg1 = total_times;
                    myHanlder.sendMessage(msg2);

                    Log.i(TAG, "onClick: "+remaining_times_a);
                    Log.i(TAG, "onClick: "+remaining_times_b);
                    Log.i(TAG, "onClick: "+remaining_times_c);
                    Log.i(TAG, "onClick: "+remaining_times_d);
                    x = edit_x.getText().toString();
                    Log.i(TAG, "onClick: "+x);
                    y = edit_y.getText().toString();
                    Log.i(TAG, "onClick: "+y);
                    String str = "/"+x+"_"+y+".xls";
                    Log.i(TAG, "onClick: "+str);
                    export(str);
                }
                else
                {
                    Toast.makeText(MainActivity.this,"请输入正确的数字",Toast.LENGTH_SHORT).show();
                }
            }
        });
        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message msg = Message.obtain();
                msg.what = 5;
                myHanlder.sendMessage(msg);
            }
        });
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //mBluetoothAdapter.startLeScan(mLeScanCallback);
        //开始扫描，扫描到之后使用mLeScanCallback回调方法
        init_bluetooth();
        //初始化蓝牙要放在蓝牙适配器获取了默认适配器之后

    }


    /**
     * 创建excel
     * @param str
     */
    public void export(String str) {
        try {
            //输出的excel的路径
            String filePath = Environment.getExternalStorageDirectory()+ str;
            Log.i(TAG, "export: "+filePath);
            file = new File(filePath);
            file.createNewFile();
            //新建立一个jxl文件，即在SDcard下生成一个test.xls
            os = new FileOutputStream(filePath);
            Log.i(TAG, "export: 1");
            wwb = Workbook.createWorkbook(os);
            Log.i(TAG, "export: 2");
            //创建sheet
            sheet = wwb.createSheet("RSSI_measure", 1);
            Log.i(TAG, "export: 3");

            jxl.write.Label id1 = new jxl.write.Label(0,0,"A7F3");
            jxl.write.Label id2 = new jxl.write.Label(1,0,"ADD7");
            jxl.write.Label id3 = new jxl.write.Label(2,0,"AD9F");
            jxl.write.Label id4 = new jxl.write.Label(3,0,"7D19");

            sheet.addCell(id1);
            sheet.addCell(id2);
            sheet.addCell(id3);
            sheet.addCell(id4);
            Log.i(TAG, "export: success");
            /*
            wwb.write();
            wwb.close();
            */
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "export: error///"+e);
        }
    }


    /**
     * 初始化蓝牙设备
     */
    private void init_bluetooth() {
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "设备不支持蓝牙", Toast.LENGTH_SHORT).show();
            Log.i(TAG, "init_bluetooth: 设备不支持蓝牙");
            finish();
        } else if (!mBluetoothAdapter.isEnabled()) {
            //弹出对话框提示用户是后打开
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, request_enabled);
            Log.i(TAG, "init_bluetooth: 打开蓝牙");
        } else {
            Log.i(TAG, "init_bluetooth: 蓝牙正常工作");
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        }
    }



    /**
     * 字节数组转换成十六进制字符串
     *
     * @param /String str 待转换的ASCII字符串
     * @return String 每个Byte之间空格分隔，如: [61 6C 6B]
     */
    public static String str2HexStr(byte[] bs) {

        char[] chars = "0123456789ABCDEF".toCharArray();
        StringBuilder sb = new StringBuilder("");
        int bit;

        for (int i = 0; i < bs.length; i++) {
            bit = (bs[i] & 0x0f0) >> 4;
            sb.append(chars[bit]);
            bit = bs[i] & 0x0f;
            sb.append(chars[bit]);
            sb.append(' ');
        }
        return sb.toString().trim();
    }

    /**
     * 发射强度的补码转化为原码
     */
    public static Integer txPowerTransfer (String txPower , String id){
        int num = Integer.parseInt(txPower,16);
        int symbol = num >> 7;
        if(symbol == 1) {
            num = (num - 1) ^ 0xff;
            num = 0 - num;
            Log.d(TAG, "txPowerTransfer: " + id + ":" + num);
        }
        else {
            num = (num - 1) ^ 0xff;
            Log.d(TAG, "txPowerTransfer: " + "ID: " + id + "原始数据： " + txPower + "转换后数据: " + num );
        }
        return num;
    }


    /**
     * 提取蓝牙广播包数据
     **/
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {

            /*
              发现A7F3
              */
            if (device.getAddress().equals(mAdress_A7F3)  ){

                        if(remaining_times_a > 0){

                            jxl.write.Number num_a = new Number(0,total_times-remaining_times_a+1,rssi);

                            Message msg =Message.obtain();
                            msg.what = 4;
                            msg.obj = "A7F3第"+(total_times-remaining_times_a+1)+"次:"+rssi+"///";
                            myHanlder.sendMessage(msg);

                            try {
                                sheet.addCell(num_a);
                            } catch (WriteException e) {
                                e.printStackTrace();
                                Log.e(TAG, "onLeScan: "+ e );
                            }
                            remaining_times_a--;
                        }

/*                }
                else{
                    Log.d(TAG, "onLeScan: 发现A7F3，无效数据包:" + dataPacket);
                }*/
            }

            /*
             发现ADD7
              */
            if(device.getAddress().equals(mAdress_ADD7)){
/*                txPower = dataPacket.substring(87,89);
                intTxPower = txPowerTransfer(txPower , "ADD7");
                if( intTxPower < 0 ) {*/
                   // if(i2++ < 20)
                       // Log.i(TAG, "onLeScan: ADD7:getRSSI" + rssi);

                    if(remaining_times_b > 0){

                        jxl.write.Number num_b = new Number(1,total_times-remaining_times_b+1,rssi);

                        Message msg =Message.obtain();
                        msg.what = 4;
                        msg.obj = "ADD7第"+(total_times-remaining_times_b+1)+"次:"+rssi+"///";
                        myHanlder.sendMessage(msg);

                        try {
                            sheet.addCell(num_b);
                        } catch (WriteException e) {
                            e.printStackTrace();
                            Log.e(TAG, "onLeScan: "+ e );
                        }
                        remaining_times_b--;
                    }

/*
                }
                else{
                    Log.d(TAG, "onLeScan: 发现ADD7，无效数据包:" + dataPacket);
                }*/
            }

            /*
             发现AD9F
              */
            if(device.getAddress().equals(mAdress_AD9F)){
/*                txPower = dataPacket.substring(87,89);
                intTxPower = txPowerTransfer(txPower , "AD9F");
                if( intTxPower < 0 ) {*/
                   // if(i3++ < 20)
                       // Log.i(TAG, "onLeScan: AD9F:getRSSI" + rssi);

                    if(remaining_times_c > 0){

                        jxl.write.Number num_c = new Number(2,total_times-remaining_times_c+1,rssi);

                        Message msg =Message.obtain();
                        msg.what = 4;
                        msg.obj = "AD9F第"+(total_times-remaining_times_c+1)+"次:"+rssi+"///";
                        myHanlder.sendMessage(msg);

                        try {
                            sheet.addCell(num_c);
                        } catch (WriteException e) {
                            e.printStackTrace();
                            Log.e(TAG, "onLeScan: "+ e );
                        }
                        remaining_times_c--;
                    }

/*                }
                else{
                Log.d(TAG, "onLeScan: 发现AD9F，无效数据包:" + dataPacket);
            }*/

            }

            /*
             发现7D19
              */
            if(device.getAddress().equals(mAdress_7D19)){
/*                txPower = dataPacket.substring(87,89);
                intTxPower = txPowerTransfer(txPower , "7D19");
                if( intTxPower < 0 ) {*/

                    if(remaining_times_d > 0){

                        jxl.write.Number num_d = new Number(3,total_times-remaining_times_d+1,rssi);

                        Message msg =Message.obtain();
                        msg.what = 4;
                        msg.obj = "7D19第"+(total_times-remaining_times_d+1)+"次:"+rssi+"///";
                        myHanlder.sendMessage(msg);

                        try {
                            sheet.addCell(num_d);
                        } catch (WriteException e) {
                            e.printStackTrace();
                            Log.e(TAG, "onLeScan: "+ e );
                        }
                        remaining_times_d--;
                    }

/*                }
                else{
                    Log.d(TAG, "onLeScan: 发现7D19，无效数据包:" + dataPacket);
                }*/
            }
            if(remaining_times>remaining_times_a && remaining_times>remaining_times_b && remaining_times>remaining_times_c
            && remaining_times>remaining_times_d)
            {
                int i = Math.max(remaining_times_a ,remaining_times_b);
                int j = Math.max(remaining_times_c,remaining_times_d);
                remaining_times = Math.max(i,j);

                Message msg = Message.obtain();
                msg.what = 1;
                msg.arg1 = remaining_times;
                myHanlder.sendMessage(msg);
            }

            if(remaining_times <= 0 && flag_start){
                try {
                    wwb.write();
                    wwb.close();
                    Log.i(TAG, "onLeScan: write success!!!");

                    Message msg3 = Message.obtain();
                    msg3.what =3;
                    myHanlder.sendMessage(msg3);

                    flag_start = !flag_start;
                    btn_start.setClickable(true);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "onLeScan: "+e );
                } catch (WriteException e) {
                    e.printStackTrace();
                    Log.e(TAG, "onLeScan: "+e );
                }

            }
        }
    };

    /**
     * 更新UI
     */
    @SuppressLint("HandlerLeak")
    class MyHanlder extends Handler{
        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case 1:
                    tv_times.setText(msg.arg1+"");
                    break;

                case 2:
                    imageView.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, drawable.greenpoint));
                    break;

                case 3:
                    imageView.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, drawable.redpoint));
                    tv_data.append("测试结束");
                    break;

                case 4:
                    tv_data.append(msg.obj+"");
                    break;
                case 5:
                    tv_data.setText("");
                    break;
            }
        }
    }

    /**
     * 结束的时候释放
     * 停止获取方向数据
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy: Good Luck!");
    }

}
