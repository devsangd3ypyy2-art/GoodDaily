package com.sangapp.gooddaily.ui.common;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.navigation.Navigation;

import com.sangapp.gooddaily.R;
import com.sangapp.gooddaily.ui.featurehub.FeatureManagerFragment;

/** Navigation helpers for module-owned advanced tools. */
public final class FeatureNavigator {
    private FeatureNavigator() {}

    public static void open(@NonNull View view, @IdRes int destination) {
        Navigation.findNavController(view).navigate(destination);
    }

    public static void openFeature(@NonNull View view, @NonNull String featureKey) {
        Bundle args = new Bundle();
        args.putString(FeatureManagerFragment.ARG_FEATURE, featureKey);
        Navigation.findNavController(view).navigate(R.id.featureManagerFragment, args);
    }
}
