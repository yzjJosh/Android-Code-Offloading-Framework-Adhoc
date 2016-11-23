package example.nqueensolver;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private EditText dimension_input;
    private Button solve_button;
    private TextView status_text;
    private NQueenSolver solver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.dimension_input = (EditText) findViewById(R.id.dimension_input);
        this.solve_button = (Button) findViewById(R.id.solve_button);
        this.status_text = (TextView) findViewById(R.id.status_text);
        this.solver = new NQueenSolver();
    }

    public void onSolveNQueenButtonClicked(View view) {
        String dimension = dimension_input.getText().toString();
        if(dimension.isEmpty()) {
            Toast.makeText(this, "Please input dimension!", Toast.LENGTH_LONG).show();
            return;
        }
        int dim = Integer.parseInt(dimension);
        if(dim <= 0) {
            Toast.makeText(this, "Dimension must be positive integer!", Toast.LENGTH_LONG).show();
            return;
        }
        Toast.makeText(this, "Start solving " + dimension + " queen problem, please wait ...", Toast.LENGTH_LONG).show();
        solve_button.setEnabled(false);
        dimension_input.setEnabled(false);
        status_text.setText("N Queen Solver is running ...");
        status_text.setVisibility(View.VISIBLE);
        new SolveTask().execute(dim);
    }

    private class SolveTask extends AsyncTask<Integer, Void, List<List<Integer>>> {

        private int dim;
        private long time;

        @Override
        protected List<List<Integer>> doInBackground(Integer... args) {
            this.dim = args[0];
            List<List<Integer>> res = null;
            long start = System.currentTimeMillis();
            try {
                res = solver.totalNQueens(dim);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
            this.time = System.currentTimeMillis() - start;
            return res;
        }

        @Override
        protected void onPostExecute(List<List<Integer>> res) {
            String message = "Found " + res.size() + " solutions for " + dim + " queen problem, spend " + time + "ms.";
            Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
            status_text.setText(message);
            solve_button.setEnabled(true);
            dimension_input.setEnabled(true);
        }

    }

}
