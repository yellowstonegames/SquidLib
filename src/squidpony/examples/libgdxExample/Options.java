package squidpony.examples.libgdxExample;

import java.util.Scanner;

/**
 * This singleton holds all user based options.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public enum Options {

    INSTANCE;
    public boolean debug = true;
    public boolean autoUseResources = true;

    /**
     * Uses the provided scanner to read in the values for the desired options.
     *
     * @param scan
     */
    public void loadOptions(Scanner scan) {
        String line;
        Scanner lineScan;
        while (scan.hasNext()) {
            line = scan.nextLine();
            lineScan = new Scanner(line);
            switch (lineScan.next()) {
                case "debug":
                    debug = lineScan.nextBoolean();
                    break;
                case "autoUseResources":
                    autoUseResources = lineScan.nextBoolean();
                    break;
            }
        }
    }
}
