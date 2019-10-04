package edu.wpi.team190.outputs;

import edu.wpi.first.wpilibj.SpeedController;
import edu.wpi.first.wpilibj.Timer;

/**
 *
 * @author Derrick
 */
public class Alignment extends Thread {

    private static final double DELAY_TIME_SEC = 0.1;
    private static final double MOTOR_POWER = 0.5;
    private final SpeedController m_alignmentMotor;
    private double m_stopTime = 0;

    public Alignment(SpeedController wingsMotor) {
        m_alignmentMotor = wingsMotor;
    }

    public void alignmentSystemDown() {
        m_stopTime = Timer.getFPGATimestamp() + DELAY_TIME_SEC;
        m_alignmentMotor.set(-MOTOR_POWER);

        synchronized(this) {
            notify();
        }
    }

    public void alignmentSystemUp() {
        m_stopTime = Timer.getFPGATimestamp() + DELAY_TIME_SEC;
        m_alignmentMotor.set(MOTOR_POWER);

        synchronized(this) {
            notify();
        }
    }

    public void alignmentSystemStop() {
        m_alignmentMotor.set(0);
    }

    public void run() {
        while(true) {
            synchronized(this) {
                try {
                    if(Timer.getFPGATimestamp() >= m_stopTime) {
                        alignmentSystemStop();
                        wait();
                    } else
                       Timer.delay(0.050);
                } catch (InterruptedException ex) {
                }
            }
        }
    }
}
