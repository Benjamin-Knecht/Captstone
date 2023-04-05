package utils;

import capstone.SynthControlContainer;
import java.awt.AWTException;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.swing.BorderFactory;
import javax.swing.border.Border;

import static java.lang.Math.*;

/**
 * @author Benjamin Knecht
 * @description utility class for procedure handling,
 * UI element handling,
 * and Static Math functions
 */
public class Utils {

    /**
     * @description method that handles and catches exceptions for the procedure
     * @param procedure the procedure to handle
     * @param printStackTrace printStackTrace
     */
    public static void handleProcedure(Procedure procedure, boolean printStackTrace){
        try{
            procedure.invoke();
        }
        catch (Exception e){
            if (printStackTrace){
                e.printStackTrace();
            }
        }
    }

    /**
     *
     */
    public static class ParameterHandling{
        /**
         *
         */
        public static final Robot PARAMETER_ROBOT;
        static{
            try{
                PARAMETER_ROBOT = new Robot();
            }
            catch(AWTException e){
                throw new ExceptionInInitializerError("Cannot construct robot instance");
            }
        }
        
        private ParameterHandling(){}
        
        /**
         * @param component Component object
         * @param container SynthControlContainer object
         * @param minVal int minimum for the value to go to
         * @param maxVal int maximum for the value to go to
         * @param valStep int the step of the mouseListener to go by
         * @param parameter RefWrapper<Integer> object
         * @param onChangeProcedure Procedure object
         */
        public static void addParameterMouseListener(Component component, SynthControlContainer container, int minVal, int maxVal, int valStep, RefWrapper<Integer> parameter, Procedure onChangeProcedure){
            component.addMouseListener(new MouseAdapter(){
                @Override
                public void mousePressed(MouseEvent e){
                    final Cursor BLANK_CURSOR = Toolkit.getDefaultToolkit().createCustomCursor(new BufferedImage(16,16,BufferedImage.TYPE_INT_ARGB), new Point(0,0), "blank_cursor");
                    component.setCursor(BLANK_CURSOR);
                    container.setMouseClickLocation(e.getLocationOnScreen());
                }
                @Override
                public void mouseReleased(MouseEvent e){
                    component.setCursor(Cursor.getDefaultCursor());
                }
            });
            component.addMouseMotionListener(new MouseAdapter(){
            @Override
            public void mouseDragged(MouseEvent e){
                if (container.getMouseLocation().y != e.getYOnScreen()){
                    boolean mouseMovingUp = container.getMouseLocation().y - e.getYOnScreen() > 0;
                    if (mouseMovingUp && parameter.val < maxVal){
                        parameter.val += valStep;
                    }
                    else if(!mouseMovingUp && parameter.val > minVal){
                       parameter.val -= valStep;
                    }
                    if (onChangeProcedure != null){
                        handleProcedure(onChangeProcedure, true);
                    }
                    PARAMETER_ROBOT.mouseMove(container.getMouseLocation().x, container.getMouseLocation().y);
                }
            }
        });
        }
    }

    /**
     * @description static class for obtaining default window design border
     */
    public static class WindowDesign{
        /**
         * @usage example in Oscillator: setBorder(Utils.WindowDesign.LINE_BORDER);
         */
        public static final Border LINE_BORDER = BorderFactory.createLineBorder(Color.BLACK);
    }

    /**
     * @description static class with Math related functions
     */
    public static class Math{
        
        /**
         * @description function to offset the tone of the oscillator
         * @param baseFrequency double the base frequency
         * @param frequencyMultiplier double the multiplier to change the frequency by
         * @return double the offset frequency
         */
        public static double offsetTone(double baseFrequency, double frequencyMultiplier){
            return baseFrequency * pow(2.0,frequencyMultiplier);
        }

        /**
         * @description function to retrieve the angular frequency given the frequency
         * @param freq double the frequency
         * @return double the angular frequency of the frequency
         */
        public static double frequencyToAngularFrequency(double freq){
            return 2.0 * PI * freq;
        }

        /**
         * @description Synthesizer function to obtain the frequency of a piano key number
         * @param keyNum int the piano key number to calculate the frequency for
         * @return double the frequency of that key
         */
        public static double getKeyFrequency(int keyNum){
            return pow(root(2,12), keyNum - 49) * 440;
        }

        /**
         * @description specific function to root a number by a root by pow(E, log(num)/root)
         * @param num double number to root
         * @param root double root
         * @return double result
         */
        public static double root(double num, double root){
            return pow(E, log(num)/root);
        }
    }
}
