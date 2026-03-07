package com.example.shipeatscustomer;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.util.Pair;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.*;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.*;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class A2_Dashboard extends AppCompatActivity {

    private TextView tvTotalOrders, tvDailySales, tvMenuItems, tvLowStock;
    private TextView tvOrdersSummary, tvSalesSummary, tvMenuSummary, tvStockSummary;
    private CardView cardOrders, cardSales, cardMenu, cardStock;

    private BarChart barChartWeekly;
    private LineChart lineChartMonthly;
    private PieChart stockPieChart;
    private TabLayout tabLayout;

    private DatabaseReference ordersRef, foodRef;

    private final ActivityResultLauncher<String> createPdfLauncher = registerForActivityResult(
            new ActivityResultContracts.CreateDocument("application/pdf"),
            uri -> { if (uri != null) writePdfToUri(uri); }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a2_dashboard);

        // 1. Initialize all UI elements
        initUI();

        // 2. Firebase References
        ordersRef = FirebaseDatabase.getInstance().getReference("Orders");
        foodRef = FirebaseDatabase.getInstance().getReference("food_items");

        // 3. Setup logic
        loadStatsAndCharts();
        setupCardClicks();
        setupTabs();
        setupBottomNav();

        // Highlight the Dashboard tab
        ImageView dashboardIcon = findViewById(R.id.dashboard_icon);
        TextView dashboardText = findViewById(R.id.dashboard_text);
        highlightCurrentTab(dashboardIcon, dashboardText);

        findViewById(R.id.btn_print_report).setOnClickListener(v -> showDateRangePicker());
    }

    private void initUI() {
        tvTotalOrders = findViewById(R.id.tv_total_orders);
        tvDailySales = findViewById(R.id.tv_daily_sales);
        tvMenuItems = findViewById(R.id.tv_menu_items);
        tvLowStock = findViewById(R.id.tv_low_stock);

        tvOrdersSummary = findViewById(R.id.tv_orders_summary);
        tvSalesSummary = findViewById(R.id.tv_sales_summary);
        tvMenuSummary = findViewById(R.id.tv_menu_summary);
        tvStockSummary = findViewById(R.id.tv_stock_summary);

        cardOrders = findViewById(R.id.card_total_orders);
        cardSales = findViewById(R.id.card_daily_sales);
        cardMenu = findViewById(R.id.card_menu_items);
        cardStock = findViewById(R.id.card_low_stock);

        barChartWeekly = findViewById(R.id.barChartWeekly);
        lineChartMonthly = findViewById(R.id.lineChartMonthly);
        stockPieChart = findViewById(R.id.stockPieChart);
        tabLayout = findViewById(R.id.tabLayoutSales);
    }

    private void loadStatsAndCharts() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        String yesterday = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.getTime());

        // --- ORDERS LISTENER ---
        ordersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int ordersT = 0, ordersY = 0;
                double revT = 0.0, revY = 0.0;
                ArrayList<BarEntry> barEntries = new ArrayList<>();
                ArrayList<Entry> lineEntries = new ArrayList<>();
                int idx = 0;

                for (DataSnapshot ds : snapshot.getChildren()) {
                    // SAFE PRICE PARSING
                    String pStr = ds.child("totalPrice").getValue(String.class);
                    double p = 0.0;
                    if (pStr != null) {
                        try {
                            // Keep only digits 0-9 and the decimal point
                            String clean = pStr.replaceAll("[^0-9.]", "");
                            if (!clean.isEmpty() && !clean.equals(".")) {
                                p = Double.parseDouble(clean);
                            }
                        } catch (Exception e) { p = 0.0; }
                    }

                    String oDate = ds.child("orderDate").getValue(String.class);
                    if (today.equals(oDate)) {
                        ordersT++; revT += p;
                    } else if (yesterday.equals(oDate)) {
                        ordersY++; revY += p;
                    }

                    barEntries.add(new BarEntry(idx, (float) p));
                    lineEntries.add(new Entry(idx, (float) p));
                    idx++;
                }

                tvTotalOrders.setText(String.valueOf(ordersT));
                tvDailySales.setText("RM " + String.format(Locale.getDefault(), "%.2f", revT));

                updateTrend(tvOrdersSummary, ordersT, ordersY);
                updateTrend(tvSalesSummary, revT, revY);
                updateCharts(barEntries, lineEntries);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });

        // --- INVENTORY LISTENER (Merged) ---
        foodRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int ok = 0, low = 0, out = 0;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    // Handle potential nulls or Longs from Firebase
                    Object qtyObj = ds.child("quantity").getValue();
                    int q = 0;
                    if (qtyObj instanceof Long) q = ((Long) qtyObj).intValue();
                    else if (qtyObj instanceof Integer) q = (Integer) qtyObj;

                    if (q <= 0) out++;
                    else if (q <= 5) low++;
                    else ok++;
                }
                tvMenuItems.setText(String.valueOf((int)snapshot.getChildrenCount()));
                tvLowStock.setText(String.valueOf(low));
                tvStockSummary.setText(out + " items currently sold out");
                updatePieChart(ok, low, out);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void updateTrend(TextView tv, double t, double y) {
        if (y == 0) {
            tv.setText("New Record Today");
            tv.setTextColor(Color.GRAY);
            return;
        }
        double diff = ((t - y) / y) * 100;
        tv.setText(String.format(Locale.getDefault(), "%s%.1f%% from yesterday", (diff >= 0 ? "+" : ""), diff));
        tv.setTextColor(diff >= 0 ? Color.parseColor("#4CAF50") : Color.RED);
    }

    private void setupCardClicks() {
        cardOrders.setOnClickListener(v -> startActivity(new Intent(this, A4_CustomerOrderActivity.class)));
        cardSales.setOnClickListener(v -> startActivity(new Intent(this, A4_CustomerOrderActivity.class)));
        cardMenu.setOnClickListener(v -> startActivity(new Intent(this, A5_MenuManagementActivity.class)));
        cardStock.setOnClickListener(v -> startActivity(new Intent(this, A3_Inventory_Management.class)));
    }

    private void updatePieChart(int ok, int low, int out) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(ok, "Available"));
        entries.add(new PieEntry(low, "Low Stock"));
        entries.add(new PieEntry(out, "Sold Out"));

        PieDataSet set = new PieDataSet(entries, "");
        set.setColors(new int[]{Color.parseColor("#4CAF50"), Color.parseColor("#FDB02C"), Color.parseColor("#E53935")});
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(12f);

        stockPieChart.setData(new PieData(set));
        stockPieChart.setCenterText("Stock Status");
        stockPieChart.setHoleRadius(40f);
        stockPieChart.animateXY(800, 800);
        stockPieChart.invalidate();
    }

    private void updateCharts(ArrayList<BarEntry> barEntries, ArrayList<Entry> lineEntries) {
        // Weekly Bar Chart
        BarDataSet barSet = new BarDataSet(barEntries, "Sales (RM)");
        barSet.setColor(Color.parseColor("#032565"));
        barChartWeekly.setData(new BarData(barSet));
        barChartWeekly.animateY(1000);
        barChartWeekly.invalidate();

        // Monthly Line Chart
        LineDataSet lineSet = new LineDataSet(lineEntries, "Revenue Trend");
        lineSet.setColor(Color.parseColor("#FDB02C"));
        lineSet.setCircleColor(Color.parseColor("#032565"));
        lineSet.setLineWidth(2f);
        lineChartMonthly.setData(new LineData(lineSet));
        lineChartMonthly.animateX(1000);
        lineChartMonthly.invalidate();
    }

    private void setupTabs() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                barChartWeekly.setVisibility(tab.getPosition() == 0 ? View.VISIBLE : View.GONE);
                lineChartMonthly.setVisibility(tab.getPosition() == 1 ? View.VISIBLE : View.GONE);
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void showDateRangePicker() {
        MaterialDatePicker<Pair<Long, Long>> picker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Select Report Period")
                .setTheme(com.google.android.material.R.style.ThemeOverlay_MaterialComponents_MaterialCalendar)
                .build();

        picker.show(getSupportFragmentManager(), "RANGE_PICKER");
        picker.addOnPositiveButtonClickListener(selection -> {
            String fileName = "Report_" + picker.getHeaderText().replace(" ", "_") + ".pdf";
            createPdfLauncher.launch(fileName);
        });
    }

    private void writePdfToUri(Uri uri) {
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();

        // Header
        paint.setColor(Color.parseColor("#032565"));
        paint.setTextSize(24f);
        paint.setFakeBoldText(true);
        canvas.drawText("ShipEats Official Sales Report", 50, 60, paint);

        paint.setTextSize(12f);
        paint.setFakeBoldText(false);
        paint.setColor(Color.GRAY);
        canvas.drawText("Admin Inventory Management Dashboard", 50, 85, paint);

        // Draw a Formal Line
        paint.setStrokeWidth(2f);
        canvas.drawLine(50, 100, 545, 100, paint);

        // Body
        paint.setColor(Color.BLACK);
        paint.setTextSize(14f);
        int y = 150;
        int spacing = 35;

        canvas.drawText("Summary Statistics", 50, y, paint);
        y += spacing;

        paint.setTextSize(12f);
        String[][] stats = {
                {"Total Orders Today", tvTotalOrders.getText().toString()},
                {"Daily Revenue", tvDailySales.getText().toString()},
                {"Total Menu Items", tvMenuItems.getText().toString()},
                {"Stock Alerts", tvLowStock.getText().toString() + " items need restock"}
        };

        for (String[] row : stats) {
            canvas.drawText(row[0], 70, y, paint);
            canvas.drawText(": " + row[1], 250, y, paint);
            y += 40;
        }

        // Footer
        canvas.drawLine(50, 780, 545, 780, paint);
        paint.setTextSize(10f);
        paint.setColor(Color.GRAY);
        canvas.drawText("Authorized by: ShipEats Admin", 50, 800, paint);
        canvas.drawText("Generated on: " + new Date().toString(), 300, 800, paint);

        document.finishPage(page);

        try {
            OutputStream outputStream = getContentResolver().openOutputStream(uri);
            if (outputStream != null) {
                document.writeTo(outputStream);
                outputStream.close();
                Toast.makeText(this, "PDF Saved Successfully!", Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            document.close();
        }
    }

    private void highlightCurrentTab(ImageView icon, TextView text) {
        if (icon != null && text != null) {
            int activeColor = Color.parseColor("#FFD700");
            icon.setColorFilter(activeColor);
            text.setTextColor(activeColor);
            text.setTypeface(null, android.graphics.Typeface.BOLD);
        }
    }

    private void setupBottomNav() {
        View footer = findViewById(R.id.footer_section);
        footer.findViewById(R.id.dashboard_nav).setOnClickListener(v -> Toast.makeText(this, "Already on Dashboard", Toast.LENGTH_SHORT).show());
        footer.findViewById(R.id.inventory_nav).setOnClickListener(v -> startActivity(new Intent(this, A3_Inventory_Management.class)));
        footer.findViewById(R.id.orders_nav).setOnClickListener(v -> startActivity(new Intent(this, A4_CustomerOrderActivity.class)));
        footer.findViewById(R.id.menu_nav).setOnClickListener(v -> startActivity(new Intent(this, A5_MenuManagementActivity.class)));
        footer.findViewById(R.id.profile_nav).setOnClickListener(v -> startActivity(new Intent(this, A6_Profile.class)));
    }
}