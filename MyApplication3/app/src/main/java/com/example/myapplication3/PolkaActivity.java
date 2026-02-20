package com.example.myapplication3;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class PolkaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.polka);

        DataRepository.init(this);

        String shelfId = getIntent().getStringExtra("shelfId");
        DataRepository.Shelf shelf = DataRepository.getShelfById(shelfId);

        if (shelf == null) { finish(); return; }

        ((TextView) findViewById(R.id.shelfTitle)).setText(shelf.name);
        ((TextView) findViewById(R.id.shelfNameValue)).setText(shelf.name);
        ((TextView) findViewById(R.id.shelfLocationValue)).setText(shelf.location);
        ((TextView) findViewById(R.id.shelfCapacityValue)).setText(shelf.capacity + " предметов");

        LinearLayout itemsContainer = findViewById(R.id.itemsContainer);
        List<DataRepository.Item> items = DataRepository.getItemsByShelfId(shelfId);

        if (items.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("На этой полке нет предметов");
            empty.setTextSize(14f);
            empty.setPadding(0, 16, 0, 0);
            itemsContainer.addView(empty);
        } else {
            for (DataRepository.Item item : items) {
                itemsContainer.addView(buildItemRow(item));
            }
        }

        findViewById(R.id.navScanner).setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });
        findViewById(R.id.navShelves).setOnClickListener(v -> {
            Intent intent = new Intent(this, PolkiActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });
    }

    private View buildItemRow(DataRepository.Item item) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12));
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        rowParams.setMargins(0, 0, 0, dpToPx(8));
        row.setLayoutParams(rowParams);
        row.setClickable(true);
        row.setFocusable(true);

        LinearLayout textBlock = new LinearLayout(this);
        textBlock.setOrientation(LinearLayout.VERTICAL);
        textBlock.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView nameView = new TextView(this);
        nameView.setText(item.name);
        nameView.setTextSize(14f);
        nameView.setTypeface(null, android.graphics.Typeface.BOLD);
        textBlock.addView(nameView);

        TextView barcodeView = new TextView(this);
        barcodeView.setText(item.barcode);
        barcodeView.setTextSize(12f);
        textBlock.addView(barcodeView);

        row.addView(textBlock);

        TextView condView = new TextView(this);
        condView.setText(item.condition);
        condView.setTextSize(12f);
        condView.setPadding(0, 0, dpToPx(4), 0);
        row.addView(condView);

        row.setOnClickListener(v -> {
            Intent intent = new Intent(this, InfoOPredmeteActivity.class);
            intent.putExtra("barcode", item.barcode);
            startActivity(intent);
        });

        return row;
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}