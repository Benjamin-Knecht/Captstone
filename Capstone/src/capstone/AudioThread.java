 package capstone;

import java.util.function.Supplier;
 import org.lwjgl.openal.AL;
 import org.lwjgl.openal.ALC;
 
 import static org.lwjgl.openal.AL10.*;
 import static org.lwjgl.openal.ALC10.*;
import utils.Utils;
 
/**
 * @author Benjamin Knecht
 * @description class that handles the thread that produce audio for the Synthesizer
 */
public class AudioThread extends Thread{
    static final int BUFFER_SIZE = 512;
    static final int BUFFER_COUNT = 8;
    
    private final Supplier<short[]> bufferSupplier;
    private final int[] buffers = new int[BUFFER_COUNT];
    private final long device = alcOpenDevice(alcGetString(0, ALC_DEFAULT_DEVICE_SPECIFIER));
    private final long context = alcCreateContext(device, new int[1]);
    private final int source;
    //private final int source2;
    //private final int source3;
    //private final int sources;// = {(source), (source2), (source3)};
    private static float xVal =0f;//x location of sound
    
    private int bufferIndex;
    private boolean closed;
    private boolean running;
    private static int index = 0;
    
    AudioThread(Supplier<short[]> bufferSupplier){
    
        this.bufferSupplier = bufferSupplier;
        alcMakeContextCurrent(context);
        AL.createCapabilities(ALC.createCapabilities(device));
        source = alGenSources();
        //source2 = alGenSources();
        //source3 = alGenSources();
        for (int i = 0; i < BUFFER_COUNT; i++){
            //buffer samples
            bufferSamples(new short[0]);
        }
        alSourcePlay(source);
        //alSourcePlay(source2);
        //alSourcePlay(source3);
        
        //catch internal exceptions
        catchInternalException();
        start();
    }
    public static void setxVal(float f){//add index param
        xVal = f;
    }
    
    boolean isRunning(){
        return running;
    }
    /**
     *
     */
    @Override
    public synchronized void run(){
        while (!closed){
            while (!running){
                Utils.handleProcedure(this::wait, false);
            }
        
        alSource3f(source,AL_POSITION, xVal, 0f, 0f);
        int processedBufs1 = alGetSourcei(source, AL_BUFFERS_PROCESSED);
            for(int i = 0; i < processedBufs1; ++i){
                short[] samples = bufferSupplier.get();
                if (samples == null){
                    running = false;
                    break;
                }
                alDeleteBuffers(alSourceUnqueueBuffers(source));
                buffers[bufferIndex] = alGenBuffers();
                bufferSamples(samples);
            }
            if (alGetSourcei(source, AL_SOURCE_STATE) != AL_PLAYING){
                alSourcePlay(source);
            }
            catchInternalException();
    }
    /*while (!closed){
        while (!running){
                Utils.handleProcedure(this::wait, false);
        }
        alSource3f(source2,AL_POSITION, xVal, 0f, 0f);
        int processedBufs2 = alGetSourcei(source2, AL_BUFFERS_PROCESSED);
            for(int i = 0; i < processedBufs2; ++i){
                short[] samples = bufferSupplier.get();
                if (samples == null){
                    running = false;
                    break;
                }
                alDeleteBuffers(alSourceUnqueueBuffers(source2));
                buffers[bufferIndex] = alGenBuffers();
                bufferSamples(samples);
            }
            if (alGetSourcei(source2, AL_SOURCE_STATE) != AL_PLAYING){
                alSourcePlay(source2);
            }
            catchInternalException();
    }
    while (!closed){
            while (!running){
                Utils.handleProcedure(this::wait, false);
            }    
        alSource3f(source3,AL_POSITION, xVal, 0f, 0f);
        int processedBufs3 = alGetSourcei(source3, AL_BUFFERS_PROCESSED);
            for(int i = 0; i < processedBufs3; ++i){
                short[] samples = bufferSupplier.get();
                if (samples == null){
                    running = false;
                    break;
                }
                alDeleteBuffers(alSourceUnqueueBuffers(source3));
                buffers[bufferIndex] = alGenBuffers();
                bufferSamples(samples);
            }
            if (alGetSourcei(source3, AL_SOURCE_STATE) != AL_PLAYING){
                alSourcePlay(source3);
            }
            catchInternalException();
        }
            
        alDeleteSources(source2);
        alDeleteSources(source3);
        */
    
        alDeleteSources(source);
        alDeleteBuffers(buffers);
        alcDestroyContext(context);
        alcCloseDevice(device);
    }
    
    synchronized void triggerPlayback(){
        running = true;
        notify();
    }
    
    void close(){
        closed = true;
        // break out of the loop
        triggerPlayback();
    }
    
    private void bufferSamples(short[] samples){
        int buf = buffers[bufferIndex++];
        alBufferData(buf,AL_FORMAT_MONO16, samples, Synthesizer.AudioInfo.SAMPLE_RATE);
        alSourceQueueBuffers(source,buf);
    //   alSourceQueueBuffers(source2,buf);
    //   alSourceQueueBuffers(source3,buf);
        bufferIndex %= BUFFER_COUNT;// 0 % 8 = 0 ... 8 % 8 = 0
    }
    
    private void catchInternalException(){
        int err = alcGetError(device);
        if (err != ALC_NO_ERROR){
            throw new OpenALException(err);
        }
    }
    
}
