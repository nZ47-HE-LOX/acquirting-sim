package com.example.acquiringsim;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    static class Card {
        double balance;
        String cvv;
        String exp;
        boolean blocked;
        Card(double b, String c, String e, boolean bl) {
            balance = b; cvv = c; exp = e; blocked = bl;
        }
    }

    Map<String, Card> CARDS = new HashMap<>();
    List<String> TRANSACTIONS = new ArrayList<>();

    EditText panInput, expInput, cvvInput, amountInput;
    TextView resultView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CARDS.put("4276000000000001", new Card(50000.00, "123", "12/27", false));
        CARDS.put("4276000000000002", new Card(100.00,   "456", "06/26", false));
        CARDS.put("4276000000000003", new Card(999999.00,"789", "01/28", true));

        panInput    = findViewById(R.id.panInput);
        expInput    = findViewById(R.id.expInput);
        cvvInput    = findViewById(R.id.cvvInput);
        amountInput = findViewById(R.id.amountInput);
        resultView  = findViewById(R.id.resultView);

        Button payBtn   = findViewById(R.id.payButton);
        Button cardsBtn = findViewById(R.id.cardsButton);

        payBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processPayment();
            }
        });

        cardsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCards();
            }
        });
    }

    boolean luhnValid(String pan) {
        String digits = pan.replaceAll("\\D", "");
        if (digits.length() < 12) return false;
        int sum = 0;
        int parity = digits.length() % 2;
        for (int i = 0; i < digits.length(); i++) {
            int d = digits.charAt(i) - '0';
            if (i % 2 == parity) {
                d *= 2;
                if (d > 9) d -= 9;
            }
            sum += d;
        }
        return sum % 10 == 0;
    }

    void processPayment() {
        String pan = panInput.getText().toString().replaceAll("\\s", "");
        String exp = expInput.getText().toString().trim();
        String cvv = cvvInput.getText().toString().trim();

        double amount;
        try {
            amount = Double.parseDouble(amountInput.getText().toString());
        } catch (Exception e) {
            showResult("ОТКАЗ\nНеверная сумма", false);
            return;
        }

        if (!luhnValid(pan)) {
            showResult("ОТКАЗ\nНеверный номер карты (Luhn)", false);
            return;
        }

        Card c = CARDS.get(pan);
        if (c == null) {
            showResult("ОТКАЗ\nКарта не найдена", false);
            return;
        }
        if (c.blocked) {
            showResult("ОТКАЗ\nКарта заблокирована", false);
            return;
        }
        if (!c.cvv.equals(cvv) || !c.exp.equals(exp)) {
            showResult("ОТКАЗ\nНеверный CVV или срок", false);
            return;
        }
        if (c.balance < amount) {
            showResult("ОТКАЗ\nНедостаточно средств\nБаланс: "
                    + String.format(Locale.US, "%.2f", c.balance) + " руб", false);
            return;
        }

        c.balance -= amount;

        String rrn = String.format(Locale.US, "%012d", (long)(Math.random() * 1e12));
        String authCode = String.format(Locale.US, "%06d", (int)(Math.random() * 1000000));
        String time = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
                .format(new Date());

        String masked = pan.substring(0, 6) + "******" + pan.substring(pan.length() - 4);

        String receipt = "ОПЛАЧЕНО\n"
                + "────────────────\n"
                + "Карта:  " + masked + "\n"
                + "Сумма:  " + String.format(Locale.US, "%.2f", amount) + " руб\n"
                + "RRN:    " + rrn + "\n"
                + "Код:    " + authCode + "\n"
                + "Время:  " + time;

        TRANSACTIONS.add(receipt);
        showResult(receipt, true);
    }

    void showCards() {
        StringBuilder sb = new StringBuilder("ТЕСТОВЫЕ КАРТЫ\n\n");
        for (Map.Entry<String, Card> e : CARDS.entrySet()) {
            Card c = e.getValue();
            String pan = e.getKey();
            sb.append(pan.substring(0, 6)).append("******")
              .append(pan.substring(pan.length() - 4)).append("\n")
              .append("  CVV:    ").append(c.cvv).append("\n")
              .append("  Срок:   ").append(c.exp).append("\n")
              .append("  Баланс: ").append(String.format(Locale.US, "%.2f", c.balance)).append(" руб\n")
              .append("  Статус: ").append(c.blocked ? "заблокирована" : "активна")
              .append("\n\n");
        }
        resultView.setTextColor(0xFFEEEEEE);
        resultView.setText(sb.toString());
    }

    void showResult(String text, boolean ok) {
        resultView.setTextColor(ok ? 0xFF4ECCA3 : 0xFFE94560);
        resultView.setText(text);
    }
}
