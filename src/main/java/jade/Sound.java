package jade;

import components.StateMachine;
import org.lwjgl.openal.AL10;
import org.lwjgl.stb.STBVorbis;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.libc.LibCStdlib;

import java.nio.IntBuffer;
import java.nio.ShortBuffer;

public class Sound {

    private int bufferId;
    private int sourceId;
    private String filePath;
    private boolean isPlaying = false;

    public Sound(String filePath, boolean loops) {
        this.filePath = filePath;

        // allocate space to store the return information from stb
        MemoryStack.stackPush();
        IntBuffer channelsBuffer = MemoryStack.stackMallocInt(1);
        MemoryStack.stackPush();
        IntBuffer sampleRateBuffer = MemoryStack.stackMallocInt(1);

        ShortBuffer rawAudioBuffer = STBVorbis.stb_vorbis_decode_filename(filePath, channelsBuffer, sampleRateBuffer);
        if (rawAudioBuffer == null) {
            System.out.println("Could not load sound " + filePath);
            MemoryStack.stackPop();
            MemoryStack.stackPop();
        }

        // retrieve the extra info that was stored in the buffers by STB
        int channels = channelsBuffer.get();
        int sampleRate = sampleRateBuffer.get();

        // free
        MemoryStack.stackPop();
        MemoryStack.stackPop();

        // find the correct openAL format
        int format = -1;
        if (channels == 1) {
            format = AL10.AL_FORMAT_MONO16;
        } else if (channels == 2) {
            format = AL10.AL_FORMAT_STEREO16;
        }

        bufferId = AL10.alGenBuffers();
        AL10.alBufferData(bufferId, format, rawAudioBuffer, sampleRate);

        // generate the source
        sourceId = AL10.alGenSources();
        AL10.alSourcei(sourceId, AL10.AL_BUFFER, bufferId);
        AL10.alSourcei(sourceId, AL10.AL_LOOPING, loops ? 1 : 0);
        AL10.alSourcei(sourceId, AL10.AL_POSITION, 0);
        AL10.alSourcef(sourceId, AL10.AL_GAIN, 0.3f);

        // free stb raw audio buffer
        LibCStdlib.free(rawAudioBuffer);
    }

    public void delete() {
        AL10.alDeleteSources(sourceId);
        AL10.alDeleteBuffers(bufferId);
    }

    public void play() {
        int state = AL10.alGetSourcei(sourceId, AL10.AL_SOURCE_STATE);
        if (state == AL10.AL_STOPPED) {
            isPlaying = false;
            AL10.alSourcei(sourceId, AL10.AL_POSITION, 0);
        }

        if (!isPlaying) {
            AL10.alSourcePlay(sourceId);
            isPlaying = true;
        }
    }

    public void stop() {
        if (isPlaying) {
            AL10.alSourceStop(sourceId);
            isPlaying = false;
        }
    }

    public String getFilePath() {
        return filePath;
    }

    public boolean isPlaying() {
        int state = AL10.alGetSourcei(sourceId, AL10.AL_SOURCE_STATE);
        if (state == AL10.AL_STOPPED) {
            isPlaying = false;
        }
        return isPlaying;
    }

}
