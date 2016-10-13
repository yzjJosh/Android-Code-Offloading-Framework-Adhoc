package example.matrixmultiply;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import mobilecloud.engine.Engine;
import mobilecloud.engine.host.Host;
import mobilecloud.engine.host.provider.StaticHostProvider;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize engine
        Engine.localInit(this);

        // Set server ip and port
        StaticHostProvider.addHost(new Host("127.0.0.1", 50382));
   //     StaticHostProvider.addHost(new Host("192.168.0.23", 50382));

        new MatMultiplyThread().start();
    }

    private static class MatMultiplyThread extends Thread {
        @Override
        public void run() {
            while(true) {
                int m = 500 + (int) (Math.random() * 250 - 150);
                int n = 500 + (int) (Math.random() * 250 - 150);
                int k = 500 + (int) (Math.random() * 250 - 150);
                int[][] mat1 = MatrixMultiply.randMat(m, k);
                int[][] mat2 = MatrixMultiply.randMat(k, n);
                Log.e(TAG, "Start multiplying matrix " + m + "x" + k + " and matrix " + k + "x" + n);
                long start = System.currentTimeMillis();
                int[][] res = MatrixMultiply.parallelMatrixMultiply(mat1, mat2);
                long end = System.currentTimeMillis();
                long time = end - start;
                boolean right = MatrixMultiply.equals(res, MatrixMultiply.sequentiallyMatrixMultiply(mat1, mat2));
                Log.e(TAG, "Calculating complete, spent time " + time + ", result is " + (right ? "right" : "wrong") + "!");
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
