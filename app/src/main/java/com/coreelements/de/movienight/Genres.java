package com.coreelements.de.movienight;

import java.util.Map;
import java.util.TreeMap;

public class Genres {
    private TreeMap<String, Integer> movieMap = new TreeMap();
    private TreeMap<String, Integer> tvMap = new TreeMap<>();
    private TreeMap<String, Integer> bothMap = new TreeMap<>();

    public Genres(TreeMap<String, Integer> movieMap, TreeMap<String, Integer> tvMap) {
        this.movieMap = movieMap;
        this.tvMap = tvMap;
        fillBothMap();
    }

    public TreeMap<String, Integer> getBothMap() {
        return bothMap;
    }

    public TreeMap<String, Integer> getMovieMap() {
        return movieMap;
    }

    public TreeMap<String, Integer> getTvMap() {
        return tvMap;
    }

    private void fillBothMap() {
        for (Map.Entry<String, Integer> entry : movieMap.entrySet()) {
            bothMap.put(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<String, Integer> entry : tvMap.entrySet()) {
            if (movieMap.get(entry.getKey()) == null) {
                bothMap.put(entry.getKey(), entry.getValue());
            }
        }
    }
}
