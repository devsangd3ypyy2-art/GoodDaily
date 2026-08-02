package com.sangapp.gooddaily.ui.music;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.sangapp.gooddaily.R;
import com.sangapp.gooddaily.data.local.prefs.LocalUserStore;
import com.sangapp.gooddaily.databinding.FragmentMusicBinding;
import com.sangapp.gooddaily.notification.NotificationSoundManager;
import com.sangapp.gooddaily.util.AppExecutors;
import com.sangapp.gooddaily.util.ThemeUtils;

public class MusicFragment extends Fragment {
    private FragmentMusicBinding binding;
    private LocalUserStore userStore;
    private MediaPlayer mediaPlayer;
    private Ringtone previewRingtone;
    private Uri playingUri;

    private final ActivityResultLauncher<String> notificationPermission = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            granted -> {
                if (granted) sendTestNotification();
                else toast("Bạn cần cấp quyền thông báo để Good Daily phát chuông.");
            });

    private final ActivityResultLauncher<String[]> pickAudio = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(),
            uri -> {
                if (uri == null || userStore == null) return;
                try {
                    requireContext().getContentResolver().takePersistableUriPermission(
                            uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (Exception ignored) {
                }
                String name = resolveName(uri);
                userStore.setMusic(uri.toString(), name);
                renderSelection();
                toast("Đã chọn: " + name);
            });

    private final ActivityResultLauncher<Intent> pickSystemTone = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() != android.app.Activity.RESULT_OK || result.getData() == null) return;
                Uri uri = (Uri) result.getData().getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                if (uri == null) return;
                Ringtone ringtone = RingtoneManager.getRingtone(requireContext(), uri);
                String title = ringtone == null ? "Âm báo hệ thống" : ringtone.getTitle(requireContext());
                userStore.setNotificationSound(uri.toString(), title);
                NotificationSoundManager.clearOldChannels(requireContext());
                renderSelection();
                toast("Đã đổi âm báo Good Daily.");
                sendTestNotification();
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMusicBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        userStore = new LocalUserStore(requireContext());
        int accent = ThemeUtils.getPrimaryColor(requireContext(), userStore.getThemeKey());
        int container = ThemeUtils.getContainerColor(requireContext(), userStore.getThemeKey());
        binding.musicIconBox.setCardBackgroundColor(container);
        binding.notificationIconBox.setCardBackgroundColor(container);
        binding.imgMusicIcon.setColorFilter(accent);
        binding.imgNotificationIcon.setColorFilter(accent);
        ThemeUtils.tintTonalButton(binding.btnBackMusic, requireContext(), userStore.getThemeKey());
        ThemeUtils.tintTonalButton(binding.btnPlayPause, requireContext(), userStore.getThemeKey());
        ThemeUtils.tintTonalButton(binding.btnPickAudio, requireContext(), userStore.getThemeKey());
        ThemeUtils.tintTonalButton(binding.btnUseAsNotification, requireContext(), userStore.getThemeKey());
        ThemeUtils.tintTonalButton(binding.btnChooseSystemTone, requireContext(), userStore.getThemeKey());

        renderSelection();
        binding.btnBackMusic.setOnClickListener(v -> androidx.navigation.Navigation.findNavController(v).popBackStack());
        binding.btnPickAudio.setOnClickListener(v -> pickAudio.launch(new String[]{"audio/*"}));
        binding.btnPlayPause.setOnClickListener(v -> togglePlayback());
        binding.btnStop.setOnClickListener(v -> stopPlayback());
        binding.btnUseAsNotification.setOnClickListener(v -> useCurrentSongAsNotification());
        binding.btnChooseSystemTone.setOnClickListener(v -> openSystemTonePicker());
        binding.btnDefaultTone.setOnClickListener(v -> resetDefaultTone());
        binding.btnTestTone.setOnClickListener(v -> sendTestNotification());
        binding.btnOpenNotificationSettings.setOnClickListener(v -> NotificationSoundManager.openNotificationSettings(requireContext()));
    }

    private void renderSelection() {
        if (binding == null || userStore == null) return;
        String musicUri = userStore.getMusicUri();
        boolean hasMusic = musicUri != null && !musicUri.trim().isEmpty();
        binding.tvTrackName.setText(hasMusic ? userStore.getMusicName() : "Chưa chọn bài hát từ máy");
        binding.tvTrackStatus.setText(hasMusic
                ? "Có thể nghe trong Good Daily hoặc dùng làm âm báo."
                : "Chọn file MP3, M4A, WAV hoặc định dạng âm thanh Android hỗ trợ.");
        binding.btnPlayPause.setEnabled(hasMusic);
        binding.btnStop.setEnabled(hasMusic);
        binding.btnUseAsNotification.setEnabled(hasMusic);
        binding.tvNotificationSound.setText(userStore.getNotificationSoundName());
    }

    private void togglePlayback() {
        String value = userStore.getMusicUri();
        if (value == null || value.trim().isEmpty()) return;
        Uri uri = Uri.parse(value);
        if (mediaPlayer != null && playingUri != null && playingUri.equals(uri)) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                binding.btnPlayPause.setText("Tiếp tục");
                binding.btnPlayPause.setIconResource(R.drawable.ic_play);
            } else {
                mediaPlayer.start();
                binding.btnPlayPause.setText("Tạm dừng");
                binding.btnPlayPause.setIconResource(R.drawable.ic_pause);
            }
            return;
        }
        stopPlayback();
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build());
            mediaPlayer.setDataSource(requireContext(), uri);
            mediaPlayer.setOnPreparedListener(player -> {
                player.start();
                binding.btnPlayPause.setText("Tạm dừng");
                binding.btnPlayPause.setIconResource(R.drawable.ic_pause);
                binding.tvPlaybackState.setText("Đang phát");
            });
            mediaPlayer.setOnCompletionListener(player -> {
                binding.btnPlayPause.setText("Phát nhạc");
                binding.btnPlayPause.setIconResource(R.drawable.ic_play);
                binding.tvPlaybackState.setText("Đã phát xong");
            });
            mediaPlayer.setOnErrorListener((player, what, extra) -> {
                toast("Không thể phát file âm thanh này.");
                stopPlayback();
                return true;
            });
            playingUri = uri;
            binding.tvPlaybackState.setText("Đang tải bài hát...");
            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            stopPlayback();
            toast("Không thể mở bài hát. Hãy chọn lại file từ máy.");
        }
    }

    private void stopPlayback() {
        if (mediaPlayer != null) {
            try { mediaPlayer.stop(); } catch (Exception ignored) {}
            mediaPlayer.release();
            mediaPlayer = null;
        }
        playingUri = null;
        if (binding != null) {
            binding.btnPlayPause.setText("Phát nhạc");
            binding.btnPlayPause.setIconResource(R.drawable.ic_play);
            binding.tvPlaybackState.setText("Sẵn sàng phát");
        }
    }

    private void useCurrentSongAsNotification() {
        String value = userStore.getMusicUri();
        if (value == null || value.trim().isEmpty()) return;
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Dùng làm âm báo?")
                .setMessage("Good Daily sẽ nhập một bản sao của bài “" + userStore.getMusicName()
                        + "” vào thư mục Notifications để Android có thể phát chuông ổn định khi app chạy nền.")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Sử dụng", (dialog, which) -> {
                    binding.btnUseAsNotification.setEnabled(false);
                    binding.btnUseAsNotification.setText("Đang chuẩn bị âm báo...");
                    Context appContext = requireContext().getApplicationContext();
                    Uri source = Uri.parse(value);
                    String name = userStore.getMusicName();
                    AppExecutors.io().execute(() -> {
                        Uri imported = NotificationSoundManager.importAsNotificationSound(appContext, source, name);
                        AppExecutors.main().post(() -> {
                            if (!isAdded() || binding == null) return;
                            binding.btnUseAsNotification.setEnabled(true);
                            binding.btnUseAsNotification.setText("Dùng bài đang chọn làm âm báo");
                            if (imported == null) {
                                toast("Không thể tạo âm báo từ file này. Hãy chọn nhạc chuông hệ thống.");
                                return;
                            }
                            userStore.setNotificationSound(imported.toString(), name);
                            NotificationSoundManager.clearOldChannels(requireContext());
                            renderSelection();
                            toast("Đã đặt bài hát làm âm báo.");
                            sendTestNotification();
                        });
                    });
                })
                .show();
    }

    private void openSystemTonePicker() {
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
        String current = userStore.getNotificationSoundUri();
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                current == null || current.isEmpty() ? RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION) : Uri.parse(current));
        pickSystemTone.launch(intent);
    }

    private void resetDefaultTone() {
        userStore.setNotificationSound("", "Âm thanh mặc định");
        NotificationSoundManager.clearOldChannels(requireContext());
        renderSelection();
        toast("Đã dùng lại âm báo mặc định của điện thoại.");
    }

    private void sendTestNotification() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS);
            return;
        }
        boolean posted = NotificationSoundManager.postTestNotification(requireContext());
        if (posted) toast("Đã gửi thông báo thử. Nếu vẫn im lặng, mở Cài đặt thông báo để bật Âm thanh.");
        else toast("Không gửi được thông báo. Hãy cấp quyền thông báo cho Good Daily.");
    }

    private String resolveName(Uri uri) {
        String result = "Bài hát đã chọn";
        try (Cursor cursor = requireContext().getContentResolver().query(
                uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (index >= 0) result = cursor.getString(index);
            }
        } catch (Exception ignored) {
        }
        return result == null || result.trim().isEmpty() ? "Bài hát đã chọn" : result;
    }

    private void toast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDestroyView() {
        stopPlayback();
        if (previewRingtone != null && previewRingtone.isPlaying()) previewRingtone.stop();
        previewRingtone = null;
        super.onDestroyView();
        binding = null;
    }
}
