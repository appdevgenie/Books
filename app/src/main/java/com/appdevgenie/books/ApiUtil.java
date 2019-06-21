package com.appdevgenie.books;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

public class ApiUtil {

    private ApiUtil() {
    }

    public static final String BASE_API_URL = "https://www.googleapis.com/books/v1/volumes";
    public static final String QUERY_PARAMETER_KEY = "q";
    public static final String KEY = "key";
    public static final String API_KEY = "AIzaSyAz3DAiKkgxjiDLqmcaTWnAuZ_P2BT6TYQ";
    public static final String TITLE = "intitle:";
    public static final String AUTHOR = "inauthor:";
    public static final String PUBLISHER = "inpublisher:";
    public static final String ISBN = "isbn:";


    public static URL buildUrl(String title) {

        URL url = null;
        Uri uri = Uri.parse(BASE_API_URL).buildUpon()
                .appendQueryParameter(QUERY_PARAMETER_KEY, title)
                .appendQueryParameter(KEY, API_KEY)
                .build();

        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    public static URL buildUrl(String title, String author, String publisher, String isbn){

        URL url = null;
        Log.d("URL String", "start");

        StringBuilder stringBuilder = new StringBuilder();
        if(!title.isEmpty())
            stringBuilder.append(TITLE + title + "+");
        if(!author.isEmpty())
            stringBuilder.append(AUTHOR + author + "+");
        if(!publisher.isEmpty())
            stringBuilder.append(PUBLISHER + publisher + "+");
        if(!isbn.isEmpty())
            stringBuilder.append(ISBN + isbn + "+");

        stringBuilder.setLength(stringBuilder.length() - 1);

        String query = stringBuilder.toString();
        Uri uri = Uri.parse(BASE_API_URL).buildUpon()
                .appendQueryParameter(QUERY_PARAMETER_KEY, query)
                .appendQueryParameter(KEY, API_KEY)
                .build();

        try {
            url = new URL(uri.toString());
            Log.d("URL String", url.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    public static String getJson(URL url) throws IOException {

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {

            InputStream inputStream = connection.getInputStream();
            Scanner scanner = new Scanner(inputStream);
            scanner.useDelimiter("\\A");

            boolean hasData = scanner.hasNext();
            if (hasData) {
                return scanner.next();
            } else {
                return null;
            }
        } catch (Exception e) {
            Log.d("Error", "getJson: " + e.toString());
            return null;
        } finally {
            connection.disconnect();
        }
    }

    public static ArrayList<Book> getBooksFromJson(String json){

        final String ID = "id";
        final String TITLE = "title";
        final String SUBTITLE = "subtitle";
        final String AUTHORS = "authors";
        final String PUBLISHER = "publisher";
        final String PUBLISHED_DATE = "publishedDate";
        final String ITEMS = "items";
        final String VOLUMEINFO = "volumeInfo";
        final String DESCRIPTION = "description";
        final String IMAGELINKS = "imageLinks";
        final String THUMBNAIL = "thumbnail";

        ArrayList<Book> books = new ArrayList<>();

        try {
            JSONObject jsonBooks = new JSONObject(json);
            JSONArray arrayBooks = jsonBooks.getJSONArray(ITEMS);
            int numberOfBooks = arrayBooks.length();

            for (int i = 0; i < numberOfBooks; i++) {

                JSONObject bookJson = arrayBooks.getJSONObject(i);
                JSONObject volumeInfoJson = bookJson.getJSONObject(VOLUMEINFO);
                JSONObject imageLinksJson = null;

                if(volumeInfoJson.has(IMAGELINKS)) {
                    imageLinksJson = volumeInfoJson.getJSONObject(IMAGELINKS);
                }

                int authorNum = 0;
                try {
                    authorNum = volumeInfoJson.getJSONArray(AUTHORS).length();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String[] authors = new String[authorNum];
                for (int j = 0; j < authorNum; j++) {
                    authors[j] = volumeInfoJson.getJSONArray(AUTHORS).get(j).toString();
                }
                Book book = new Book(
                        bookJson.getString(ID),
                        volumeInfoJson.getString(TITLE),
                        (volumeInfoJson.isNull(SUBTITLE)?"":volumeInfoJson.getString(SUBTITLE)),
                        authors,
                        (volumeInfoJson.isNull(PUBLISHER)?"":volumeInfoJson.getString(PUBLISHER)),
                        (volumeInfoJson.isNull(PUBLISHED_DATE)?"":volumeInfoJson.getString(PUBLISHED_DATE)),
                        (volumeInfoJson.isNull(DESCRIPTION)?"":volumeInfoJson.getString(DESCRIPTION)),
                        (imageLinksJson == null)?"":imageLinksJson.getString(THUMBNAIL)
                );
                books.add(book);

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return books;

    }
}
