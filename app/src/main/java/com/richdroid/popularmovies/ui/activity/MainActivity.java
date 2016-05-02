package com.richdroid.popularmovies.ui.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.gson.Gson;
import com.richdroid.popularmovies.R;
import com.richdroid.popularmovies.app.AppController;
import com.richdroid.popularmovies.model.AllMovieResponse;
import com.richdroid.popularmovies.model.Movie;
import com.richdroid.popularmovies.network.DataManager;
import com.richdroid.popularmovies.network.DataRequester;
import com.richdroid.popularmovies.ui.adapter.MovieGridAdapter;
import com.richdroid.popularmovies.ui.settings.SettingsActivity;
import com.richdroid.popularmovies.utils.Constants;
import com.richdroid.popularmovies.utils.NetworkUtils;
import com.richdroid.popularmovies.utils.ProgressBarUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private List<Movie> mDatasetList;
    private DataManager mDataMan;
    private ProgressBarUtil mProgressBar;
    private LinearLayout mNoNetworkRetryLayout;
    private SharedPreferences pref;
    private String mMovieFilterSort = Constants.MOST_POPULAR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AppController app = ((AppController) getApplication());
        mDataMan = app.getDataManager();

        mProgressBar = new ProgressBarUtil(this);
        pref = PreferenceManager.getDefaultSharedPreferences(this);

        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mNoNetworkRetryLayout = (LinearLayout) findViewById(R.id.network_retry_full_linearlayout);
        Button retryButton = (Button) findViewById(R.id.button_retry);
        retryButton.setOnClickListener(this);

        getSupportActionBar().setTitle(getResources().getString(R.string.most_popular_movies));

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a grid layout manager
        mLayoutManager = new GridLayoutManager(this, 2);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            // use a grid layout manager with two columns
            mLayoutManager = new GridLayoutManager(this, 2);
        } else {
            // use a grid layout manager with three columns
            mLayoutManager = new GridLayoutManager(this, 3);
        }

        mRecyclerView.setLayoutManager(mLayoutManager);
        mDatasetList = new ArrayList<Movie>();

        // specify an adapter
        mAdapter = new MovieGridAdapter(this, mDatasetList);
        mRecyclerView.setAdapter(mAdapter);

    }

    @Override
    protected void onResume() {
        super.onResume();
        //Get preference saved by settings
        String movieFilterSort =
                pref.getString(getString(R.string.sort_by_key), getString(R.string.sort_by_default));
        setActionBarTitle(movieFilterSort);
        if (!mMovieFilterSort.equalsIgnoreCase(movieFilterSort) || mDatasetList.isEmpty()) {
            mMovieFilterSort = movieFilterSort;
            fetchMoviesIfOnline();
        }

    }

    private void setActionBarTitle(String movieFilterSort) {
        if (movieFilterSort.equals(Constants.HIGHEST_RATED)) {
            getSupportActionBar().setTitle(getResources().getString(R.string.top_rated_movies));
        } else if (movieFilterSort.equals(Constants.MOST_POPULAR)) {
            getSupportActionBar().setTitle(getResources().getString(R.string.most_popular_movies));
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_retry:
                fetchMoviesIfOnline();
                break;
        }
    }

    private void fetchMoviesIfOnline() {
        if (NetworkUtils.isOnline(this)) {
            mNoNetworkRetryLayout.setVisibility(View.GONE);
            if (mMovieFilterSort.equals(Constants.HIGHEST_RATED)) {
                mProgressBar.show();
                Log.v(TAG, "Calling : get top rated movies api");
                mDataMan.getMovies(
                        new WeakReference<DataRequester>(mPopularMoviesRequester), mMovieFilterSort, TAG);
            } else if (mMovieFilterSort.equals(Constants.MOST_POPULAR)) {
                mProgressBar.show();
                Log.v(TAG, "Calling : get popular movies api");
                mDataMan.getMovies(
                        new WeakReference<DataRequester>(mPopularMoviesRequester), mMovieFilterSort, TAG);
            }


        } else {
            if (mDatasetList.isEmpty()) {
                NetworkUtils.showSnackbar(mRecyclerView, getResources().getString(R.string.no_internet_connection));
                mNoNetworkRetryLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    private DataRequester mPopularMoviesRequester = new DataRequester() {

        @Override
        public void onFailure(Throwable error) {
            if (isFinishing()) {
                return;
            }

            mProgressBar.hide();
            Log.v(TAG, "Failure : movies onFailure");

            mDatasetList.clear();
            mAdapter.notifyDataSetChanged();
            NetworkUtils.showSnackbar(mRecyclerView, getResources().getString(R.string.unable_to_reach_server));
        }

        @Override
        public void onSuccess(Object respObj) {
            if (isFinishing()) {
                return;
            }

            mProgressBar.hide();
            mDatasetList.clear();
            mAdapter.notifyDataSetChanged();
            Log.v(TAG, "Success : movies data : " + new Gson().toJson(respObj).toString());
            AllMovieResponse response = (AllMovieResponse) respObj;

            if (response != null && response.getResults() != null && response.getResults().size() > 0) {
                List<Movie> movieList = response.getResults();
                for (Movie movie : movieList) {
                    mDatasetList.add(movie);
                }
                mAdapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu xml; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
