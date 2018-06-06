package com.coreelements.de.movienight.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.coreelements.de.movienight.Movie;
import com.coreelements.de.movienight.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ResultsAdapter extends RecyclerView.Adapter<ResultsAdapter.ResultsViewHolder> {

    private List<Movie> mMovies;
    private Context mContext;

    public ResultsAdapter (Context context, List<Movie> movies) {
        mMovies = movies;
        mContext = context;
    }

    @NonNull
    @Override
    public ResultsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.results_list_item_picture, parent, false);
        ResultsViewHolder viewHolder = new ResultsViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ResultsViewHolder holder, int position) {
        holder.bindResults(mMovies.get(position));
    }

    @Override
    public int getItemCount() {
        return mMovies.size();
    }

    public class ResultsViewHolder extends ViewHolder implements View.OnClickListener{

        public TextView mTitleLabel, mVoteAverageLabel, mReleaseDateLabel;
        private final String posterPrefixURL = "https://image.tmdb.org/t/p/w185_and_h278_bestv2";
        private String posterIdURL;
        private String posterURL;
        private Movie mSelectedMovie;
        public ImageView posterView;


        public ResultsViewHolder(View itemView) {
            super(itemView);

            mTitleLabel = (TextView) itemView.findViewById(R.id.titleLabel);
            mVoteAverageLabel = (TextView) itemView.findViewById(R.id.voteAverageLabel);
            mReleaseDateLabel = (TextView) itemView.findViewById(R.id.releaseDateLabel);
            posterView = (ImageView)itemView.findViewById(R.id.posterImageView);

            itemView.setOnClickListener(this);
        }

        public void bindResults(Movie movie) {
            mSelectedMovie = movie;
            mTitleLabel.setText(mSelectedMovie.getTitle());
            mVoteAverageLabel.setText("Average Rating: " + mSelectedMovie.getVoteAverage());
            if (mSelectedMovie.isTvShow()) {
                mReleaseDateLabel.setText("First Aired On: " + mSelectedMovie.getReleaseDate());
            } else {
                mReleaseDateLabel.setText("Released On: " + mSelectedMovie.getReleaseDate());
            }
            posterIdURL = mSelectedMovie.getPosterURL();
            posterURL = posterPrefixURL + posterIdURL;
            Picasso.with(mContext).load(posterURL).into(posterView);
        }



        @Override
        public void onClick(View v) {
            String overview = String.format("%s\n\nAverage Rating: %.1f\nAmount Of Ratings: %d\nReleased On: %s",
                                            mSelectedMovie.getOverview(),
                                            mSelectedMovie.getVoteAverage(),
                                            mSelectedMovie.getVoteCount(),
                                            mSelectedMovie.getReleaseDate());
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setIcon(posterView.getDrawable())
                    .setTitle(mSelectedMovie.getTitle())
                    .setMessage(overview)
                    .setNeutralButton("Got It!", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // FIRE ZE MISSILES!
                        }
                    });
            builder.create().show();
        }
    }
}
