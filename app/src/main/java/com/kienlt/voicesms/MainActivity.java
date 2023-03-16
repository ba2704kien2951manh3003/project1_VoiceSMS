package com.kienlt.voicesms;
// Import các thư viện và khai báo biến cho các thành phần UI, các hằng số yêu cầu quyền truy cập, và TextToSpeech (chuyển văn bản thành giọng nói).
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
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


import java.text.SimpleDateFormat;
import java.util.Date;
import android.app.AlertDialog;
import android.content.DialogInterface;



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
        btnReadSMS.setOnClickListener(view -> {
            readSMS();
        });
        }

        // Phương thức speak() được sử dụng để đọc nội dung tin nhắn bằng TextToSpeech.
        private void speak(String text) {
            if (tts != null && !tts.isSpeaking()) {
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
            }
        }



    private String readSMS() {
        final String[] smsContent = new String[1];
        // Kiểm tra quyền đọc tin nhắn
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED) {

            // Lấy danh sách tin nhắn trong hộp thư đến
            Cursor cursor = getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);

            // Tạo mảng để chứa danh sách tin nhắn
            ArrayList<String> smsList = new ArrayList<>();

            // Đọc từng tin nhắn và thêm vào mảng
            while (cursor != null && cursor.moveToNext()) {
                String sender = cursor.getString(cursor.getColumnIndexOrThrow("address"));
                String body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
                long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow("date"));

                // Định dạng thời gian dưới dạng ngày/giờ/phút/giây
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
                String date = sdf.format(new Date(timestamp));

                String sms =
                        "Người gửi: " + sender + "\n" +
                        "Vào lúc: " + date   + "\n" +
                        "Nội dung tin nhắn: " + body   + "\n";
                smsList.add(sms);
            }

            // Hiển thị danh sách tin nhắn để người dùng chọn
            final CharSequence[] items = smsList.toArray(new CharSequence[smsList.size()]);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("CHỌN TIN NHẮN MUỐN ĐỌC");
            builder.setItems(items, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    // Lấy tin nhắn được chọn
                    String selectedSMS = items[item].toString();
                    smsContent[0] = selectedSMS;
                    //Đọc tin nhắn lên
                    //In tin nhắn lên màn hình
                    tvSMSContent.setText(smsContent[0]);
                    speak(smsContent[0]);
                }
            });
            AlertDialog alert = builder.create();
            alert.show();

            // Đóng con trỏ
            if (cursor != null) {
                cursor.close();
            }
        }
        return smsContent[0];
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