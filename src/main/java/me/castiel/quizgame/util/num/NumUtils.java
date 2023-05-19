package me.castiel.quizgame.util.num;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class NumUtils {

    public static <E> List<E> randomFromList(List<E> list, int num) {
        List<E> clone = new ArrayList<>(list);
        Collections.shuffle(clone);
        List<E> l = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            l.add(clone.get(i));
        }
        return l;
    }
}
