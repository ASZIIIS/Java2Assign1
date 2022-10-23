import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MovieAnalyzer {

    public class Movie {

        private String[] attributes = new String[16]; //to save all details of the movie

        public Movie(String line) {
            int id = 0;
            boolean flag = true;
            char[] ss = line.concat(",").toCharArray();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < ss.length; ++i) {
                if (ss[i] == '\"') {
                    if (ss[i + 1] == '\"') {
                        sb.append('\"');
                        ++i;
                    } else {
                        flag = !flag;
                    }
                } else if (ss[i] == ',' && flag) {
                    attributes[id] = sb.toString();
                    sb = new StringBuilder();
                    ++id;
                } else {
                    sb.append(ss[i]);
                }
            }
        }

        public String getSeriesTitle() {
            return attributes[1];
        }

        public int getReleasedYear() {
            return Integer.parseInt(attributes[2]);
        }

        public int getRuntime() {
            return Integer.parseInt(attributes[4].split(" ")[0]);
        }

        public String[] getGenre() {
            return attributes[5].split(", ");
        }

        public int getOverview() {
            return attributes[7].length();
        }

        public float getRate() {
            return Float.parseFloat(attributes[6]);
        }

        public ArrayList<String> getStars() {
            ArrayList<String> list = new ArrayList<String>();
            for (int i = 10; i < 14; ++i) {
                if (attributes[i].length() > 0) {
                    list.add(attributes[i]);
                }
            }
            list.sort(new Comparator<String>() {
                @Override
                public int compare(String x, String y) {
                    return x.compareTo(y);
                }
            });
            return list;
        }

    }

    private ArrayList<Movie> movies = new ArrayList<Movie>(); //to save the movies in the database

    public MovieAnalyzer(String dataset_path) {
        String line;
        try (BufferedReader infile = new BufferedReader(new FileReader(dataset_path))) {
            line = infile.readLine();
            Movie m;
            while ((line = infile.readLine()) != null) {
                m = new Movie(line);
                movies.add(m);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<Integer, Integer> getMovieCountByYear() {
        LinkedHashMap<Integer, Integer> movieCountByYear = new LinkedHashMap<Integer, Integer>();
        movies.stream().sorted(Comparator.comparing(Movie::getReleasedYear).reversed()).forEach(m -> {
            if (movieCountByYear.containsKey(m.getReleasedYear())) {
                movieCountByYear.put(m.getReleasedYear(), movieCountByYear.get(m.getReleasedYear()) + 1);
            } else {
                movieCountByYear.put(m.getReleasedYear(), 1);
            }
        });
        return movieCountByYear;
    }

    public Map<String, Integer> getMovieCountByGenre() {
        HashMap<String, Integer> movieCountByGenre_ = new HashMap<String, Integer>();
        LinkedHashMap<String, Integer> movieCountByGenre = new LinkedHashMap<String, Integer>();
        movies.stream().forEach(m -> {
            for (String s : m.getGenre()) {
                if (movieCountByGenre_.containsKey(s)) {
                    movieCountByGenre_.put(s, movieCountByGenre_.get(s) + 1);
                } else {
                    movieCountByGenre_.put(s, 1);
                }
            }
        });
        List<Map.Entry<String, Integer>> list = new ArrayList<>(movieCountByGenre_.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> x, Map.Entry<String, Integer> y) {
                int compare = (x.getValue()).compareTo(y.getValue());
                if (compare == 0) {
                    compare = y.getKey().compareTo(x.getKey());
                }
                return -compare;
            }
        });
        for (Map.Entry<String, Integer> e : list) {
            movieCountByGenre.put(e.getKey(), e.getValue());
        }
        return movieCountByGenre;
    }

    public Map<List<String>, Integer> getCoStarCount() {
        HashMap<List<String>, Integer> coStarCount = new HashMap<List<String>, Integer>();
        movies.stream().forEach(m -> {
            ArrayList<String> co_stars = m.getStars();
            for (int i = 0; i < co_stars.size(); ++i){
                for (int j = i + 1; j < co_stars.size(); ++j){
                    ArrayList<String> co_star = new ArrayList<String>();
                    co_star.add(co_stars.get(i));
                    co_star.add(co_stars.get(j));
                    if (coStarCount.containsKey(co_star)) {
                        coStarCount.put(co_star, coStarCount.get(co_star) + 1);
                    } else {
                        coStarCount.put(co_star, 1);
                    }
                }
            }
        });
        return coStarCount;
    }

    public List<String> getTopMovies(int top_k, String by) {
       ArrayList<String> topMovies = new ArrayList<String>();
        if (by.equals("runtime")) {
            movies.stream().sorted(Comparator.comparing(Movie::getRuntime).reversed().thenComparing(Movie::getSeriesTitle)).forEach(m -> {
                if (topMovies.size() < top_k) {
                    topMovies.add(m.getSeriesTitle());
                }
            });
            return topMovies;
        }
        if (by.equals("overview")) {
            movies.stream().sorted(Comparator.comparing(Movie::getOverview).reversed().thenComparing(Movie::getSeriesTitle)).forEach(m -> {
                if (topMovies.size() < top_k) {
                    topMovies.add(m.getSeriesTitle());
                }
            });
            return topMovies;
        }
        return null;
    }

    public List<String> getTopStars(int top_k, String by) {
        return null;
    }

    public List<String> searchMovies(String genre, float min_rating, int max_runtime) {
        ArrayList<String> searchMovies = new ArrayList<String>();
        movies.stream().filter(m -> {
            boolean flag = false;
            for (String s : m.getGenre()) {
                if (s.equals(genre)) {
                    flag = true;
                }
            }
            if (m.getRuntime() > max_runtime) {
                flag = false;
            }
            if (m.getRate() < min_rating) {
                flag = false;
            }
            return flag;
        }).sorted(Comparator.comparing(Movie::getSeriesTitle)).forEach(m -> {
            searchMovies.add(m.getSeriesTitle());
        });
        return searchMovies;
    }

}