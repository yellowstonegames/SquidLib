package squidpony.gdx.tools;

import com.badlogic.gdx.math.Matrix3;
import squidpony.squidmath.NumberTools;
import squidpony.squidmath.PhantomNoise;

public class MatrixWork {
    public static void main(String[] args){
        float gold_0 = (float) PhantomNoise.goldenDouble[0][0];
        float gold_1_0 = (float) PhantomNoise.goldenDouble[1][0];
        float gold_1_1 = (float) PhantomNoise.goldenDouble[1][1];
        Matrix3 mat_0 = new Matrix3(new float[]{
                1, 0, 0,
                0, NumberTools.cos_(gold_0), NumberTools.sin_(gold_0),
                0, -NumberTools.sin_(gold_0), NumberTools.cos_(gold_0)});
        System.out.println(mat_0);
        System.out.println();
        Matrix3 mat_1_0 = new Matrix3(new float[]{
                NumberTools.cos_(gold_1_0), NumberTools.sin_(gold_1_0), 0,
                -NumberTools.sin_(gold_1_0), NumberTools.cos_(gold_1_0), 0,
                0, 0, 1
        });
        System.out.println(mat_1_0);
        System.out.println();
        Matrix3 mat_1_1 = new Matrix3(new float[]{
                NumberTools.cos_(gold_1_1), 0, NumberTools.sin_(gold_1_1),
                0, 1, 0,
                -NumberTools.sin_(gold_1_1), 0, NumberTools.cos_(gold_1_1),
        });
        System.out.println(mat_1_1);
        System.out.println();
        mat_1_0.mul(mat_1_1);
        System.out.println(mat_1_0);
        System.out.println();
        mat_1_0.mul(mat_0);
        System.out.println(mat_1_0);
        System.out.println();
    }
}
