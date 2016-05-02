package com.richdroid.popularmovies.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.richdroid.popularmovies.R;
import com.richdroid.popularmovies.model.Movie;
import com.richdroid.popularmovies.ui.activity.MovieDetailActivity;
import com.richdroid.popularmovies.utils.PabloPicasso;

import java.util.List;

/**
 * Created by richa.khanna on 3/18/16.
 */
public class MovieGridAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static Context mContext;
    private static List<Movie> mDatasetList;
    private static final int PERSON_CONTACT_DETAIL = 0;
    private static final int ANIMAL_CONTACT_DETAIL = 1;
    // Allows to remember the last item shown on screen
    private int lastAnimatedItemPosition = -1;
    public static final String ARG_MOVIE_DETAIL = "MOVIE_DETAIL";


    // Provide a suitable constructor (depends on the kind of dataset)
    public MovieGridAdapter(Context context, List<Movie> datasetList) {
        mContext = context;
        mDatasetList = datasetList;
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class MovieViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {
        private TextView mTVTitle;
        private CardView mCardView;
        private ImageView mIVThumbNail;
        private TextView mTVRating;


        public MovieViewHolder(View view) {
            super(view);
            this.mTVTitle = (TextView) view.findViewById(R.id.tv_title);
            this.mCardView = (CardView) view.findViewById(R.id.card_view);
            this.mIVThumbNail = (ImageView) view.findViewById(R.id.iv_thumbnail);
            this.mTVRating = (TextView) view.findViewById(R.id.tv_rating);
            this.mIVThumbNail.setOnClickListener(this);
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int itemPosition = getAdapterPosition();
            Movie movie = mDatasetList.get(itemPosition);

            switch (view.getId()) {
                case R.id.iv_thumbnail:

                    Bundle bundle = new Bundle();
                    bundle.putParcelable(ARG_MOVIE_DETAIL, movie);

                    Intent movieDetailIntent = new Intent(mContext, MovieDetailActivity.class);
                    movieDetailIntent.putExtras(bundle);
                    mContext.startActivity(movieDetailIntent);
                    break;

                default:
                    Toast.makeText(mContext, "You clicked at position " + itemPosition +
                            " on movie thumbnail : " +
                            movie.getTitle(), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public boolean onLongClick(View v) {
            int itemPosition = getAdapterPosition();
            if (itemPosition != RecyclerView.NO_POSITION) {
                Movie movie = mDatasetList.get(itemPosition);

                //If delete button is already shown, hide it by setting flag to false
                //And the item will be updated when onBindViewHolder will be called.
                if (movie.isToShowDeleteIcon()) {
                    movie.setToShowDeleteIcon(false);
                } else {
                    movie.setToShowDeleteIcon(true);
                }

                notifyItemChanged(itemPosition);
            }
            return true;
        }
    }


    // Create new views (invoked by the layout manager)
    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.movie_item_view, parent, false);

        switch (viewType) {
            case PERSON_CONTACT_DETAIL:
            case ANIMAL_CONTACT_DETAIL:
                return new MovieViewHolder(view);
        }
        return null;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        MovieViewHolder cusHolder = (MovieViewHolder) holder;
        cusHolder.mTVTitle.setText(mDatasetList.get(position).getTitle());
        cusHolder.mTVRating.setText(String.valueOf(mDatasetList.get(position).getVoteAverage()));
        String completePosterPath = mDatasetList.get(position).getPosterPath();
        PabloPicasso.with(mContext).load(completePosterPath).placeholder(R.mipmap.placeholder)
                .into(cusHolder.mIVThumbNail);
        cusHolder.mIVThumbNail.setVisibility(View.VISIBLE);

//        if (mDatasetList.get(position).isToShowDeleteIcon()) {
//            String completePosterPath = BASE_URL_IMAGE_POSTER + mDatasetList.get(position).getPosterPath();
//            PabloPicasso.with(mContext).load(completePosterPath).noPlaceholder()
//                    .into(cusHolder.mIVThumbNail);
//            cusHolder.mIVThumbNail.setVisibility(View.VISIBLE);
//        } else {
//            cusHolder.mIVThumbNail.setVisibility(View.GONE);
//        }

        setEnterAnimation(cusHolder.mCardView, position);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDatasetList.size();
    }

    @Override
    public int getItemViewType(int position) {
//        Movie movie = mDatasetList.get(position);
//        String contactType = movie.getType();
//        if (contactType.equalsIgnoreCase("Animal")) {
//            return ANIMAL_CONTACT_DETAIL;
//        }
        return PERSON_CONTACT_DETAIL;
    }

    private void setEnterAnimation(View viewToAnimate, int position) {
        // If the bound view wasn't previously displayed on screen, it will be animated
        if (position > lastAnimatedItemPosition) {
            //Animation using xml
            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.translate_left);
            viewToAnimate.startAnimation(animation);

//            Or Animation using ObjectAnimator
//            ObjectAnimator anim = ObjectAnimator.ofFloat(viewToAnimate, "translationX", 300, 0);
//            anim.setDuration(1500);
//            DecelerateInterpolator decelerateInterpolator = new DecelerateInterpolator(2);
//            anim.setInterpolator(decelerateInterpolator);
//            anim.start();

//            Or Animation using setTranslationX
//            viewToAnimate.setTranslationX(300);
//            viewToAnimate.animate().translationX(0).
//                    setInterpolator(new DecelerateInterpolator(2)).setDuration(1500).start();

            lastAnimatedItemPosition = position;
        }
    }

    /**
     * The view could be reused while the animation is been happening.
     * In order to avoid that is recommendable to clear the animation when is detached.
     */
    @Override
    public void onViewDetachedFromWindow(final RecyclerView.ViewHolder holder) {
        ((MovieViewHolder) holder).mCardView.clearAnimation();
    }
}
