package com.appdevgenie.books;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.net.URL;

public class SearchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        final EditText etTitle = findViewById(R.id.etTitle);
        final EditText etAuthor = findViewById(R.id.etAuthor);
        final EditText etPublisher = findViewById(R.id.etPublisher);
        final EditText etIsbn = findViewById(R.id.etIsbn);

        Button bSearch = findViewById(R.id.bSearch);
        bSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String title = etTitle.getText().toString().trim();
                String authors = etAuthor.getText().toString().trim();
                String publisher = etPublisher.getText().toString().trim();
                String isbn = etIsbn.getText().toString().trim();

                if(title.isEmpty() && authors.isEmpty() && publisher.isEmpty() && isbn.isEmpty()){
                    Toast.makeText(SearchActivity.this, "Enter valid info!", Toast.LENGTH_SHORT).show();
                }else{
                    URL queryUrl = ApiUtil.buildUrl(title, authors, publisher, isbn);

                    Context context = getApplicationContext();
                    int position = SpUtil.getPreferenceInt(context, SpUtil.POSITION);
                    if(position == 0 || position == 5){
                        position = 1;
                    }else{
                        position++;
                    }

                    String key = SpUtil.QUERY + String.valueOf(position);
                    String value = title + "," + authors + "," + publisher + "," + isbn;
                    SpUtil.setPreferenceString(context, key, value);
                    SpUtil.setPreferenceInt(context, SpUtil.POSITION, position);

                    Intent intent = new Intent(getApplicationContext(), BookListActivity.class);
                    intent.putExtra("Query", queryUrl);
                    startActivity(intent);

                }
            }
        });
    }
}
