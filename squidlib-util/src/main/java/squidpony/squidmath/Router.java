package squidpony.squidmath;

/**
 * A small interface that provides a reroute() function to take an x,y position and get an int index for some alternate
 * indexing scheme into a 1D array or List. Example uses might be transposing x and y when looking them up in the other
 * scheme, or performing some offset or rotation to the position. Functions to generate Routers are provided in the
 * {@link Generator} nested class.
 * Created by Tommy Ettinger on 12/22/2016.
 */
public interface Router {
    int reroute(int x, int y);

    class Generator {
        private Generator(){}
        public static Router simple(final int width, final int height)
        {

            return new Router() {
                @Override
                public int reroute(int x, int y) {
                    return (x + y * width) % (width * height);
                }
            };
        }
        public static Router transpose(final int width, final int height)
        {
            return new Router() {
                @Override
                public int reroute(int x, int y) {
                    return (y + x * width) % (width * height);
                }
            };
        }
        public static Router rotate(final int width, final int height, final int rotations)
        {
            switch (rotations & 3) {
                case 0:
                    return new Router() {
                        @Override
                        public int reroute(int x, int y) {
                            return (x + y * width) % (width * height);
                        }
                    };
                case 1:
                    return new Router() {
                        @Override
                        public int reroute(int x, int y) {
                            return ((height - 1 - y) + x * width) % (width * height);
                        }
                    };
                case 2:
                    return new Router() {
                        @Override
                        public int reroute(int x, int y) {
                            return ((width - 1 - x) + (height - 1 - y) * width) % (width * height);
                        }
                    };
                default:
                    return new Router() {
                        @Override
                        public int reroute(int x, int y) {
                            return (y + (width - 1 - x) * width) % (width * height);
                        }
                    };
            }
        }
        public static Router flip(final int width, final int height, final boolean flipX, final boolean flipY)
        {
            if(flipX && flipY)
                return new Router() {
                    @Override
                    public int reroute(int x, int y) {
                        return ((width - 1 - x) + (height - 1 - y) * width) % (width * height);
                    }
                };
            if(flipX)
            {

                return new Router() {
                    @Override
                    public int reroute(int x, int y) {
                        return ((width - 1 - x) + y * width) % (width * height);
                    }
                };
            }
            if(flipY) {
                return new Router() {
                    @Override
                    public int reroute(int x, int y) {
                        return (x + (height - 1 - y) * width) % (width * height);
                    }
                };
            }
            return new Router() {
                @Override
                public int reroute(int x, int y) {
                    return (x + y * width) % (width * height);
                }
            };
        }
        public static Router chain(final int width, final int height, final Router first, final Router second)
        {
            return new Router() {
                @Override
                public int reroute(int x, int y) {
                    int t = first.reroute(x, y);
                    return second.reroute(t % width, t / width);
                }
            };
        }
    }
}
