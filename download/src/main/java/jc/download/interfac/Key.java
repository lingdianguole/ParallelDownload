package jc.download.interfac;

public interface Key {

    String STRING_CHARSET_NAME = "UTF-8";

    @Override
    boolean equals(Object o);

    @Override
    int hashCode();

    @Override
    String toString();
}
