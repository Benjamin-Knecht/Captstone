package capstone;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.function.Function;
import javax.swing.JPanel;
import utils.Utils;

/**
 * @author Benjamin Knecht
 * @description Class which paints JPanel (visualization of the Oscillators individual waveforms)
 */
public class WaveViewerSeperated extends JPanel{
    private Oscillator[] oscillators;
    private boolean ANTIALIASING = true;
    Graphics2D graphics2D;
    double [] samples1;
    double [] samples2;
    double [] samples3;

   /**
     * @description constructor with Oscillator array argument
     * @param oscillators Oscillator[] the array of Oscillators
     */
    public WaveViewerSeperated(Oscillator[] oscillators){
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
        if (ANTIALIASING == false)
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        else 
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setColor(Color.white);
        int j = 1;
        for(Oscillator oscillator : oscillators){
            if (oscillator.get_on_off()== true){ // don't include samples if on_off is off (false)
                if (j==1)
                    samples1 = oscillator.getSampleWavetableSeperated(numSamples,1);
                if (j==2)
                    samples2 = oscillator.getSampleWavetableSeperated(numSamples,2);
                if (j==3)
                    samples3 = oscillator.getSampleWavetableSeperated(numSamples,3);
            }
            else{
                if (j==1)
                    samples1 = oscillator.getEmptySampleWavetable(numSamples);
                if (j==2)
                    samples2 = oscillator.getEmptySampleWavetable(numSamples);
                if (j==3)
                    samples3 = oscillator.getEmptySampleWavetable(numSamples);
            }
            j++;
        }
        int midY = getHeight() / 2;
        Function<Double, Integer> sampleToYCoord = sample -> (int)(midY + sample * (midY - PAD));
        
        graphics2D.drawLine(PAD, midY, getWidth() - PAD, midY);
        graphics2D.drawLine(PAD, PAD, PAD, getHeight() - PAD);
        for (int i = 0; i < numSamples; ++i){
            graphics2D.setColor(Color.magenta);
            int nextY1 = i == numSamples - 1 ? sampleToYCoord.apply(samples1[i]) : sampleToYCoord.apply(samples1[i+1]);
            graphics2D.drawLine(PAD + i, sampleToYCoord.apply(samples1[i]), PAD + i + 1, nextY1);
            
            graphics2D.setColor(Color.cyan);
            int nextY2 = i == numSamples - 1 ? sampleToYCoord.apply(samples2[i]) : sampleToYCoord.apply(samples2[i+1]);
            graphics2D.drawLine(PAD + i, sampleToYCoord.apply(samples2[i]), PAD + i + 1, nextY2);
            
            graphics2D.setColor(Color.yellow);
            int nextY3 = i == numSamples - 1 ? sampleToYCoord.apply(samples3[i]) : sampleToYCoord.apply(samples3[i+1]);
            graphics2D.drawLine(PAD + i, sampleToYCoord.apply(samples3[i]), PAD + i + 1, nextY3);
        }
    }
    public void setAntialiasing(){
        ANTIALIASING = ANTIALIASING != true;
    }
}