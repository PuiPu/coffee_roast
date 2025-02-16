package com.example.coffeetemperature.views;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.example.coffeetemperature.R;

import java.util.ArrayList;

public class saveFileActivity extends AppCompatActivity {
    private EditText fileNameEditText, folderPathEditText, descriptionEditText;
    private Button saveButton;
    private Handler handler = new Handler(Looper.getMainLooper());

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_savefile);

        // 取得 UI 元件
        fileNameEditText = findViewById(R.id.fileNameBlank);
        folderPathEditText = findViewById(R.id.folderBlank);
        saveButton = findViewById(R.id.saveButton);

        // 設定按鈕監聽器
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fileName = fileNameEditText.getText().toString().trim();
                String folderPath = folderPathEditText.getText().toString().trim();

                // 檢查檔名或資料夾路徑是否為空
                if (TextUtils.isEmpty(fileName) || TextUtils.isEmpty(folderPath)) {
                    // 使用 Handler 顯示提示
                    handler.post(() -> Toast.makeText(saveFileActivity.this, R.string.EN_loss_folderName_fileName_caution, Toast.LENGTH_SHORT).show());
                } else {
                    // 儲存資料
                    saveData(fileName, folderPath);

                    // 跳轉回 MainActivity
                    Intent intent = new Intent(saveFileActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void saveData(String fileName, String folderPath) {
        // 這裡撰寫儲存資料的邏輯
        Toast.makeText(this, getString(R.string.EN_file_saved_successfully), Toast.LENGTH_LONG).show();
    }

    public void get_csv(ArrayList<String> temperatures) {
        // add time format to data
        // add data to csv file
        // save csv file to local storage


//        String str = "2023-11-02T10:12:00Z,25,26,27,28\n";
//        String str1 = "2023-11-02T10:12:00Z,25\n2023-11-02T10:13:00Z,26\n2023-11-02T10:14:00Z,27\n";
//        String str2 = "2023-11-02T10:15:00Z,28";
        // String path = Environment.getExternalStorageDirectory().getAbsolutePath()+ "/" +folder;
        //String path = Environment.getExternalStorageDirectory().getAbsolutePath();

        //String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+"folder1";
        //FileOutputStream fos = null;
        //try{
        //fos = openFile(filename,file_type);
        //fos.close();
        //}catch (Exception e){
        //    System.out.println("Error: " + e.toString());
        //}
        //BufferedWriter writer = new BufferedWriter(new  File(path),
        //.csv file
        //FileWriter file = new FileWriter(path,true);

        //file.close();
        // save csv file to local storage
        //String path = Environment.getExternalStorageDirectory().getAbsolutePath()+ "/" + folder;
        // save file to local storage
    }
}