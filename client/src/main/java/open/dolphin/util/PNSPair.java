package open.dolphin.util;

import java.util.List;
import java.util.Objects;

/**
 * PNSPair.
 * Generics に対応した open.dolphin.client.NameValuePair
 * @author pns
 * @param <N>
 * @param <V>
 */
public class PNSPair<N,V> implements Comparable<PNSPair<N,V>>{

    private N name;
    private V value;

    /**
     * Value を比較して，targetPair が pairList の何番目の要素であるかを返す.
     * int index = PNSPair.<String,Integer>getIndex(targetPair, targetList)
     * @param <N>
     * @param <V>
     * @param targetPair
     * @param pairList
     * @return
     */
    public static <N,V extends Comparable<V>> int getIndex(PNSPair<N,V> targetPair, List<PNSPair<N,V>> pairList) {
        return getIndex(targetPair.getValue(), pairList);
    }

    /**
     * Value を比較して targetValue が pairList の何番目の要素であるかを返す
     * int index = PNSPair.<String,Integer>getIndex(int, targetList)
     * @param <N>
     * @param <V>
     * @param targetVal
     * @param pairList
     * @return
     */
    public static <N,V extends Comparable<V>> int getIndex(V targetVal, List<PNSPair<N,V>> pairList) {
        int index = 0;
        for (int i = 0; i < pairList.size(); i++) {
            if (pairList.get(i).getValue().equals(targetVal)) {
                index = i;
                break;
            }
        }
        return index;
    }

    public PNSPair() {
    }

    public PNSPair(N n, V v) {
        this();
        name = n;
        value = v;
    }

    public void setValue(V v) {
        value = v;
    }

    public V getValue() {
        return value;
    }

    public void setName(N n) {
        name = n;
    }

    public N getName() {
        return name;
    }

    /**
     * name の文字列を返す
     * @return
     */
    @Override
    public String toString() {
        return name.toString();
    }

    @Override
    public int hashCode() {
        return value.hashCode() + 15;
    }


    /**
     * value を比較して同じなら equal とする
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PNSPair<?, ?> other = (PNSPair<?, ?>) obj;
        if (!Objects.equals(this.value, other.getValue())) {
            return false;
        }
        return true;
    }

    /**
     * value を比較して大小を返す
     * @param other
     * @return
     */
    @Override
    public int compareTo(PNSPair<N,V> other) {
        if (other != null) {
            V otherValue = other.getValue();
            if (value instanceof Comparable<?>) {
                return ((Comparable)value).compareTo(otherValue);
            }
        }
        return -1;
    }
}
