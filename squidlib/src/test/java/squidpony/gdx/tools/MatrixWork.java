package squidpony.gdx.tools;

import com.badlogic.gdx.math.Matrix3;
import squidpony.ArrayTools;
import squidpony.squidmath.NumberTools;
import squidpony.squidmath.PhantomNoise;

/*
LAYERED:
2x2:
final double x2 = + x * +0.6088885514347261 + y * -0.7943553508622062;
final double y2 = + x * +0.7943553508622062 + y * +0.6088885514347261;

3x3:
final double x2 = + x * +0.0227966890756033 + y * +0.6762915140143574 + z * -0.7374004675850091;
final double y2 = + x * +0.2495309026014970 + y * +0.7103480212381728 + z * +0.6592220931706847;
final double z2 = + x * +0.9680388783970242 + y * -0.1990510681264026 + z * -0.1525764462988358;

4x4:
final double x2 = + x * +0.5699478528112771 + y * +0.7369836852218905 + z * -0.0325828875824773 + w * -0.3639975881105405;
final double y2 = + x * +0.1552282348051943 + y * +0.1770952336543200 + z * -0.7097702517705363 + w * +0.6650917154025483;
final double z2 = + x * +0.0483833371062336 + y * +0.3124109456042325 + z * +0.6948457959606478 + w * +0.6469518300143685;
final double w2 = + x * +0.8064316315440612 + y * -0.5737907885437848 + z * +0.1179845891415618 + w * +0.0904374415002696;

5x5:
final double x2 = + x * +0.1524127934921893 + y * -0.2586710352203958 + z * -0.4891826043642151 + w * +0.7663312575129502 + u * -0.2929089192051232;
final double y2 = + x * -0.0716486050004579 + y * -0.5083828718253534 + z * -0.5846508329893165 + w * -0.3242340701968086 + u * +0.5400343264823232;
final double z2 = + x * +0.5391124130592424 + y * +0.4637201165727557 + z * -0.0268449575347777 + w * +0.2805630001516211 + u * +0.6471616940596671;
final double w2 = + x * -0.4908590743023694 + y * -0.3159190659906883 + z * +0.4868180845277980 + w * +0.4733894151555028 + u * +0.4492456287606979;
final double u2 = + x * +0.6656547456376498 + y * -0.6028584537113622 + z * +0.4289447660591045 + w * -0.0882009139887838 + u * -0.0676076855220496;

6x6:
final double x2 = + x * -0.0850982316788443 + y * +0.0621411489653063 + z * +0.6423842935800755 + w * +0.5472782330246069 + u * -0.5181072879831091 + v * -0.1137065126038194;
final double y2 = + x * +0.1080560582151551 + y * -0.3254670556393390 + z * -0.3972292333437380 + w * +0.0964380840482216 + u * -0.5818281028726723 + v * +0.6182273380506453;
final double z2 = + x * +0.2504893307323878 + y * -0.3866469165898269 + z * -0.2346647170372642 + w * +0.7374659593233097 + u * +0.4257828596124605 + v * -0.1106816328431182;
final double w2 = + x * +0.0990858373676681 + y * +0.4040947615164614 + z * +0.3012734241554820 + w * +0.1520113643725959 + u * +0.4036980496402723 + v * +0.7440701998573674;
final double u2 = + x * -0.7720417581190233 + y * -0.5265151283855897 + z * +0.1995725381386031 + w * -0.0464596713813553 + u * +0.2186511264128518 + v * +0.1990962291039879;
final double v2 = + x * +0.5606136879764017 + y * -0.5518123912290505 + z * +0.4997557173523122 + w * -0.3555852919481873 + u * +0.0731165180984564 + v * +0.0560452079067605;

RIDGED: (lacunarity fixed at 2.0)

2x2:
final double x2 = + x * +1.2177771028694522 + y * -1.5887107017244124;
final double y2 = + x * +1.5887107017244124 + y * +1.2177771028694522;

3x3:
final double x2 = + x * +0.0455933781512065 + y * +1.3525830280287148 + z * -1.4748009351700182;
final double y2 = + x * +0.4990618052029940 + y * +1.4206960424763455 + z * +1.3184441863413694;
final double z2 = + x * +1.9360777567940484 + y * -0.3981021362528052 + z * -0.3051528925976716;

4x4:
final double x2 = + x * +1.1398957056225543 + y * +1.4739673704437810 + z * -0.0651657751649546 + w * -0.7279951762210809;
final double y2 = + x * +0.3104564696103886 + y * +0.3541904673086399 + z * -1.4195405035410726 + w * +1.3301834308050966;
final double z2 = + x * +0.0967666742124671 + y * +0.6248218912084650 + z * +1.3896915919212955 + w * +1.2939036600287370;
final double w2 = + x * +1.6128632630881223 + y * -1.1475815770875697 + z * +0.2359691782831236 + w * +0.1808748830005391;

5x5:
final double x2 = + x * +0.3048255869843786 + y * -0.5173420704407916 + z * -0.9783652087284301 + w * +1.5326625150259003 + u * -0.5858178384102464;
final double y2 = + x * -0.1432972100009157 + y * -1.0167657436507067 + z * -1.1693016659786330 + w * -0.6484681403936172 + u * +1.0800686529646464;
final double z2 = + x * +1.0782248261184848 + y * +0.9274402331455114 + z * -0.0536899150695553 + w * +0.5611260003032422 + u * +1.2943233881193341;
final double w2 = + x * -0.9817181486047388 + y * -0.6318381319813766 + z * +0.9736361690555960 + w * +0.9467788303110056 + u * +0.8984912575213958;
final double u2 = + x * +1.3313094912752996 + y * -1.2057169074227243 + z * +0.8578895321182090 + w * -0.1764018279775676 + u * -0.1352153710440993;

6x6:
final double x2 = + x * -0.1701964633576885 + y * +0.1242822979306125 + z * +1.2847685871601510 + w * +1.0945564660492137 + u * -1.0362145759662182 + v * -0.2274130252076388;
final double y2 = + x * +0.2161121164303102 + y * -0.6509341112786780 + z * -0.7944584666874760 + w * +0.1928761680964432 + u * -1.1636562057453446 + v * +1.2364546761012907;
final double z2 = + x * +0.5009786614647755 + y * -0.7732938331796537 + z * -0.4693294340745284 + w * +1.4749319186466194 + u * +0.8515657192249211 + v * -0.2213632656862364;
final double w2 = + x * +0.1981716747353363 + y * +0.8081895230329228 + z * +0.6025468483109641 + w * +0.3040227287451918 + u * +0.8073960992805446 + v * +1.4881403997147349;
final double u2 = + x * -1.5440835162380466 + y * -1.0530302567711793 + z * +0.3991450762772062 + w * -0.0929193427627107 + u * +0.4373022528257036 + v * +0.3981924582079757;
final double v2 = + x * +1.1212273759528033 + y * -1.1036247824581010 + z * +0.9995114347046243 + w * -0.7111705838963747 + u * +0.1462330361969127 + v * +0.1120904158135210;

 */
public class MatrixWork {
    public static void main(String[] args){
        final double lac = 1.0; // lacunarity; 1.0 for layered, 2.0 for ridged.

        final String dimNames = "xyzwuv";
        double gold = PhantomNoise.goldenDouble[0][0] * 3.0,
                cg = NumberTools.cos_(gold), sg = NumberTools.sin_(gold);
        double[][] seed = new double[][] {
                {cg, sg,},
                {-sg, cg,},
        };
        System.out.printf("%dx%d: \n", 2, 2);
        for (int y = 0; y < 2; y++) {
            System.out.printf("final double %s2 =", dimNames.charAt(y));
            for (int x = 0; x < 2; x++) {
                System.out.printf(" + %s * %+1.16f", dimNames.charAt(x), seed[y][x] * lac);
            }
            System.out.println(';');
        }
        System.out.println();
        for (int d = 3; d <= 6; d++) {
            double[][] L = new double[d][d], R = new double[d][d], W = new double[d][d];
            gold = PhantomNoise.goldenDouble[d-2][0] * 3.0;
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
                gold = PhantomNoise.goldenDouble[d-2][g] * 3.0;
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
                System.out.printf("final double %s2 =", dimNames.charAt(y));
                for (int x = 0; x < d; x++) {
                    System.out.printf(" + %s * %+1.16f", dimNames.charAt(x), W[y][x] * lac);
                }
                System.out.println(';');
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
