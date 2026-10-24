/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package thegame;

import java.util.Comparator;

/**
 *
 * @author www.b23prodtm.info
 */
public class RandomComparator<E> implements Comparator<E>  {
    public static int _getRandomInt(int min, int max) {
        return (int) Math.min(max, min + (int) Math.round(Math.random() * (double) (max - min)));
    }
    
    public int compare(E o1, E o2) {        
        int rand = _getRandomInt(-1, 1);
        return o1 != null && o2 != null ? o1.equals(o2) ? 0 : rand == 0 ? -1 : rand : -1;
    }
    
    long hash = System.nanoTime();
    public int hashCode() {
        return (int)hash;
    }
    public boolean equals(Object o) {
        return o == null ? false : o.hashCode() == hashCode();
    }
    
}
