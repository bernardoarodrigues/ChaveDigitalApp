package com.control.chavedigital;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class UsersFragments extends FragmentStateAdapter {

    // Constructor
    public UsersFragments(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    // Creates Fragment
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 1) {
            return new DeviceSolic();
        }

        return new DeviceUsers();
    }

    // Tabs count
    @Override
    public int getItemCount() {
        return 2;
    }

}
