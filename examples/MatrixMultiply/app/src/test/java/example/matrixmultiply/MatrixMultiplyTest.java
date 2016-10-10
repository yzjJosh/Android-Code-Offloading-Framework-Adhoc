package example.matrixmultiply;

import org.junit.Test;

import static org.junit.Assert.*;

public class MatrixMultiplyTest {

    @Test
    public void testMultiply() {
        for(int i=0; i<100; i++) {
            int[][] m1 = MatrixMultiply.randMat(79, 65);
            int[][] m2 = MatrixMultiply.randMat(65, 79);
            assertTrue(MatrixMultiply.equals(MatrixMultiply.sequentiallyMatrixMultiply(m1, m2), MatrixMultiply.parallelMatrixMultiply(m1, m2)));
        }
    }
}