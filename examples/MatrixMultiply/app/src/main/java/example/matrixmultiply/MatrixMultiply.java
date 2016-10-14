package example.matrixmultiply;

import android.util.Log;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import mobilecloud.engine.RemoteExecutionFailedException;
import mobilecloud.lib.Remote;
import mobilecloud.lib.RemoteExecutionListener;

public class MatrixMultiply {

    private static final int NUM_OF_THREADS = 4;

    /**
     * Multiply two matrixes parallelly
     * @param mat1 the first matrix
     * @param mat2 the second matrix
     * @return the result
     */
    public static int[][] parallelMatrixMultiply(int[][] mat1, int[][] mat2) {
        int m1 = mat1.length, n1 = mat1[0].length;
        int m2 = mat2.length, n2 = mat2[0].length;
        int[][] transMat2 = new int[n2][m2];
        for(int i=0; i<m2; i++) {
            for(int j=0; j<n2; j++) {
                transMat2[j][i] = mat2[i][j];
            }
        }
        ExecutorService executor = Executors.newCachedThreadPool();
        List<Future<int[][]>> futureList = new LinkedList<>();
        int[][] res = new int[m1][n2];

        int rowsPerThread = (int) Math.ceil((double)m1/NUM_OF_THREADS);

        for(int i=0; i<NUM_OF_THREADS; i++) {
            int lo = i*rowsPerThread;
            int hi = Math.min(lo + rowsPerThread - 1, m1-1);
            futureList.add(executor.submit(new Worker(rows(res, lo, hi), rows(mat1, lo, hi), transMat2)));
        }

        for(Future<int[][]> f: futureList) {
            try {
                f.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        executor.shutdown();
        return res;
    }

    private static int[][] rows(int[][] mat, int lo, int hi) {
        int[][] res = new int[hi-lo+1][];
        for(int i=lo; i<=hi; i++) {
            res[i-lo] = mat[i];
        }
        return res;
    }

    private static class WorkerListener implements RemoteExecutionListener {

        private static final String TAG = WorkerListener.class.getSimpleName();

        @Override
        public boolean onRemoteExecutionStart(Method method, Object o, Object[] objects) {
            Log.d(TAG, "Method " + method.getName() + " is running remotely ...");
            return true;
        }

        @Override
        public void onRemoteExecutionComplete(Method method, Object o, Object[] objects, Object o1, boolean b, RemoteExecutionFailedException e) {
            Log.d(TAG, "Remote invocation completes. Status is " + (b? "success": "failed"));
        }
    }

    private static class Worker implements Callable<int[][]>, Serializable{

        private static final String TAG = Worker.class.getSimpleName();

        private int[][] res;
        private int[][] rows;
        private int[][] cols;

        public Worker(int[][] res, int[][] row, int[][] cols) {
            this.res = res;
            this.rows = row;
            this.cols = cols;
        }

        @Remote(listener = WorkerListener.class)
        @Override
        public int[][] call() throws Exception {
            for(int i=0; i<rows.length; i++) {
                for(int j=0; j<cols.length; j++) {
                    res[i][j] = multiplyVector(rows[i], cols[j]);
                }
            }
            return res;
        }

        private int multiplyVector(int[] row, int[] col) {
            int res = 0;
            for (int i = 0; i < row.length; i++) {
                res += row[i] * col[i];
            }
            return res;
        }
    }

    public static int[][] randMat(int m, int n) {
        int[][] res = new int[m][n];
        for(int i=0; i<m; i++) {
            for(int j=0; j<n; j++) {
                res[i][j] = (int) (Math.random()*2000 - 1000);
            }
        }
        return res;
    }

    /**
     * Multiply 2 matrixes sequentially
     * @param mat1 mat1
     * @param mat2 mat2
     * @return result
     */
    public static int[][] sequentiallyMatrixMultiply(int[][] mat1, int[][] mat2) {
        int m1 = mat1.length, n1 = mat1[0].length;
        int m2 = mat2.length, n2 = mat2[0].length;
        int[][] res = new int[m1][n2];
        for(int i=0; i<m1; i++) {
            for(int j=0; j<n2; j++) {
                for(int k=0; k<n1; k++) {
                    res[i][j] += mat1[i][k] * mat2[k][j];
                }
            }
        }
        return res;
    }

    public static boolean equals(int[][] mat1, int[][] mat2) {
        int m1 = mat1.length, n1 = mat1[0].length;
        int m2 = mat2.length, n2 = mat2[0].length;
        if(m1 != m2 || n1 != n2) {
            return false;
        }
        for(int i=0; i<m1; i++) {
            for(int j=0; j<n1; j++) {
                if(mat1[i][j] != mat2[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }

}
