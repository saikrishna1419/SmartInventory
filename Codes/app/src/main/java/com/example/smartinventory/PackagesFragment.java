package com.example.smartinventory;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class PackagesFragment extends Fragment {
    private FirebaseFirestore db;
    private RecyclerView packagesRecyclerView;
    private PackageAdapter packageAdapter;
    private List<String> packageList;  // Assuming packageList contains tracking IDs
    private List<String> allPackagesList; // Store all packages for filtering
    private static final String TAG = "PackagesFragment";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_packages, container, false);

        db = FirebaseFirestore.getInstance();
        packagesRecyclerView = view.findViewById(R.id.recyclerViewPackages);
        packagesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        packageList = new ArrayList<>();
        allPackagesList = new ArrayList<>();
        packageAdapter = new PackageAdapter(packageList, getContext());
        packagesRecyclerView.setAdapter(packageAdapter);

        EditText searchET = view.findViewById(R.id.searchET);
        searchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterPackages(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void afterTextChanged(Editable s) {}
        });

        fetchPackageData();
        return view;
    }

    private void fetchPackageData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.e(TAG, "No authenticated user found");
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userEmail = user.getEmail();
        Log.d(TAG, "Authenticated user email: " + userEmail);

        // Fetch the userName based on the email from the "customers" collection
        db.collection("customers")
                .whereEqualTo("email", userEmail)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        String username = task.getResult().getDocuments().get(0).getString("userName");
                        Log.d(TAG, "Fetched userName: " + username);
                        fetchTrackingIds(username);  // Fetch tracking IDs
                    } else {
                        Log.e(TAG, "User not found in customers collection");
                        Toast.makeText(getContext(), "User not found", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchTrackingIds(String username) {
        CollectionReference trackingIdRef = db.collection("users").document(username).collection("inventory");

        trackingIdRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String trackingId = document.getId(); // Get the tracking ID
                    allPackagesList.add(trackingId);
                    packageList.add(trackingId);
                }
                packageAdapter.notifyDataSetChanged(); // Notify adapter for the package list update
            } else {
                Log.e(TAG, "Error fetching tracking IDs", task.getException());
                Toast.makeText(getContext(), "Error fetching tracking IDs", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterPackages(String query) {
        packageList.clear(); // Clear the current displayed list
        if (query.isEmpty()) {
            // Show all packages if search query is empty
            packageList.addAll(allPackagesList);
        } else {
            // Filter packages based on the query
            for (String trackingId : allPackagesList) {
                if (trackingId.toLowerCase().contains(query.toLowerCase())) {
                    packageList.add(trackingId);
                }
            }
        }
        packageAdapter.notifyDataSetChanged();  // Refresh RecyclerView
    }
}
