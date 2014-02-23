/* blacken - a library for Roguelike games
 * Copyright © 2010, 2011 Steven Black <yam655@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package com.googlecode.blacken.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.RandomAccess;
import java.util.regex.*;

/**
 * An extended random class
 * 
 * @author yam655
 */
public class Random extends java.util.Random {
    
    private static final long serialVersionUID = 3049695947451276476L;
    private static Random instance = null;

    public static Random getInstance() {
        if (instance == null) {
            instance = new Random();
        }
        return instance;
    }
        
    private final Pattern guessPattern = 
        Pattern.compile("\\s*(\\d+)?\\s*(?:([:])\\s*(\\d+))??\\s*(?:([d:])\\s*(\\d+))?\\s*(?:([+-/*])\\s*(\\d+))?\\s*");
    /**
     * Create a new random number generator
     */
    public Random() {
        // empty
    }

    /**
     * @param arg0 random number seed
     */
    public Random(long arg0) {
        super(arg0);
    }

    /**
     * Find best <code>num</code> out of <code>outof</code> dice with 
     * <code>sides</code>.
     * 
     * @param num best number of dice to use
     * @param outof total number of dice to use
     * @param sides number of sides on the dice
     * @return sum of best <code>num</code> out of <em>outof</em><b>d</b><em>sides</em>
     */
    public int bestOf(int num, int outof, int sides) {
        // 
        if (num > outof) {
            int t = num;
            num = outof;
            outof = t;
        }
        ArrayList<Integer> arr = new ArrayList<>(outof);
        int ret = 0;
        while(outof > 0) {
            arr.add(dice(1, sides));
            outof -= 1;
        }
        Collections.sort(arr);
        while(num > 0) {
            ret += arr.get(arr.size()-1);
            num -= 1;
        }
        return ret;
    }
    /**
     * Find the best <code>num</code> numbers in the <code>group</code>
     * 
     * @param num number of integers to use
     * @param group group of integers to use
     * @return sum of best <code>num</code> integers in <code>group</code>
     */
    public int bestOf(int num, List<Integer> group) {
        int outof = group.size();
        if (num > outof) {
            throw new IllegalArgumentException();
        }
        ArrayList<Integer> arr = new ArrayList<>(group);
        int ret = 0;
        Collections.sort(arr);
        while(num > 0) {
            ret += arr.get(arr.size()-1);
            num -= 1;
        }
        return ret;
    }
    
    /**
     * Find the best <code>num</code> out of <code>outof</code> using the
     * guessed string <code>g</code>.
     * 
     * @param num number of integers to use
     * @param outof number of integers to check
     * @param g random string parsed using {@link #guess(String)}
     * @return sum of best <code>num</code> out of <code>outof</code> runs of <code>g</code>
     */
    public int bestOf(int num, int outof, String g) {
        if (num > outof) {
            int t = num;
            num = outof;
            outof = t;
        }
        ArrayList<Integer> arr = new ArrayList<>(outof);
        int ret = 0;
        while(outof > 0) {
            arr.add(guess(g));
            outof -= 1;
        }
        Collections.sort(arr);
        while(num > 0) {
            ret += arr.get(arr.size()-1);
            num -= 1;
        }
        return ret;
    }
    
    /**
     * Get a random choice from a List
     * 
     * <p>If the List does not support RandomAccess, it ends up calling
     * {@link #choice(Collection)}.</p>
     * 
     * @param <T> type contained in the List
     * @param c List from which to pull a random item
     * @return random item from List
     */
    public <T> T choice(List<T> c) {
        for (Class<?> cls : c.getClass().getInterfaces()) {
            if (cls.equals(RandomAccess.class)) {
                T ret = c.get((int)(nextFloat() * c.size()));
                return ret;
            }
        }
        return choice((Collection<T>)c);
    }
    
    /**
     * Get a random choice from a Collection
     * 
     * <p>This calls {@link Collection#toArray()} so if that is slow, this will
     * be slow.</p>
     * 
     * @param <T> type contained in the Collection
     * @param c Set from which to pull a random item
     * @return random item from Collection
     */
    public <T> T choice(Collection<T> c) {
        Object[] a = c.toArray();
        @SuppressWarnings("unchecked")
        T ret = (T)a[(int)(nextFloat() * a.length)];
        return ret;
    }

    /**
     * Emulate a dice roll and return the sum.
     * 
     * @param num number of dice to sum
     * @param sides number of sides on the dice
     * @return sum of dice
     */
    public int dice(int num, int sides) {
        int ret = 0;
        while(num > 0){
            ret += nextInt(sides) + 1;
            num -= 1;
        }
        return ret;
    }
    
    /**
     * Get all possible results from a set of dice rolls.
     * 
     * @param num number of dice used
     * @param sides number of sides on each die
     * @return list of results
     */
    public List<Integer> diceList(int num, int sides) {
        List<Integer> ret = new ArrayList<>(num);
        for (int c = 0; c < num; c++) {
            ret.add(nextInt(sides) + 1);
        }
        return ret;
    }
    /**
     * Guess the way to turn the string to a randomized number.
     * 
     * <p>We support the following types of strings:
     * <ul>
     *   <li>"42": simple absolute string</li>
     *   <li>"10:20": simple random range (inclusive between 10 and 20)</li>
     *   <li>"d6": synonym for "1d6"</li>
     *   <li>"3d6": sum of 3 6-sided dice</li>
     *   <li>"3:4d6": best 3 of 4 6-sided dice</li>
     * </ul></p>
     * 
     * <p>We support the following suffixes for the supported types:
     * <ul>
     *   <li> "+4": add 4 to the value
     *   <li> "-3": subtract 3 from the value
     *   <li> "*100": multiply value by 100
     *   <li> "/8": divide value by 8
     * </ul></p>
     * 
     * @param str string to guess
     * @return random number
     */
    public int guess(String str) {

        Matcher mat = guessPattern.matcher(str);
        int ret = 0;
        if (mat.matches()) {
            String num1 = mat.group(1); // 12
            String wmode = mat.group(2); // :
            String wnum = mat.group(3); // 23
            String mode = mat.group(4); // db
            String num2 = mat.group(5); // 34
            String pmode = mat.group(6); // +-
            String pnum = mat.group(7); // 45
            int a, b, w, p;
            a = num1 == null ? 0 : Integer.parseInt(num1);
            b = num2 == null ? 0 : Integer.parseInt(num2);
            w = wnum == null ? 0 : Integer.parseInt(wnum);
            p = pnum == null ? 0 : Integer.parseInt(pnum);
            if (num1 != null && num2 != null) {
                if (wnum != null) {
                    if (":".equals(wmode)) { 
                        if ("d".equals(mode)) { 
                            ret = bestOf(a, w, b);
                        }
                    }
                } else if ("d".equals(mode)) { 
                    ret = dice(a, b);
                } else if (":".equals(mode)) { 
                    ret = nextInt(a, b+1);
                }
            } else if (num1 != null) {
                if (":".equals(wmode)) { 
                    ret = nextInt(a, w+1);
                } else {
                    ret = a;
                }
            } else if (num2 != null) {
                if (mode != null) {
                    switch (mode) {
                        case "d":
                            ret = dice(1, b);
                            break;
                        case ":":
                            ret = nextInt(0, b+1);
                            break;
                    }
                }
            } else {
                if (":".equals(wmode)) { 
                    ret = nextInt(0, w+1);
                }
            }
            if (pmode != null) {
                switch (pmode) {
                    case "+":
                        ret += p;
                        break;
                    case "-":
                        ret -= p;
                        break;
                    case "*":
                        ret *= p;
                        break;
                    case "/":
                        ret /= p;
                        break;
                }
            }
        }
        return ret;
    }
    /**
     * Find the next integer within a range.
     * 
     * @param bottom inclusive bottom
     * @param top exclusive top
     * @return n where bottom <= n < top
     */
    public int nextInt(int bottom, int top) {
        int diff = top - bottom;
        if (diff == 0) {
            return bottom;
        }
        return bottom + nextInt(diff);
    }

    /**
     * Shuffle a List in-place.
     * 
     * <p>This just calls {@link Collections#shuffle(List, java.util.Random)}.</p>
     * 
     * @param <T> type contained in the List
     * @param list list to shuffle
     */
    public <T> void shuffle(List<T> list) {
        Collections.shuffle(list, this);
    }

}
