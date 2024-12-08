package com.example.smartinventory;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class WarehousePagerAdapter extends FragmentStateAdapter {

    public WarehousePagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return position == 0 ? new ProductsFragment() : new PackagesFragment();
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
