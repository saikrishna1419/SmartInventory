package com.example.smartinventory;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class InventoryPagerAdapter extends FragmentStateAdapter {

    public InventoryPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new ProductsFragment(); // Tab for products
        } else {
            return new PackagesFragment(); // Tab for packages
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
