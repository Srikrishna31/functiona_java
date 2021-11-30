package com.util;


import java.util.ArrayList;
import java.util.Collections;

import com.functional.Effect;
import com.functional.Function;
import com.functional.TailCall;

import static com.functional.TailCall.ret;
import static com.functional.TailCall.sus;
/**
 * A functional list that supports all the functional operations on a list.
 */
public class List<T> {
    /**
     * Returns the first element of the list. If the list is empty it throws exception.
     * @param list
     * @param <T>
     * @return
     */
    public static <T> T head(java.util.List<T> list) {
        if (list.isEmpty()) {
            throw new IllegalStateException("head of empty list");
        }

        return list.get(0);
    }

    private static <T> java.util.List<T> copy(java.util.List<T> ts) {
        return new ArrayList<>(ts);
    }

    /**
     * Returns the tail of the list - removing the first element. Note that this
     * function returns a copy of the input list. The runtime is O(n).
     * @param list
     * @param <T>
     * @return
     */
    public static<T> java.util.List<T> tail(java.util.List<T> list) {
        if (list.isEmpty()) {
            throw new IllegalStateException("tail of empty list");
        }

        java.util.List<T> workList = copy(list);
        workList.remove(0);
        return Collections.unmodifiableList(workList);
    }

    /**
     * A general purpose function which can be used to turn the list into any other type.
     * This function operates on the list from left to right, applying the accumulating operator
     * on the left, and the accumulating value on the right(for each element of the list).
     * @param ts: List of values which need to be folded into another type.
     * @param identity : The identity of the operation. This will be returned if the
     *                 input list is empty.
     * @param f : Accumulating function, which is a curried function, which accepts a parameter of type U, and
     *          returns a function that accepts a parameter of type T and returns a U.
     * @param <T> : Type of list elements.
     * @param <U> : The type of reduction element.
     * @return the reduced type object.
     */
    public static <T,U> U foldLeft(java.util.List<T> ts, U identity, Function<U, Function<T, U>> f) {
        class FoldHelper {
            TailCall<U> go(java.util.List<T> ts, U acc) {
                if (ts.isEmpty()) {
                    return ret(acc);
                } else {
                    return sus(() -> go(tail(ts), f.apply(acc).apply(head(ts))));
                }
            }
        }

        return new FoldHelper().go(ts, identity).eval();
    }

    /**
     * Another general purpose function which can be used to turn the list into any other type.
     * This function operates on the list from right to left, applying the accumulating operator
     * on the right, and the accumulating value on the left(for each element of the list).
     * @param ts: List of values which need to be folded into another type.
     * @param identity : The identity element of the operation. This will be returned if the
     *                 input list is empty.
     * @param f : Accumulationg function that takes a parameter of type T, and returns a function
     *          that takes a parameter of type U and returns a U.
     * @param <T> : The type of elements in the list.
     * @param <U> : The type of reduction element.
     * @return the reduced type object.
     */
    public static <T,U> U foldRight(java.util.List<T> ts, U identity, Function<T, Function<U, U>> f) {
        class FoldHelper {
            TailCall<U> go(java.util.List<T> ts, U acc) {
                return ts.isEmpty() ? ret(acc) :
                        sus(() -> go(tail(ts), f.apply(head(ts)).apply(acc)));
            }
        }

        return new FoldHelper().go(reverse(ts), identity).eval();
    }

    /**
     * Append an element to the end of list. This copies the input list, and then adds an element
     * at the end, and returns the copy.
     * @param list : input list to which an element should be appended
     * @param t : element to be appended
     * @param <T> : The type of elements in the list.
     * @return the appended list.
     */
    public static <T> java.util.List<T> append(java.util.List<T> list, T t) {
        java.util.List<T> ts = copy(list);
        ts.add(t);

        return Collections.unmodifiableList(ts);
    }

    /**
     * This is dual of append function. This function prepends an element in front of the
     * input list.
     * @param t : element to be prepended.
     * @param list : input list to which an element should be prepended
     * @param <T> : the type of elements in the list.
     * @return the prepended list.
     */
    public static <T> java.util.List<T> prepend(T t, java.util.List<T> list) {
        return foldLeft(list, java.util.List.of(t), a -> b -> append(a, b));
    }

    /**
     * This function reverses the input list. It returns a copy of the input list.
     * The runtime of this function is O(n2). Be careful when applying to large lists.
     * @param ts : the list to be reversed.
     * @param <T> : the type of the list elements.
     * @return the reversed list.
     */
    public static <T> java.util.List<T> reverse(java.util.List<T> ts) {
        return foldLeft(ts, java.util.List.of(), a -> b -> prepend(b, a));
    }

    /**
     * This function maps a list of type T to a list of type U. It uses foldLeft to
     * achieve its result.
     * @param ts : List of elements to be transformed.
     * @param f : function to be applied to each element of the list.
     * @param <T> : Type parameter of input list.
     * @param <U> : Type parameter of output list.
     * @return the transformed list.
     */
    public static <T,U> java.util.List<U> mapWithFoldLeft(java.util.List<T> ts, Function<T, U> f) {
        return foldLeft(ts, java.util.List.<U>of(), acc -> t -> append(acc, f.apply(t)));

    }

    /**
     * This function maps a list of type T to a list of type U. It uses foldRight to
     * achieve its result.
     * @param ts : List of elements to be transformed.
     * @param f : function to be applied to each element of the list.
     * @param <T> : Type parameter of input list.
     * @param <U> : Type parameter of output list.
     * @return the transformed list.
     */
    public static <T,U> java.util.List<U> mapWithFoldRight(java.util.List<T> ts, Function<T, U> f) {
        return foldRight(ts, java.util.List.<U>of(), t -> acc -> prepend(f.apply(t), acc));
    }

    /**
     * This is a convenience shortcut function for mapping a list. It is same as mapWithFoldLeft
     * or mapWithFoldRight. All it does is delegate to mapWithFoldLeft. The choice of using mapWithFoldLeft is
     * completely arbitrary. If one were to use mapWithFoldRight in the implementation, the results would be
     * identical.
     * @param ts : List of elements to be transformed.
     * @param f : function to be applied to each element of the list.
     * @param <T> : Type parameter of input list.
     * @param <U> : Type parameter of output list.
     * @return the transformed object.
     */
    public static <T,U> java.util.List<U> map(java.util.List<T> ts, Function<T, U> f) {
        return mapWithFoldLeft(ts, f);
    }

    /**
     * This function is analogous to map function, but allows to apply sideffect to each element of the list.
     * It can be used to log the results or send over a network or any other side effecting operation.
     * @param ts : List of elements for which to apply the sideeffect.
     * @param e : the side effecting function.
     * @param <T> : Type parameter of the List and the effect function.
     */
    public static <T> void forEach(java.util.List<T> ts, Effect<T> e) {
        for(T t : ts) {
            e.apply(t);
        }
    }

    /**
     * A generic function, helpful to generate a list of values, based on the start and end values, and the
     * predicate. It is called unfold, since it starts out with a seed value, and incrementally (corecursively)
     * builds up the subsequent values based on a condition.
     * @param seed : Starting value to be inserted in the list.
     * @param f : A function that generates the next element based on the current element.
     * @param p : A predicate which returns a boolean, given the current value of type T.
     * @param <T> : The type parameter of the elements in the list.
     * @return the constructed list of elements.
     */
    public static <T> java.util.List<T> unfold(T seed, Function<T, T> f, Function<T, Boolean> p) {
        java.util.List<T> res = new ArrayList<>();
        var temp = seed;
        while(p.apply(temp)) {
            res = append(res, temp);
            temp = f.apply(temp);
        }

        return res;
    }

    public static java.util.List<Integer> range(Integer start, Integer end) {
        return unfold(start, x -> x + 1, x -> x < end);
    }
}
