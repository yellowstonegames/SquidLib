package squidpony.gdx.tools;

import com.badlogic.gdx.math.Matrix3;
import squidpony.ArrayTools;
import squidpony.squidmath.NumberTools;
import squidpony.squidmath.PhantomNoise;

/*
3x3:
-0.0274288894836461, +0.7290764961819305, +0.6853079636883495,
-0.9053457947767863, -0.3092463840200059, +0.2927177497885901,
+0.4245136549634499, -0.6124211435583022, +0.6685588682179822,

4x4:
+0.1905791786518885, +0.3861174185764629, -0.5847328850860769, -0.6893092454147123,
+0.4111245283526767, +0.7677083019755620, +0.4726745850368516, +0.1429766710123569,
-0.8377519222061313, +0.3163846746990775, +0.3122834687899689, -0.3195533236867484,
+0.3063633089361206, -0.4041101515389197, +0.5829861966402288, -0.6364257601360758,

5x5:
-0.0417804356625072, -0.4447059379310900, -0.7593004186144269, +0.1743978892123604, +0.4444146757006882,
-0.0527174114260949, -0.2540107473276194, -0.3358591819841499, -0.7222118862871126, -0.5500306916666624,
+0.6676221025365682, +0.6127355630197416, -0.3712597762050637, -0.1764713764044092, +0.1112639238877099,
-0.7030600537850631, +0.5259486213519964, -0.1486959515977248, -0.3167611363905264, +0.3310376957845657,
+0.2390983145240142, -0.2974569502761614, +0.3923621717947575, -0.5660380699891813, +0.6179240795663868,

6x6:
-0.0380778061544851, -0.0303724527465320, +0.4356293160913650, +0.7933753851627695, +0.4197685953950022, +0.0894521710543777,
-0.0351875174538162, -0.0850113478089023, -0.2035454436153332, -0.3017280518525850, +0.6256003464393485, +0.6883737459015009,
-0.3085763896526368, -0.7945848211997655, -0.4523290146687495, +0.2204308565934835, -0.0005929883712112, -0.1508373488249853,
+0.7172834955411770, +0.1566898043832359, -0.5790337995177075, +0.2870787399236230, +0.1649382369085783, -0.1393314322019783,
-0.5924507996312636, +0.5326785531364373, -0.3844530979937220, +0.0722080755104951, +0.3197386906135813, -0.3372709605140012,
+0.1958822090885441, -0.2344455917280123, +0.2916686002573239, -0.3847270179266404, +0.5550232778653369, -0.6058996139948896,
 */
public class MatrixWork {
    public static void main(String[] args){
        double gold = PhantomNoise.goldenDouble[0][0],
                cg = NumberTools.cos_(gold), sg = NumberTools.sin_(gold);
        double[][] seed = new double[][] {
                {cg, sg,},
                {-sg, cg,},
        };
        for (int d = 3; d <= 6; d++) {
            double[][] L = new double[d][d], R = new double[d][d], W = new double[d][d];
            gold = PhantomNoise.goldenDouble[d-2][0];
            cg = NumberTools.cos_(gold);
            sg = NumberTools.sin_(gold);
            L[0][0] = cg;
            L[0][1] = sg;
            L[1][0] = -sg;
            L[1][1] = cg;
            for (int diag = 2; diag < d; diag++) {
                L[diag][diag] = 1.0;
            }
            for (int g = 1; g < PhantomNoise.goldenDouble[d-2].length; g++) {
                gold = PhantomNoise.goldenDouble[d-2][g];
                cg = NumberTools.cos_(gold);
                sg = NumberTools.sin_(gold);
                R[0][0] = cg;
                R[0][1+g] = sg;
                R[1+g][0] = -sg;
                R[1+g][1+g] = cg;
                for (int diag = 1; diag < d; diag++) {
                    if(diag == 1+g) continue;
                    R[diag][diag] = 1.0;
                }
                for (int i = 0; i < d; i++) {
                    for (int j = 0; j < d; j++) {
                        double s = 0.0;
                        for (int k = 0; k < d; k++) {
                            s += L[i][k] * R[k][j];
                        }
                        W[i][j] = s;
                    }
                }
                ArrayTools.insert(W, L, 0, 0);
                ArrayTools.fill(R, 0.0);
            }
            ArrayTools.insert(seed, R, d-seed.length, d-seed.length);
            for (int diag = 0; diag < d-seed.length; diag++) {
                R[diag][diag] = 1.0;
            }
            for (int i = 0; i < d; i++) {
                for (int j = 0; j < d; j++) {
                    double s = 0.0;
                    for (int k = 0; k < d; k++) {
                        s += L[i][k] * R[k][j];
                    }
                    W[i][j] = s;
                }
            }
            System.out.printf("%dx%d: \n", d, d);
            for (int y = 0; y < d; y++) {
                for (int x = 0; x < d; x++) {
                    System.out.printf("%+1.16f, ", W[y][x]);
                }
                System.out.println();
            }
            System.out.println();
            seed = W;
        }
    }
    public static void mainOld(String[] args){
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
