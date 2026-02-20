package com.example.myapplication3;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Arrays;
import java.util.List;

public class InfoOPredmeteActivity extends AppCompatActivity {

    private String barcode;
    private DataRepository.Item item;

    private LinearLayout viewModeLayout;
    private LinearLayout editModeLayout;
    private LinearLayout editButton;
    private LinearLayout saveButton;
    private LinearLayout cancelButton;

    private TextView itemNameText;
    private TextView itemBarcodeValue;
    private TextView itemShelfValue;
    private TextView itemConditionValue;
    private LinearLayout historyContainer;

    private Spinner editShelfSpinner;
    private Spinner editConditionSpinner;

    private List<String> shelfIds;
    private List<String> conditions = Arrays.asList("Отличное", "Хорошее", "Удовлетворительное", "Плохое");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info_o_predmete);

        DataRepository.init(this);

        barcode = getIntent().getStringExtra("barcode");
        item    = DataRepository.findItemByBarcode(barcode);

        if (item == null) { finish(); return; }

        itemNameText       = findViewById(R.id.itemNameText);
        itemBarcodeValue   = findViewById(R.id.itemBarcodeValue);
        itemShelfValue     = findViewById(R.id.itemShelfValue);
        itemConditionValue = findViewById(R.id.itemConditionValue);
        historyContainer   = findViewById(R.id.historyContainer);

        viewModeLayout = findViewById(R.id.viewModeLayout);
        editModeLayout = findViewById(R.id.editModeLayout);
        editButton     = findViewById(R.id.editButton);
        saveButton     = findViewById(R.id.saveButton);
        cancelButton   = findViewById(R.id.cancelButton);

        editShelfSpinner     = findViewById(R.id.editShelfSpinner);
        editConditionSpinner = findViewById(R.id.editConditionSpinner);

        setupSpinners();
        populateView();

        editButton.setOnClickListener(v -> setEditMode(true));
        cancelButton.setOnClickListener(v -> setEditMode(false));
        saveButton.setOnClickListener(v -> saveChanges());

        findViewById(R.id.navScanner).setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });
        findViewById(R.id.navShelves).setOnClickListener(v -> {
            startActivity(new Intent(this, PolkiActivity.class));
            overridePendingTransition(0, 0);
        });
    }

    private void setupSpinners() {
        shelfIds = DataRepository.getAllShelfIds();
        java.util.Collections.sort(shelfIds);
        List<String> shelfNames = new java.util.ArrayList<>();
        for (String id : shelfIds) {
            DataRepository.Shelf s = DataRepository.getShelfById(id);
            if (s != null) shelfNames.add(s.name + " (" + s.location + ")");
        }

        ArrayAdapter<String> shelfAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, shelfNames);
        shelfAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        editShelfSpinner.setAdapter(shelfAdapter);

        ArrayAdapter<String> condAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, conditions);
        condAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        editConditionSpinner.setAdapter(condAdapter);
    }

    private void populateView() {
        DataRepository.Shelf shelf = DataRepository.getShelfById(item.shelfId);
        String shelfDisplay = shelf != null ? shelf.name + " (" + shelf.location + ")" : item.shelfId;

        itemNameText.setText(item.name);
        itemBarcodeValue.setText(item.barcode);
        itemShelfValue.setText(shelfDisplay);
        itemConditionValue.setText(item.condition);

        historyContainer.removeAllViews();
        for (String entry : item.history) {
            TextView tv = new TextView(this);
            tv.setText("• " + entry);
            tv.setTextColor(Color.parseColor("#87a9d4"));
            tv.setTextSize(13f);
            tv.setPadding(0, 4, 0, 4);
            historyContainer.addView(tv);
        }
    }

    private void setEditMode(boolean editing) {
        viewModeLayout.setVisibility(editing ? View.GONE  : View.VISIBLE);
        editModeLayout.setVisibility(editing ? View.VISIBLE : View.GONE);
        editButton.setVisibility(editing ? View.GONE  : View.VISIBLE);
        saveButton.setVisibility(editing ? View.VISIBLE : View.GONE);
        cancelButton.setVisibility(editing ? View.VISIBLE : View.GONE);

        if (editing) {
            int shelfIdx = shelfIds.indexOf(item.shelfId);
            if (shelfIdx >= 0) editShelfSpinner.setSelection(shelfIdx);
            int condIdx = conditions.indexOf(item.condition);
            if (condIdx >= 0) editConditionSpinner.setSelection(condIdx);
        }
    }

    private void saveChanges() {
        int shelfIdx = editShelfSpinner.getSelectedItemPosition();
        int condIdx  = editConditionSpinner.getSelectedItemPosition();

        String newShelfId   = shelfIds.get(shelfIdx);
        String newCondition = conditions.get(condIdx);

        DataRepository.updateItem(this, barcode, newShelfId, newCondition);

        item = DataRepository.findItemByBarcode(barcode);

        setEditMode(false);
        populateView();

        android.widget.Toast.makeText(this, "Изменения сохранены", android.widget.Toast.LENGTH_SHORT).show();
    }
}