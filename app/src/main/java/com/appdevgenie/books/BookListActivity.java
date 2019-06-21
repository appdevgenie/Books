package com.appdevgenie.books;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

public class BookListActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private ProgressBar pbLoading;
    private TextView tvError;
    private RecyclerView rvBooks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_list);

        pbLoading = findViewById(R.id.pbLoading);
        tvError = findViewById(R.id.tvError);
        rvBooks = findViewById(R.id.rv_books);

        Intent intent = getIntent();
        String query  = intent.getStringExtra("Query");
        //Log.d("URL String", query);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rvBooks.setLayoutManager(linearLayoutManager);

        URL bookUrl;

        try {
            if(query == null || query.isEmpty()) {
                bookUrl = ApiUtil.buildUrl("cooking");
            }else{
                bookUrl = new URL(query);
            }
            new BooksQueryTask().execute(bookUrl);

        } catch (Exception e) {
            Log.d("Error", "onCreate: " + e.getMessage());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.book_list_menu, menu);
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(this);

        ArrayList<String> recentList = SpUtil.getQueryList(getApplicationContext());
        int itemNum = recentList.size();

        MenuItem recentMenu;
        for (int i = 0; i < itemNum; i++) {
            recentMenu = menu.add(Menu.NONE, i, Menu.NONE, recentList.get(i));

        }

        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_advanced_search:

                Intent intent = new Intent(BookListActivity.this, SearchActivity.class);
                startActivity(intent);

                return true;

            default:

                int position = item.getItemId() + 1;
                String preferenceName = SpUtil.QUERY + String.valueOf(position);
                String query = SpUtil.getPreferenceString(getApplicationContext(), preferenceName);
                String[] prefParams = query.split("\\,");
                String[] queryParams = new String[4];
                for (int i = 0; i < prefParams.length; i++) {
                    queryParams[i] = prefParams[i];
                }
                URL boolUrl = ApiUtil.buildUrl(
                        (queryParams[0] == null)?"":queryParams[0],
                        (queryParams[1] == null)?"":queryParams[1],
                        (queryParams[2] == null)?"":queryParams[2],
                        (queryParams[3] == null)?"":queryParams[3]
                );
                new BooksQueryTask().execute(boolUrl);

                return true;
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {

        try {
            URL bookUrl = ApiUtil.buildUrl(query);
            new BooksQueryTask().execute(bookUrl);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    public class BooksQueryTask extends AsyncTask<URL, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pbLoading.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(URL... urls) {
            URL searchUrl = urls[0];
            String result = null;

            try {
                result = ApiUtil.getJson(searchUrl);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            pbLoading.setVisibility(View.INVISIBLE);

            if (s == null) {
                tvError.setVisibility(View.VISIBLE);
                rvBooks.setVisibility(View.INVISIBLE);
            } else {
                tvError.setVisibility(View.INVISIBLE);
                rvBooks.setVisibility(View.VISIBLE);

                ArrayList<Book> books = ApiUtil.getBooksFromJson(s);
                String resultString = "";

                BooksAdapter booksAdapter = new BooksAdapter(books);
                rvBooks.setAdapter(booksAdapter);
            }


        }
    }
}
