package com.molotkov;

import java.util.Iterator;
import java.util.function.BiConsumer;

public class Utils {

    public static <T1, T2> void iterateSimultaneously(final Iterable<T1> c1, final Iterable<T2> c2, final BiConsumer<T1, T2> consumer) {
        final Iterator<T1> i1 = c1.iterator();
        final Iterator<T2> i2 = c2.iterator();
        while (i1.hasNext() && i2.hasNext()) {
            consumer.accept(i1.next(), i2.next());
        }
    }
}
