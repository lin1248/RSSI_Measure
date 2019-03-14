package com.example.rssi_measure;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.FileOutputStream;
import java.io.OutputStream;

import jxl.Workbook;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    TextView tv_times;
    EditText edit_x,edit_y;
    Button btn_start,btn_delete;
    WritableSheet sheet;//创建工作表
    OutputStream os;//创建输出流
    WritableWorkbook wwb;//创建Excel工作簿
    int remaining_times,x,y;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_measure);
        imageView = findViewById(R.id.imageView);
        edit_x = findViewById(R.id.edit_x);
        edit_y = findViewById(R.id.edit_y);
        tv_times = findViewById(R.id.tv_remaining_times);
        btn_start = findViewById(R.id.btn_start);
        btn_delete = findViewById(R.id.btn_delete);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main , menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * 导出数据
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_export://监听菜单按钮

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 输出数据
     * @param str
     */
    public void export(String str) {
        try {
            //输出的excel的路径
            String filePath = Environment.getExternalStorageDirectory() + str;
            //新建立一个jxl文件，即在SDcard下生成一个test.xls
            os = new FileOutputStream(filePath);
            wwb = Workbook.createWorkbook(os);
            //创建sheet
            sheet = wwb.createSheet("RSSI_measure", 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
