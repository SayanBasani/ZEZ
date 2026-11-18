package com.zez.app;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.zez.app.adapter.ContactsAdapter;
import com.zez.app.model.ChatContactItem;
import com.zez.app.model.ChatUser;
import com.zez.app.util.ChatApiClient;
import com.zez.app.util.ChatPreferencesHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ContactsActivity extends AppCompatActivity implements ContactsAdapter.OnContactClickListener {
    private static final int REQ_READ_CONTACTS = 2001;

    private TextView tvTitle;
    private TextView tvCount;
    private ImageButton btnBack;
    private ImageButton btnRefresh;
    private EditText etSearch;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recyclerView;
    private LinearLayout layoutEmpty;
    private ProgressBar pbLoading;

    private ContactsAdapter adapter;
    private final List<ChatContactItem> allRegistered = new ArrayList<>();
    private final List<ChatContactItem> allNotRegistered = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        initViews();
        setupRecyclerView();
        checkPermissionAndLoad();
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tv_contacts_title);
        tvCount = findViewById(R.id.tv_contacts_count);
        btnBack = findViewById(R.id.btn_contacts_back);
        btnRefresh = findViewById(R.id.btn_contacts_refresh);
        etSearch = findViewById(R.id.et_search_contacts);
        swipeRefresh = findViewById(R.id.swipe_refresh_contacts);
        recyclerView = findViewById(R.id.recycler_contacts);
        layoutEmpty = findViewById(R.id.layout_empty_contacts);
        pbLoading = findViewById(R.id.pb_contacts_loading);

        btnBack.setOnClickListener(v -> finish());
        btnRefresh.setOnClickListener(v -> checkPermissionAndLoad());
        swipeRefresh.setOnRefreshListener(this::checkPermissionAndLoad);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterContacts(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupRecyclerView() {
        adapter = new ContactsAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void checkPermissionAndLoad() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, REQ_READ_CONTACTS);
        } else {
            loadDeviceContacts();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_READ_CONTACTS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadDeviceContacts();
            } else {
                swipeRefresh.setRefreshing(false);
                pbLoading.setVisibility(View.GONE);
                layoutEmpty.setVisibility(View.VISIBLE);
                tvCount.setText("Permission required");
                Toast.makeText(this, "Contacts permission is required to find friends", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadDeviceContacts() {
        pbLoading.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);

        new Thread(() -> {
            Map<String, String> phoneToNameMap = new LinkedHashMap<>();
            ContentResolver cr = getContentResolver();
            Cursor cursor = null;

            try {
                cursor = cr.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        new String[]{
                                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                                ContactsContract.CommonDataKinds.Phone.NUMBER
                        },
                        null,
                        null,
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
                );

                if (cursor != null) {
                    int nameIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                    int numIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

                    while (cursor.moveToNext()) {
                        String name = nameIdx >= 0 ? cursor.getString(nameIdx) : "Unknown";
                        String number = numIdx >= 0 ? cursor.getString(numIdx) : "";
                        if (number != null && !number.trim().isEmpty()) {
                            String clean = number.replaceAll("[^\\d+]", "");
                            if (!clean.isEmpty() && !phoneToNameMap.containsKey(clean)) {
                                phoneToNameMap.put(clean, name);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) cursor.close();
            }

            List<String> phoneList = new ArrayList<>(phoneToNameMap.keySet());
            String currentUserId = ChatPreferencesHelper.getUserId(this);

            runOnUiThread(() -> {
                if (phoneList.isEmpty()) {
                    pbLoading.setVisibility(View.GONE);
                    swipeRefresh.setRefreshing(false);
                    layoutEmpty.setVisibility(View.VISIBLE);
                    tvCount.setText("0 contacts");
                    return;
                }

                // Sync with backend
                ChatApiClient.syncContacts(ContactsActivity.this, currentUserId, phoneList, new ChatApiClient.ChatCallback<ChatApiClient.ContactSyncResult>() {
                    @Override
                    public void onSuccess(ChatApiClient.ContactSyncResult result) {
                        pbLoading.setVisibility(View.GONE);
                        swipeRefresh.setRefreshing(false);

                        allRegistered.clear();
                        allNotRegistered.clear();

                        Map<String, ChatUser> regMap = new HashMap<>();
                        if (result != null && result.registered != null) {
                            for (ChatUser u : result.registered) {
                                if (u != null && u.getPhoneNumber() != null) {
                                    regMap.put(u.getPhoneNumber(), u);
                                    if (u.getPhoneNumber().length() >= 10) {
                                        regMap.put(u.getPhoneNumber().substring(u.getPhoneNumber().length() - 10), u);
                                    }
                                }
                            }
                        }

                        for (Map.Entry<String, String> entry : phoneToNameMap.entrySet()) {
                            String num = entry.getKey();
                            String name = entry.getValue();
                            String last10 = num.length() >= 10 ? num.substring(num.length() - 10) : num;

                            ChatUser matched = regMap.get(num);
                            if (matched == null && last10 != null) matched = regMap.get(last10);

                            if (matched != null) {
                                allRegistered.add(new ChatContactItem(name, matched.getPhoneNumber(), matched.getAvatarUrl(), true, matched.getId(), matched.getStatusMessage()));
                            } else {
                                allNotRegistered.add(new ChatContactItem(name, num, null, false, null, null));
                            }
                        }

                        int total = allRegistered.size() + allNotRegistered.size();
                        tvCount.setText(total + " contacts (" + allRegistered.size() + " on Zez)");

                        if (total == 0) {
                            layoutEmpty.setVisibility(View.VISIBLE);
                        } else {
                            layoutEmpty.setVisibility(View.GONE);
                            adapter.setData(allRegistered, allNotRegistered);
                        }
                    }

                    @Override
                    public void onError(String errorMessage) {
                        pbLoading.setVisibility(View.GONE);
                        swipeRefresh.setRefreshing(false);
                        Toast.makeText(ContactsActivity.this, "Sync error: " + errorMessage, Toast.LENGTH_SHORT).show();

                        // Fallback show device contacts as invite
                        allNotRegistered.clear();
                        for (Map.Entry<String, String> entry : phoneToNameMap.entrySet()) {
                            allNotRegistered.add(new ChatContactItem(entry.getValue(), entry.getKey(), null, false, null, null));
                        }
                        tvCount.setText(allNotRegistered.size() + " contacts");
                        adapter.setData(Collections.emptyList(), allNotRegistered);
                    }
                });
            });
        }).start();
    }

    private void filterContacts(String query) {
        if (query == null || query.trim().isEmpty()) {
            adapter.setData(allRegistered, allNotRegistered);
            return;
        }

        String lower = query.toLowerCase(Locale.ROOT).trim();
        List<ChatContactItem> filteredReg = new ArrayList<>();
        for (ChatContactItem c : allRegistered) {
            if (c.getDisplayName().toLowerCase(Locale.ROOT).contains(lower) || c.getPhoneNumber().contains(lower)) {
                filteredReg.add(c);
            }
        }

        List<ChatContactItem> filteredNotReg = new ArrayList<>();
        for (ChatContactItem c : allNotRegistered) {
            if (c.getDisplayName().toLowerCase(Locale.ROOT).contains(lower) || c.getPhoneNumber().contains(lower)) {
                filteredNotReg.add(c);
            }
        }

        adapter.setData(filteredReg, filteredNotReg);
    }

    @Override
    public void onContactClick(ChatContactItem contact) {
        if (contact.isRegistered() && contact.getChatUserId() != null) {
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("extra_other_user_id", contact.getChatUserId());
            intent.putExtra("extra_other_user_name", contact.getDisplayName());
            intent.putExtra("extra_other_user_phone", contact.getPhoneNumber());
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onInviteClick(ChatContactItem contact) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Hey " + contact.getDisplayName() + "! Let's chat on Zez: https://zez-backend.onrender.com");
        startActivity(Intent.createChooser(shareIntent, "Invite " + contact.getDisplayName() + " to Zez"));
    }
}
