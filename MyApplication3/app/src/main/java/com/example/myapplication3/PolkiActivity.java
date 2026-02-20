package com.example.myapplication3;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class PolkiActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.polki);

        DataRepository.init(this);

        RecyclerView recyclerView = findViewById(R.id.shelvesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<DataRepository.Shelf> shelves = DataRepository.getAllShelves();
        shelves.sort((a, b) -> a.id.compareTo(b.id));

        recyclerView.setAdapter(new ShelfAdapter(shelves));

        findViewById(R.id.navScanner).setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });
        findViewById(R.id.navShelves).setOnClickListener(v -> {
        });
    }

    private class ShelfAdapter extends RecyclerView.Adapter<ShelfAdapter.ShelfVH> {

        private final List<DataRepository.Shelf> data;

        ShelfAdapter(List<DataRepository.Shelf> data) { this.data = data; }

        @Override
        public ShelfVH onCreateViewHolder(ViewGroup parent, int viewType) {
            LinearLayout card = new LinearLayout(PolkiActivity.this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 0, dpToPx(8));
            card.setLayoutParams(params);

            TextView name = new TextView(PolkiActivity.this);
            name.setTag("name");
            name.setTextSize(16f);
            name.setTypeface(null, android.graphics.Typeface.BOLD);
            card.addView(name);

            TextView location = new TextView(PolkiActivity.this);
            location.setTag("location");
            location.setTextSize(13f);
            location.setPadding(0, dpToPx(4), 0, dpToPx(8));
            card.addView(location);

            View divider = new View(PolkiActivity.this);
            card.addView(divider, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(1)));

            LinearLayout footer = new LinearLayout(PolkiActivity.this);
            footer.setOrientation(LinearLayout.HORIZONTAL);
            footer.setPadding(0, dpToPx(8), 0, 0);

            TextView capacity = new TextView(PolkiActivity.this);
            capacity.setTag("capacity");
            capacity.setTextSize(13f);
            capacity.setLayoutParams(new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            footer.addView(capacity);

            TextView count = new TextView(PolkiActivity.this);
            count.setTag("count");
            count.setTextSize(13f);
            footer.addView(count);

            card.addView(footer);

            card.setClickable(true);
            card.setFocusable(true);

            return new ShelfVH(card);
        }

        @Override
        public void onBindViewHolder(ShelfVH holder, int position) {
            DataRepository.Shelf shelf = data.get(position);

            ((TextView) holder.itemView.findViewWithTag("name")).setText(shelf.name);
            ((TextView) holder.itemView.findViewWithTag("location")).setText(shelf.location);
            ((TextView) holder.itemView.findViewWithTag("capacity"))
                    .setText("Вместимость: " + shelf.capacity + " пред.");

            int itemCount = DataRepository.getItemsByShelfId(shelf.id).size();
            ((TextView) holder.itemView.findViewWithTag("count"))
                    .setText(itemCount + " / " + shelf.capacity);

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(PolkiActivity.this, PolkaActivity.class);
                intent.putExtra("shelfId", shelf.id);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() { return data.size(); }

        class ShelfVH extends RecyclerView.ViewHolder {
            ShelfVH(View v) { super(v); }
        }

        private int dpToPx(int dp) {
            float density = getResources().getDisplayMetrics().density;
            return Math.round(dp * density);
        }
    }
}