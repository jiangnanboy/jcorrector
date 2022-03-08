package sy.core.spelling;


/**
 * @author sy
 * @date 2022/2/25 22:34
 */
public class SpellingEntry<E, T> {
    private E word;
    private T freq;

    public SpellingEntry(E word, T freq) {
        this.word = word;
        this.freq = freq;
    }
    public E getWord() {
        return word;
    }

    public void setWord(E word) {
        this.word = word;
    }

    public T getFreq() {
        return freq;
    }

    public void setFreq(T freq) {
        this.freq = freq;
    }




}
