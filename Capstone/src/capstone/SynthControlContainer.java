package capstone;

import java.awt.Component;
import java.awt.Point;
import javax.swing.JPanel;

/**
 *
 * @author Benjamin Knecht
 * @description the control container which Oscillator extends 
 * which accesses variables from Synthesizer
 * and allows JLabels to be added for each Oscillator
 */
public class SynthControlContainer extends JPanel{

    /**
     *
     */
    protected Point mouseClickLocation;
    private Synthesizer synth;

    /**
     *
     */
    protected boolean on;
    
    /**
     * @description Constructor with one Synthesizer argument
     * @param synth Synthesizer object to set the local variable as
     */
    public SynthControlContainer(Synthesizer synth){
        this.synth = synth;
    }
    
    /**
     * @description obtain the location of the mouse location
     * @return Point the location of the mouse
     */
    public Point getMouseLocation(){
        return mouseClickLocation;
    }

    /**
     *@description set the location of the mouse
     * @param mouseClickLocation Point to set the mouse location at
     */
    public void setMouseClickLocation(Point mouseClickLocation){
        this.mouseClickLocation = mouseClickLocation;
    }
/*
    public boolean ison(){
     return on;   
    }

    public void getOn(boolean on){
        this.on = on;
    }
*/
    /**
     * @param component the component to be added to the JPanel
     * @return the component added
     */
    @Override
    public Component add(Component component){
        component.addKeyListener(synth.getKeyAdapter());
        return super.add(component);
    }

    /**
     *
     * @param component
     * @param index
     * @return
     */
    @Override
    public Component add(Component component, int index){
        component.addKeyListener(synth.getKeyAdapter());
        return super.add(component,index);
    }

    /**
     *
     * @param name
     * @param component
     * @return
     */
    @Override
    public Component add(String name, Component component){
        component.addKeyListener(synth.getKeyAdapter());
        return super.add(name, component);
    }

    /**
     *
     * @param component
     * @param constraints
     */
    @Override
    public void add(Component component, Object constraints){
        component.addKeyListener(synth.getKeyAdapter());
        super.add(component, constraints);
    }

    /**
     *
     * @param component
     * @param constraints
     * @param index
     */
    @Override
    public void add(Component component, Object constraints, int index){
        component.addKeyListener(synth.getKeyAdapter());
        super.add(component, constraints, index);
    }
    
}
