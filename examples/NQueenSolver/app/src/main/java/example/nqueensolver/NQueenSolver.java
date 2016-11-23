package example.nqueensolver;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import mobilecloud.lib.Remote;

public class NQueenSolver {

    int totalNQueens(int n) throws ExecutionException, InterruptedException {
        int result = 0;
        if(n<=0) return result;

        ExecutorService threadPool = Executors.newCachedThreadPool();
        List<Future<Integer>> items = new ArrayList<>();

        for(int i = 0; i < n; i++) {
            Future<Integer> item = threadPool.submit(new worker(i, n));
            items.add(item);
        }

        for(int i = 0; i < items.size(); i++) {
            result += items.get(i).get();
        }
        return result;
    }

    private class worker implements Callable<Integer> {

        private boolean[][] board;
        private boolean[] occupied;
        private int i;
        private int n;
        private int nums = 0;

        worker(int i, int n) {
            this.board = new boolean[n][n];
            this.occupied = new boolean[n];
            this.i = i;
            this.n = n;
        }

        @Remote
        public Integer call() {
            occupied[i] = true;
            board[0][i] = true;
            helper(board, occupied, 1, n);
            return nums;
        }

        private void helper(boolean[][] board, boolean[] occupied, int row, int n){
            if(row==n){
                nums++;
                return;
            }

            for(int i=0; i<n; i++){
                if(!isValid(board, occupied, row, i)) continue;
                board[row][i] = true;
                occupied[i] = true;
                helper(board, occupied, row+1, n);
                board[row][i] = false;
                occupied[i] = false;
            }
        }

        private boolean isValid(boolean[][] board, boolean[] occupied, int row, int col){

            if(occupied[col]) return false;

            for(int i=1; row-i>=0 && col-i>=0; i++){
                if(board[row-i][col-i]) return false;
            }
            for(int i=1; row-i>=0 && col+i<board.length; i++){
                if(board[row-i][col+i]) return false;
            }

            return true;
        }
    }
}
