package com.example.sofornyilvantarto;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class AutokActivity extends AppCompatActivity {

    private String serverIp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_autok);

        // Kinyerjük a MainActivity-ből kapott IP címet
        serverIp = getIntent().getStringExtra("SERVER_IP");

        // A felső sáv (ActionBar) beállítása és a Vissza gomb engedélyezése
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Elérhető autók");
        }

        TabLayout tabLayout = findViewById(R.id.tab_layout_autok);
        ViewPager2 viewPager = findViewById(R.id.view_pager_autok);

        // ViewPager2 Adapter beállítása a két fülhöz
        viewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                switch (position) {
                    case 0: return AutokListaFragment.newInstance("ELERHETO", serverIp);
                    case 1: return AutokListaFragment.newInstance("FOGLALT", serverIp);
                    default: return AutokListaFragment.newInstance("ELERHETO", serverIp);
                }
            }
            @Override
            public int getItemCount() { return 2; }
        });

        // TabLayout és ViewPager2 összekötése
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0: tab.setText("Elérhető"); break;
                case 1: tab.setText("Foglalt"); break;
            }
        }).attach();

        // Cím frissítése a felső sávban lapozáskor
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                if (getSupportActionBar() != null) {
                    switch (position) {
                        case 0: getSupportActionBar().setTitle("Elérhető autók"); break;
                        case 1: getSupportActionBar().setTitle("Foglalt autók"); break;
                    }
                }
            }
        });
    }

    // A Vissza gomb működése a bal felső sarokban
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}