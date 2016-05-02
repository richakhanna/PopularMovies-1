package com.richdroid.popularmovies.ui.activity;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.richdroid.popularmovies.R;
import com.richdroid.popularmovies.model.Movie;
import com.richdroid.popularmovies.ui.adapter.MovieGridAdapter;
import com.richdroid.popularmovies.utils.PabloPicasso;
import com.squareup.picasso.Callback;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MovieDetailActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private CollapsingToolbarLayout mCollapsingToolBar;
    private ImageView mIvBackDrop;
    private TextView mTvTitle;
    private TextView mTvReleaseDate;
    private TextView mTvRating;
    private TextView mTvOverview;
    private ImageView mIvPoster;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        int primaryDark = getResources().getColor(R.color.colorPrimaryDark);
        setStatusBarColor(primaryDark);

        mCollapsingToolBar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        mIvBackDrop = (ImageView) findViewById(R.id.iv_backdrop);
        mTvTitle = (TextView) findViewById(R.id.tv_title);
        mTvReleaseDate = (TextView) findViewById(R.id.tv_release);
        mTvRating = (TextView) findViewById(R.id.tv_rating);
        mTvOverview = (TextView) findViewById(R.id.tv_movie_overview);
        mIvPoster = (ImageView) findViewById(R.id.iv_poster);

        Bundle data = getIntent().getExtras();
        Movie movie = data.getParcelable(MovieGridAdapter.ARG_MOVIE_DETAIL);

        Log.d(TAG, "Received movie  from Intent " + movie.toString());

        mCollapsingToolBar.setTitle(movie.getOriginalTitle());
        mCollapsingToolBar.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));
        mTvTitle.setText(movie.getOriginalTitle());

        String sourceDateStr = movie.getReleaseDate();
        SimpleDateFormat sourceDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

        Date sourceDate = null;
        try {
            sourceDate = sourceDateFormat.parse(sourceDateStr);
        } catch (ParseException e) {
            Log.e(TAG, e.getMessage());
        }

        SimpleDateFormat finalDateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH);
        String finalDateStr = finalDateFormat.format(sourceDate);

        mTvReleaseDate.setText(finalDateStr);
        mTvRating.setText(String.valueOf(movie.getVoteAverage()));
        mTvOverview.setText(movie.getOverview());

        PabloPicasso.with(this).load(movie.getPosterPath()).fit()
                .placeholder(R.mipmap.placeholder)
                .into(mIvPoster);

        PabloPicasso.with(this).load(movie.getBackdropPath()).into(mIvBackDrop, new Callback() {
            @Override
            public void onSuccess() {
                Bitmap bitmap = ((BitmapDrawable) mIvBackDrop.getDrawable()).getBitmap();
                Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                    public void onGenerated(Palette palette) {
                        applyPalette(palette);
                    }
                });
            }

            @Override
            public void onError() {

            }
        });
    }

    private void applyPalette(Palette palette) {
        int primaryDark = getResources().getColor(R.color.colorPrimaryDark);
        int primary = getResources().getColor(R.color.colorPrimary);
        mCollapsingToolBar.setContentScrimColor(palette.getMutedColor(primary));
        mCollapsingToolBar.setStatusBarScrimColor(palette.getDarkMutedColor(primaryDark));
        setStatusBarColor(palette.getDarkMutedColor(primaryDark));
    }

    private void setStatusBarColor(int darkMutedColor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(darkMutedColor);
        }
    }


}
