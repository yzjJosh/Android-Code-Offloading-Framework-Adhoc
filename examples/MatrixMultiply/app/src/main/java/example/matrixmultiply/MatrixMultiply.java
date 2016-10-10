package example.matrixmultiply;

import android.util.Log;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import mobilecloud.engine.Engine;
import mobilecloud.lib.Remotable;
import mobilecloud.lib.Remote;

public class MatrixMultiply {

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
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Future<int[]>> futureList = new LinkedList<>();
        int[][] res = new int[m1][n2];
        for(int i=0; i<m1; i++) {
            futureList.add(executor.submit(new RowGetter(res[i], mat1[i], transMat2)));
        }

        Iterator<Future<int[]>> it = futureList.iterator();
        for(int i=0; i<m1; i++) {
            try {
                it.next().get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        executor.shutdown();
        return res;
    }

    private static class RowGetter implements Callable<int[]>, Remotable{

        private static final String TAG = RowGetter.class.getSimpleName();

        private boolean isNew = true;
        private boolean isOnServer = Engine.isOnCloud();
        private int id = System.identityHashCode(this);

        private int[] res;
        private int[] row;
        private int[][] cols;

        public RowGetter(int[] res, int[] row, int[][] cols) {
            this.res = res;
            this.row = row;
            this.cols = cols;
        }

        @Remote
        @Override
        public int[] call() throws Exception {
            try {
                Method method = RowGetter.class.getMethod("call");
                if(Engine.getInstance().shouldMigrate(method, this)) {
                    Log.d(TAG, "Calculating row remotely ...");
                    return (int[]) Engine.getInstance().invokeRemotely(method, this);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.d(TAG, "Calculating row locally ...");
            for(int i=0; i<cols.length; i++) {
                res[i] = multiplyVector(row, cols[i]);
            }
            return res;
        }

        private int multiplyVector(int[] row, int[] col) {
            int res = 0;
            for(int i=0; i<row.length; i++) {
                res += row[i] * col[i];
            }
            return res;
        }

        @Override
        public void setIsOnServer(boolean b) {
            this.isOnServer = b;
        }

        @Override
        public boolean isOnServer() {
            return this.isOnServer;
        }

        @Override
        public int getId() {
            return this.id;
        }

        @Override
        public void setId(int i) {
            this.id = i;
        }

        @Override
        public boolean isNew() {
            return this.isNew;
        }

        @Override
        public void setIsNew(boolean b) {
            this.isNew = b;
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
