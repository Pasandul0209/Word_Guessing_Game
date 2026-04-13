package com.kasthuri.word_guessing_game;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;

public class LeaderBoardActivity extends AppCompatActivity {

    private static final String PUBLIC_KEY = "69d87f1d8f40bc2f6032d011";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leader_board);

        ListView lvScores = findViewById(R.id.lvScores);
        Button btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        ArrayList<String> entries = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, entries);
        lvScores.setAdapter(adapter);

        entries.add("Loading scores...");
        adapter.notifyDataSetChanged();

        String url = "http://dreamlo.com/lb/" + PUBLIC_KEY + "/json";

        StringRequest req = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        entries.clear();
                        JSONObject root = new JSONObject(response);
                        JSONObject lb = root.getJSONObject("dreamlo")
                                .getJSONObject("leaderboard");
                        Object entryObj = lb.get("entry");
                        JSONArray arr;
                        if (entryObj instanceof JSONArray) {
                            arr = (JSONArray) entryObj;
                        } else {
                            arr = new JSONArray();
                            arr.put(entryObj);
                        }
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject e = arr.getJSONObject(i);
                            String name  = e.getString("name");
                            int    sc    = e.getInt("score");
                            int    secs  = e.getInt("seconds");
                            entries.add((i + 1) + ".  " + name +
                                    "   Score: " + sc +
                                    "   Time: " + secs + "s");
                        }
                        adapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        entries.clear();
                        entries.add("No scores yet!");
                        adapter.notifyDataSetChanged();
                    }
                },
                error -> {
                    entries.clear();
                    entries.add("Could not load leaderboard.");
                    adapter.notifyDataSetChanged();
                }
        );

        Volley.newRequestQueue(this).add(req);
    }
}