package com.example.studdy;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private LinearLayout dotsLayout;
    private Button nextButton;
    private int[] layouts = {R.layout.slide_1, R.layout.slide_2}; // The two slider layouts
    private TextView[] dots;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        viewPager = findViewById(R.id.viewPager);
        dotsLayout = findViewById(R.id.dotsLayout);
        nextButton = findViewById(R.id.nextButton);

        // set up the ViewPager with the adapter
        MyPagerAdapter pagerAdapter = new MyPagerAdapter();
        viewPager.setAdapter(pagerAdapter);

        // set up the dots indicator
        addDotsIndicator(0);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                addDotsIndicator(position);
                if (position == layouts.length - 1) {
                    nextButton.setText(">>");
                } else {
                    nextButton.setText(">");
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        // Handle Next/Skip button click
        nextButton.setOnClickListener(v -> {
            int current = viewPager.getCurrentItem();
            if (current < layouts.length - 1) {
                // Move to the next slide
                viewPager.setCurrentItem(current + 1);
            } else {
                // Last slide, go to MainActivity
                startActivity(new Intent(OnboardingActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

    private void addDotsIndicator(int position) {
        dots = new TextView[layouts.length];
        dotsLayout.removeAllViews();

        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText("•"); // Unicode for a dot
            dots[i].setTextSize(35);
            dots[i].setTextColor(ContextCompat.getColor(this, i == position ? R.color.purple_500 : R.color.grey_500));
            dotsLayout.addView(dots[i]);
        }
    }

    // Adapter for the ViewPager
    private class MyPagerAdapter extends PagerAdapter {
        @Override
        public int getCount() {
            return layouts.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            LayoutInflater inflater = LayoutInflater.from(OnboardingActivity.this);
            View view = inflater.inflate(layouts[position], container, false);
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = (View) object;
            container.removeView(view);
        }
    }
}