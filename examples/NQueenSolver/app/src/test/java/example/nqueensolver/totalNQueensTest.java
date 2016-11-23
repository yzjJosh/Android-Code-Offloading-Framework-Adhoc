package example.nqueensolver;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

import static junit.framework.Assert.assertEquals;

/**
 * Created by LiviaZhao on 11/23/16.
 */

public class totalNQueensTest {
    public NQueenSolver solver = new NQueenSolver();
    @Test
    public void testTotalQueens() throws ExecutionException, InterruptedException {
        Assert.assertEquals(solver.totalNQueens(8).size(), 92);
        Assert.assertEquals(solver.totalNQueens(9).size(), 352);
    }
}
