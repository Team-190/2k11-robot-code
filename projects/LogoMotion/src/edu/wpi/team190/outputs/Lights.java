package edu.wpi.team190.outputs;

import edu.wpi.first.wpilibj.DigitalOutput;

/**
 *
 * @author Jeff 
 */
public class Lights extends Thread {
    private static final boolean ACTIVE_STATE = false;

    private final DigitalOutput m_redLight;
    private final DigitalOutput m_whiteLight;
    private final DigitalOutput m_blueLight;
    private final DigitalOutput m_greenLight;
    private final DigitalOutput m_cameraLights;

    // Morse code
    private boolean m_morseCodeThreadEnable = false;
    private final long MORSE_DOT_DELAY = 150;                     // between parts of a char
    private final long MORSE_DASH_DELAY = 3 * MORSE_DOT_DELAY;
    private final long MORSE_CHAR_DELAY = MORSE_DASH_DELAY;       // between two characters
    private final long MORSE_WORD_DELAY = 6 * MORSE_DOT_DELAY;    // between words (the morse methods
                                                                  // below end with a dot delay; totals 7)

    /*
     * Color mode definitions
     */
    private static abstract class LightColor {
        abstract void set(Lights lights);
    }

    private static class RedColor extends LightColor {
        void set(Lights lights) {
            lights.m_redLight.set(ACTIVE_STATE);
            lights.m_blueLight.set(!ACTIVE_STATE);
            lights.m_whiteLight.set(!ACTIVE_STATE);
        }
    }

    private static class BlueColor extends LightColor {
        void set(Lights lights) {
            lights.m_redLight.set(!ACTIVE_STATE);
            lights.m_blueLight.set(ACTIVE_STATE);
            lights.m_whiteLight.set(!ACTIVE_STATE);
        }
    }

    private static class WhiteColor extends LightColor {
        void set(Lights lights) {
            lights.m_redLight.set(!ACTIVE_STATE);
            lights.m_blueLight.set(!ACTIVE_STATE);
            lights.m_whiteLight.set(ACTIVE_STATE);
        }
    }

    private static class OffColor extends LightColor {
        void set(Lights lights) {
            lights.m_redLight.set(!ACTIVE_STATE);
            lights.m_blueLight.set(!ACTIVE_STATE);
            lights.m_whiteLight.set(!ACTIVE_STATE);
        }
    }

    public static class Colors {
        public static final LightColor RED = new RedColor();
        public static final LightColor BLUE = new BlueColor();
        public static final LightColor WHITE = new WhiteColor();
        public static final LightColor OFF = new OffColor();
    }
    
    public Lights(DigitalOutput redLight,
                  DigitalOutput whiteLight,
                  DigitalOutput blueLight,
                  DigitalOutput greenLight,
                  DigitalOutput cameraLights) {
        m_redLight = redLight;
        m_whiteLight = whiteLight;
        m_blueLight = blueLight;
        m_greenLight  = greenLight;
        m_cameraLights = cameraLights;

        m_cameraLights.set(!ACTIVE_STATE);
        m_whiteLight.set(!ACTIVE_STATE);
        m_redLight.set(!ACTIVE_STATE);
        m_blueLight.set(!ACTIVE_STATE);
        m_greenLight.set(!ACTIVE_STATE);
    }

    public void setRequestedTubeLight(LightColor color) {
        color.set(this);
    }

    /**
     * 
     * @param b should be true if the claw has the tube, and false otherwise. 
     */
    public void setClawHasTubeLight(boolean b) {
        m_greenLight.set(b ? ACTIVE_STATE:!ACTIVE_STATE);
    }

    /**
     * Sets the state of the camera ring lights.
     * @param b If true, the lights will be turned on.
     */
    public void setCameraLights(boolean b) {
        m_cameraLights.set(b? ACTIVE_STATE:!ACTIVE_STATE);
    }

    public void showDeploymentLights() {
            m_greenLight.set(ACTIVE_STATE);
            m_redLight.set(ACTIVE_STATE);
            m_whiteLight.set(ACTIVE_STATE);
            m_blueLight.set(ACTIVE_STATE);
            m_cameraLights.set(ACTIVE_STATE);
    }

    /**
     * When enabled, the lights system will identify the robot in Morse code
     * until disabled. When disabled, normal functionality will resume (the correct
     * state must be subsequently respecified, however; the lights will bet set to
     * off).
     * @param b Morse code mode enabled when true, disabled otherwise.
     */
    public void setMorseCodeEnabled(boolean b) {
        m_morseCodeThreadEnable = b;

        // Inform the thread of a status change
        if(b)
            synchronized (this) {
                notify();
            }
        else {
            this.interrupt();
            setRequestedTubeLight(Colors.OFF);
        }
    }

    /**
     *
     * The following code uses wait() to implement delays such that the thread
     * may be interrupted at any time. *Do not* replace these with Timer.delay();
     * this would prevent the thread from properly relinquishling control over
     * the lights when Morse mode is disabled.
     *
     */

    /**
     * Provides Morse code identification.
     */
    public void run() {
        while(true) {
            try {
                if(m_morseCodeThreadEnable) {

                    /// "190"
                    // 1
                    morseDot();
                    morseDash();
                    morseDash();
                    morseDash();
                    morseDash();

                    delay(MORSE_CHAR_DELAY);
                    // 9
                    morseDash();
                    morseDash();
                    morseDash();
                    morseDash();
                    morseDot();

                    delay(MORSE_CHAR_DELAY);

                    // 0
                    morseDash();
                    morseDash();
                    morseDash();
                    morseDash();
                    morseDash();

                    delay(MORSE_CHAR_DELAY);

                    // End of "190"
                    delay(MORSE_WORD_DELAY);
                } else {
                    // Wait until notified/interrupted (i.e. the thread should
                    // be restarted).
                    synchronized (this) {
                        setRequestedTubeLight(Lights.Colors.OFF);
                        wait();
                    }
                }
            } catch (InterruptedException ex) {
            }
        }

    }

    /**
     * Represents a Morse dot (on-delay-off-delay).
     */
    private void morseDot() throws InterruptedException {
        m_blueLight.set(ACTIVE_STATE);
        m_whiteLight.set(ACTIVE_STATE);
        m_redLight.set(ACTIVE_STATE);
        m_greenLight.set(ACTIVE_STATE);

        delay(MORSE_DOT_DELAY);

        m_blueLight.set(!ACTIVE_STATE);
        m_whiteLight.set(!ACTIVE_STATE);
        m_redLight.set(!ACTIVE_STATE);
        m_greenLight.set(!ACTIVE_STATE);

        delay(MORSE_DOT_DELAY);
    }

    /**
     * Represents a Morse dash (on-delay-delay-delay-off-delay).
     */
    private void morseDash() throws InterruptedException {
        m_blueLight.set(ACTIVE_STATE);
        m_whiteLight.set(ACTIVE_STATE);
        m_redLight.set(ACTIVE_STATE);
        m_greenLight.set(ACTIVE_STATE);

        delay(MORSE_DASH_DELAY);

        m_blueLight.set(!ACTIVE_STATE);
        m_whiteLight.set(!ACTIVE_STATE);
        m_redLight.set(!ACTIVE_STATE);
        m_greenLight.set(!ACTIVE_STATE);

        delay(MORSE_DOT_DELAY);
    }

    private void delay(long ms) throws InterruptedException {
        synchronized (this) {
            wait(ms);
        }
    }
}