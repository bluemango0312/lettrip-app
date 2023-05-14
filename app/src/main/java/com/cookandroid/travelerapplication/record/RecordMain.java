package com.cookandroid.travelerapplication.record;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cookandroid.travelerapplication.R;
import com.cookandroid.travelerapplication.helper.FileHelper;
import com.cookandroid.travelerapplication.task.InsertData_Travel;
import com.cookandroid.travelerapplication.task.SelectData_Course;
import com.cookandroid.travelerapplication.task.UpdateData_Travel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class RecordMain extends AppCompatActivity implements S3Uploader.OnUploadListener {

    private static final int REQUEST_CODE_PERMISSION = 100;
    private static final int REQUEST_CODE_IMAGE = 200;

    private Button uploadButton;
    private ImageView imageView;
    private ProgressBar progressBar;

    private S3Uploader s3Uploader;

    String IP_ADDRESS, user_id, travel_id;
    private EditText edittext_title;
    ArrayList<Course> courseArrayList;
    int sum = 0, number_of_courses = 0;

    Button dateBtn_start, dateBtn_end;
    Spinner spinner;
    Spinner spinner2;
    FileHelper fileHelper;
    ArrayAdapter<CharSequence> adapter;
    ArrayAdapter<CharSequence> adapter2;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter recyclerView_adapter;
    private RecyclerView.LayoutManager layoutManager;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_main);
        fileHelper = new FileHelper(this);
        uploadButton = findViewById(R.id.uploadButton_s3);
        imageView = findViewById(R.id.imageView_s3);
        progressBar = findViewById(R.id.progressBar_s3);
        s3Uploader = new S3Uploader(this);

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestImageUpload();
            }
        });

        findViewById(R.id.button_down_s3).setOnClickListener(v -> {
            String imageUrl = "https://lettripbucket.s3.ap-northeast-2.amazonaws.com/1516b285-750f-4aef-bddf-74f80d5e1cee.png";
            ImageUtils.DownloadImageTask downloadImageTask = new ImageUtils.DownloadImageTask(imageView);
            downloadImageTask.execute(imageUrl);
        });



        IP_ADDRESS = fileHelper.readFromFile("IP_ADDRESS");
        user_id = fileHelper.readFromFile("user_id");
        dateBtn_start = findViewById(R.id.dateBtn_start);
        dateBtn_end = findViewById(R.id.dateBtn_end);
        recyclerView = findViewById(R.id.RecyclerView_Record);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        travel_id = "0";
        courseArrayList = new ArrayList<>();


        //도시 선택
        adapter = ArrayAdapter.createFromResource(this, R.array.my_array_state, R.layout.spinner_layout);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner = findViewById(R.id.cityDropdown);
        spinner.setAdapter(adapter);

        adapter2 = ArrayAdapter.createFromResource(this, R.array.city_array_default, R.layout.spinner_layout);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner2 = findViewById(R.id.cityDropdown_detail);
        spinner2.setAdapter(adapter2);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id){
                switch (position) {
                    case 0:
                        setCitySpinnerAdapterItem(R.array.city_array_default);
                        break;
                    case 1:
                        setCitySpinnerAdapterItem(R.array.서울특별시);
                        break;
                    case 2:
                        setCitySpinnerAdapterItem(R.array.광주광역시);
                        break;
                    case 3:
                        setCitySpinnerAdapterItem(R.array.대구광역시);
                        break;
                    case 4:
                        setCitySpinnerAdapterItem(R.array.대전광역시);
                        break;
                    case 5:
                        setCitySpinnerAdapterItem(R.array.부산광역시);
                        break;
                    case 6:
                        setCitySpinnerAdapterItem(R.array.울산광역시);
                        break;
                    case 7:
                        setCitySpinnerAdapterItem(R.array.인천광역시);
                        break;
                    case 8:
                        setCitySpinnerAdapterItem(R.array.강원도);
                        break;
                    case 9:
                        setCitySpinnerAdapterItem(R.array.경기도);
                        break;
                    case 10:
                        setCitySpinnerAdapterItem(R.array.경상도);
                        break;
                    case 11:
                        setCitySpinnerAdapterItem(R.array.전라도);
                        break;
                    case 12:
                        setCitySpinnerAdapterItem(R.array.충청도);
                        break;
                    case 13:
                        setCitySpinnerAdapterItem(R.array.제주특별자치도);
                        break;
                    case 14:
                        setCitySpinnerAdapterItem(R.array.세종특별자치시);
                        break;
                    // 다른 case문들을 추가하여 필요한 도시 목록을 처리합니다.
                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        //날짜 지정
        dateBtn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog dialog = new Dialog(RecordMain.this);
                dialog.setContentView(R.layout.activity_record_date);

                Button close = dialog.findViewById(R.id.cancelBtn);
                close.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });


                CalendarView calendarView = dialog.findViewById(R.id.calendarView);
                calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
                    @Override
                    public void onSelectedDayChange(@NonNull CalendarView calendarView, int year, int month, int dayOfMonth) {
                        String selectedDate = year + "-" + (month + 1) + "-" + dayOfMonth;
                        dateBtn_start.setText(selectedDate);
                        //날짜 db 저장하기
                    }
                });


                Button ok = dialog.findViewById(R.id.okBtn);
                ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //받아온 데이터를 db에 저장
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
        });
        dateBtn_end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog dialog = new Dialog(RecordMain.this);
                dialog.setContentView(R.layout.activity_record_date);

                Button close = dialog.findViewById(R.id.cancelBtn);
                close.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });


                CalendarView calendarView = dialog.findViewById(R.id.calendarView);
                calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
                    @Override
                    public void onSelectedDayChange(@NonNull CalendarView calendarView, int year, int month, int dayOfMonth) {
                        String selectedDate = year + "-" + (month + 1) + "-" + dayOfMonth;
                        dateBtn_end.setText(selectedDate);
                    }
                });


                Button ok = dialog.findViewById(R.id.okBtn);
                ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //받아온 데이터를 db에 저장
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
        });
        Button button_travel_upload = findViewById(R.id.button_travel_upload);
        button_travel_upload.setOnClickListener(v -> {
            if (dateBtn_start.getText().toString().trim().equals("") || dateBtn_end.getText().toString().trim().equals("")){
                Toast.makeText(this,"시작 날짜 또는 마지막 날짜를 입력하세요",Toast.LENGTH_SHORT).show();
            } else if (spinner.getSelectedItem().toString().trim().equals("도 선택") || spinner.getSelectedItem().toString().trim().equals("시 선택")) {
                Toast.makeText(this,"도/시를 입력하세요",Toast.LENGTH_SHORT).show();
                Toast.makeText(this,"총 비용을 입력하세요",Toast.LENGTH_SHORT).show();
            } else {
                findViewById(R.id.addPlaceBtn).setVisibility(View.VISIBLE);
                button_travel_upload.setVisibility(View.INVISIBLE);
                spinner.setEnabled(false);
                spinner2.setEnabled(false);
                dateBtn_end.setEnabled(false);
                dateBtn_start.setEnabled(false);
                button_travel_upload.setVisibility(View.INVISIBLE);
                findViewById(R.id.button_travel_update).setVisibility(View.VISIBLE);
                TravelUpload();
            }
        });

        findViewById(R.id.button_travel_update).setOnClickListener(v -> {
            showConfirmationDialog();
        });

        findViewById(R.id.addPlaceBtn).setOnClickListener(v -> {
            Intent intent = new Intent(this, CourseActivity.class);
            intent.putExtra("province", spinner.getSelectedItem().toString().trim());
            intent.putExtra("city", spinner2.getSelectedItem().toString().trim());
            intent.putExtra("depart_date", dateBtn_start.getText().toString().trim());
            startActivity(intent);

        });


        requestPermissions();
    }
    private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_CODE_PERMISSION);
        }
    }

    private void requestImageUpload() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_CODE_IMAGE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_IMAGE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri imageUri = data.getData();
                if (imageUri != null) {
                    uploadImage(imageUri);
                }
            }
        }
    }

    private void uploadImage(Uri imageUri) {
        progressBar.setVisibility(View.VISIBLE);
        uploadButton.setEnabled(false);
        s3Uploader.uploadImage(imageUri, this);
    }

    public void Refresh() {
        // Record class, SelectData_Record task, RecordAdapter
        courseArrayList = new ArrayList<>();
        SelectData_Course task = new SelectData_Course(courseArrayList);
        travel_id = fileHelper.readFromFile("travel_id");
        task.execute("http://" + IP_ADDRESS + "/0503/selectdata_course.php", travel_id);
        try {
            new Handler().postDelayed(() -> {
                recyclerView_adapter = new CourseAdapter(courseArrayList, this);
                recyclerView.setAdapter(recyclerView_adapter);
            }, 1000); // 0.5초 지연 시간
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void showConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("확인");
        builder.setMessage("최종 등록 하시겠습니까?");

        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 예 버튼 클릭 시 동작
                dialog.dismiss();
                sum = 0; number_of_courses = 0;
                for (int i = 0; i < courseArrayList.size(); i++){
                    sum += Integer.parseInt(courseArrayList.get(i).getCost());
                    number_of_courses += 1;
                }
                travel_id = fileHelper.readFromFile("travel_id");
                UpdateData_Travel updateData_travel = new UpdateData_Travel();
                updateData_travel.execute("http://"+IP_ADDRESS+"/0503/updatedata_travel.php", travel_id, Integer.toString(number_of_courses) , Integer.toString(sum));
                finish();
            }
        });

        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 아니오 버튼 클릭 시 동작
                dialog.dismiss();
                // 여기에 원하는 동작을 추가하세요.
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Refresh();
    }

    private void setCitySpinnerAdapterItem(int array_resource) {
        adapter = new ArrayAdapter<>(this, R.layout.spinner_layout, (String[])getResources().getStringArray(array_resource));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner2.setAdapter(adapter);
    }

    private String getCurrentTime() {
        // 현재 시간 가져오기
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault());
        String currentTime = sdf.format(date);
        return currentTime;
    }

    private void TravelUpload(){
        String province = spinner.getSelectedItem().toString().trim();
        String city = spinner2.getSelectedItem().toString().trim();
        String number_of_courses = "0";
        String created_date = getCurrentTime();
        String visited = "0";
        String depart_date = dateBtn_start.getText().toString().trim();
        String last_date = dateBtn_end.getText().toString().trim();
        String total_cost = "0";
        InsertData_Travel insertData_travel = new InsertData_Travel();
        insertData_travel.execute("http://"+IP_ADDRESS+"/0503/InsertData_Travel.php",
                user_id,created_date,visited, depart_date,last_date, total_cost, province, city, number_of_courses);

        new Handler().postDelayed(() -> {
            String withdraw_result = insertData_travel.getReturn_string();
            if (withdraw_result.equals("실패")) {
                Toast.makeText(this, "여행 추가는 완료되었으나 travel_id를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
            } else if (withdraw_result.equals("에러")) {
                Toast.makeText(this, "여행 추가가 에러났습니다.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "여행 추가에 성공했습니다.", Toast.LENGTH_SHORT).show();
                fileHelper.writeToFile("travel_id", withdraw_result);
            }
        }, 500); // 0.5초 지연 시간
    }

    @Override
    public void onProgress(int progress) {
        progressBar.setProgress(progress);
    }

    @Override
    public void onSuccess(String imageUrl) {
        progressBar.setVisibility(View.GONE);
        uploadButton.setEnabled(true);
        Toast.makeText(this, "이미지 업로드 성공: " + imageUrl, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFailure() {
        progressBar.setVisibility(View.GONE);
        uploadButton.setEnabled(true);
        Toast.makeText(this, "이미지 업로드 실패", Toast.LENGTH_SHORT).show();
    }
}
