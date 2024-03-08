package com.pro4j.marbles;

import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.pro4j.promise.Promise;
import com.pro4j.promise.Promise.RejectCallback;
import com.pro4j.promise.Timeout;
import com.tencent.wxpfc.app.R;

public class MainActivity extends AppCompatActivity {
    TextView label;
    Integer balance = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        label = (TextView) findViewById(R.id.label);

        (findViewById(R.id.depositButton)).setOnClickListener(view -> deposit());
        (findViewById(R.id.withdrawButton)).setOnClickListener(view -> withdraw());
        (findViewById(R.id.resetButton)).setOnClickListener(view -> resetBalance());
    }

    private void withdraw() {
        new Promise<>((resolve, reject) -> {
            if (balance - 10 < 0) {
                reject.apply("insufficient balance");
                return;
            }
            resolve.apply(balance);
        }).delay(Timeout.ofMillis(100)).except((throwable, o) -> {
            if (o instanceof String) {
                showMessage(MainActivity.this, (String) o);
            }
        }).then(o -> balance -= 10).done(o -> updateBalance());
    }

    private void deposit() {
        Promise.delay(Timeout.ofMillis(100), null).then(o -> balance += 10).done(o -> updateBalance());
    }

    private void resetBalance() {
        balance = 0;
        updateBalance();
    }

    private void updateBalance() {
        runOnUiThread(() -> label.setText("Your balance: "+ balance));
    }

    private void showMessage(Context context,String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }
}