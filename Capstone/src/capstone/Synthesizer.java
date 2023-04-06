package capstone;

import WavFile.WavFile;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import javax.swing.*;
import javax.swing.table.TableColumn;
import utils.Utils;

/**
 * @author Benjamin Knecht
 * @description the Synthesizer class handles fields and methods related to
 * the functionality of the synthesizer and it's UI
 * as well as the logic for the UI elements
 */
public class Synthesizer {
    private static final HashMap<Character, Double> KEY_FREQUENCIES = new HashMap();
    private static final HashMap<Integer, Double> ALL_FREQUENCIES = new HashMap();
    private static final HashMap<Character, Boolean> pressedB = new HashMap();
    private boolean shouldGenerate;
    private final JFrame frame = new JFrame("Capstone: Synthesizer by Benjamin Knecht");
    private final AudioThread thread;
    private final Oscillator[] oscillators = new Oscillator[3];
    private final WaveViewer waveViewer;
    private final WaveViewerSeperated waveViewerSeperated;
    private final PianoUI piano;
    private double dur = 1.0;
    
        private final KeyAdapter keyAdapter = new KeyAdapter(){
            private final Set<Character> pressed = new HashSet<>();
            private int keysPressed = 0;
            private double frequencySum = 0.0;
            private double frequencySumAvg = 0.0;
            
            @Override
            public void keyPressed(KeyEvent e){
                if (!KEY_FREQUENCIES.containsKey(e.getKeyChar())){
                    return;//prevent null throw exception
                }
                pressed.add(e.getKeyChar());
                keysPressed = pressed.size();
                if (keysPressed>1){// mix the audio
                    Character[] pressedA = pressed.toArray(Character[]::new);
                       for (int i = 0; i<keysPressed; i++){
                           if (pressedB.get(pressedA[i]) == false){
                                frequencySum += KEY_FREQUENCIES.get(pressedA[i]);
                           }
                       }
                }
                else{
                    frequencySum = KEY_FREQUENCIES.get(e.getKeyChar());
                }
                frequencySumAvg = frequencySum;
                frequencySumAvg/=keysPressed;
                pressedB.put(e.getKeyChar(), true);
                for(Oscillator o : oscillators){
                        if (keysPressed ==1)
                            o.setKeyFrequency(frequencySum);
                        else if (Wavetable.SIZE>frequencySumAvg && frequencySumAvg>0)
                            o.setKeyFrequency(frequencySumAvg);
                }
                if (!thread.isRunning()){
                    shouldGenerate = true;
                    thread.triggerPlayback();
                }
            }
            @Override
            public void keyReleased(KeyEvent e){
                pressed.remove(e.getKeyChar());
                keysPressed = pressed.size();
                if (keysPressed>1){// mix the audio
                    Character[] pressedA = pressed.toArray(Character[]::new); 
                       for (int i = 0; i<keysPressed; i++){
                           if (pressedB.get(pressedA[i]) == true){
                                frequencySum -= KEY_FREQUENCIES.get(pressedA[i]);
                           }
                       }
                }
                else {
                    if (!KEY_FREQUENCIES.containsKey(e.getKeyChar())){
                            return;//prevent null throw exception
                    }
                    else
                    frequencySum = KEY_FREQUENCIES.get(e.getKeyChar());
                }
                frequencySumAvg = frequencySum;
                frequencySumAvg/=keysPressed;
                pressedB.put(e.getKeyChar(), false);
                shouldGenerate = false;
            }
        };
    static{
        final int STARTING_KEY = 16;
        final int KEY_FREQUENCY_INCREMENT = 2;
        final char[] KEYS = "zxcvbnm,./asdfghjkl;'qwertyuiop[]".toCharArray();
        for (int i = STARTING_KEY, key = 0; i < KEYS.length * KEY_FREQUENCY_INCREMENT + STARTING_KEY; i += KEY_FREQUENCY_INCREMENT, ++key){
            KEY_FREQUENCIES.put(KEYS[key], Utils.Math.getKeyFrequency(i));
            pressedB.put(KEYS[key], false);
        }
    }
    Synthesizer() throws IOException{
        this.waveViewer = new WaveViewer(oscillators);
        this.waveViewerSeperated = new WaveViewerSeperated(oscillators);
        this.piano = new PianoUI();
        
        this.thread = new AudioThread(() ->
        {
            if (!shouldGenerate){
                return null;
            }
            short[] s = new short[AudioThread.BUFFER_SIZE];
            for (int i = 0; i < AudioThread.BUFFER_SIZE; ++i){
                double d = 0;
                for (Oscillator o : oscillators){
                    if (o.get_on_off()== true){ // don't include samples in audio if on_off is off (false)
                        d += o.getNextSample() / oscillators.length;// (i + 0.5) / 2 = 0.75 mixed audio
                    }
                }
                s[i] = (short)(Short.MAX_VALUE * d);
            }
            return s;
        });
        
        int y = 0;
        for (int i = 0; i < oscillators.length; ++i){
            oscillators[i] = new Oscillator(this);
            oscillators[i].setLocation(5,y);
            frame.add(oscillators[i]);
            y += 105;
        }
        Oscillator o = new Oscillator(this);
        o.setLocation(5,0);
        waveViewer.setBounds(290,0,300,310);
        frame.add(waveViewer);
        waveViewerSeperated.setBounds(589,0,300,310);
        waveViewerSeperated.setBackground(Color.black);
        frame.add(waveViewerSeperated);
        frame.add(o);
        Component wavParams = synthParams();
        wavParams.setBounds(895,0,200,65);
        frame.add(wavParams);
        Component legend = legend();
        legend.setBounds(895,70,200,240);
        frame.add(legend);
        
        frame.add(piano);
        piano.repaint();
        Component wav = saveWavSamplesButton();
        wav.setBounds(5,435,1090,80);
        frame.add(wav);
        
        
        frame.addKeyListener(keyAdapter);
        frame.addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent e){
                thread.close();
            }
        });
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setSize(1113, 557);
        frame.setResizable(false);
        frame.setLayout(null);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        
    }

    /**
     * @description getter for keyAdapter
     * @return the keyAdapter
     */
    public KeyAdapter getKeyAdapter(){
        return keyAdapter;
    }
    
    /**
     * @description repaints both WaveViewer and WaveViewerSeperated Objects
     */
    public void updateWaveviewer(){
        waveViewerSeperated.repaint();
        waveViewer.repaint();
    }
    
    /**
     *  @description static class to give access to the sample rate in other methods
     */
    public static class AudioInfo{
        /**
         * @usage (Synthesizer object).AudioInfo.SAMPLE_RATE
         */
        public static int SAMPLE_RATE = 44100;//8000;//44100;
    }

    private JButton saveWavSamplesButton(){
        JButton s = new JButton("Save wav samples (located in samples directory where this program is run)");
        s.setFont(new Font("Arial", Font.PLAIN, 24));
        s.addActionListener((ActionEvent e) -> {
            String string = "";
            for (Oscillator o : oscillators){
                String c = o.getWaveName();
                if (null != c) //System.out.println(c);
                switch (c) {
                    case "Sine":
                        string = string.concat(c);
                        break;
                    case "Saw":
                        string = string.concat(c);
                        break;
                    case "Square":
                        string = string.concat(c.substring(0,3));
                        break;
                    case "Triangle": 
                        string = string.concat(c.substring(0,3));
                        break;
                    default:
                        break;
                }
            }
            saveWavSamples(string);
            s.setFocusable(false);
        });
            s.setFocusable(false);
        return s;
    }
      
    private void saveWavSamples(String name){
        int it = 0;
        new File("samples").mkdirs();
        String nametmp = name;
        Path path = Paths.get("samples/"+name);
        if (Files.exists(path) == true){
            for (int i =1; i < 1000; i++){
                Path p = Paths.get("samples/"+nametmp);
                if (Files.exists(p) == true){
                    nametmp = name + i;
                    it ++;
                }
                else{
                    new File("samples/"+nametmp).mkdirs();
                    i=1000;
                }
            }
        }
        else
            new File("samples/"+name).mkdirs();
        for (int i = 16; i < 65 * 1 + 16; i++){
            ALL_FREQUENCIES.put(i-16, Utils.Math.getKeyFrequency(i));
        }
        for (int i = 0;i<65;i++){
            for (Oscillator o : oscillators){
                o.setKeyFrequency(ALL_FREQUENCIES.get(i));
                o.resetWI();
            }
		try
		{
                        double duration = dur;		// Seconds

			// Calculate the number of frames required for specified duration
			long numFrames = (long)(duration * Synthesizer.AudioInfo.SAMPLE_RATE);
                        
			// Create a wav file with the name specified as the first argument
			WavFile wavFile = WavFile.newWavFile(new File("samples/"+nametmp,name + it + "key" + (i + 36) +".wav"), 1, numFrames, 16, Synthesizer.AudioInfo.SAMPLE_RATE);

			// Create a buffer of 100 frames
			double[][] buffer = new double[1][Synthesizer.AudioInfo.SAMPLE_RATE];
            
			// Initialise a local frame counter
			long frameCounter = 0;
			// Loop until all frames written
			while (frameCounter < numFrames)
			{
				// Determine how many frames to write, up to a maximum of the buffer size
				long remaining = wavFile.getFramesRemaining();
				int toWrite = (remaining > 100) ? 100 : (int) remaining;

				// Fill the buffer, one tone per channel
                                    for (int s=0; s<toWrite ; s++, frameCounter++){
                                        double d = 0;
                                        for (Oscillator o : oscillators){
                                            if (o.get_on_off()== true){ // don't include samples in audio if on_off is off (false)
                                                d += (((double) o.getNextSample()) / ((double) oscillators.length));// * (1.0 + o.getToneOffset());// (i + 0.5) / 2 = 0.75 mixed audio
                                            }
                                            buffer[0][s] = d;
                                        }
                                    }
				// Write the buffer
				wavFile.writeFrames(buffer, toWrite);
			}
			// Close the wavFile
			wavFile.close();
        }
		catch (Exception e)
		{
			System.err.println(e);
		}
	}
    }
    
    private JPanel legend(){
        JPanel legend = new JPanel();
        legend.setBounds(150, 150, 913 ,347);
        legend.setBorder(Utils.WindowDesign.LINE_BORDER);
        JLabel legendText = new JLabel("Tuning & Pitch Legend");

        String[][] array = {
            {"(+)Tuning","(x)Freq Mult","(#)Half Steps"},
            {"0.083","1.060","1 (semitone)"},
            {"0.167","1.122","2 (whole)"},
            {"0.250","1.189","3 (min. 3rd)"},
            {"0.333","1.260","4 (maj. 3rd)"},
            {"0.417","1.335","5 (fourth)"},
            {"0.500","1.414","6 (tritone)"},
            {"0.583","1.500","7 (fifth)"},
            {"0.667","1.587","8 (min. 6th)"},
            {"0.750","1.682","9 (maj. 6th)"},
            {"0.833","1.782","10 (min. 7th)"},
            {"0.917","1.888","11 (maj. 7th)"},
            {"1.000","2.000","12 (octave)"},
        };
        String[] collumns = {"(+)Tuning","(x)Freq Mult","(#)Half Steps"};
        
        JTable legendTable = new JTable(array,collumns);

        legendTable.setAutoResizeMode( JTable.AUTO_RESIZE_ALL_COLUMNS );
        
        /**/TableColumn columnA = legendTable.getColumn("(+)Tuning");
        columnA.setMinWidth(0);
        columnA.setMaxWidth(55);
        
        TableColumn columnC = legendTable.getColumn("(x)Freq Mult");
        columnC.setMinWidth(0);
        columnC.setMaxWidth(67);
        //legendTable.setModel(tableModel);
        legendTable.setEnabled(false);
        legend.add(legendText);
        legend.add(legendTable);
        return legend;
    }
   
    private JPanel synthParams(){
        JPanel synthParams = new JPanel();
        synthParams.setBounds(150, 150, 913 ,347);
        synthParams.setBorder(Utils.WindowDesign.LINE_BORDER);
        JComboBox<Wavetable> combobox = new JComboBox<>(new Wavetable[] {Wavetable.Sine, Wavetable.Square, Wavetable.Saw, Wavetable.Triangle});
        combobox.setSelectedItem(Wavetable.Sine);
        JLabel sampleRateText = new JLabel("Sample Rate: ");
        JComboBox sampleRate = new JComboBox();
        JLabel antialiasingText = new JLabel("Antialiasing: ");
        JCheckBox antialiasing = new JCheckBox("On",true);
        JLabel cyclesText = new JLabel("Number of Cycles: ");
        JComboBox cycles = new JComboBox();
        JLabel durText = new JLabel("Duration: (s)");
        JComboBox duration = new JComboBox();
        
        sampleRate.addItem(8000);
        sampleRate.addItem(11025);
        sampleRate.addItem(22050);
        sampleRate.addItem(32000);
        sampleRate.addItem(44100);
        sampleRate.addItem(48000);
        sampleRate.addItem(96000);
        sampleRate.setSelectedIndex(4);
        sampleRate.setEnabled(false);
        sampleRate.addActionListener((ActionEvent e) -> {
            //Synthesizer.AudioInfo.SAMPLE_RATE = (int)sampleRate.getSelectedItem();
            sampleRate.setFocusable(false);
            antialiasing.setFocusable(false);
            duration.setFocusable(false);
            cycles.setFocusable(false);
            //if (sampleRate.getSelectedIndex() == 0)
               // oscillators
            //System.out.println(AudioInfo.SAMPLE_RATE);
        });
        antialiasing.addActionListener((ActionEvent e) -> {
            //synthParams.setFocusable(false);
            waveViewer.setAntialiasing();
            waveViewer.repaint();
            waveViewerSeperated.setAntialiasing();
            waveViewerSeperated.repaint();
            antialiasing.setFocusable(false);
            duration.setFocusable(false);
            cycles.setFocusable(false);
        });

        cycles.addItem(1);
        cycles.addItem(2);
        cycles.addItem(3);
        cycles.addItem(5);
        cycles.addItem(10);
        cycles.addItem(20);
        cycles.setSelectedIndex(2);
        cycles.addActionListener((ActionEvent e) -> {
            for (Oscillator o : oscillators)
                o.setNumCycles((int)cycles.getSelectedItem());
            waveViewer.repaint();
            waveViewerSeperated.repaint();
            duration.setFocusable(false);
            cycles.setFocusable(false);
        });
        
        duration.addItem(1.0);
        duration.addItem(2.0);
        duration.addItem(5.0);
        duration.addItem(10.0);
        duration.addItem(20.0);
        duration.addActionListener((ActionEvent e) -> {
            this.dur = (double) duration.getSelectedItem();
            duration.setFocusable(false);
            cycles.setFocusable(false);
        });
        
        duration.setBounds(165,65,953,367);
        durText.setBounds(135,65,953,367);
        //synthParams.add(sampleRateText, 0);
        //synthParams.add(sampleRate, 1);
        //synthParams.add(antialiasingText, 2);
        //synthParams.add(antialiasing, 3);
        synthParams.add(cyclesText, 0);
        synthParams.add(cycles, 1);
        synthParams.add(durText, 2);
        synthParams.add(duration, 3);
        return synthParams;
    }
}
