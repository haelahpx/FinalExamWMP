package com.example.finalexamtaskwmp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnrollmentActivity extends AppCompatActivity {

    private Button addButton, summaryButton;
    private LinearLayout subjectContainer;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private int totalCredits = 0;
    private static final int MAX_CREDITS = 24;

    private final List<String> subjectList = Arrays.asList(
            "Wireless and Mobile Programming - 4",
            "Software Engineer - 4",
            "Numerical Method - 4",
            "Computer Science - 4",
            "Economic Survival - 4",
            "Web Programming - 4",
            "3DCGA - 4",
            "Artificial Intelligience - 4",
            "Survival English - 4",
            "Water - 4"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enrollment);

        subjectContainer = findViewById(R.id.subjectContainer);
        addButton = findViewById(R.id.addButton);
        summaryButton = findViewById(R.id.summaryButton);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        populateSubjects();

        addButton.setOnClickListener(v -> addSubjects());

        summaryButton.setOnClickListener(v -> {
            Intent intent = new Intent(EnrollmentActivity.this, EnrollmentSummaryActivity.class);
            startActivity(intent);
        });
    }

    private void populateSubjects() {
        for (String subjectInfo : subjectList) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(subjectInfo);
            checkBox.setTag(subjectInfo);
            subjectContainer.addView(checkBox);
        }
    }

    private void addSubjects() {
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (userId == null) {
            Toast.makeText(this, "User is not logged in!", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> selectedSubjects = new ArrayList<>();
        int totalSelectedCredits = 0;

        for (int i = 0; i < subjectContainer.getChildCount(); i++) {
            View view = subjectContainer.getChildAt(i);
            if (view instanceof CheckBox) {
                CheckBox checkBox = (CheckBox) view;
                if (checkBox.isChecked()) {
                    String subjectInfo = (String) checkBox.getTag();
                    selectedSubjects.add(subjectInfo);
                    try {
                        int credits = Integer.parseInt(subjectInfo.split("-")[1].trim());
                        totalSelectedCredits += credits;
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Error parsing credits for " + subjectInfo, Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }
        }

        if (selectedSubjects.isEmpty()) {
            Toast.makeText(this, "No subjects selected!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (totalCredits + totalSelectedCredits > MAX_CREDITS) {
            Toast.makeText(this, "Credit limit exceeded! Maximum allowed credits: " + MAX_CREDITS, Toast.LENGTH_SHORT).show();
            return;
        }

        for (String subjectInfo : selectedSubjects) {
            checkIfSubjectExists(userId, subjectInfo);
        }
    }

    private void checkIfSubjectExists(String userId, String subjectInfo) {
        db.collection("students").document(userId)
                .collection("enrolledSubjects")
                .whereEqualTo("name", subjectInfo.split("-")[0].trim())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().isEmpty()) {
                        // Subject doesn't exist, add it
                        addSubjectToFirestore(userId, subjectInfo);
                    } else {
                        Toast.makeText(EnrollmentActivity.this, subjectInfo + " is already enrolled!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addSubjectToFirestore(String userId, String subjectInfo) {
        db.collection("students").document(userId)
                .collection("enrolledSubjects")
                .add(createSubjectMap(subjectInfo))
                .addOnSuccessListener(docRef ->
                        Toast.makeText(EnrollmentActivity.this, subjectInfo + " added successfully!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(EnrollmentActivity.this, "Failed to add " + subjectInfo, Toast.LENGTH_SHORT).show());
    }

    private Map<String, Object> createSubjectMap(String subjectInfo) {
        Map<String, Object> subjectMap = new HashMap<>();
        String[] parts = subjectInfo.split("-");
        subjectMap.put("name", parts[0].trim());
        subjectMap.put("credits", Integer.parseInt(parts[1].trim()));
        return subjectMap;
    }
}
