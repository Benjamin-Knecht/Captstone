package capstone;

import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.concurrent.ThreadLocalRandom;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import utils.RefWrapper;
import utils.Utils;

/**
 * @author Benjamin Knecht
 * @description the Oscillator class handles related information for the Oscillators including:
 * getter methods getVolumeMultiplier(), getToneOffset(), and getter for sample arrays for the Oscillators
 * setter methods setKeyFrequency(), applyToneOffset(), setVolumeMultiplier()
 */
public class Oscillator extends SynthControlContainer{
    private static final int TONE_OFFSET_LIMIT = 2000;
    public Wavetable wavetable = Wavetable.Sine;
    private double keyFrequency;
    private int wavetableStepSize = 0;
    private int wavetableIndex = 0;
    private RefWrapper<Integer> toneOffset = new RefWrapper<>(0);
    private RefWrapper<Integer> volume = new RefWrapper<>(100);
    private boolean on_off = true;
    private int cycles = 3;
    
    /**
     * @description Empty constructor for Oscillator
     * super goes to:
     * public SynthControlContainer(Synthesizer synth){
        this.synth = synth;
        }
     */
    public Oscillator() {
        super(null);
    }

    /**
     * @description Constructor for Oscillator with one argument
     * @param synth the Synthesizer Object to be used by Oscillator
     */
    public Oscillator(Synthesizer synth){
        super(synth);
        JComboBox<Wavetable> combobox = new JComboBox<>(new Wavetable[] {Wavetable.Sine, Wavetable.Square, Wavetable.Saw, Wavetable.Triangle});
        combobox.setSelectedItem(Wavetable.Sine);
        combobox.setBounds(10,10,75,25);
        combobox.addItemListener(l->
        {
            if (l.getStateChange() == ItemEvent.SELECTED){
                wavetable = (Wavetable) l.getItem();
            }
            synth.updateWaveviewer();
        });

        add(combobox);
        
        JLabel toneParameter = new JLabel("x0.00");
        toneParameter.setBounds(165,65,50,25);
        toneParameter.setBorder(Utils.WindowDesign.LINE_BORDER);
        Utils.ParameterHandling.addParameterMouseListener(toneParameter, this, -TONE_OFFSET_LIMIT, TONE_OFFSET_LIMIT, 1, toneOffset, () ->{
            applyToneOffset();
            toneParameter.setText(" x" + String.format("%.3f", getToneOffset()));
            synth.updateWaveviewer();
        });
        add(toneParameter);
        JLabel toneText = new JLabel("Tone");
        toneText.setBounds(172,40,75,25);
        add(toneText);
        JLabel volumeParameter = new JLabel(" 100%");
        volumeParameter.setBounds( 222,65,45,25);
        volumeParameter.setBorder(Utils.WindowDesign.LINE_BORDER);
        Utils.ParameterHandling.addParameterMouseListener(volumeParameter, this, 0, 100, 1, volume, () -> {
            volumeParameter.setText(" " + volume.val + "%");
            synth.updateWaveviewer();
        });
        add(volumeParameter);
        JLabel volumeText = new JLabel("Volume");
        volumeText.setBounds(225, 40, 75, 25);
        add(volumeText);
        setSize(279,100);
        
        JButton reset = new JButton("Reset");
        reset.setBounds(150, 10, 70 ,30);
        add(reset);
        reset.addActionListener((ActionEvent e) -> {
            setToneOffset(0);
            applyToneOffset();
            volumeParameter.setText(" 100%");
            setVolumeMultiplier(100);
            toneParameter.setText("x0.00");
            reset.setFocusable(false);
            synth.updateWaveviewer();
        });
        
        JButton randomize = new JButton("Randomize");
        randomize.setBounds(35,60,100,30);
        add(randomize);
        randomize.addActionListener((ActionEvent e) -> {
            int randomWave = ThreadLocalRandom.current().nextInt(0, 3 + 1);
            switch (randomWave) {
                case 0:
                    this.wavetable = wavetable.Sine;
                    break;
                case 1:
                    this.wavetable = wavetable.Square;
                    break;
                case 2:
                    this.wavetable = wavetable.Saw;
                    break;
                default:
                    this.wavetable = wavetable.Triangle;
                    break;
            }
            combobox.setSelectedIndex(randomWave);
            int randomVol = ThreadLocalRandom.current().nextInt(0, 100 + 1);
            int randomToneOffset = ThreadLocalRandom.current().nextInt(-2000, 2000 + 1);
            setToneOffset(randomToneOffset);
            applyToneOffset();
            volumeParameter.setText(" "+ randomVol+ "%");
            setVolumeMultiplier(randomVol);
            toneParameter.setText("x"+randomToneOffset/1000.0);
            randomize.setFocusable(false);
            synth.updateWaveviewer();
        });
        /*JLabel color = new JLabel("Color");
        color.setBounds(10, 60, 70, 30);
        add(color);
        */
        JCheckBox on = new JCheckBox("On", on_off);
        on.setBounds(220, 10, 50 ,30);
        add(on);
        on.addActionListener((ActionEvent e) -> {
            on_off = on_off != true;
            on.setFocusable(false);
            synth.updateWaveviewer();
        });
        setBorder(Utils.WindowDesign.LINE_BORDER);
        setLayout(null);
    }
    
    /**
     * @return the Boolean local variable for the specific oscillator of the "On" checkbox of the UI
     */
    public boolean get_on_off(){
        return on_off;
    }
    
    public void resetWI(){
        wavetableIndex = 0;
    }
    public String getWaveName(){
        return this.wavetable.toString();
    }
    
    /**
     * @description retrieve the sample
     * @return the sample
     */
    public double getNextSample(){
        double sample = wavetable.getSamples()[wavetableIndex] * getVolumeMultiplier();
        wavetableIndex = (wavetableIndex + wavetableStepSize) % Wavetable.SIZE;
        return sample;
    }

    /**
     * @description used to set the keyFrequency and applyToneOffSet
     * @param frequency the frequency
     */
    public void setKeyFrequency(double frequency){
        keyFrequency = frequency;
        applyToneOffset();
    }
        
    /**
     * @description retrieves the COMBINED samples of all oscillators
     * @param numSamples number of samples of the waveTable
     * @return double array (the COMBINED samples of all oscillators)
     */
    public double[] getSampleWavetable(int numSamples){
        double[] samples = new double[numSamples];
        double frequency = 1.0 / (numSamples / (double)Synthesizer.AudioInfo.SAMPLE_RATE) * cycles;
        int index = 0;
        int stepSize = (int)(Wavetable.SIZE * Utils.Math.offsetTone(frequency, getToneOffset()) / Synthesizer.AudioInfo.SAMPLE_RATE);
        for (int i = 0; i < numSamples; ++i){
            samples[i] = wavetable.getSamples()[index] * getVolumeMultiplier();
            index = (index + stepSize) % Wavetable.SIZE;
        }
        return samples;
    }

    /**
     * @description retrieves the samples of the oscillator at specified oscillator
     * @param numSamples number of samples of the waveTable
     * @param index the index of the Oscillator Array, the oscillator to get the waveTable for.
     * @return double array (the samples of the oscillator at index index)
     */
    public double[] getSampleWavetableSeperated(int numSamples,int index){
        double[] samples = new double[numSamples];
        double frequency = 1.0 / (numSamples / (double)Synthesizer.AudioInfo.SAMPLE_RATE) * cycles;
        int stepSize = (int)(Wavetable.SIZE * Utils.Math.offsetTone(frequency, getToneOffset()) / Synthesizer.AudioInfo.SAMPLE_RATE);
        for (int i = 0; i < numSamples; ++i){
            samples[i] = wavetable.getSamples()[index] * getVolumeMultiplier() / 3;
            index = (index + stepSize) % Wavetable.SIZE;
        }
        return samples;
    }

    /**
     * @description method in order to return an empty waveTable,
     * used because if the oscillator is turned OFF,
     * there still needs to be a waveTable even though there are no samples for that oscillator.
     * @param numSamples the size of the waveTable
     * @return double array (0 filled)
     */
    public double[] getEmptySampleWavetable(int numSamples){
        double[] samples = new double[numSamples];
        for (int i = 0; i < numSamples; ++i){
            samples[i] = 0;
        }
        return samples;
    }
    
    private void setToneOffset(int val){
        toneOffset.val = val;
    }
    public double getToneOffset(){
        return toneOffset.val / 1000.0;
    }
    private void setVolumeMultiplier(int val){
        volume.val = val;
    }
    private double getVolumeMultiplier(){
        return volume.val / 100.0;
    }
    public void applyToneOffset(){
        wavetableStepSize = (int)(Wavetable.SIZE * Utils.Math.offsetTone(keyFrequency, getToneOffset())) / Synthesizer.AudioInfo.SAMPLE_RATE;
    }
    public void setNumCycles(int cycles){
        this.cycles = cycles;
    }
    
}
