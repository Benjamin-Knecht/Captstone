package utils;

/**
 * @author Benjamin Knecht
 * @description procedure interface which can throw and Exception
 */
public interface Procedure {

    /**
     * @throws Exception
     */
    void invoke() throws Exception;
}
