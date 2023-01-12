package capstone;

import java.awt.Font;
import javax.swing.JPanel;
import utils.Utils;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
/**
 * @author Benjamin Knecht
 * @description Class which paints JPanel (static visual aid for the notes)
 */
public class PianoUI extends JPanel{
    
    public PianoUI(){
        setBounds(5,315,1090,120);
        setBorder(Utils.WindowDesign.LINE_BORDER);
    }
    
    /**
     * @description Overridden method for painting the JPanel
     * @param graphics Graphics Object
     */
    @Override
    public void paintComponent(Graphics graphics){
        final int PAD = 1085/38;
        super.paintComponent(graphics);
        
        Graphics2D whiteNotes = (Graphics2D)graphics;
        int xloc = PAD/2;
        for (int i = 0; i < 38;i++){
            whiteNotes.drawRect(xloc, PAD/2, PAD, PAD*3);
            xloc+=PAD;
        }
        Graphics2D blackNotes = (Graphics2D)graphics;
        int xloc2 = (int) (PAD/2 +7.5);
        for (int i = 1; i < 39;i++){
            if (i != 3 && i != 7 && i !=10 && i !=14 && i !=17 && i !=21 && i !=24 && i !=28 && i !=31 && i !=35 && i !=38)
                blackNotes.fillRect(xloc2+PAD/2, PAD/2, PAD/2, PAD*2);
            xloc2+=PAD;
        }
        Graphics2D noteName = (Graphics2D)graphics;
        int xloc3 = PAD;
        for (int i = 1; i < 7;i++){
            noteName.setFont(new Font("SansSerif", Font.PLAIN, 14)); 
            noteName.drawString("C", (int) (xloc3-7.5), PAD*3+5);
            noteName.setFont(new Font("SansSerif", Font.BOLD, 12)); 
            noteName.drawString(""+(i+1), (int) (xloc3)+2, (int) (PAD*3+10));
            xloc3+=PAD*7;
        }
        int xloc4 = PAD;
        noteName.setFont(new Font("SansSerif", Font.BOLD, 13)); 
        noteName.drawString("(Z)", (int) (xloc4-7.5), PAD*4);
        xloc4+=PAD*7;
        noteName.drawString("(M)", (int) (xloc4-7.5), PAD*4);
        xloc4+=PAD*7;
        noteName.drawString("(D)", (int) (xloc4-7.5), PAD*4);
        xloc4+=PAD*7;
        noteName.drawString("(L)", (int) (xloc4-7.5), PAD*4);
        xloc4+=PAD*7;
        noteName.drawString("(R)", (int) (xloc4-7.5), PAD*4);
        xloc4+=PAD*7;
        noteName.drawString("(P)", (int) (xloc4-7.5), PAD*4);
        noteName.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
    }
}
