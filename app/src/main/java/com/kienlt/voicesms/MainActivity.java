package com.kienlt.voicesms;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Locale;

import android.database.Cursor;
import android.net.Uri;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    private static final int PERMISSIONS_REQUEST_READ_SMS = 2;

    private Button btnReadSMS;
    private TextView tvSMSContent;
    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnReadSMS = findViewById(R.id.btn_read_sms);
        tvSMSContent = findViewById(R.id.tv_sms_content);

        // Kiểm tra quyền truy cập ghi âm
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO);
        }

        // Kiểm tra quyền truy cập đọc tin nhắn
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS}, PERMISSIONS_REQUEST_READ_SMS);
        }

        // Khởi tạo TextToSpeech
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    // Chọn bộ giọng tiếng Việt
                    tts.setLanguage(new Locale("vi", "VN"));
                }
            }
        });

        // Xử lý sự kiện khi nhấn nút "Đọc tin nhắn"
        btnReadSMS.setOnClickListener(view -> {
            String smsContent = readSMS();
            if (smsContent != null) {
                tvSMSContent.setText(smsContent);
                speak(smsContent);
            }
        });
    }


    // Phương thức đọc tin nhắn (Tạm thời là đọc auto tin nhắn đầu tiên) và trả về nội dung của tin nhắn đó.
    private String readSMS() {
        String smsContent = null;
        //Kiểm tra có đã được cấp quyền đọc tin nhắn chưa
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED) {
            // Lấy tin nhắn đầu tiên trong hộp thư đến
            Cursor cursor = getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                smsContent = cursor.getString(cursor.getColumnIndexOrThrow("body"));
                cursor.close();
            }
        }
        return smsContent;
    }

    // Phương thức sử dụng TextToSpeech để đọc nội dung tin nhắn
    private void speak(String text) {
        if (tts != null && !tts.isSpeaking()) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Hủy TextToSpeech khi đóng ứng dụng
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }

    // Xử lý kết quả yêu cầu cấp quyền truy cập
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO || requestCode == PERMISSIONS_REQUEST_READ_SMS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Cấp quyền truy cập thành công", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Không cấp quyền truy cập", Toast.LENGTH_SHORT).show();
            }
        }
    }
}