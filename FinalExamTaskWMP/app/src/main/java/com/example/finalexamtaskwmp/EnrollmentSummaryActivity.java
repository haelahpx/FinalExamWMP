package com.example.finalexamtaskwmp;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class EnrollmentSummaryActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView summaryTextView;
    private int totalCredits = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enrollment_summary);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        summaryTextView = findViewById(R.id.summaryTextView);

        loadEnrollmentSummary();
    }

    private void loadEnrollmentSummary() {
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("students").document(userId)
                .collection("enrolledSubjects")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<String> enrolledSubjects = new ArrayList<>();
                        totalCredits = 0;

                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            String subjectName = doc.getString("name");
                            Long credits = doc.getLong("credits");

                            if (subjectName != null && credits != null) {
                                enrolledSubjects.add(subjectName + " (" + credits + " credits)");
                                totalCredits += credits.intValue();
                            }
                        }

                        if (!enrolledSubjects.isEmpty()) {
                            StringBuilder summary = new StringBuilder("Enrolled Subjects:\n");
                            for (String subject : enrolledSubjects) {
                                summary.append("- ").append(subject).append("\n");
                            }
                            summary.append("\nTotal Credits: ").append(totalCredits);
                            summaryTextView.setText(summary.toString());
                        } else {
                            summaryTextView.setText("No subjects enrolled yet!");
                        }
                    } else {
                        Toast.makeText(this, "Failed to load enrolled subjects", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
