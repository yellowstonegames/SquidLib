package squidpony.squidai;

import java.awt.*;
import java.util.HashMap;

/**
 * Created by Tommy Ettinger on 7/13/2015.
 */
public class AreaUtils {
    public static HashMap<Point, Double> arrayToHashMap(boolean[][] map)
    {
        HashMap<Point, Double> ret = new HashMap<>();
        for(int i = 0; i < map.length; i++)
        {
            for(int j = 0; j < map[i].length; j++)
            {
                if(map[i][j])
                    ret.put(new Point(i, j), 1.0);
            }
        }
        return ret;
    }
    public static HashMap<Point, Double> arrayToHashMap(double[][] map)
    {
        HashMap<Point, Double> ret = new HashMap<>();
        for(int i = 0; i < map.length; i++)
        {
            for(int j = 0; j < map[i].length; j++)
            {
                if(map[i][j] > 0.0)
                    ret.put(new Point(i, j), map[i][j]);
            }
        }
        return ret;
    }
}
