package com.example.smartinventory;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.Serializable;

public class ProductsDetailFragment extends Fragment {
    private static final String ARG_PRODUCT = "product";

    public static ProductsDetailFragment newInstance(InventoryItem product) {
        ProductsDetailFragment fragment = new ProductsDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PRODUCT, (Serializable) product);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_products_detail, container, false);
        TextView productNameTextView = view.findViewById(R.id.productNameTextView);
        TextView quantityTextView = view.findViewById(R.id.quantityTextView);
        TextView dateTextView = view.findViewById(R.id.dateTextView);
        TextView noteTextView = view.findViewById(R.id.noteTextView);

        if (getArguments() != null) {
            InventoryItem product = (InventoryItem) getArguments().getSerializable(ARG_PRODUCT);
            if (product != null) {
                productNameTextView.setText(product.getProductName());
                quantityTextView.setText(product.getQuantity());
                dateTextView.setText(product.getDate());
                noteTextView.setText(product.getNote());
            }
        }

        return view;
    }
}
