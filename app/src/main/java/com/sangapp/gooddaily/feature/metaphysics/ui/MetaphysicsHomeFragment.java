package com.sangapp.gooddaily.feature.metaphysics.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.sangapp.gooddaily.R;
import com.sangapp.gooddaily.databinding.FragmentMetaphysicsHomeBinding;

public class MetaphysicsHomeFragment extends Fragment {
    private FragmentMetaphysicsHomeBinding binding;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMetaphysicsHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding.cardMaiHoa.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.maiHoaCastFragment));
        binding.cardLiuHao.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.liuHaoCastFragment));
        binding.cardDivinationHistory.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.divinationHistoryFragment));
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
