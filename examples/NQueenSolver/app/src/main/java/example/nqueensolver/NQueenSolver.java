package example.nqueensolver;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class NQueenSolver {

    public List<List<Integer>> totalNQueens(int n) throws ExecutionException, InterruptedException {
        List<List<Integer>> result = new ArrayList<List<Integer>>();
        if(n<=0) return result;

        ExecutorService threadPool = Executors.newCachedThreadPool();
        List<Future<List<List<Integer>>>> items = new ArrayList<>();

        for(int i = 0; i < n; i++) {
            Future<List<List<Integer>>> item = threadPool.submit(new worker(i, n));
            items.add(item);
        }

        for(int i = 0; i < items.size(); i++) {
            result.addAll(items.get(i).get());
        }
        return result;
    }

    private class worker implements Callable<List<List<Integer>>> {

        public List<List<Integer>> res = new ArrayList<>();
        public boolean[][] board;
        public boolean[] occupied;
        public int i;
        public int n;

        public worker(int i, int n) {
            this.board = new boolean[n][n];
            this.occupied = new boolean[n];
            this.i = i;
            this.n = n;
        }

        public List<List<Integer>> call() {
            occupied[i] = true;
            board[0][i] = true;
            helper(res, board, occupied, 1, n);
            return res;
        }
    }

    public void helper(List<List<Integer>> res, boolean[][] board, boolean[] occupied, int row, int n){
        if(row==n){
            List<Integer> temp = new ArrayList<Integer>();
            for(boolean[] item : board){
              for(int i=0; i<item.length; i++) {
                  if (item[i]) {
                      temp.add(i);
                      break;
                  }
              }
            }
            res.add(temp);
            return ;
        }

        for(int i=0; i<n; i++){
            if(!isValid(board, occupied, row, i)) continue;
            board[row][i] = true;
            occupied[i] = true;
            helper(res, board, occupied, row+1, n);
            board[row][i] = false;
            occupied[i] = false;
        }
    }

    public boolean isValid(boolean[][] board, boolean[] occupied, int row, int col){

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
