package com.sangapp.gooddaily.ui.backup;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.sangapp.gooddaily.BuildConfig;
import com.sangapp.gooddaily.R;
import com.sangapp.gooddaily.data.backup.AutoBackupScheduler;
import com.sangapp.gooddaily.data.backup.FullBackupManager;
import com.sangapp.gooddaily.data.local.prefs.LocalUserStore;
import com.sangapp.gooddaily.databinding.FragmentBackupCenterBinding;
import com.sangapp.gooddaily.util.ThemeUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BackupCenterFragment extends Fragment {
    private FragmentBackupCenterBinding binding;
    private FullBackupManager manager;
    private LocalUserStore store;
    private String pendingExportPassword = "";
    private Uri pendingImportUri;

    private final ActivityResultLauncher<String> createBackup = registerForActivityResult(
            new ActivityResultContracts.CreateDocument("application/octet-stream"), uri -> {
                if (uri != null) manager.exportTo(uri, pendingExportPassword, callback());
            });
    private final ActivityResultLauncher<String[]> openBackup = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(), uri -> { if (uri != null) { pendingImportUri=uri; askImportPassword(); } });

    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentBackupCenterBinding.inflate(inflater, container, false); return binding.getRoot();
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        manager = new FullBackupManager(requireContext()); store = new LocalUserStore(requireContext());
        ThemeUtils.tintFilledButton(binding.btnExportEncryptedBackup, requireContext(), store.getThemeKey());
        ThemeUtils.tintTonalButton(binding.btnImportFullBackup, requireContext(), store.getThemeKey());
        ThemeUtils.tintTonalButton(binding.btnBackupNowInternal, requireContext(), store.getThemeKey());
        String[] options={"Tắt","Hằng tuần","Hằng tháng"};
        binding.dropdownAutoBackup.setAdapter(new ArrayAdapter<>(requireContext(),android.R.layout.simple_dropdown_item_1line,options));
        binding.dropdownAutoBackup.setText(label(store.getAutoBackupCadence()),false);
        binding.dropdownAutoBackup.setOnItemClickListener((parent,v,position,id)->{
            String cadence=position==1?"WEEKLY":position==2?"MONTHLY":"OFF";
            store.setAutoBackupCadence(cadence); AutoBackupScheduler.schedule(requireContext(),cadence);
        });
        binding.btnExportEncryptedBackup.setOnClickListener(v -> askExportPassword());
        binding.btnImportFullBackup.setOnClickListener(v -> openBackup.launch(new String[]{"application/zip","application/octet-stream","*/*"}));
        binding.btnBackupNowInternal.setOnClickListener(v -> new Thread(() -> {
            try { File f=manager.createInternalAutoBackup(); requireActivity().runOnUiThread(() -> { toast("Đã tạo "+f.getName()); renderBackups(); }); }
            catch(Exception e){requireActivity().runOnUiThread(() -> toast("Không thể backup: "+e.getMessage()));}
        }).start());
        renderBackups();
    }

    private void askExportPassword(){
        EditText input=passwordField("Mật khẩu (để trống nếu muốn ZIP thường)");
        new MaterialAlertDialogBuilder(requireContext()).setTitle("Bảo vệ file backup").setMessage("Có mật khẩu: file .gdz mã hóa AES. Không mật khẩu: file ZIP thường.").setView(input)
                .setNegativeButton("Hủy",null).setPositiveButton("Tiếp tục",(d,w)->{
                    pendingExportPassword=input.getText().toString();
                    String ext=pendingExportPassword.isEmpty()?".zip":".gdz";
                    createBackup.launch("GoodDaily_Full_"+new SimpleDateFormat("yyyy-MM-dd_HH-mm",Locale.US).format(new Date())+ext);
                }).show();
    }

    private void askImportPassword(){
        EditText input=passwordField("Mật khẩu nếu file đã mã hóa");
        new MaterialAlertDialogBuilder(requireContext()).setTitle("Xem trước backup").setView(input).setNegativeButton("Hủy",null).setPositiveButton("Xem",(d,w)->{
            String password=input.getText().toString();
            manager.preview(pendingImportUri,password,new FullBackupManager.PreviewCallback(){
                @Override public void onPreview(String text){confirmRestore(text,password);}
                @Override public void onError(String message){toast("Không đọc được backup: "+message);}
            });
        }).show();
    }

    private void confirmRestore(String preview,String password){
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Chọn cách khôi phục")
                .setMessage(preview + "\n\n• Gộp dữ liệu: giữ dữ liệu hiện tại và thêm bản ghi chưa có.\n• Thay thế: xóa dữ liệu hiện tại rồi phục hồi toàn bộ backup.\n\nNên xuất một bản dự phòng trước khi thay thế.")
                .setNegativeButton("Hủy", null)
                .setNeutralButton("Thay thế", (d,w) -> manager.importFrom(pendingImportUri, password, false, callback()))
                .setPositiveButton("Gộp dữ liệu", (d,w) -> manager.importFrom(pendingImportUri, password, true, callback()))
                .show();
    }

    private FullBackupManager.Callback callback(){return new FullBackupManager.Callback(){
        @Override public void onSuccess(String message){toast(message);renderBackups();}
        @Override public void onError(String message){new MaterialAlertDialogBuilder(requireContext()).setTitle("Backup thất bại").setMessage(message).setPositiveButton("Đóng",null).show();}
    };}

    private void renderBackups(){
        if(binding==null)return; binding.autoBackupListContainer.removeAllViews(); List<File> files=manager.listAutoBackups();
        for(File f:files){
            MaterialCardView card=new MaterialCardView(requireContext());card.setRadius(dp(17));card.setCardElevation(0);card.setStrokeWidth(dp(1));card.setStrokeColor(getResources().getColor(R.color.outline,requireContext().getTheme()));card.setCardBackgroundColor(getResources().getColor(R.color.surface,requireContext().getTheme()));
            LinearLayout row=new LinearLayout(requireContext());row.setOrientation(LinearLayout.VERTICAL);row.setPadding(dp(15),dp(13),dp(15),dp(13));
            TextView title=text(f.getName(),15,true);TextView meta=text(size(f.length())+" · "+new SimpleDateFormat("dd/MM/yyyy HH:mm",Locale.US).format(new Date(f.lastModified())),12,false);meta.setTextColor(getResources().getColor(R.color.on_surface_variant,requireContext().getTheme()));meta.setPadding(0,dp(4),0,0);row.addView(title);row.addView(meta);card.addView(row);
            card.setOnClickListener(v->share(f));card.setOnLongClickListener(v->{new MaterialAlertDialogBuilder(requireContext()).setTitle("Xóa backup?").setNegativeButton("Hủy",null).setPositiveButton("Xóa",(d,w)->{f.delete();renderBackups();}).show();return true;});
            LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);p.bottomMargin=dp(9);binding.autoBackupListContainer.addView(card,p);
        }
        if(files.isEmpty()){TextView empty=text("Chưa có backup tự động.",14,false);empty.setTextColor(getResources().getColor(R.color.on_surface_variant,requireContext().getTheme()));empty.setPadding(dp(16),dp(24),dp(16),dp(24));binding.autoBackupListContainer.addView(empty);}
    }
    private void share(File file){Uri uri=FileProvider.getUriForFile(requireContext(), BuildConfig.APPLICATION_ID+".fileprovider",file);Intent i=new Intent(Intent.ACTION_SEND);i.setType("application/zip");i.putExtra(Intent.EXTRA_STREAM,uri);i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);startActivity(Intent.createChooser(i,"Chia sẻ backup"));}
    private EditText passwordField(String hint){EditText e=new EditText(requireContext());e.setHint(hint);e.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);return e;}
    private String label(String c){return "WEEKLY".equals(c)?"Hằng tuần":"MONTHLY".equals(c)?"Hằng tháng":"Tắt";}
    private String size(long b){return b>1024*1024?String.format(Locale.US,"%.1f MB",b/1024d/1024d):String.format(Locale.US,"%.0f KB",b/1024d);}
    private TextView text(String value,int sp,boolean bold){TextView v=new TextView(requireContext());v.setText(value);v.setTextSize(sp);v.setTextColor(getResources().getColor(R.color.on_surface,requireContext().getTheme()));if(bold)v.setTypeface(v.getTypeface(),android.graphics.Typeface.BOLD);return v;}
    private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
    private void toast(String m){Toast.makeText(requireContext(),m,Toast.LENGTH_LONG).show();}
    @Override public void onDestroyView(){super.onDestroyView();binding=null;}
}
