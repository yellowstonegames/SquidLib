package squidpony.panel;

/**
 * Created by Tommy Ettinger on 1/23/2016.
 */
public interface IMarkup<T> {
    String getMarkup(T value);
    String closeMarkup();
    class StringMarkup implements IMarkup<String>
    {
        @Override
        public String getMarkup(String value) {
            return "[" + value + "]";
        }

        @Override
        public String closeMarkup() {
            return "[]";
        }
    }
}
