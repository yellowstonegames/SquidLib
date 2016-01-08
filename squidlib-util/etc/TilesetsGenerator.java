package squidpony.tileset;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;

import com.google.gson.Gson;

import squidpony.squidgrid.mapping.styled.Tile;
import squidpony.squidgrid.mapping.styled.Tileset;

/**
 * Class that generates the other {@code .java} files in this package. This is
 * required to be compatible with GWT (for libgdx's html5 backend) as it avoids
 * to load .js files from disk in {@code DungeonBoneGen} (this is impossible in
 * GWT, because Gson isn't GWT-compatible). As a side effect, it removes
 * SquidLib's runtime dependency on Gson. Gson is only required to run this
 * class.
 * 
 * <p>
 * In Eclipse, I execute this class by right-clicking on this class and
 * selecting 'Run as > Java Application'. It works without further setup.
 * Generated .java files are put in the adequate location, i.e. close to this
 * file.
 * </p>
 * 
 * </p>
 * 
 * @author smelC
 */
public class TilesetsGenerator {

	private static final String EOL = System.getProperty("line.separator");

	public static void main(String[] args) {
		final String[] jss = {"etc/caves_limit_connectivity.js", "etc/caves_tiny_corridors.js", "etc/corner_caves.js",
				"etc/default_dungeon.js", "etc/horizontal_corridors_v1.js", "etc/horizontal_corridors_v2.js",
				"etc/horizontal_corridors_v3.js", "etc/limit_connectivity_fat.js", "etc/limited_connectivity.js",
				"etc/maze_2_wide.js", "etc/maze_plus_2_wide.js", "etc/open_areas.js", "etc/ref2_corner_caves.js",
				"etc/rooms_and_corridors_2_wide_diagonal_bias.js", "etc/rooms_and_corridors.js",
				"etc/rooms_limit_connectivity.js", "etc/round_rooms_diagonal_corridors.js", "etc/simple_caves_2_wide.js",
				"etc/square_rooms_with_random_rects.js"};
		main0(jss);
	}

	/**
	 * @param args
	 *            The {@code .js} files containing {@link Tileset}s.
	 */
	private static void main0(String[] args) {
		final Gson gson = new Gson();
		for (String arg : args) {
			if (!arg.endsWith(".js")) {
				errLog("File " + arg
						+ " doesn't look like a gson file (expected extension: .js). Skipping it.");
				continue;
			}
			final int slashIdx = arg.lastIndexOf('.');
			final String gsonFileName = slashIdx < 0 ? arg : arg.substring(0, slashIdx);
			generate(gson, TilesetsGenerator.class.getResourceAsStream("/" + arg),
					gsonFileNametoJavaFileName(gsonFileName));
		}
	}

	/**
	 * @param is
	 *            A stream to a gson file. Closed by this method.
	 * @param javaClassName
	 *            The name of the class to generate
	 */
	private static void generate(Gson gson, InputStream is, String javaClassName) {
		/* This closes 'is' */
		final String content = stringifyStream(is);
		final Tileset ts = gson.fromJson(content, Tileset.class);

		/* Now dump 'ts' as a Java class */
		final StringBuilder java = new StringBuilder();
		appendln(java,
				"/* File generated automatically by TilesetsGenerator.java. Do not edit. This file is committed for convenience. */");
		appendln(java, "package squidpony.tileset;");
		appendln(java, "");
		appendln(java, "import squidpony.squidgrid.mapping.styled.*;");
		appendln(java, "");
		final String javaFileName = javaClassName + ".java";
		appendln(java, "/** @author TilesetsGenerator.java */");
		appendln(java, "public class " + javaClassName + " {");
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
		appendln(java, 4, "Tile t = null;");
		appendln(java, 4, "String[] data = null;");
		initializeTileArray(java, "h_tiles", ts.h_tiles, ts);
		initializeTileArray(java, "v_tiles", ts.v_tiles, ts);
		appendln(java, "  }");
		appendln(java, "");
		java.append("}");
		writeToDisk(java, javaClassName + ".java");
	}

	private static void initializeTileArray(StringBuilder java, String fieldName, Tile[] ts_tiles,
			Tileset ts) {
		final int len = ts_tiles.length;
		/* Initialize array */
		appendln(java, 4, "INSTANCE." + fieldName + " = new Tile[" + len + "];");
		for (int i = 0; i < len; i++) {
			final Tile source = ts_tiles[i];
			/* Fill temporary variable to copy Tile.data */
			appendln(java, "    /* Build " + fieldName + " #" + i + " */");
			appendln(java, 4, "data = new String[" + source.data.length + "];");
			for (int j = 0; j < source.data.length; j++) {
				appendln(java, 4, "data[" + j + "] = \"" + source.data[j] + "\";");
			}
			/* Build new Tile */
			java.append("    t = new Tile(");
			java.append(source.a_constraint);
			java.append(", ");
			java.append(source.b_constraint);
			java.append(", ");
			java.append(source.c_constraint);
			java.append(", ");
			java.append(source.d_constraint);
			java.append(", ");
			java.append(source.e_constraint);
			java.append(", ");
			java.append(source.f_constraint);
			appendln(java, ", data);");
			appendln(java, 4, "INSTANCE." + fieldName + "[" + i + "] = t;");
		}
	}

	private static void initalizeTopLevel(StringBuilder java, String fieldName, Object c, Tileset ts)
			throws IllegalArgumentException, IllegalAccessException {
		for (Field field : c.getClass().getFields())
			appendln(java, 4, "INSTANCE." + fieldName + "." + field.getName() + "=" + field.get(c) + ";");
	}

	private static void writeToDisk(StringBuilder java, String filename) {
		final String dest = "squidlib-util/src/main/java/squidpony/tileset/" + filename;
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
	 * @param s
	 *            An extension less filename.
	 */
	private static String gsonFileNametoJavaFileName(String s) {
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

	/* A Tommy Ettinger © trick coming from DungeonBoneGen. Handy! */
	/**
	 * @param is
	 * @return The content of {@©ode is} as a {@link String}.
	 */
	private static String stringifyStream(InputStream is) {
		java.util.Scanner s = new java.util.Scanner(is);
		s.useDelimiter("\\A");
		String nx = s.hasNext() ? s.next() : "";
		s.close();
		return nx;
	}

	private static StringBuilder appendln(StringBuilder buf, int indent, String s) {
		for (int i = 0; i < indent; i++)
			buf.append(' ');
		appendln(buf, s);
		return buf;
	}

	private static StringBuilder appendln(StringBuilder buf, String s) {
		buf.append(s);
		buf.append(EOL);
		return buf;
	}

	private static void errLog(String msg, Throwable e) {
		if (msg != null)
			System.err.println(msg);
		e.printStackTrace(System.err);
	}

	private static void errLog(String msg) {
		System.err.println(msg);
	}

	private static void infoLog(String msg) {
		System.out.println(msg);
	}
}
