package com.kasthuri.word_guessing_game;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private String secretWord = "";
    private int score = 100;
    private int attemptsLeft = 10;
    private int level = 1;
    private int elapsedSeconds = 0;
    private boolean gameOver = false;
    private boolean tipUsed = false;
    private String playerName = "";

    private Handler timerHandler = new Handler();
    private Runnable timerRunnable;

    private TextView tvWelcome, tvLevel, tvScore, tvTimer, tvAttempts,
            tvWordDisplay, tvStatus, tvTip;
    private EditText etGuess, etLetter;
    private Button btnGuess, btnCheckLetter, btnWordLength, btnTip, btnLeaderboard;

    private static final String API_NINJA_KEY = "Ryljt9zxLdMgCxQqROIQbBoHg4PoOj4CrD5DUtyi";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Running
        SharedPreferences prefs = getSharedPreferences("WordGame", MODE_PRIVATE);
        playerName = prefs.getString("playerName", "Player");

        tvWelcome     = findViewById(R.id.tvWelcome);
        tvLevel       = findViewById(R.id.tvLevel);
        tvScore       = findViewById(R.id.tvScore);
        tvTimer       = findViewById(R.id.tvTimer);
        tvAttempts    = findViewById(R.id.tvAttempts);
        tvWordDisplay = findViewById(R.id.tvWordDisplay);
        tvStatus      = findViewById(R.id.tvStatus);
        tvTip         = findViewById(R.id.tvTip);
        etGuess       = findViewById(R.id.etGuess);
        etLetter      = findViewById(R.id.etLetter);
        btnGuess       = findViewById(R.id.btnGuess);
        btnCheckLetter = findViewById(R.id.btnCheckLetter);
        btnWordLength  = findViewById(R.id.btnWordLength);
        btnTip         = findViewById(R.id.btnTip);
        btnLeaderboard = findViewById(R.id.btnLeaderboard);

        tvWelcome.setText("Hello, " + playerName + " Welcome!");

        fetchNewWord();

        btnGuess.setOnClickListener(v -> handleGuess());
        btnCheckLetter.setOnClickListener(v -> handleLetterCheck());
        btnWordLength.setOnClickListener(v -> handleWordLength());
        btnTip.setOnClickListener(v -> handleTip());
        btnLeaderboard.setOnClickListener(v ->
                startActivity(new Intent(this, LeaderBoardActivity.class)));
    }

    private void fetchNewWord() {
        resetGameState();
        tvStatus.setText("Loading a new word...");

        String url = "https://random-word-api.herokuapp.com/word?length=" + getWordLength();

        StringRequest req = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONArray arr = new JSONArray(response);
                        secretWord = arr.getString(0).toLowerCase();
                        tvWordDisplay.setText(getWordMask());
                        tvStatus.setText("Guess the " + secretWord.length() + "-letter word!");
                        startTimer();
                    } catch (Exception e) {
                        loadFallbackWord();
                    }
                },
                error -> loadFallbackWord()
        );

        Volley.newRequestQueue(this).add(req);
    }

    private void loadFallbackWord() {
        String[] fallback = {"apple", "brave", "crane", "delta", "eagle",
                "flame", "globe", "honey", "ivory", "joker"};
        secretWord = fallback[(int) (Math.random() * fallback.length)];
        tvWordDisplay.setText(getWordMask());
        tvStatus.setText("Guess the word!");
        startTimer();
    }

    private int getWordLength() {
        if (level <= 2) return 4;
        if (level <= 4) return 5;
        if (level <= 6) return 6;
        return 7;
    }

    private String getWordMask() {
        StringBuilder sb = new StringBuilder();
        for (char c : secretWord.toCharArray()) sb.append("_ ");
        return sb.toString().trim();
    }

    private void resetGameState() {
        score = 100;
        attemptsLeft = 10;
        elapsedSeconds = 0;
        gameOver = false;
        tipUsed = false;
        stopTimer();
        updateUI();
        btnTip.setEnabled(false);
        tvTip.setText("");
        if (etGuess != null) etGuess.setText("");
        if (etLetter != null) etLetter.setText("");
    }

    private void updateUI() {
        tvScore.setText("Score: " + score);
        tvAttempts.setText("Attempts left: " + attemptsLeft);
        tvLevel.setText("Level " + level);
    }

    private void startTimer() {
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                elapsedSeconds++;
                tvTimer.setText("Time: " + elapsedSeconds + "s");
                timerHandler.postDelayed(this, 1000);
            }
        };
        timerHandler.postDelayed(timerRunnable, 1000);
    }

    private void stopTimer() {
        if (timerRunnable != null)
            timerHandler.removeCallbacks(timerRunnable);
    }

    private void handleGuess() {
        if (gameOver) return;
        String guess = etGuess.getText().toString().trim().toLowerCase();
        etGuess.setText("");

        if (guess.isEmpty()) {
            tvStatus.setText("Please type a word!");
            return;
        }

        if (guess.equals(secretWord)) {
            stopTimer();
            gameOver = true;
            tvWordDisplay.setText(secretWord.toUpperCase());
            tvStatus.setText("Correct! Well done, " + playerName + "!");
            submitScore();
            level++;
            new Handler().postDelayed(this::fetchNewWord, 2500);
        } else {
            attemptsLeft--;
            score = Math.max(0, score - 10);
            updateUI();

            if (attemptsLeft <= 7 && !tipUsed) {
                btnTip.setEnabled(true);
            }

            if (attemptsLeft == 0 || score == 0) {
                stopTimer();
                gameOver = true;
                tvStatus.setText("Out of attempts! The word was: " + secretWord.toUpperCase());
                new Handler().postDelayed(this::fetchNewWord, 3000);
            } else {
                tvStatus.setText("Wrong guess! Try again. (-10pts)");
            }
        }
    }

    private void handleLetterCheck() {
        if (gameOver) return;
        String letter = etLetter.getText().toString().trim().toLowerCase();
        etLetter.setText("");

        if (letter.isEmpty() || letter.length() != 1) {
            tvStatus.setText("Enter a single letter!");
            return;
        }

        score = Math.max(0, score - 5);
        updateUI();

        int count = 0;
        for (char c : secretWord.toCharArray())
            if (c == letter.charAt(0)) count++;

        if (count == 0)
            tvStatus.setText("Letter '" + letter.toUpperCase() + "' is NOT in the word. (-5pts)");
        else
            tvStatus.setText("Letter '" + letter.toUpperCase() + "' appears " + count + " time(s)! (-5pts)");
    }

    private void handleWordLength() {
        if (gameOver) return;
        score = Math.max(0, score - 5);
        updateUI();
        tvStatus.setText("The word has " + secretWord.length() + " letters. (-5pts)");
    }

    private void handleTip() {
        if (tipUsed || gameOver) return;
        tipUsed = true;
        btnTip.setEnabled(false);
        score = Math.max(0, score - 15);
        updateUI();

        String url = "https://api.api-ninjas.com/v1/rhyme?word=" + secretWord;

        StringRequest req = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONArray arr = new JSONArray(response);
                        if (arr.length() > 0)
                            tvTip.setText("Tip - Rhymes with: " + arr.getString(0));
                        else
                            tvTip.setText("Tip - The word has " + secretWord.length() + " letters");
                    } catch (Exception e) {
                        tvTip.setText("Tip - First letter is: " + secretWord.charAt(0));
                    }
                },
                error -> tvTip.setText("Tip - First letter is: " + secretWord.charAt(0))
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("X-Api-Key", API_NINJA_KEY);
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(req);
    }
    private void submitScore() {
        String privateKey = "Mjwkxl52Kk-HrYYf0gKJNA3Mr18niikkeLhCtVtaCkkg";
        String name = playerName.replace(" ", "%20");
        String url = "http://dreamlo.com/lb/" + privateKey + "/add/" +
                name + "/" + score + "/" + elapsedSeconds;

        StringRequest req = new StringRequest(Request.Method.GET, url,
                response -> Toast.makeText(this, "Score submitted!", Toast.LENGTH_SHORT).show(),
                error -> Toast.makeText(this, "Score saved locally.", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(req);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimer();
    }
}
