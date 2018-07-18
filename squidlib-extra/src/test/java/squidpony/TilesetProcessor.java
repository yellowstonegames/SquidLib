package squidpony;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import squidpony.squidgrid.mapping.styled.Maximums;
import squidpony.squidgrid.mapping.styled.OldConfig;
import squidpony.squidgrid.mapping.styled.OldTile;
import squidpony.squidgrid.mapping.styled.OldTileset;
import squidpony.squidmath.GreasedRegion;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
 * A reworking of smelC's earlier TilesetsGenerator that might be able to take advantage of newer SquidLib features.
 * Created by Tommy Ettinger on 12/4/2016.
 */
public class TilesetProcessor extends ApplicationAdapter {
    @Override
    public void create() {
        super.create();
        Json json = new Json(JsonWriter.OutputType.json);
        final String[] jss = {"caves_limit_connectivity.js", "caves_tiny_corridors.js", "corner_caves.js",
                "default_dungeon.js", "horizontal_corridors_v1.js", "horizontal_corridors_v2.js",
                "horizontal_corridors_v3.js", "limit_connectivity_fat.js", "limited_connectivity.js",
                "maze_2_wide.js", "maze_plus_2_wide.js", "open_areas.js", "ref2_corner_caves.js",
                "rooms_and_corridors_2_wide_diagonal_bias.js", "rooms_and_corridors.js",
                "rooms_limit_connectivity.js", "round_rooms_diagonal_corridors.js", "simple_caves_2_wide.js",
                "square_rooms_with_random_rects.js"};

        for(String js : jss)
        {
            FileHandle fh = Gdx.files.classpath(js);
            OldTileset ts = json.fromJson(OldTileset.class, fh);

            		/* Now dump 'ts' as a Java class */
            final StringBuilder java = new StringBuilder();
            appendln(java,
                    "/* File generated automatically by TilesetsGenerator.java. Do not edit. This file is committed for convenience. */");
            appendln(java, "package squidpony.tileset;");
            appendln(java, "");
            appendln(java, "import squidpony.squidgrid.mapping.styled.*;");
            appendln(java, "");
            final String fixed = fixFileName(fh.nameWithoutExtension()), javaFileName = fixed + ".java";
            appendln(java, "/** @author TilesetsGenerator.java */");
            appendln(java, "public class " + fixed + " {");
            appendln(java, "");
            appendln(java, "  public static final Tileset INSTANCE = new Tileset();");
            appendln(java, "");
            appendln(java, "  static {");
            appendln(java, 4, "/* Initialize #INSTANCE */ ");
            try {
                initalizeTopLevel(java, "config", ts.config, ts);
                initalizeTopLevel(java, "max_tiles", ts.max_tiles, ts);
            } catch (Throwable e) {
                errLog("Could not write " + javaFileName, e);
                return;
            }
            initializeTileArray(java, "h_tiles", ts.h_tiles);
            initializeTileArray(java, "v_tiles", ts.v_tiles);
            appendln(java, "  }");
            appendln(java, "");
            java.append("}");
            writeToDisk(java, fixed + ".java");
        }

        Gdx.app.exit();
    }

    public static void main(String[] args) {
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        new HeadlessApplication(new squidpony.TilesetProcessor(), config);
    }

    private static void initializeTileArray(StringBuilder java, String fieldName, OldTile[] ts_tiles) {
        final int len = ts_tiles.length;
		/* Initialize array */
        appendln(java, 4, "INSTANCE." + fieldName + " = new Tile[" + len + "];");
        for (int i = 0; i < len; i++) {
            final OldTile source = ts_tiles[i];
			/* Fill temporary variable to copy Tile.data */
            appendln(java, "    /* Build " + fieldName + " #" + i + " */");

            java.append("    INSTANCE.").append(fieldName).append("[").append(i).append("] =");
            /* Build new Tile */
            java.append(" new Tile(");
            java.append(source.a_constraint);
            java.append(',');
            java.append(source.b_constraint);
            java.append(',');
            java.append(source.c_constraint);
            java.append(',');
            java.append(source.d_constraint);
            java.append(',');
            java.append(source.e_constraint);
            java.append(',');
            java.append(source.f_constraint);
            java.append('\n');
            GreasedRegion gr = new GreasedRegion(source.data, '.');
            appendln(java, 4,"," + gr.serializeToString() + ");");
        }
    }

    private static void initalizeTopLevel(StringBuilder java, String fieldName, OldConfig c, OldTileset ts)
            throws IllegalArgumentException, IllegalAccessException {
        appendln(java, 4, "INSTANCE." + fieldName + ".is_corner=" + c.is_corner + ";");
        appendln(java, 4, "INSTANCE." + fieldName + ".num_x_variants=" + c.num_x_variants + ";");
        appendln(java, 4, "INSTANCE." + fieldName + ".num_y_variants=" + c.num_y_variants + ";");
        appendln(java, 4, "INSTANCE." + fieldName + ".short_side_length=" + c.short_side_length + ";");
        appendln(java, 4, "INSTANCE." + fieldName + ".num_colors[0]=" + c.num_color_0 + ";");
        appendln(java, 4, "INSTANCE." + fieldName + ".num_colors[1]=" + c.num_color_1 + ";");
        appendln(java, 4, "INSTANCE." + fieldName + ".num_colors[2]=" + c.num_color_2 + ";");
        appendln(java, 4, "INSTANCE." + fieldName + ".num_colors[3]=" + c.num_color_3 + ";");
    }

    private static void initalizeTopLevel(StringBuilder java, String fieldName, Maximums c, OldTileset ts)
            throws IllegalArgumentException, IllegalAccessException {
        appendln(java, 4, "INSTANCE." + fieldName + ".h=" + c.h + ";");
        appendln(java, 4, "INSTANCE." + fieldName + ".v=" + c.v + ";");
    }

    private static void writeToDisk(StringBuilder java, String filename) {
        final String dest = "generated/" + filename; //"squidlib-util/src/main/java/squidpony/tileset"
        final PrintWriter pw;
        try {
            pw = new PrintWriter(dest);
        } catch (FileNotFoundException e) {
            errLog("Cannot write " + filename + " to disk", e);
            return;
        }
        pw.append(java);
        pw.flush();
        pw.close();
        infoLog("Written " + dest);
    }

    /**
     * @param s An extension-less filename.
     * @return a name suitable for a Java class
     */
    private static String fixFileName(String s) {
        String result = "";
        final int bound = s.length();
        boolean upperNext = false;
        for (int i = 0; i < bound; i++) {
            final char c = s.charAt(i);
            if (c == '_')
                upperNext = true;
            else {
                result += (i == 0 || upperNext) ? Character.toUpperCase(c) : c;
                upperNext = false;
            }
        }
        return result;
    }

    private static StringBuilder appendln(StringBuilder buf, int indent, String s) {
        for (int i = 0; i < indent; i++)
            buf.append(' ');
        appendln(buf, s);
        return buf;
    }

    private static StringBuilder appendln(StringBuilder buf, String s) {
        buf.append(s);
        buf.append('\n');
        return buf;
    }

    private static void errLog(String msg, Throwable e) {
        if (msg != null)
            Gdx.app.error(SquidTags.GENERATION, msg);
        Gdx.app.error(SquidTags.GENERATION, e.toString() + e.getMessage());
    }

    private static void errLog(String msg) {
        Gdx.app.error(SquidTags.GENERATION, msg);
    }

    private static void infoLog(String msg) {
        Gdx.app.log(SquidTags.GENERATION, msg);
    }

}

