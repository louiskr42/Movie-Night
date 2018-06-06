package com.coreelements.de.movienight;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.coreelements.de.movienight.adapters.ResultsAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private int minVoteCount;
    private double minVoteAverage;

    private int minReleaseYear;
    private int minReleaseMonth;
    private int minReleaseDay;

    private int maxReleaseYear;
    private int maxReleaseMonth;
    private int maxReleaseDay;

    public static final String TAG = MainActivity.class.getSimpleName();
    private final String apiKey = "9124c7d5ecff2a6e5927e02a75897775";
    private final String movieGenreURL = "https://api.themoviedb.org/3/genre/movie/list?api_key=" + apiKey + "&language=en-US";
    private final String tvGenreURL = "https://api.themoviedb.org/3/genre/tv/list?api_key=" + apiKey + "&language=en-US";
    private int currentMoviePage = 1;
    private int maxMoviePages;
    private int currentTVPage = 1;
    private int maxTVPages;
    ArrayList<Movie> movies = new ArrayList<>();
    ArrayList<Integer> genreIdList = new ArrayList<>();
    ArrayList<String> genresListDialog = new ArrayList<>();

    String tvOrMovie = "movie";
    String sortBy = "Popularity";

    Genres genres;

    Spinner minDSP, minMSP, maxDSP, maxMSP, sortSP;
    Button searchBTN, genreBTN;
    RecyclerView resultsRV;
    EditText voteCountET, voteAverageET, minYET, maxYET;
    RadioButton movieBtn, tvBtn, bothBtn;
    RadioGroup mRadioGroup;

    ProgressBar mProgressBar;

    TextView noResultsTV, genresTV;

    ArrayAdapter<String> genreAD, dayAD, monthAD;
    ArrayAdapter<CharSequence> sortAD;
    RecyclerView.Adapter mAdapter;

    TreeMap<String, Integer> movieMap = new TreeMap<>();
    TreeMap<String, Integer> tvMap = new TreeMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchBTN = (Button)findViewById(R.id.searchButton);
        searchBTN.setOnClickListener(this);

        genreBTN = (Button)findViewById(R.id.genreButton);
        genreBTN.setOnClickListener(this);

        genresTV = (TextView)findViewById(R.id.genreTextView);

        sortSP = (Spinner)findViewById(R.id.sortSpinner);
        sortSP.setOnItemSelectedListener(this);

        sortAD = ArrayAdapter.createFromResource(this, R.array.sorting, android.R.layout.simple_spinner_item);
        sortAD.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        sortSP.setAdapter(sortAD);

        mProgressBar = (ProgressBar)findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.INVISIBLE);

        noResultsTV = (TextView)findViewById(R.id.noResultsTextView);
        noResultsTV.setVisibility(View.INVISIBLE);

        resultsRV = (RecyclerView)findViewById(R.id.resultRecyclerView);
        resultsRV.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                int currentPosition = ((LinearLayoutManager)resultsRV.getLayoutManager()).findLastVisibleItemPosition();

                if (!resultsRV.canScrollVertically(1)) {

                    currentMoviePage++;
                    currentTVPage++;

                    if (tvOrMovie.equals("movie")) {
                        if (currentMoviePage < maxMoviePages) {
                            makeCall(true);
                            resultsRV.scrollToPosition(currentPosition);
                        }
                    } else if (tvOrMovie.equals("tv")){
                        if (currentTVPage < maxTVPages) {
                            makeCall(true);
                            resultsRV.scrollToPosition(currentPosition);
                        }
                    } else {
                        if (currentMoviePage < maxMoviePages && currentTVPage < maxTVPages) {
                            makeCall(true);
                            resultsRV.scrollToPosition(currentPosition);
                        }
                    }

                    Log.v("Scroll", "UNTEN!");

                }
            }
        });

        movieBtn = (RadioButton) findViewById(R.id.movieButton);
        movieBtn.setOnClickListener(this);
        tvBtn = (RadioButton)findViewById(R.id.tvButton);
        tvBtn.setOnClickListener(this);
        bothBtn = (RadioButton)findViewById(R.id.bothButton);
        bothBtn.setOnClickListener(this);
        mRadioGroup = (RadioGroup)findViewById(R.id.radioGroup);

        voteCountET = (EditText)findViewById(R.id.voteCountEditText);
        voteAverageET = (EditText)findViewById(R.id.voteAverageEditText);
        minDSP = (Spinner)findViewById(R.id.minDaySpinner);
        minDSP.setOnItemSelectedListener(this);
        minMSP = (Spinner)findViewById(R.id.minMonthSpinner);
        minMSP.setOnItemSelectedListener(this);
        minYET = (EditText) findViewById(R.id.minYearEditText);
        maxDSP = (Spinner)findViewById(R.id.maxDaySpinner);
        maxDSP.setOnItemSelectedListener(this);
        maxMSP = (Spinner)findViewById(R.id.maxMonthSpinner);
        maxMSP.setOnItemSelectedListener(this);
        maxYET = (EditText) findViewById(R.id.maxYearEditText);




        genreAD = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        genreAD.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


        callGenres();


        dayAD = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        dayAD.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        for (int i = 1; i<=31; i++) {
            dayAD.add(""+ i);
        }

        monthAD = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        monthAD.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        for (int i = 1; i<=12; i++) {
            monthAD.add(""+ i);
        }

        minDSP.setAdapter(dayAD);
        minMSP.setAdapter(monthAD);
        maxDSP.setAdapter(dayAD);
        maxMSP.setAdapter(monthAD);


    }

    private void callGenres() {
        resultsRV.setVisibility(View.INVISIBLE);
        noResultsTV.setVisibility(View.INVISIBLE);
        callMovieGenres(movieGenreURL);
        callTvGenres(tvGenreURL);
    }

    private void fillGenres() {
        genres = new Genres(movieMap, tvMap);
        genreAD.clear();

        if (tvOrMovie.equals("movie")) {
            for (String genre : genres.getMovieMap().keySet()) {
                genreAD.add(genre);
            }
        } else if (tvOrMovie.equals("tv")) {
            for (String genre : genres.getTvMap().keySet()) {
                genreAD.add(genre);
            }
        } else {
            for (String genre : genres.getBothMap().keySet()) {
                genreAD.add(genre);
            }
        }

        genreAD.notifyDataSetChanged();
    }

    private void callTvGenres(String tvURL) {

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder().url(tvURL).build();

                Call call = client.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.v("FAIL", "Fehler bei TV");

                    }

                    @Override
                    public void onResponse(Call call, Response response){

                        try {
                            final String jsonData = response.body().string();
                            Log.v(TAG, jsonData);

                            if (response.isSuccessful()){
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            loadTvGenres(jsonData);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });

                            }
                        } catch (IOException e) {
                            Log.e(TAG, "IO Exception caught: ", e);
                            e.printStackTrace();
                        }
                    }
                });
    }

    private void loadTvGenres(String jsonData) throws JSONException{
        JSONObject jsonObject = new JSONObject(jsonData);
        JSONArray jsonArray = jsonObject.getJSONArray("genres");

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject genre = jsonArray.getJSONObject(i);
            tvMap.put(genre.getString("name"), genre.getInt("id"));
        }

        fillGenres();
        //checkConditions();
    }

    public void callMovieGenres(String movieURL) {


        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder().url(movieURL).build();


                Call call = client.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.v("FAIL", "Fehler bei Movie");
                    }

                    @Override
                    public void onResponse(Call call, Response response){

                        try {
                            final String jsonData = response.body().string();
                            Log.v(TAG, jsonData);


                            if (response.isSuccessful()){

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            loadMovieGenres(jsonData);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });

                            }
                        } catch (IOException e) {
                            Log.e(TAG, "IO Exception caught: ", e);
                            e.printStackTrace();
                        }
                    }
                });



    }

    public  void loadMovieGenres(String jsonData) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonData);
        JSONArray jsonArray = jsonObject.getJSONArray("genres");

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject genre = jsonArray.getJSONObject(i);
            movieMap.put(genre.getString("name"), genre.getInt("id"));
        }
    }

    public void displayResults(boolean isRefresh) {

        mProgressBar.setVisibility(View.INVISIBLE);

        if (tvOrMovie.equals("both")) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                movies.sort(new Comparator<Movie>() {

                    @Override
                    public int compare(Movie o1, Movie o2) {
                        if (o1.equals(o2)) {
                            return 0;
                        }
                        return o1.compareToByPopularity(o2);
                    }
                });
            }

        }

        if (tvOrMovie.equals("both")) {
            if (mAdapter == null) {
                mAdapter = new ResultsAdapter(this, movies);
            }

        } else {
            mAdapter = new ResultsAdapter(this, movies);
        }

        if (!isRefresh) {
            resultsRV.setAdapter(mAdapter);
        } else {
            mAdapter.notifyDataSetChanged();
        }

        if (!isRefresh) {
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
            resultsRV.setLayoutManager(mLayoutManager);
        }


        resultsRV.setHasFixedSize(true);

        if (movies.size() == 0) {
            noResultsTV.setVisibility(View.VISIBLE);
        } else {
            resultsRV.setVisibility(View.VISIBLE);
        }
    }

    private void makeCall(final boolean isRefresh) {
        //String tmdbGetMoviesForGenreURL = "https://api.themoviedb.org/3/genre/" + genreID + "/movies?sort_by=created_at.asc&include_adult=true&language=en-US&api_key=" + apiKey;
        String tmdbMoviesForGenreURL;
        final String tmdbTvForGenreURL;
        String genreID = "";
        for (int genreId : genreIdList) { //%7C
            if (genreId == genreIdList.get(0)) {
                genreID = "" + genreId;
            } else {
                genreID = genreID + "%7C" + genreId;
            }
        }
        String sortByOrder;
        if (sortBy.contains("descending")) {
            sortByOrder = "desc";
        } else {
            sortByOrder = "asc";
        }
        if (sortBy.contains("Popularity")) {
            tmdbMoviesForGenreURL = "https://api.themoviedb.org/3/discover/movie?api_key=" + apiKey + "&language=en-US&sort_by=popularity." + sortByOrder + "&include_adult=true&include_video=false&page=" +
                    currentMoviePage + "&with_genres=" +
                    genreID;
            tmdbTvForGenreURL = "https://api.themoviedb.org/3/discover/tv?api_key=" + apiKey + "&language=en-US&sort_by=popularity." + sortByOrder + "&page=" + currentTVPage + "&with_genres=" +
                    genreID + "&include_null_first_air_dates=true";
        } else if (sortBy.contains("Release date")) {
            tmdbMoviesForGenreURL = "https://api.themoviedb.org/3/discover/movie?api_key=" + apiKey + "&language=en-US&sort_by=release_date." + sortByOrder + "&include_adult=true&include_video=false&page=" +
                    currentMoviePage + "&with_genres=" +
                    genreID;
            tmdbTvForGenreURL = "https://api.themoviedb.org/3/discover/tv?api_key=" + apiKey + "&language=en-US&sort_by=release." + sortByOrder + "&page=" + currentTVPage + "&with_genres=" +
                    genreID + "&include_null_first_air_dates=true";
        } else if (sortBy.contains("Revenue")) {
            tmdbMoviesForGenreURL = "https://api.themoviedb.org/3/discover/movie?api_key=" + apiKey + "&language=en-US&sort_by=revenue." + sortByOrder + "&include_adult=true&include_video=false&page=" +
                    currentMoviePage + "&with_genres=" +
                    genreID;
            tmdbTvForGenreURL = "https://api.themoviedb.org/3/discover/tv?api_key=" + apiKey + "&language=en-US&sort_by=revenue." + sortByOrder + "&page=" + currentTVPage + "&with_genres=" +
                    genreID + "&include_null_first_air_dates=true";
        } else if (sortBy.contains("Average vote")) {
            tmdbMoviesForGenreURL = "https://api.themoviedb.org/3/discover/movie?api_key=" + apiKey + "&language=en-US&sort_by=vote_average." + sortByOrder + "&include_adult=true&include_video=false&page=" +
                    currentMoviePage + "&with_genres=" +
                    genreID;
            tmdbTvForGenreURL = "https://api.themoviedb.org/3/discover/tv?api_key=" + apiKey + "&language=en-US&sort_by=vote_average." + sortByOrder + "&page=" + currentTVPage + "&with_genres=" +
                    genreID + "&include_null_first_air_dates=true";
        } else {
            tmdbMoviesForGenreURL = "https://api.themoviedb.org/3/discover/movie?api_key=" + apiKey + "&language=en-US&sort_by=vote_count."  + sortByOrder + "&include_adult=true&include_video=false&page=" +
                    currentMoviePage + "&with_genres=" +
                    genreID;
            tmdbTvForGenreURL = "https://api.themoviedb.org/3/discover/tv?api_key=" + apiKey + "&language=en-US&sort_by=vote_count." + sortByOrder + "&page=" + currentTVPage + "&with_genres=" +
                    genreID + "&include_null_first_air_dates=true";
        }

        final OkHttpClient client = new OkHttpClient();

        Request request;

        if (tvOrMovie.equals("movie") || tvOrMovie.equals("both")) {
            request = new Request.Builder().url(tmdbMoviesForGenreURL).build();
        } else {
            request = new Request.Builder().url(tmdbTvForGenreURL).build();
        }

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProgressBar.setVisibility(View.INVISIBLE);
                        noResultsTV.setVisibility(View.INVISIBLE);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response){

                try {
                    final String jsonData = response.body().string();

                    Log.v(TAG, jsonData);

                    if (response.isSuccessful()){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run(){
                                try {
                                    loadMovies(jsonData, 0, isRefresh);
                                    if (tvOrMovie.equals("both")) {
                                        makeSecondCall(client, tmdbTvForGenreURL, isRefresh);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                } catch (IndexOutOfBoundsException ioobe) {
                                    ioobe.printStackTrace();
                                }
                            }
                        });

                    }
                } catch (IOException e) {
                    Log.e(TAG, "IO Exception caught: ", e);
                    e.printStackTrace();
                }
            }
        });
    }

    private void makeSecondCall(OkHttpClient client, String tmdbTvForGenreURL, final boolean isRefresh) {
        Call call2 = client.newCall(new Request.Builder().url(tmdbTvForGenreURL).build());
        call2.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProgressBar.setVisibility(View.INVISIBLE);
                        noResultsTV.setVisibility(View.INVISIBLE);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response){

                try {
                    final String jsonData = response.body().string();
                    Log.v(TAG, jsonData);

                    if (response.isSuccessful()){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run(){
                                try {
                                    loadMovies(jsonData, 1, isRefresh);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                } catch (IndexOutOfBoundsException ioobe) {
                                    ioobe.printStackTrace();
                                }
                            }
                        });

                    }
                } catch (IOException e) {
                    Log.e(TAG, "IO Exception caught: ", e);
                    e.printStackTrace();
                }
            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()){
            isAvailable = true;
        }
        return isAvailable;
    }

    private void loadMovies(String jsonData, int tv, boolean isRefresh) throws JSONException{
        boolean isTV = jsonData.contains("original_name");
        JSONObject root = new JSONObject(jsonData);
        JSONArray jsonMovies = root.getJSONArray("results");

        if (isTV) {
            maxTVPages = root.getInt("total_pages");
        } else {
            maxMoviePages = root.getInt("total_pages");
        }

        if (tv == 0 && isRefresh == false) {
            movies.clear();
        }

        for (int i = 0; i < jsonMovies.length(); i++) {
            JSONObject jsonMovie = jsonMovies.getJSONObject(i);
            Movie movie;
            try {
                if (tvOrMovie.equals("movie")) {
                    movie = new Movie(jsonMovie.getString("title"), jsonMovie.getDouble("vote_average"), jsonMovie.getInt("vote_count"), jsonMovie.getString("release_date"),
                            jsonMovie.getString("overview"), jsonMovie.getString("poster_path"), jsonMovie.getDouble("popularity"), false);

                } else if (tvOrMovie.equals("tv")) {
                    movie = new Movie(jsonMovie.getString("name"), jsonMovie.getDouble("vote_average"), jsonMovie.getInt("vote_count"), jsonMovie.getString("first_air_date"),
                            jsonMovie.getString("overview"), jsonMovie.getString("poster_path"), jsonMovie.getDouble("popularity"), true);

                } else {
                    if (!isTV) {
                        movie = new Movie(jsonMovie.getString("original_title"), jsonMovie.getDouble("vote_average"), jsonMovie.getInt("vote_count"), jsonMovie.getString("release_date"),
                                jsonMovie.getString("overview"), jsonMovie.getString("poster_path"), jsonMovie.getDouble("popularity"), false);
                    } else {
                        movie = new Movie(jsonMovie.getString("original_name"), jsonMovie.getDouble("vote_average"), jsonMovie.getInt("vote_count"), jsonMovie.getString("first_air_date"),
                                jsonMovie.getString("overview"), jsonMovie.getString("poster_path"), jsonMovie.getDouble("popularity"), true);
                    }
                }

                try {
                    String[] releaseDate = movie.getReleaseDate().split("-");
                    int releaseYear = Integer.parseInt(releaseDate[0]);
                    int releaseMonth = Integer.parseInt(releaseDate[1]);
                    int releaseDay = Integer.parseInt(releaseDate[2]);

                    if (isReleasedInTime(releaseYear, releaseMonth, releaseDay)) {
                        if (isEnoughVotes(movie.getVoteCount())) {
                            if (isRatingThreshold(movie.getVoteAverage())) {
                                movies.add(movie);
                            }
                        }
                    }
                } catch (NumberFormatException nfe) {
                    nfe.printStackTrace();
                }
            } catch (JSONException jse) {
                jse.printStackTrace();
            }


        }

        if (tvOrMovie.equals("both")) {
            if (tv == 1) {
                displayResults(isRefresh);
            }
        } else {
            displayResults(isRefresh);
        }
    }

    private boolean isReleasedInTime(int releaseYear, int releaseMonth, int releaseDay) {
        if (releaseYear > minReleaseYear && releaseYear < maxReleaseYear) {
            return true;
        } else if (releaseYear == minReleaseYear) {
            if (isReleasedLaterMonth(releaseMonth)) {
                return true;
            } else if (releaseMonth == minReleaseMonth) {
                if (isReleasedLaterDay(releaseDay)) {
                        return true;
                }
            }
        } else if (releaseYear == maxReleaseYear) {
            if (isReleasedEarlierMonth(releaseMonth)) {
                return true;
            } else if (releaseMonth == maxReleaseMonth) {
                if (isReleasedEarlierDay(releaseDay)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isReleasedEarlierDay(int releaseDay) {
        if (releaseDay <= maxReleaseDay) {
            return true;
        }
        return false;
    }

    private boolean isReleasedEarlierMonth(int releaseMonth) {
        if (releaseMonth < maxReleaseMonth) {
            return true;
        }

        return false;
    }

    private boolean isReleasedLaterDay(int releaseDay) {
        if (releaseDay >= minReleaseDay) {
            return true;
        }
        return false;
    }

    private boolean isReleasedLaterMonth(int releaseMonth) {
        if (releaseMonth > minReleaseMonth) {
            return true;
        }

        return false;
    }

    private boolean isEnoughVotes(int voteCount) {
        if (voteCount >= minVoteCount) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isRatingThreshold(double voteAverage) {
        if (voteAverage >= minVoteAverage) {
            return true;
        } else {
            return false;
        }
    }

    private void getInput() {

        sortBy = sortSP.getSelectedItem().toString();

        minReleaseDay = Integer.parseInt(minDSP.getSelectedItem().toString());
        minReleaseMonth = Integer.parseInt(minMSP.getSelectedItem().toString());
        if (minYET.getText().toString().trim().length() == 0 || minYET.getText().toString().trim().length() > 4 || minYET.getText().toString().trim().length() < 4) {
            minReleaseYear = 0;
            minYET.setText("0");
        } else {
            minReleaseYear = Integer.parseInt(minYET.getText().toString().trim());
        }
        maxReleaseDay = Integer.parseInt(maxDSP.getSelectedItem().toString());
        maxReleaseMonth = Integer.parseInt(maxMSP.getSelectedItem().toString());
        if (maxYET.getText().toString().trim().length() == 0 || maxYET.getText().toString().trim().length() > 4 || maxYET.getText().toString().trim().length() < 4) {
            maxReleaseYear = 2045;
            maxYET.setText("2045");
        } else {
            maxReleaseYear = Integer.parseInt(maxYET.getText().toString().trim());
        }

        if (voteCountET.getText().toString().trim().replace(",", ".").length() == 0) {
            minVoteCount = 0;
            voteCountET.setText("0");
        } else {
            minVoteCount = Integer.parseInt(voteCountET.getText().toString().trim().replace(",", "."));
        }
        if (voteAverageET.getText().toString().trim().replace(",", ".").length() == 0) {
            minVoteAverage = 0;
            voteAverageET.setText("0");
        } else {
            minVoteAverage = Double.parseDouble(voteAverageET.getText().toString().trim().replace(",", "."));
        }

        if (genreAD.getCount() == 0) {
            callGenres();
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.searchButton:
                if (!genreIdList.isEmpty()) {
                    currentMoviePage = 1;
                    currentTVPage = 1;
                    checkConditions();
                } else {
                    Toast.makeText(this, "Please select one or more genres!", Toast.LENGTH_SHORT).show();
                    callGenres();
                }
            break;

            case R.id.movieButton:
                genreIdList.clear();
                genresListDialog.clear();
                genresTV.setText("Selected Genres: ");
                sortSP.setVisibility(View.VISIBLE);
                sortSP.setClickable(true);
                tvOrMovie = "movie";
                callGenres();
                break;

            case R.id.tvButton:
                genreIdList.clear();
                genresListDialog.clear();
                genresTV.setText("Selected Genres: ");
                sortSP.setVisibility(View.VISIBLE);
                sortSP.setClickable(true);
                tvOrMovie = "tv";
                callGenres();
                break;

            case R.id.bothButton:
                genreIdList.clear();
                genresListDialog.clear();
                genresTV.setText("Selected Genres: ");
                sortSP.setVisibility(View.INVISIBLE);
                sortSP.setClickable(false);
                tvOrMovie = "both";
                callGenres();
                break;

            case R.id.genreButton:

                if (genreAD.getCount() == 0) {
                    callGenres();
                    Toast.makeText(this, "Loading Genres, please wait a moment.", Toast.LENGTH_SHORT).show();
                } else {
                    genreDialog();
                }
                break;
        }


    }

    public void updateGenreTextView() {
        for (String genre : genresListDialog) {
            if (genreIdList.contains(genres.getBothMap().get(genre.replace(" [selected]", "")))) {
                if (genre.equals(genresListDialog.get(0))) {
                    genresTV.setText("Selected Genres: " + genre);
                } else {
                    genresTV.setText(genresTV.getText() + ", " + genre);
                }
            }
        }
    }

    private void genreDialog() {

        genresListDialog = new ArrayList<>();


            if (tvOrMovie.equals("movie")) {
                for (String genre : genres.getMovieMap().keySet()) {
                    genresListDialog.add(genre);
                }
            } else if (tvOrMovie.equals("tv")) {
                for (String genre : genres.getTvMap().keySet()) {
                    genresListDialog.add(genre);
                }
            } else {
                for (String genre : genres.getBothMap().keySet()) {
                    genresListDialog.add(genre);
                }
            }

            final CharSequence genresChar[] = new CharSequence[genresListDialog.size()];
            for (int i = 0; i < genresListDialog.size(); i++) {
                    if (genreIdList.contains(genres.getBothMap().get(genresListDialog.get(i)))) {
                        genresChar[i] = genresListDialog.get(i) + " [selected]";
                    } else {
                        genresChar[i] = genresListDialog.get(i);
                    }
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Genres:");
            builder.setPositiveButton("Got It!", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    updateGenreTextView();
                }
            });
            builder.setItems(genresChar, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    int id = genres.getBothMap().get(genresListDialog.get(which).toString().replace(" [selected]", ""));
                    if (genreIdList.contains(id)) {
                        genreIdList.remove(genreIdList.indexOf(id));
                    } else {
                        genreIdList.add(id);
                    }
                    genreDialog();
                }
            });
            builder.show();
    }

    private void checkConditions() {
        try {
            if (isNetworkAvailable()) {
                getInput();
                if (isCorrectInput()) {
                    noResultsTV.setVisibility(View.INVISIBLE);
                    mProgressBar.setVisibility(View.VISIBLE);
                    resultsRV.setVisibility(View.INVISIBLE);
                    makeCall(false);
                }
            } else {
                Toast.makeText(this, "Network is unavailable!", Toast.LENGTH_SHORT).show();
            }
        } catch (NumberFormatException nfe) {
            Toast.makeText(this, "Invalid input!", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isCorrectInput() {
        if (minReleaseYear <= maxReleaseYear) {
            if (minReleaseYear < 1) {
                minReleaseYear = 0;
                minYET.setText(0 + "");
            }
            if (minVoteAverage < 1) {
                minVoteAverage = 0;
                voteAverageET.setText(0 + "");
            }
            if (minVoteCount < 1) {
                minVoteCount = 0;
                voteCountET.setText(0 +"");
            }
            if (minVoteAverage > 10) {
                minVoteAverage = 10;
                voteAverageET.setText(10 + "");
            }
            return true;
        } else {
            Toast.makeText(this, "The minimum release year must be smaller or equal to the maximum release year!", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

}

