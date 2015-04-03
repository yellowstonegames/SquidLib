package squidpony.squidgrid.mapping;

import java.awt.Point;
import java.util.Random;

/**
 * Created by Tommy Ettinger on 4/1/2015.
 */
public class DungeonUtility {
    public static Point randomFloor(char[][] map, Random rng)
    {
        int width = map.length;
        int height = map[0].length;
        if(width < 3 || height < 3)
            return null;
        Point pt = new Point(rng.nextInt(width), rng.nextInt(height));
        for(int i = 0; i < 20; i++)
        {
            if(map[pt.x][pt.y] == '.')
            {
                return pt;
            }
            else
            {
                pt.x = rng.nextInt(width);
                pt.y = rng.nextInt(height);
            }
        }
        pt.x = 1;
        pt.y = 1;
        if(map[pt.x][pt.y] == '.')
            return pt;

        while(map[pt.x][pt.y] != '.')
        {
            pt.x += 1;
            if(pt.x >= width - 1)
            {
                pt.x = 1;
                pt.y += 1;
            }
            if(pt.y >= height - 1)
                return null;
        }
        return pt;
    }

    public static char[][] hashesToLines(char[][] map)
    {
        int Width = map[0].length+2;
        int Height = map.length+2;

        char[][] neo = new char[Height][Width], dungeon = new char[Height][Width];
        for(int i = 1; i < Height - 1; i++)
        {
            for(int j = 1; j < Width - 1; j++)
            {
                dungeon[i][j] = map[i-1][j-1];
            }
        }
        for(int i = 0; i < Height; i++)
        {
            neo[i][0] = '\1';
            neo[i][Width-1] = '\1';
            dungeon[i][0] = '\1';
            dungeon[i][Width-1] = '\1';
        }
        for(int i = 0; i < Width; i++)
        {
            neo[0][i] = '\1';
            neo[Height-1][i] = '\1';
            dungeon[0][i] = '\1';
            dungeon[Height-1][i] = '\1';
        }

        for (int y = 1; y < Height - 1; y++)
        {
            for (int x = 1; x < Width - 1; x++)
            {
                if (map[y-1][x-1] == '#')
                {
                    int q = 0;
                    q |= (y <= 1 || map[y - 2][x-1] == '#') ? 1 : 0;
                    q |= (y <= 1 || x >= Width - 2 || map[y - 2][x + 0] == '#') ? 2 : 0;
                    q |= (x >= Width - 2  || map[y - 1][x + 0] == '#') ? 4 : 0;
                    q |= (y >= Height - 2 || x >= Width - 2 || map[y + 0][x + 0] == '#') ? 8 : 0;
                    q |= (y >= Height - 2 || map[y + 0][x-1] == '#') ? 16 : 0;
                    q |= (y >= Height - 2 || x <= 1 || map[y + 0][x - 2] == '#') ? 32 : 0;
                    q |= (x <= 1 || map[y - 1][x - 2] == '#') ? 64 : 0;
                    q |= (y <= 1 || x <= 1 || map[y - 2][x - 2] == '#') ? 128 : 0;

                    if (q == 0xff)
                    {
                        neo[y][x] = '\1';
                        dungeon[y][x] = '\1';
                    }
                    else
                    {
                        neo[y][x] = '#';
                    }
                }
                else
                {
                    neo[y][x] = dungeon[y][x];
                }
            }
        }

        for (int y = 0; y < Height; y++)
        {
            for (int x = 0; x < Width; x++)
            {
                if (dungeon[y][x] == '#')
                {
                    boolean n = (y <= 0 || dungeon[y - 1][x] == '#');
                    boolean e = (x >= Width - 1 || dungeon[y][x + 1] == '#');
                    boolean s = (y >= Height - 1 || dungeon[y + 1][x] == '#');
                    boolean w = (x <= 0 || dungeon[y][x - 1] == '#');

                    if (n)
                    {
                        if (e)
                        {
                            if (s)
                            {
                                if (w)
                                {
                                    neo[y][x] = '┼';
                                }
                                else
                                {
                                    neo[y][x] = '├';
                                }
                            }
                            else if (w)
                            {
                                neo[y][x] = '┴';
                            }
                            else
                            {
                                neo[y][x] = '└';
                            }
                        }
                        else if (s)
                        {
                            if (w)
                            {
                                neo[y][x] = '┤';
                            }
                            else
                            {
                                neo[y][x] = '│';
                            }
                        }
                        else if (w)
                        {
                            neo[y][x] = '┘';
                        }
                        else
                        {
                            neo[y][x] = '│';
                        }
                    }
                    else if (e)  // ┼ ├ ┤ ┴ ┬ ┌ ┐ └ ┘ │ ─
                    {
                        if (s)
                        {
                            if (w)
                            {
                                neo[y][x] = '┬';
                            }
                            else
                            {
                                neo[y][x] = '┌';
                            }
                        }
                        else if (w)
                        {
                            neo[y][x] = '─';
                        }
                        else
                        {
                            neo[y][x] = '─';
                        }
                    }
                    else if (s)
                    {
                        if (w)
                        {
                            neo[y][x] = '┐';
                        }
                        else
                        {
                            neo[y][x] = '│';
                        }
                    }
                    else if (w)
                    {
                        neo[y][x] = '─';
                    }
                    else
                    {
                        neo[y][x] = '─';
                    }

                }
                else
                {
                    neo[y][x] = dungeon[y][x];
                }
            }
        }
        //vertical crossbar removal
        for (int y = 1; y < Height; y++)
        {
            for (int x = 0; x < Width; x++)
            {
                // ┼ ├ ┤ ┴ ┬ ┌ ┐ └ ┘ │ ─
                if (neo[y][x] == '┼' || neo[y][x] == '├' || neo[y][x] == '┤' || neo[y][x] == '┴')
                {
                    if (neo[y - 1][x] == '┼' || neo[y - 1][x] == '├' || neo[y - 1][x] == '┤' || neo[y - 1][x] == '┬')
                    {
                        if ((x >= Width - 1 || dungeon[y - 1][x + 1] == '#' || dungeon[y - 1][x + 1] == '\1') &&
                                (x <= 0 || dungeon[y - 1][x - 1] == '#' || dungeon[y - 1][x - 1] == '\1') &&
                                (x >= Width - 1 || dungeon[y][x + 1] == '#' || dungeon[y][x + 1] == '\1') &&
                                (x <= 0 || dungeon[y][x - 1] == '#' || dungeon[y][x - 1] == '\1'))
                        {
                            switch (neo[y][x])
                            {
                                case '┼':
                                    neo[y][x] = '┬';
                                    break;
                                case '├':
                                    neo[y][x] = '┌';
                                    break;
                                case '┤':
                                    neo[y][x] = '┐';
                                    break;
                                case '┴':
                                    neo[y][x] = '─';
                                    break;
                            }
                            switch (neo[y - 1][x])
                            {
                                case '┼':
                                    neo[y - 1][x] = '┴';
                                    break;
                                case '├':
                                    neo[y - 1][x] = '└';
                                    break;
                                case '┤':
                                    neo[y - 1][x] = '┘';
                                    break;
                                case '┬':
                                    neo[y - 1][x] = '─';
                                    break;

                            }
                        }
                    }
                }
            }
        }
        //horizontal crossbar removal
        for (int y = 0; y < Height; y++)
        {
            for (int x = 1; x < Width; x++)
            {
                // ┼ ├ ┤ ┴ ┬ ┌ ┐ └ ┘ │ ─
                if (neo[y][x] == '┼' || neo[y][x] == '┤' || neo[y][x] == '┬' || neo[y][x] == '┴')
                {
                    if (neo[y][x - 1] == '┼' || neo[y][x - 1] == '├' || neo[y][x - 1] == '┬' || neo[y][x - 1] == '┴')
                    {
                        if ((y >= Height - 1 || x >= Width - 1 || dungeon[y + 1][x - 1] == '#' || dungeon[y + 1][x - 1] == '\1') &&
                                (y <= 0 || dungeon[y - 1][x - 1] == '#' || dungeon[y - 1][x - 1] == '\1') &&
                                (y >= Height - 1 || dungeon[y + 1][x] == '#' || dungeon[y + 1][x] == '\1') &&
                                (y <= 0 || dungeon[y - 1][x] == '#' || dungeon[y - 1][x] == '\1'))
                        {
                            switch (neo[y][x])
                            {
                                case '┼':
                                    neo[y][x] = '├';
                                    break;
                                case '┤':
                                    neo[y][x] = '│';
                                    break;
                                case '┬':
                                    neo[y][x] = '┌';
                                    break;
                                case '┴':
                                    neo[y][x] = '└';
                                    break;
                            }
                            switch (neo[y][x - 1])
                            {
                                case '┼':
                                    neo[y][x - 1] = '┤';
                                    break;
                                case '├':
                                    neo[y][x - 1] = '│';
                                    break;
                                case '┬':
                                    neo[y][x - 1] = '┐';
                                    break;
                                case '┴':
                                    neo[y][x - 1] = '┘';
                                    break;

                            }
                        }
                    }
                }
            }
        }
        char[][] portion = new char[Height-2][Width-2];
        for(int i = 1; i < Height - 1; i++)
        {
            for(int j = 1; j < Width - 1; j++)
            {
                switch (neo[i][j])
                {
                    case '\1':
                        portion[i - 1][j - 1] = ' ';
                        break;
                    default: // ┼┌┘
                        portion[i - 1][j - 1] = neo[i][j];
                }
            }
        }
        return transposeLines(portion);
    }
    public static char[][] transposeLines(char[][] map)
    {

        int Width = map[0].length;
        int Height = map.length;
        char[][] portion = new char[Height][Width];
        for(int i = 0; i < Height; i++)
        {
            for(int j = 0; j < Width; j++)
            {
                switch (map[i][j])
                {
                    case '\1':
                        portion[i][j] = ' ';
                        break;
                    case '├':
                        portion[i][j] = '┬';
                        break;
                    case '┤':
                        portion[i][j] = '┴';
                        break;
                    case '┴':
                        portion[i][j] = '┤';
                        break;
                    case '┬':
                        portion[i][j] = '├';
                        break;
                    case '┐':
                        portion[i][j] = '└';
                        break;
                    case '└':
                        portion[i][j] = '┐';
                        break;
                    case '│':
                        portion[i][j] = '─';
                        break;
                    case '─':
                        portion[i][j] = '│';
                        break;
//                    case '├ ┤ ┴ ┬ ┌ ┐ └ ┘ │ ─':
                    default: // ┼┌┘
                        portion[i][j] = map[i][j];
                }
            }
        }
        return portion;
    }
}
