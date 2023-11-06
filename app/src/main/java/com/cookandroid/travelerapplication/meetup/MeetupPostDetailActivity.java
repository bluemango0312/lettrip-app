package com.cookandroid.travelerapplication.meetup;

import static org.jetbrains.anko.Sdk27PropertiesKt.setImageResource;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.cookandroid.travelerapplication.R;
import com.cookandroid.travelerapplication.helper.FileHelper;
import com.cookandroid.travelerapplication.task.InsertData_Poke;
import com.cookandroid.travelerapplication.task.SelectData_Poke;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MeetupPostDetailActivity extends AppCompatActivity implements SelectData_Poke.AsyncTaskCompleteListener, InsertData_Poke.AsyncTaskCompleteListener {
    ImageButton backBtn;
    ImageView chatBtn;
    ImageView gpsInfo;
    TextView city1;
    TextView city2;
    Button edit;
    Button delete;
    TextView meetupDate;
    ImageView profilePhoto;
    TextView userName;
    ImageView userSex;
    TextView userBirth;
    TextView contents;
    ImageView pokeBtn;
    TextView pokeNumTextView;
    String IP_ADDRESS, user_id;
    int resultSize = 0;
    String message = "초기화메시지"; //poke 한줄메시지
    private PopupWindow popupWindow;
    String meet_up_post_id = "";
    MeetupPost meetupPost;
    TextView postTitle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meetup_post_detail);
        FileHelper fileHelper = new FileHelper(this);
        IP_ADDRESS = fileHelper.readFromFile("IP_ADDRESS");
        user_id = fileHelper.readFromFile("user_id");
        meetupPost = new MeetupPost(); // 오류 방지를 위한 초기화

        backBtn = findViewById(R.id.backBtn);
        chatBtn = findViewById(R.id.chatBtn);
        gpsInfo = findViewById(R.id.gpsInfo);
        city1 = findViewById(R.id.city1);
        city2 = findViewById(R.id.city2);
        edit = findViewById(R.id.editBtn);
        delete = findViewById(R.id.trashBtn);
        meetupDate = findViewById(R.id.dateSelectTextView);
        profilePhoto = findViewById(R.id.profilePhoto);
        userName = findViewById(R.id.userName);
        userSex = findViewById(R.id.userSex);
        userBirth = findViewById(R.id.userBirth);
        contents = findViewById(R.id.meetupPostContext);
        pokeBtn = findViewById(R.id.pokeBtn);
        pokeNumTextView = findViewById(R.id.pokeNumTextView);
        postTitle = findViewById(R.id.postTitle);

        //밋업포스트 불러오기
        Intent intent = getIntent();
        meetupPost = (MeetupPost) intent.getSerializableExtra("meetup_post");

        ArrayList<PokeItem> pokeItemArrayList = new ArrayList<>();
        SelectData_Poke task = new SelectData_Poke(pokeItemArrayList, this);
        task.execute("http://" + IP_ADDRESS + "/1028/SelectData_Poke.php", meetupPost.getMeet_up_post_id());

        if(!meetupPost.getUser_id().equals(user_id)){
            // 자신의 글이 아니라면
            edit.setVisibility(View.INVISIBLE);
            delete.setVisibility(View.INVISIBLE);

            pokeBtn.setVisibility(View.VISIBLE);
        }else{
            // 자신의 글이라면
            edit.setVisibility(View.VISIBLE);
            delete.setVisibility(View.VISIBLE);

            pokeBtn.setClickable(false);
            Toast.makeText(getApplicationContext(), "자신의 글은 쿸 찌를 수 없습니다.", Toast.LENGTH_SHORT).show();
        }

        pokeNumTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!meetupPost.getUser_id().equals(user_id)){
                    // 자신의 글이 아니라면
                    Toast.makeText(getApplicationContext(),"타인의 글에 찌른 목록은 볼 수 없습니다",Toast.LENGTH_SHORT).show();
                    return;
                }

                if(resultSize == 0) {
                    Toast.makeText(getApplicationContext(),"쿸 찌른 사람이 없습니다",Toast.LENGTH_SHORT).show();
                }else{
                    Intent intent = new Intent(getApplicationContext(), PokeListActivity.class);
                    intent.putExtra("meet_up_post_id",meetupPost.getMeet_up_post_id());
                    startActivity(intent);
                }
            }
        });

        //뒤로가기
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MeetupPostMainAcitivty.class);
                startActivity(intent);
            }
        });

        //db에서 데이터 받아와서 화면 구성
        String city1Text = meetupPost.getProvince();//"서울특별시";
        String city2Text= meetupPost.getCity();//"서울";
        String dateString = meetupPost.getCreated_date();//dateFormat.format(date);
        String userNameText = meetupPost.getNickname();
        String userSexText = meetupPost.getSex();
        String contentsText = meetupPost.getContent();
        String title = meetupPost.getPostTitle();
        Uri profileUri = Uri.parse(meetupPost.getImage_url());


        //- 데이터 불러오는 코드 추가**

        //불러온 데이터로 화면 업데이트
        if(meetupPost.getIs_gps_enabled().equals("1")){
            gpsInfo.setImageResource(R.drawable.meetup_post_gps_icon);
        } else{
            gpsInfo.setImageResource(R.drawable.meetup_post_nongps_icon);
        }

        city1.setText(city1Text);
        city2.setText(city2Text);

        String originalFormat = "yyyy-MM-dd HH:mm:ss.SSSSSS";
        String targetFormat = "yyyy.MM.dd";

//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        meetupDate.setText("📆 " + reformatDate(dateString, originalFormat, targetFormat));

        userName.setText(userNameText);
        if(userSexText == "FEMALE"){
            userSex.setImageResource(R.drawable.woman_icon);
        } else if(userSexText == "MALE"){
            userSex.setImageResource(R.drawable.man_icon);
        }

        String dateString2 = (meetupPost.getBirth_date().equals("null") ? "":
                reformatDate(meetupPost.getBirth_date(), originalFormat, targetFormat));
        userBirth.setText(dateString2);

        contents.setText(contentsText);
        postTitle.setText(title);
        profilePhoto.setImageURI(profileUri);

        //수정, 삭제 버튼 추가

        //쿸 찌르기
        pokeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopup();
                // 팝업창의 배경 설정
                WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
                layoutParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
                layoutParams.dimAmount = 0.7f;
                getWindow().setAttributes(layoutParams);
            }
        });
    }



    private void showPopup() {
        // 팝업을 위한 레이아웃 파일을 inflate
        View popupView = getLayoutInflater().inflate(R.layout.popup_poke, null);

        // 팝업을 위한 레이아웃을 담은 뷰를 사용하여 팝업 윈도우를 생성
        popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setOutsideTouchable(true); // 외부를 터치해도 닫히도록 설정


        // 팝업창 크기 조절
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int width = (int) (dm.widthPixels * 0.8);
        int height = (int) (dm.heightPixels * 0.3);

        popupWindow.setWidth(width);
        popupWindow.setHeight(height);

        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);

        // 팝업 내부의 버튼이나 위젯에 대한 이벤트 처리 등을 추가
        Button addPokeBtn = popupView.findViewById(R.id.addPokeBtn);
        EditText pokeMessage = popupView.findViewById(R.id.pokeMessage);

        InsertData_Poke insertData_poke = new InsertData_Poke(this);
        addPokeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                message = pokeMessage.getText().toString();
                if(message.equals("")){
                    Toast.makeText(getApplicationContext(), "메시지를 입력하세요.", Toast.LENGTH_SHORT).show();
                }else{
                    popupWindow.dismiss();
                    WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
                    layoutParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
                    layoutParams.dimAmount = 0.0f; // 배경 어둡게 설정을 해제
                    getWindow().setAttributes(layoutParams);

                    //todo:message db 저장 - poke 테이블에 추가
                    String brief_message = message;
                    String meet_up_post_id =  meetupPost.getMeet_up_post_id();
                    insertData_poke.execute("http://" + IP_ADDRESS + "/1028/InsertData_Poke.php", brief_message, meet_up_post_id, user_id);
                }


            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            // 팝업이 떠 있는지 확인하고 떠 있으면 닫습니다.
            if (popupWindow != null && popupWindow.isShowing()) {
                popupWindow.dismiss();
                return true;
            }
        }
        return super.onTouchEvent(event);
    }




    public static String reformatDate(String originalDate, String originalFormat, String targetFormat) {
        try {
            SimpleDateFormat sourceDateFormat = new SimpleDateFormat(originalFormat);
            SimpleDateFormat targetDateFormat = new SimpleDateFormat(targetFormat);

            Date date = sourceDateFormat.parse(originalDate);
            return targetDateFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }


    @Override
    public void onTaskComplete(ArrayList<PokeItem> result) {
        runOnUiThread(()->{
            pokeNumTextView.setText((result == null ? "0명이 쿸 찔렀습니다." :result.size()+"명이 쿸 찔렀습니다."));
            if(result != null){
                resultSize = result.size();
            }
        });
    }

    @Override
    public void onTaskComplete_InsertData_Poke(String result) {
        runOnUiThread(() ->{
            if (result.equals("연결 실패")){
                Toast.makeText(getApplicationContext(), "찌르기 실패입니다. serverURL을 확인하세요", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(getApplicationContext(), "찌르기 완료! poke_id" + result, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
