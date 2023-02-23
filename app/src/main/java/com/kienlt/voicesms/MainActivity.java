package com.kienlt.voicesms;
// Import các thư viện và khai báo biến cho các thành phần UI, các hằng số yêu cầu quyền truy cập, và TextToSpeech (chuyển văn bản thành giọng nói).
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

    // Phương thức onCreate() được gọi khi ứng dụng được khởi động, và được sử dụng để tạo các thành phần UI và kiểm tra quyền truy cập.
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

        // Khởi tạo TextToSpeech và chọn bộ giọng tiếng Việt
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    // Chọn bộ giọng tiếng Việt
                    tts.setLanguage(new Locale("vi", "VN"));
                }
            }
        });

        // Xử lý sự kiện khi nhấn nút "Đọc tin nhắn". Đầu tiên, phương thức readSMS() được gọi để đọc nội dung tin nhắn và trả về.
        // Sau đó, nội dung tin nhắn được hiển thị trên TextView và đọc bằng TextToSpeech nếu nội dung khác null.
        btnReadSMS.setOnClickListener(view -> {
            String smsContent = readSMS();
            if (smsContent != null) {
                tvSMSContent.setText(smsContent);
                speak(smsContent);
            }
        });
    }

    // Phương thức speak() được sử dụng để đọc nội dung tin nhắn bằng TextToSpeech.
    private void speak(String text) {
        if (tts != null && !tts.isSpeaking()) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    // Phương thức readSMS() được sử dụng để đọc nội dung tin nhắn đầu tiên trong hộp thư đến và trả về nó.
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Phương thức onDestroy() được gọi khi ứng dụng được đóng lại để hủy TextToSpeech.
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }

    // Phương thức onRequestPermissionsResult() được sử dụng để xử lý kết quả yêu cầu cấp quyền truy cập.
    // Nếu được cấp quyền, ứng dụng sẽ hiển thị một thông báo thành công, ngược lại, thông báo lỗi sẽ được hiển thị.
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