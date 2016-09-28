package example.helloword;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import mobilecloud.annotation.Remote;


public class MainActivity extends AppCompatActivity {

    @Remote
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        System.out.println("Hello World!");
    }

}
