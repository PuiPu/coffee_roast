# 這樣 isConfirm 會產生 race condition
```java
package com.example.coffeetemperature.views;  
  
import android.app.AlertDialog;  
import android.content.Context;  
import android.widget.Toast;  
  
import com.example.coffeetemperature.R;  
  
import java.util.function.Consumer;  
  
public class confirmDialog {  
    private String title;  
    private String message;  
    private boolean isConfirm;  
    public Context context;  
    AlertDialog.Builder builder;  
  
    public confirmDialog(Context context, String title, String message) {  
        this.title = title;  
        this.message = message;  
        this.context = context;  
        this.builder = new AlertDialog.Builder(context);  
        ConfirmationDialog(result -> {  
            isConfirm = result;  
        });  
    }  
    public boolean isConfirm() {  
        Toast.makeText(context, "isConfirm: " + isConfirm, Toast.LENGTH_SHORT).show();  
        return isConfirm;  
    }  
  
    private void ConfirmationDialog(Consumer<Boolean> callback) {  
        // 建立 AlertDialog        builder.setTitle(title);  
        builder.setMessage(message);  
  
        // 設定 "確定" 按鈕  
        builder.setPositiveButton(R.string.EN_accept, (dialog, which) -> {  
            callback.accept(true); // 回傳 true        });  
  
        // 設定 "取消" 按鈕  
        builder.setNegativeButton(R.string.EN_deny, (dialog, which) -> {  
            callback.accept(false); // 回傳 false        });  
  
        builder.create().show();//創建並顯示AlertDialog  
    }  
}
```
# nRF 跟我做的 android 不會自動更新 data 是因為
```c++
    pCharacteristic = pService->createCharacteristic(
        CHARACTERISTIC_UUID,
        BLECharacteristic::PROPERTY_NOTIFY  |
        BLECharacteristic::PROPERTY_INDICATE |
        BLECharacteristic::PROPERTY_READ |
        BLECharacteristic::PROPERTY_WRITE
    );
```
沒有加上 **BLECharacteristic::PROPERTY_INDICATE** 