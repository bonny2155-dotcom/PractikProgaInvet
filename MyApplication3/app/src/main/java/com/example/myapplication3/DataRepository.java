package com.example.myapplication3;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataRepository {

    public static class Item {
        public String barcode;
        public String name;
        public String category;
        public String shelfId;
        public String condition;
        public List<String> history;

        public Item(String barcode, String name, String category, String shelfId, String condition) {
            this.barcode = barcode;
            this.name = name;
            this.category = category;
            this.shelfId = shelfId;
            this.condition = condition;
            this.history = new ArrayList<>();
        }
    }

    public static class Shelf {
        public String id;
        public String name;
        public String location;
        public int capacity;

        public Shelf(String id, String name, String location, int capacity) {
            this.id = id;
            this.name = name;
            this.location = location;
            this.capacity = capacity;
        }
    }

    private static final Map<String, Item> items = new HashMap<>();
    private static final Map<String, Shelf> shelves = new HashMap<>();
    private static boolean initialized = false;

    private static final String PREFS_NAME = "inventory_prefs";
    private static final String KEY_ITEMS = "items_json";

    private static void initShelves() {
        shelves.put("shelf_1", new Shelf("shelf_1", "Полка A-1", "Склад А, ряд 1", 50));
        shelves.put("shelf_2", new Shelf("shelf_2", "Полка A-2", "Склад А, ряд 2", 50));
        shelves.put("shelf_3", new Shelf("shelf_3", "Полка B-1", "Склад Б, ряд 1", 40));
        shelves.put("shelf_4", new Shelf("shelf_4", "Полка B-2", "Склад Б, ряд 2", 40));
        shelves.put("shelf_5", new Shelf("shelf_5", "Полка C-1", "Склад В, ряд 1", 60));
    }

    private static void initDefaultItems() {
        Item laptop = new Item("4601234567890", "Ноутбук Dell", "Электроника", "shelf_1", "Отличное");
        laptop.history.add("12.01.2025 — поступление на склад");
        laptop.history.add("20.01.2025 — перемещено: Полка B-1 → Полка A-1");
        items.put(laptop.barcode, laptop);

        Item monitor = new Item("4607890123456", "Монитор Samsung", "Электроника", "shelf_1", "Хорошее");
        monitor.history.add("05.02.2025 — поступление на склад");
        items.put(monitor.barcode, monitor);

        Item phone = new Item("4600000111222", "Телефон iPhone 13", "Электроника", "shelf_1", "Отличное");
        phone.history.add("15.03.2025 — поступление на склад");
        items.put(phone.barcode, phone);

        Item keyboard = new Item("4609876543210", "Клавиатура Logitech", "Периферия", "shelf_2", "Отличное");
        keyboard.history.add("10.01.2025 — поступление на склад");
        items.put(keyboard.barcode, keyboard);

        Item pc = new Item("46098765432453", "Пк Dell", "Периферия", "shelf_2", "Отличное");
        pc.history.add("11.01.2025 — поступление на склад");
        items.put(pc.barcode, pc);

        Item mouse = new Item("4601111222333", "Мышь Logitech", "Периферия", "shelf_2", "Хорошее");
        mouse.history.add("10.01.2025 — поступление на склад");
        mouse.history.add("01.03.2025 — перемещено: Полка C-1 → Полка A-2");
        items.put(mouse.barcode, mouse);

        Item webcam = new Item("4603344027168", "Веб-камера Logitech", "Периферия", "shelf_2", "Хорошее");
        webcam.history.add("22.02.2025 — поступление на склад");
        items.put(webcam.barcode, webcam);

        Item tablet = new Item("4603333444555", "Планшет iPad", "Электроника", "shelf_3", "Отличное");
        tablet.history.add("18.01.2025 — поступление на склад");
        items.put(tablet.barcode, tablet);

        Item printer = new Item("4604444555666", "Принтер HP", "Оргтехника", "shelf_3", "Удовлетворительное");
        printer.history.add("02.12.2024 — поступление на склад");
        printer.history.add("14.02.2025 — проведено техобслуживание");
        items.put(printer.barcode, printer);

        Item router = new Item("4605555666777", "Роутер TPLink", "Сетевое оборудование", "shelf_4", "Отличное");
        router.history.add("07.03.2025 — поступление на склад");
        items.put(router.barcode, router);

        Item ups = new Item("4606666777888", "ИБП", "Электрооборудование", "shelf_4", "Хорошее");
        ups.history.add("11.11.2024 — поступление на склад");
        items.put(ups.barcode, ups);

        Item headset = new Item("4607777888999", "Гарнитура", "Периферия", "shelf_5", "Отличное");
        headset.history.add("25.02.2025 — поступление на склад");
        items.put(headset.barcode, headset);

        Item projector = new Item("4608888999000", "Проектор", "Оргтехника", "shelf_5", "Удовлетворительное");
        projector.history.add("30.10.2024 — поступление на склад");
        projector.history.add("15.01.2025 — замена лампы");
        items.put(projector.barcode, projector);
    }

    public static synchronized void init(Context context) {
        if (initialized) return;
        initShelves();
        SharedPreferences prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_ITEMS, null);
        if (json != null) {
            loadItemsFromJson(json);
        } else {
            initDefaultItems();
        }
        initialized = true;
    }

    private static void loadItemsFromJson(String json) {
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                Item item = new Item(
                        obj.getString("barcode"),
                        obj.getString("name"),
                        obj.getString("category"),
                        obj.getString("shelfId"),
                        obj.getString("condition")
                );
                JSONArray hist = obj.getJSONArray("history");
                for (int j = 0; j < hist.length(); j++) {
                    item.history.add(hist.getString(j));
                }
                items.put(item.barcode, item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            initDefaultItems();
        }
    }

    private static void saveItems(Context context) {
        try {
            JSONArray arr = new JSONArray();
            for (Item item : items.values()) {
                JSONObject obj = new JSONObject();
                obj.put("barcode", item.barcode);
                obj.put("name", item.name);
                obj.put("category", item.category);
                obj.put("shelfId", item.shelfId);
                obj.put("condition", item.condition);
                JSONArray hist = new JSONArray();
                for (String h : item.history) hist.put(h);
                obj.put("history", hist);
                arr.put(obj);
            }
            context.getApplicationContext()
                    .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    .edit().putString(KEY_ITEMS, arr.toString()).apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static Item findItemByBarcode(String barcode) {
        return items.get(barcode);
    }

    public static List<Shelf> getAllShelves() {
        return new ArrayList<>(shelves.values());
    }

    public static Shelf getShelfById(String id) {
        return shelves.get(id);
    }

    public static List<Item> getItemsByShelfId(String shelfId) {
        List<Item> result = new ArrayList<>();
        for (Item item : items.values()) {
            if (shelfId.equals(item.shelfId)) {
                result.add(item);
            }
        }
        return result;
    }

    public static List<String> getAllShelfNames() {
        List<String> names = new ArrayList<>();
        for (Shelf shelf : shelves.values()) {
            names.add(shelf.name + " (" + shelf.location + ")");
        }
        return names;
    }

    public static List<String> getAllShelfIds() {
        return new ArrayList<>(shelves.keySet());
    }

    public static void updateItem(Context context, String barcode, String newShelfId, String newCondition) {
        Item item = items.get(barcode);
        if (item == null) return;

        boolean shelfChanged = !item.shelfId.equals(newShelfId);
        boolean conditionChanged = !item.condition.equals(newCondition);

        if (shelfChanged) {
            Shelf oldShelf = shelves.get(item.shelfId);
            Shelf newShelf = shelves.get(newShelfId);
            String oldName = oldShelf != null ? oldShelf.name : item.shelfId;
            String newName = newShelf != null ? newShelf.name : newShelfId;
            item.history.add(getTodayDate() + " — перемещено: " + oldName + " → " + newName);
            item.shelfId = newShelfId;
        }
        if (conditionChanged) {
            item.history.add(getTodayDate() + " — изменено состояние: " + item.condition + " → " + newCondition);
            item.condition = newCondition;
        }

        if (shelfChanged || conditionChanged) {
            saveItems(context);
        }
    }

    private static String getTodayDate() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault());
        return sdf.format(new java.util.Date());
    }
}