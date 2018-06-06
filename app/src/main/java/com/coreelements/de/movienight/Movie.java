package com.coreelements.de.movienight;

public class Movie {
    private final int voteCount;
    private final double voteAverage;
    private final String title;
    private  String releaseDate = null;
    private final String overview;
    private final String posterURL;
    private boolean isTvShow;
    private double popularity;


    public Movie(String title, double voteAverage, int voteCount, String releaseDate, String overview, String posterURL, double popularity, boolean isTvShow) {
        this.title = title;
        this.voteAverage = voteAverage;
        this.voteCount = voteCount;
        this.releaseDate = releaseDate;
        this.overview = overview;
        this.posterURL = posterURL;
        this.isTvShow = isTvShow;
        this.popularity = popularity;
    }

    public boolean isTvShow() {
        return isTvShow;
    }

    public int compareToByPopularity(Movie other) {
        if (popularity == other.getPopularity()) {
            return 0;
        }else if (popularity > other.getPopularity()) {
            return -1;
        } else {
            return 1;
        }
    }

    public double getPopularity() {
        return popularity;
    }

    public String getPosterURL() {
        return posterURL;
    }

    public String getOverview() {
        return overview;
    }

    public String getTitle() {
        return title;
    }

    public double getVoteAverage() {
        return voteAverage;
    }

    public int getVoteCount() {
        return voteCount;
    }

    public String getReleaseDate() {
        return releaseDate;
    }
}
