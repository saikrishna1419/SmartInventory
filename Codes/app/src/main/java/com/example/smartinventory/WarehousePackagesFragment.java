package com.example.smartinventory;

import android.content.Intent;
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

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class WarehousePackagesFragment extends Fragment {
    private FirebaseFirestore db;
    private RecyclerView packagesRecyclerView;
    private WarehousePackagesAdapter warehousePackagesAdapter;
    private List<String> packageList;
    private List<String> allPackagesList;
    private static final String TAG = "PackagesFragment";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_warehouse_packages, container, false);

        db = FirebaseFirestore.getInstance();
        packagesRecyclerView = view.findViewById(R.id.recyclerViewPackages);
        packagesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        packageList = new ArrayList<>();
        allPackagesList = new ArrayList<>();

        // Retrieve username from arguments
        String username = getArguments().getString("username");
        if (username != null) {
            // Pass package list and username to the adapter
            warehousePackagesAdapter = new WarehousePackagesAdapter(packageList, username, getContext());
            packagesRecyclerView.setAdapter(warehousePackagesAdapter);
            fetchTrackingIds(username);
        } else {
            Toast.makeText(getContext(), "Username not found", Toast.LENGTH_SHORT).show();
        }

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

        return view;
    }

    private void fetchTrackingIds(String username) {
        CollectionReference trackingIdRef = db.collection("users").document(username).collection("inventory");

        trackingIdRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String trackingId = document.getId();
                    allPackagesList.add(trackingId);
                    packageList.add(trackingId);
                }
                warehousePackagesAdapter.notifyDataSetChanged();
            } else {
                Log.e(TAG, "Error fetching tracking IDs", task.getException());
                Toast.makeText(getContext(), "Error fetching tracking IDs", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterPackages(String query) {
        packageList.clear();
        if (query.isEmpty()) {
            packageList.addAll(allPackagesList);
        } else {
            for (String trackingId : allPackagesList) {
                if (trackingId.toLowerCase().contains(query.toLowerCase())) {
                    packageList.add(trackingId);
                }
            }
        }
        warehousePackagesAdapter.notifyDataSetChanged();
    }
}

