package utils;

/**
 *
 * @author Benjamin Knecht
 * @description refWrapper in order to wrap specific data types for use in Oscillator
 * @param <T> The data type
 */
public class RefWrapper<T> {
    /**
     * 
     */
    public T val;

    /**
     * @description wraps the variable
     * @param val data type T the variable to wrap
     */
    public RefWrapper(T val){
        this.val = val;
    }
}
