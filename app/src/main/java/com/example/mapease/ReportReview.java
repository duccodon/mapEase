package com.example.mapease;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

import com.example.mapease.adapter.ReportAdapter;
import com.example.mapease.model.ReportTitle;

public class ReportReview extends AppCompatActivity {
    ImageButton backBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_report_review);

        backBtn = findViewById(R.id.reportReviewBackBtn);
        Intent i = getIntent();
        String reporterId = i.getStringExtra("reporterId");
        String reviewId = i.getStringExtra("reviewId");

        // Tạo danh sách các mục
        Resources res = getResources();
        String[] report_title = res.getStringArray(R.array.report_title);
        String[] report_description = res.getStringArray(R.array.report_description);
        ArrayList<ReportTitle> reportTitles = new ArrayList<>();
        for (int temp = 0; temp < 8; temp++) {
            reportTitles.add(new ReportTitle(report_title[temp], report_description[temp]));
        }
        /* reportTitles.add(new ReportTitle("Off topic", "Review doesn't pertain to an experience at or with this business"));
        reportTitles.add(new ReportTitle("Spam", "Review is from a bot, a fake account, or contains ads and promotions"));
        reportTitles.add(new ReportTitle("Conflict of interest", "Review is from someone affiliated with the business or a competitor's business"));
        reportTitles.add(new ReportTitle("Profanity", "Review contains swear words, has sexually explicit language, or details graphic violence"));
        reportTitles.add(new ReportTitle("Bullying or harassment", "Review personally attacks a specific individual"));
        reportTitles.add(new ReportTitle("Discrimination or hate speech", "Review has harmful language about an individual or group based on identity"));
        reportTitles.add(new ReportTitle("Personal information", "Review contains personal information, such as an address or phone number"));
        reportTitles.add(new ReportTitle("Not helpful", "Review doesn't help people decide whether to go to this place")); */

        // Kết nối ListView với Adapter
        ListView listView = findViewById(R.id.listView);
        ReportAdapter adapter = new ReportAdapter(this, reportTitles);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ReportTitle clickedReportTitle = reportTitles.get(position);
                Intent i = new Intent(getApplicationContext(), ReportSubmit.class);
                i.putExtra("title", clickedReportTitle.getTitle());
                i.putExtra("description", clickedReportTitle.getDescription());
                i.putExtra("reviewId", reviewId);
                i.putExtra("reporterId", reporterId);
                startActivity(i);
                finish();
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}