package com.example.mapease;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mapease.model.Review;
import com.example.mapease.model.User;
import com.github.clans.fab.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class Admin_ReportDetail extends AppCompatActivity {
    TextView title, createdAt, description, reporterName;
    FloatingActionButton acceptBtn, declineBtn;
    private FirebaseDatabase database;
    private DatabaseReference reportRef, userRef;
    private FirebaseAuth auth;
    String reportId, createdAtStr, descriptionStr, reporterIdStr, titleStr, reviewIdStr, stateStr;
    ImageButton backBtn;
    ArrayList<User> userList;
    Button viewReviewBtn;
    ImageView reportState;
    int maximumReports = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_report_detail);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance("https://mapease22127072-default-rtdb.asia-southeast1.firebasedatabase.app");
        reportRef = database.getReference("reports");
        userRef = database.getReference("user");

        title = findViewById(R.id.detailReportTitle);
        createdAt = findViewById(R.id.detailCreatedAt);
        description = findViewById(R.id.detailDescription);
        reporterName = findViewById(R.id.detailReporterName);
        acceptBtn = findViewById(R.id.acceptReportButton);
        declineBtn = findViewById(R.id.declineReportButton);
        backBtn = findViewById(R.id.backButtonDetailReport);
        viewReviewBtn = findViewById(R.id.viewReviewButton);
        reportState = findViewById(R.id.reportDetailStatus);

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList = new ArrayList<>();

                for(DataSnapshot userSnapshot : snapshot.getChildren()) {
                    try {
                        User user = userSnapshot.getValue(User.class);
                        //if (user != null && user.getId().contentEquals(Id))
                        user.setId(userSnapshot.getKey());
                        userList.add(user);
                    } catch (Exception e) {
                        Log.e("RetrieveUser", "Error parsing user", e);
                    }
                }

                Intent intent = getIntent();
                createdAtStr = intent.getStringExtra("createdAt");
                createdAt.setText(formatDate(createdAtStr));
                descriptionStr = intent.getStringExtra("description");
                description.setText(descriptionStr);
                reporterIdStr = intent.getStringExtra("reporterId");
                for(User user : userList){
                    if(user != null && user.getId().contentEquals(reporterIdStr)){
                        reporterName.setText(user.getUsername());
                    }
                }
                titleStr = intent.getStringExtra("title");
                title.setText(titleStr);
                reviewIdStr = intent.getStringExtra("reviewId");
                reportId = intent.getStringExtra("Id");
                stateStr = intent.getStringExtra("state");
                int state = Integer.parseInt(stateStr);
                updateStateUI(state);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(),
                        "Failed to load users: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });

        acceptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateReportState(1); // Accept
            }
        });

        declineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateReportState(2); // Decline
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        viewReviewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), Admin_ReviewDetail.class);
                i.putExtra("reviewId", reviewIdStr);
                startActivity(i);
            }
        });

    }

    private void updateReportState(int newState) {
        if (reportId == null || reportId.isEmpty()) {
            Toast.makeText(this, "Invalid report ID", Toast.LENGTH_SHORT).show();
            return;
        }

        int currentState = Integer.parseInt(stateStr);
        if (currentState == newState) {
            Toast.makeText(this, "Report is already " + (newState == 1 ? "accepted" : "declined"), Toast.LENGTH_SHORT).show();
            return;
        }

        if (newState == 2) { // Decline: Delete the report and notify reporter
            userRef.child(reporterIdStr).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User reporter = snapshot.getValue(User.class);
                    if (reporter != null) {
                        reporter.setId(snapshot.getKey());
                        reportRef.child(reportId).removeValue()
                                .addOnSuccessListener(aVoid -> {
                                    sendReportDeletionEmail(reporter.getEmail(), reporter.getUsername(),
                                            reportId, titleStr, descriptionStr, createdAtStr, reviewIdStr);
                                    Toast.makeText(Admin_ReportDetail.this,
                                            "Report declined and deleted successfully", Toast.LENGTH_SHORT).show();
                                    stateStr = String.valueOf(newState);
                                    updateStateUI(newState);
                                    acceptBtn.setEnabled(false);
                                    declineBtn.setEnabled(false);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(Admin_ReportDetail.this,
                                            "Failed to delete report: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(Admin_ReportDetail.this,
                                "Failed to load reporter information", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(Admin_ReportDetail.this,
                            "Failed to load reporter: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else if (newState == 1) { // Accept: Check report count and act accordingly
            reportRef.child(reportId).child("state").setValue(newState)
                    .addOnSuccessListener(aVoid -> {
                        stateStr = String.valueOf(newState);
                        updateStateUI(newState);
                        acceptBtn.setEnabled(false);
                        declineBtn.setEnabled(false);

                        if (reviewIdStr == null || reviewIdStr.isEmpty()) {
                            Toast.makeText(this, "Invalid review ID", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        reportRef.orderByChild("reviewId").equalTo(reviewIdStr).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                long reportCount = snapshot.getChildrenCount();
                                if (reportCount >= maximumReports) {
                                    DatabaseReference reviewRef = database.getReference("reviews").child(reviewIdStr);
                                    reviewRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot reviewSnapshot) {
                                            Review review = reviewSnapshot.getValue(Review.class);
                                            if (review != null) {
                                                String userId = review.getUserID();
                                                String reviewContent = review.getContent();
                                                String reviewCreatedAt = review.getCreateAt();
                                                userRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                                        User reviewAuthor = userSnapshot.getValue(User.class);
                                                        if (reviewAuthor != null) {
                                                            reviewAuthor.setId(userSnapshot.getKey());
                                                            reviewRef.removeValue()
                                                                    .addOnSuccessListener(aVoid2 -> {
                                                                        reportRef.orderByChild("reviewId").equalTo(reviewIdStr)
                                                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                    @Override
                                                                                    public void onDataChange(@NonNull DataSnapshot reportSnapshot) {
                                                                                        for (DataSnapshot report : reportSnapshot.getChildren()) {
                                                                                            report.getRef().removeValue();
                                                                                        }
                                                                                        sendReviewDeletionEmail(reviewAuthor.getEmail(),
                                                                                                reviewAuthor.getUsername(),
                                                                                                reviewIdStr, reviewContent,
                                                                                                reviewCreatedAt); // Removed locationName
                                                                                        Toast.makeText(Admin_ReportDetail.this,
                                                                                                "Review and all related reports deleted successfully",
                                                                                                Toast.LENGTH_SHORT).show();
                                                                                        finish();
                                                                                    }

                                                                                    @Override
                                                                                    public void onCancelled(@NonNull DatabaseError error) {
                                                                                        Toast.makeText(Admin_ReportDetail.this,
                                                                                                "Failed to delete reports: " + error.getMessage(),
                                                                                                Toast.LENGTH_SHORT).show();
                                                                                    }
                                                                                });
                                                                    })
                                                                    .addOnFailureListener(e -> {
                                                                        Toast.makeText(Admin_ReportDetail.this,
                                                                                "Failed to delete review: " + e.getMessage(),
                                                                                Toast.LENGTH_SHORT).show();
                                                                    });
                                                        } else {
                                                            Toast.makeText(Admin_ReportDetail.this,
                                                                    "Failed to load review author", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {
                                                        Toast.makeText(Admin_ReportDetail.this,
                                                                "Failed to load review author: " + error.getMessage(),
                                                                Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            } else {
                                                Toast.makeText(Admin_ReportDetail.this,
                                                        "Failed to load review details", Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Toast.makeText(Admin_ReportDetail.this,
                                                    "Failed to load review: " + error.getMessage(),
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                } else {
                                    Toast.makeText(Admin_ReportDetail.this,
                                            "Report accepted successfully. Review has " + reportCount + " report(s).",
                                            Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(Admin_ReportDetail.this,
                                        "Failed to count reports: " + error.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to update report: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
    private void updateStateUI(int state) {
        switch (state) {
            case 0:
                reportState.setImageResource(R.drawable.ic_report_status);
                acceptBtn.setEnabled(true);
                declineBtn.setEnabled(true);
                break;
            case 1:
                reportState.setImageResource(R.drawable.ic_baseline_accept_24);
                acceptBtn.setEnabled(false);
                declineBtn.setEnabled(false);
                break;
            case 2:
                reportState.setImageResource(R.drawable.ic_decline);
                acceptBtn.setEnabled(false);
                declineBtn.setEnabled(false);
                break;
            default:
                reportState.setImageResource(R.drawable.ic_report_status);
                acceptBtn.setEnabled(true);
                declineBtn.setEnabled(true);
                break;
        }
    }
    private String formatDate(String isoTime) {
        try {
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault());
            Date pastDate = isoFormat.parse(isoTime);
            Date now = new Date();
            long diffInMillis = now.getTime() - pastDate.getTime();

            long seconds = TimeUnit.MILLISECONDS.toSeconds(diffInMillis);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis);
            long hours = TimeUnit.MILLISECONDS.toHours(diffInMillis);
            long days = TimeUnit.MILLISECONDS.toDays(diffInMillis);
            long weeks = days / 7;
            long months = days / 30;
            long years = days / 365;

            if (seconds < 60) {
                return "Just now";
            } else if (minutes < 60) {
                return minutes + "m ago";
            } else if (hours < 24) {
                return hours + "h ago";
            } else if (days < 7) {
                return days + "d ago";
            } else if (weeks < 4) {
                return weeks + "w ago";
            } else if (months < 12) {
                return months + "mo ago";
            } else {
                return years + "y ago";
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return isoTime; //return origin if fail
        }
    }
    private void send_email(String n_, String t_, String tt_, String receiver) {
        Properties properties = System.getProperties();
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "465"); // Fixed typo: "mail.smtp,port" to "mail.smtp.port"
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.auth", "true");

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                // Use an App Password for Gmail
                return new PasswordAuthentication("mmapease@gmail.com", "jwembnsnfggscemy");
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(receiver));
            message.setSubject(t_);
            message.setText("From: " + n_ + "\n" + tt_);

            // Run email sending in a background thread
            new Thread(() -> {
                try {
                    Transport.send(message);
                    runOnUiThread(() -> {
                        Log.d("Email", "Email sent successfully to " + receiver);
                    });
                } catch (MessagingException e) {
                    e.printStackTrace();
                    runOnUiThread(() -> {
                        Toast.makeText(Admin_ReportDetail.this,
                                "Failed to send email: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            }).start();
        } catch (MessagingException e) {
            Toast.makeText(this, "Error preparing email: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            throw new RuntimeException(e);
        }
    }
    private void sendReviewDeletionEmail(String email, String username, String reviewId, String reviewContent,
                                         String createdAt) { // Removed locationName parameter
        String senderName = "MapEase Team";
        String subject = "Your Review Has Been Removed from MapEase";
        String body = String.format(
                "Dear %s,\n\n" +
                        "We regret to inform you that your review for a location on MapEase has been removed. " +
                        "After careful consideration, our moderation team determined that the review violated our community guidelines due to multiple reports from other users.\n\n" +
                        "Review Details:\n" +
                        "- Review ID: %s\n" +
                        "- Posted on: %s\n" +
                        "- Content: %s...\n\n" +
                        "Best regards,\n" +
                        "The MapEase Team\n" +
                        "mmapease@gmail.com",
                username, reviewId, createdAt,
                reviewContent.substring(0, Math.min(reviewContent.length(), 50))
        );

        send_email(senderName, subject, body, email);
    }

    private void sendReportDeletionEmail(String email, String username, String reportId, String title,
                                         String description, String createdAt, String reviewId) {
        String senderName = "MapEase Team";
        String subject = "Update on Your Report Submission on MapEase";
        String body = String.format(
                "Dear %s,\n\n" +
                        "Thank you for submitting a report on MapEase. After reviewing your report (ID: %s) regarding a review (ID: %s), " +
                        "our moderation team has determined that the reported content does not violate our community guidelines. " +
                        "As a result, the report has been declined and removed from our system.\n\n" +
                        "Report Details:\n" +
                        "- Report Title: %s\n" +
                        "- Submitted on: %s\n" +
                        "- Description: %s...\n\n" +
                        "We appreciate your effort to keep MapEase a safe and reliable platform. " +
                        "If you have any questions or need further assistance, please reach out to our support team at support@mapease.com.\n\n" +
                        "Best regards,\n" +
                        "The MapEase Team\n" +
                        "mmapease@gmail.com",
                username, reportId, reviewId, title, createdAt,
                description != null ? description.substring(0, Math.min(description.length(), 50)) : ""
        );

        send_email(senderName, subject, body, email);
    }
}