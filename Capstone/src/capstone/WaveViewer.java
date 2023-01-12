package capstone;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.function.Function;
import javax.swing.JPanel;
import utils.Utils;

/**
 * @author Benjamin Knecht
 * @description Class which paints JPanel (visualization of the Oscillators combined waveform)
 */
public class WaveViewer extends JPanel{
    private Oscillator[] oscillators;
    private boolean ANTIALIASING = true;
    Graphics2D graphics2D;
    /**
     * @description constructor with Oscillator array argument
     * @param oscillators Oscillator[] the array of Oscillators
     */
    public WaveViewer(Oscillator[] oscillators){
        this.oscillators = oscillators;
        setBorder(Utils.WindowDesign.LINE_BORDER);
    }

    /**
     * @description Overridden method for painting the JPanel
     * @param graphics Graphics Object
     */
    @Override
    public void paintComponent(Graphics graphics){
        final int PAD = 25;
        super.paintComponent(graphics);
        graphics2D = (Graphics2D)graphics;
        int numSamples = getWidth() - PAD * 2;
        double[] mixedSamples = new double[numSamples];
        if (ANTIALIASING == false)
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        else 
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        for(Oscillator oscillator : oscillators){
            if (oscillator.get_on_off()== true){ // don't include samples if on_off is off (false)
                double [] samples = oscillator.getSampleWavetable(numSamples);
                for (int i = 0; i< samples.length; ++i){
                    mixedSamples[i] += samples[i] / oscillators.length;
                }
            }
            
        }
        int midY = getHeight() / 2;
        Function<Double, Integer> sampleToYCoord = sample -> (int)(midY + sample * (midY - PAD));
        
        graphics2D.drawLine(PAD, midY, getWidth() - PAD, midY);
        graphics2D.drawLine(PAD, PAD, PAD, getHeight() - PAD);
        for (int i = 0; i < numSamples; ++i){
            int nextY = i == numSamples - 1 ? sampleToYCoord.apply(mixedSamples[i]) : sampleToYCoord.apply(mixedSamples[i+1]);
            graphics2D.drawLine(PAD + i, sampleToYCoord.apply(mixedSamples[i]), PAD + i + 1, nextY);
        }
    }
    public void setAntialiasing(){
        ANTIALIASING = ANTIALIASING != true;
    }
}
