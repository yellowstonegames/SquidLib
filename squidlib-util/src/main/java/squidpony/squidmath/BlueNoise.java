package squidpony.squidmath;

import java.nio.charset.StandardCharsets;

/**
 * Provides access to a precalculated, tiling plane of 2D blue noise, that is, noise without high-frequency components,
 * as well as seeded modifications to that tiling blue noise plane that make it have even fewer patterns.
 * Not actually a noise class like {@link FastNoise}. The blue-noise distribution has the practical effect that a value
 * only appears next to a similar value very rarely. For a quick introduction, see
 * <a href="https://blog.demofox.org/2018/01/30/what-the-heck-is-blue-noise/">Alan Wolfe's blog</a>, which has many more
 * useful posts and links that often relate to blue noise and its relatives, like low-discrepancy sequences (which
 * SquidLib calls quasi-random sequences, like {@link VanDerCorputQRNG}).
 * <br>
 * This uses 64x64 grayscale texture 0 from <a href="http://momentsingraphics.de/BlueNoise.html">Christoph Peters'
 * invaluable blue noise resource</a>. It may use more of the textures from that site in the future.
 * <br>
 * Created by Tommy Ettinger on 10/15/2019.
 */
public class BlueNoise {
    private BlueNoise() {}
    /**
     * The raw data this class uses, as a 1D byte array. You probably don't want to use this field, but you might, so it
     * is public... but don't write to it. It's static, which is probably OK even on Android as long as no one changes
     * it, and uses {@link StandardCharsets} to decode itself from a String, which only works on GWT 2.8.2 and newer.
     * This can be considered [x][y] indexed or [y][x] indexed; the original image was [y][x] indexed. 
     */
    public static final byte[] RAW_NOISE = ("ÁwK1¶\025à\007ú¾íNY\030çzÎúdÓi ­rì¨ýÝI£g;~O\023×\006vE1`»Ü\004)±7\fº%LÓD\0377ÜE*\fÿí\177£RÏA2\r(Å\0026\023¯?*Â;ÌE!Â\022,è\006ºá6h\"ó¢Én\"<sZÅAt×\022\002x,aèkZõ"+
            "l±×\033dÅ&k°Ö÷nCÚ]%é\177ø\022S\001Øl´uÉ\036þ«À>Zß\000O®ñ\021Õæe÷¨ê^Â±\030þ®\021¹?èUªE6è\023_|¼¢!­t½P\005ÙG¥¸u.\030ò>Tÿ3nXCvíp*³\033ìÑyC¼/\031P1;òSÝÈ2KÒ\""+
            "È3r Óø·V\000\034ä4\bVê\020õgÇ\0331êÞ`¯ÅeãÓ­ò×\rÈ\034KÏ\013h5\tÃ\037T\002~Í´ kÐq@~ïc\003x\023ó»\005OxÛÃJÎeIÒ7´p]\013#J\006 $`F¿¡*³`åôS½F¤bùÝl¦Há\rû¡æ\013%º\005\035à©"+
            "G[âc\020§=,mñµ=þÃ-\034å\ròM¿?Ïöq9¹\017xæ\032eù2¦\026:~Ùå-:¶ð'Ww¿KcªÕ\\¢OÀ-Ð³:¥+Éî!\\Ñ\f$qß}¦WB*«Õýz¨\025ìPÌ\0027|ÞRq\001Ä¬%ÿr¯\030Ò\016_Ç3Ö=\0260úè8\roø"+
            "a\007Ù}ýAs¼áû¬Tè\024²_\007øÊxe\036µ1VØ(ª@ÚUÊ\007»Óaî\021WÆM{B\033s\005®óÉyiÍ¯\032ê%M\030±Nh\0267{Â¢K9Ö¹\026à:\tjæ¿~]÷h.µ\024J\"óC-\032KkÏ=ò\003é«Ûö»b\"ßU\b·B#ÞT"+
            "pÀhèÔ2\tÊFÙ\003+Íñ lGa\000ÁQìË¢\033D\004\035Ãð¤pé®\\ Ýµ2º¡b)¿6kNëFl§\035Mÿ|1È?úª\017GZ÷£ì¶\037p[\017ä1¤&s-`û7±Òt\rYÑ9z\016Éêvü\tã\034pÖJ\007£*\017Å6×íÂ\023óµ"+
            "\026.]Ì$q¹\034x-bVãø¼«wÃî³\020ÙH¸vÞP\022é3MÞ>\000Á*úeA\"ZD®û\037ÉYÔ\177µ\002t.f«\\JÖuÝ\003¡òß>Ô\f¨\0223B\002RÐ?[÷©\013#Æo[ü¹\"¬d\030á¸Q\0344ÂªÕ}Ç\017ç`xñ2¬ü`è\026XÑ9å\t"+
            "ïR´e4U­\003l¿<NÑhÝ#ù}\030Æm;ÐWô«)¢Í}ñoG¦Ó\003hð'V<µà\024D!Ë=÷º\037jÃ9\036AÁw\020ÈLúé\177\036´_\r¨ÜO,æ|\016?ÛjE\0076×S\nôxâT\022I6»\003Ò\031Oq¿Ûn Mà\020zFþ)§"+
            "~â\013ùÖ*ë Ü7(Æ¡õ.ê¾i7·\004ë\036fF»\000Àï\027^µ&Ê1?¾,´ùÜn¦v+Å¢\0008õ±(Ã^¢Ø³VÎ¹Ni¨]z¶d\030F\005WuËL)åt¬ÂüØ4b\035T/¯æÄÿvê®b\036\brÍ\033éFöa\016ée\031W\nv\0020eô\024"+
            "\001-Ë\031G»õ¢\nÐ«r×:\025\000Ñ\\B\fU\024±Ñ ygM\033\023Øí[©:dP\n±>Õ'¸Ðå;ªïÊ\034çpCÚaït2ÿn>RäZð%¸â£°y\034ò¢1Îz&îqãGû\017Ø,§BYý\177LBà\000½$Íjá»TªsI/f\026R@½4"+
            "Å£\020²ãØÆ\027-ýÁ5\fHh÷V?ÄláH§[8\n·È;óÏlãÂ5Ï¹&\024{ò0\025}\005ÇòþÀØw\016\\­Py\036=X\t$¯Ak^Æ#\016¼Û(\022ù¹\005Á÷f!Uqº\t1³ ¡\006pöÇXÚ=] ù\"8Þb\035|L&µâúÒ$\006"+
            "öÒ¿J}9dívÛ\022°ç\000ÔrìcI­X;n\032ÚO.¬ß¤\031_÷R^Ü/²E\013s­\003ÆêL\020C¯ê\tY£-gßl/`ýç§ÌW\004¹IÍ\037}Q/<¥\004~Õë$wÌ\023ë|\004JíyØA\025ë¨gSé\032&m³Òv½Ö3Ípð9ÄA·"+
            "ì«'\024ö\032(¦ù2¾\032ß_¶Ì\0310Æ^³\000>ZÑ)Á8Ë&­iÇ:\036Ìü5¾ÔAW/¤[\002m\025¨@\003y\031M\017UÉFsÕf3ÁàQp[ïC«\007õPåüi\rIåg¼õC°ýNå\013ÿu¶\râw£cPàö\t\031äò%O_¼!Ú¯èÏ"+
            "\177\034\007¹L°x;\bÅÚ\017iÏx$s8»B¢ò,\027¨4\034k`\022t£\\¾.ÖóN?)´\016{Ài>±Åúæ\016Hkaþ4Ýøá;ï\t\"Îèb®%7KÂ\021ÓY¬\037Ý|ÁPÕs\fãÙ¹ ñGV#Â]\001ð 4¬FÌ\177Ü/uÓô(<½¤"+
            "%²_-Ziý\027G{¹æý+°î\006nÎ\004büÈM+ó?Ð4Ý{\027¥l®Þ\024ÏmIÇÚ]þ\035\bg;¥R¶ÇX\023gÅApÌ\023¼ÚA¥·1ò\003V\035`ÜoDeâN÷1W±à&E\177¬\007oX\003²çÍû2~;§çr\023é+Qºï\"ü\026|\006ãNð"+
            "\r¡Oÿ|)ÈTuÜÒÇ§\0265'Å\017\033<£\022·í\\Á\030§Æi=c\bDí!W»*\004=¶¡Òc¨\021ÊCÝ2ªÓvýÙë\035­äù\036Kk\021<tPôÎ½\001®y@¹évôk2Ò\0369âJú-\021½&M·Õù\fgRöyHâvW²j\\ëG\036¸/"+
            "©Tk1Hc9ê\006Á¬'ì¶/\f\177\\Úñ¤gÓÆHþfÔzëUóÚ\034È_r Ë­×\0360Ä\005>(ô\004¡Àqb<\024½Ò\boµ\025Ò\\ãbþAä L9\024Qÿ,[\bÝr\017´V$¯E Îw©k\020æ-M7\030ãnXì\027Ö½5\032Î+\017ôßË"+
            "']@óÄ%¨}0DÌ\032m×¬f\bÌo$ß¯\035½«N5Çö\fÞ`3\0048Hþ°ÀyðCÿ¸jª[oOzÛ=S\006}çù°x6ßY\001@ºõ\nJ¼)XÄí´úÀCc7æzñ'çt@Àm\026¶\"áÃ¢#Ú\006(Å\0166IÎü\"\016è§ûi±Â "+
            "\020Ú ¤MíhÜ\037uïÞ\0027\031w0^\rÊ\005T\023Ñ^¦.üïQù]}îVeµ\\¥~Ý+ä²Ç@_¸Jí\"6FnQcÀ\fs\031Ê,¡T±3[¦z\020MÔS¬ê~öiÖ?ÃlE\005\034ÖYÊuB¬Ô+\017<Í\026õKÑ\037øTwh\006(\024ÊuÓ"+
            "µ*þÔ²Iq\004ÕÅ\025?Íõ£#à\0265¸'¨\033ú°ßº~K®9'£\t\032¸kã¨v2æ=d¼\007:\032ñ1Ód©Ý\032\0024ÉâDïf8û¾\022ê<gû%¶ç_¿r\001FÏK\n]5$òä\020j½êÛb5È\003R¼\np¯ë\024Ë¦ÙQ¾\177ãøVNò"+
            "¥_\023~«&á^O©áSl,\f=´f YÜîoÉçwV\reÈ\002OzðF\"ùBÛ\034ÄWHmaÿ®F :\b-¿x >º\007Y\027¡xÏ.\035~ÁD\002ª×íÅûv¼,E\003À£<*X\033qÏ,²\026^rÐ+b¢\001$Ø/ûå(\016!oÆ²jè"+
            "ÜøÏ.GÙÆö?\b·ó\rÑu\0338øR|1\017®9\025¦Ù\037MÒý¬Ùõ¶A\022üT×ç¬\r³|ð;xªÃCsÐì\027×D\f®Shêu±hÞI^5ëYÈi\025§ÕåcÑSeµ3ðmEz3â§m¿(9ÄQäHÎ^·\013\033Yº0MZþ%aÄ"+
            "s\033\000¥Q7\032T)Én­ú#·¢á»%@L\006$êù\017t\031â\013Â `LÉ;\nyîd\002\030.¿\020÷Lï7Üùfâ|ï6\021ã´È(\013¼å\000î\025ÙfA\021/K\001Zôm¶Ä\177>Ê¨Y¸d#ñ\005\037ë[øI\034¤×g«s ÙjÊ{\025©\b@Î¸¦L"+
            "+B_ñcÐ©{:»,RÄ\004wÐîo¯Ë\035Û¤G0^Þ)\0018Îp·~.Û²Ì2ºtú@æ£=+\004³I'Às4\035\002Òõ|Ø2r!H\\\007rçª\\7\ny.ÿ\025ä¯\t¾PéI­0TÕ¤\024f\004àY;ÇS\007(ZýÀRâaÔé¡÷oX"+
            "h\006»¢\020NßøÃ\021Óã¢\034Jõ'¾\033ÝÀëU:qÑg ÷{Õi\021üà\030CôÂO!n\016$®îÎ´z\022§p ôRe\fDÞÂ«8\"WÍ²> j4°ý=Í\177\rØlRúG\026a¸NïD\030¥Ä\\s\n:©ëEØ\177\0274fÞEìÌ9º\021,È³%ë"+
            "\023Häuÿ\032í&Yh·5>³gªÓø\016Æ)¦6Í¶1ò@y»7ËaäþÓx·öa¾Jö\0350]\013C}«Û8wOýÇmó.¼]}ØGÂð-\000`¤îÎ\0060æ>#|ß[ÿu\013WmÛ&\004ì\035²X)\027É0O\001åp\tÄÔ°lÞûX\004\031òÑ`"+
            "Ü\027­aB\nË§\001\035\020«ßÈo\022%{¾m J°\031¾ã!\000°ÐU¤h0H¿d\t¤Ñ)­9wS\001KÄ&èg¾¥3\177¸Q(ÖåS6¶kæuVC\036JàU\032Ê\005ì<×cFî Kd\024>Üó}\021áAí8v³Z@\024dè$½\027/u²Ï=o\013!"+
            "@\002¡Ãyú\024NÏ )ø¿[­þBñÚ^2Ãj-\025³~ÇætÁ\013Ð¥\003¯nÝ\037ðÊûÙ¡óc¥î\017P\036ù×Vèdð0j\016\"FfÛ,õ<\005²×y\013.Ôq´*¦v\017õN©ûÔ'\n6øª*E\034lQ#ÆU\022¼fR¸3NË?Ø6`¹G­É"+
            "\025Òá±ïº©wÅ\\\027ëH£ê5b\023Oü¶!Ìm=^ºPÖ`þ´5õÕ,üI\b}'j\003\023z¯\bl\000Þ{+\005K·Z<qË4]\036\fáo½4dËj\033»\006Èå:ÕUáz\b¾àñq\030yÞÃèbCz ç«;ÁäªÕ äEÉ(§ñÂ8k"+
            "$ôþ\027QÔûB#ý©:öP}#nÁfB3ìW\037J¥/\005É9ïY'\n¸\031_Î!\026G÷8^¾T÷t\021Zä£\n|Å+¨ç\007|¡V°ðLÝ\021'á°AÜô®\f\033¥\001±\022Êÿ´ÚgS!¬\023Ih©Û\0013MtôÙ[rÇén¤0\031µLÍ?\035rÖ"+
            "W@ÚdJ*Æ6kÐ~UÁs]\rÍ+ZEé+bÙq¡e4xé¼Ô\002 Ì:nîÄ²\013.\007µ)\006Øeé\003³ü½3ì°\020tÀó³\021å\001\032¹/Ô\000\037í¹tÿÐ{KÆö%?æÃ&D\016r>{ø\036X&âj½<¦Q\027Dñ\177Ã:Û-cH"+
            "j\037Sú#ÖD[a<\bóD¤m8\030¨5»\026·Ð\tQ\031]ü¦ñaä/¿PÓ\177B\030LûÑ{ßÌ¯Z\020#pS©î\027Ï§âË_;oª!wÊß©i´*L¿àU\007ðPà\n_2X{ô±ÖË*µU\025®ç\016÷¥Écì\037Xýi5ã¦öº\tÇ{\005"
    ).getBytes(StandardCharsets.ISO_8859_1);

    /**
     * Stores the same values as {@link #RAW_NOISE} if considered [x][y] indexed; available for convenient usage by
     * GreasedRegion and other classes that can use 2D byte arrays.
     */
    public static final byte[][] RAW_2D = new byte[64][64];

    /**
     * A 256-element array of {@link GreasedRegion}s, each 64x64, where the first GreasedRegion has an impossibly strict
     * threshold on what points to include (it is empty), but the second has some points far apart, the third has more,
     * and so on until the last element includes almost all points.
     */
    public static final GreasedRegion[] LEVELS = new GreasedRegion[256];
    static {
        for (int x = 0; x < 64; x++) {
            System.arraycopy(RAW_NOISE, x << 6, RAW_2D[x], 0, 64);
        }
        LEVELS[0] = new GreasedRegion(64, 64);
        for (int i = -127; i < 128; i++) {
            LEVELS[i+128] = new GreasedRegion(RAW_2D, -128, i);
        }
    }
    
    /**
     * Gets a byte from the raw data this stores, treating it as an infinite 2D plane of tiling 64x64 regions. You can
     * interpret the blue noise in many ways, but it is meant to be used with a threshold to select mostly widely-spaced
     * points when the threshold requires values greater than 100 or less than -100, or irregularly-shaped patterns of
     * points when the threshold is in the middle, like for values less than 0.
     * @param x x position, can be any int; results will repeat every 64 cells
     * @param y y position, can be any int; results will repeat every 64 cells
     * @return a byte that obeys a blue-noise distribution, so a value is only next to a similar value very rarely
     */
    public static byte get(final int x, final int y)
    {
        return RAW_NOISE[(y << 6 & 0xFC0) | (x & 0x3F)];
    }

    /**
     * Gets a modified byte from the raw data this stores, using seed to adjust repeated sections of blue noise so
     * patterns are much less noticeable. Unlike {@link #get(int, int)}, this won't repeat every 64 cells on x or y, but
     * it isn't as correct at matching blue-noise frequencies.
     * @param x x position, can be any int; results will not repeat for a very long time
     * @param y y position, can be any int; results will not repeat for a very long time
     * @param seed seed value, can be any int; should be the same for all calls that should be from the same 2D plane
     * @return a byte that obeys an almost-blue-noise distribution, so a value is only next to a similar value rarely
     */
    public static byte getSeeded(final int x, final int y, int seed){
        seed ^= (x >>> 6) * 0x1827F5 ^ (y >>> 6) * 0x123C21;
        return (byte) (RAW_NOISE[(y << 6 & 0xFC0) | (x & 0x3F)] ^
                ((seed ^ (seed << 19 | seed >>> 13) ^ (seed << 5 | seed >>> 27) ^ 0xD1B54A35) * 0x125493 >>> 26)
                ^ (x + y >> 2 & 0x3F) ^ (x - y >> 2 & 0x3F));
    }
    
    public static byte[][] generateMetropolis(int seedA, int seedB)
    {
        final int[][] early = new int[64][64];
        final byte[][] done = new byte[64][64];
        int min = 0x40000000, max = 0;
        int choice, prob, stateA = seedA, stateB = seedB, a, b;
        stateA = stateA + 0xC1C64E6D | 0;
        a = (stateA ^ stateA >>> 17) * ((stateB = stateB + 0x9E3779BB | 0) >>> 12 | 1);
        a = (a ^ a >>> 16) * 0xAC451;
        a ^= a >>> 15;
        b = (stateA ^ stateA >>> 17) * ((stateB = stateB + 0x9E3779BB | 0) >>> 12 | 1);
        b = (b ^ b >>> 16) * 0xAC451;
        b ^= b >>> 15;

        for (int n = 0; n < 0x7F800; n++) {
            int x = n & 0x3F, y = (n >>> 6 & 0x3F),
//            int x = stateA + n & 63, y = stateB - n & 63,
                    cx = x, cy = y, choiceTicks = 0, probTicks = 0,
                    curr = RAW_NOISE[(y << 6 & 0xFC0) | (x & 0x3F)] + 128, chosen;
            stateA = stateA + 0xC1C64E6D | 0;
            choice = (stateA ^ stateA >>> 17) * ((stateB = stateB + 0x9E3779BB | 0) >>> 12 | 1);
            choice = (choice ^ choice >>> 16) * 0xAC451;
            choice ^= choice >>> 15;

            a = a + 0x9E3779D | 0;
            prob = (a ^ a >>> 17) * ((b = b + ((a | -a) >> 31 & 0xABC1236B) | 0) >>> 12 | 1);
            prob = (prob ^ prob >>> 16) * 0xAC451;
            prob ^= prob >>> 15;

            for (int i = 0; i < 1024; i++) {
                cx += -(choice & 1) | 1;
                cy += 1 - (choice & 2);
                choice >>>= 2;
                if (++choiceTicks == 16) {
                    stateA = stateA + 0xC1C64E6D | 0;
                    choice = (stateA ^ stateA >>> 17) * ((stateB = stateB + 0x9E3779BB | 0) >>> 12 | 1);
                    choice = (choice ^ choice >>> 16) * 0xAC451;
                    choice ^= choice >>> 15;
                    choiceTicks = 0;
                }
                if ((chosen = RAW_NOISE[(cy << 6 & 0xFC0) | (cx & 0x3F)] + 128) < curr) {
                    if (((curr * (prob & 0xFF)) >>> 8) > chosen) {
                        cx = x;
                        cy = y;
                    } else {
                        x = cx;
                        y = cy;
                        curr = chosen;
                    }
                    prob >>>= 8;
                    if (++probTicks == 4) {
                        a = a + 0x9E3779D | 0;
                        prob = (a ^ a >>> 17) * ((b = b + ((a | -a) >> 31 & 0xABC1236B) | 0) >>> 12 | 1);
                        prob = (prob ^ prob >>> 16) * 0xAC451;
                        prob ^= prob >>> 15;
                        probTicks = 0;
                    }
                } else {
                    x = cx;
                    y = cy;
                    curr = chosen;
                }
            }
            ++early[x & 63][y & 63];
        }
        for (int x = 0; x < 64; x++) {
            for (int y = 0; y < 64; y++) {
                final int e = early[x][y];
                min = Math.min(min, e);
                max = Math.max(max, e);
            }
        }
        System.out.println("original min="+min+", original max="+max);
        if(max != min) {
            for (int x = 0; x < 64; x++) {
                for (int y = 0; y < 64; y++) {
//                    done[x][y] = (byte) (early[x][y] * 255 / max - 128);
                    done[x][y] = (byte) ((early[x][y] - min) * 255 / (max - min) - 128);
                }
            }
        }
        return done;
    }
}
