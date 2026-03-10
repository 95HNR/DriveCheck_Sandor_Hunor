package com.example.sofornyilvantarto.uj;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class KerelmekActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kerelmek);

        // A rendszer által (Manifest alapján) létrehozott sáv használata
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Beérkező kérelmek");
        }

        TabLayout tabLayout = findViewById(R.id.tab_layout_kerelmek);
        ViewPager2 viewPager = findViewById(R.id.view_pager_kerelmek);

        viewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                switch (position) {
                    case 0: return KerelmekListaFragment.newInstance("BEERKEZO");
                    case 1: return KerelmekListaFragment.newInstance("JOVAHAGYOTT");
                    case 2: return KerelmekListaFragment.newInstance("ELUTASITOTT");
                    default: return KerelmekListaFragment.newInstance("BEERKEZO");
                }
            }
            @Override
            public int getItemCount() { return 3; }
        });

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0: tab.setText("Beérkező"); break;
                case 1: tab.setText("Jóváhagyott"); break;
                case 2: tab.setText("Elutasított"); break;
            }
        }).attach();

        // Cím frissítése a felső sávban lapozáskor
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                if (getSupportActionBar() != null) {
                    switch (position) {
                        case 0: getSupportActionBar().setTitle("Beérkező kérelmek"); break;
                        case 1: getSupportActionBar().setTitle("Jóváhagyott kérelmek"); break;
                        case 2: getSupportActionBar().setTitle("Elutasított kérelmek"); break;
                    }
                }
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}