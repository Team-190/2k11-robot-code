package edu.wpi.team190.outputs;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.SpeedController;
import edu.wpi.first.wpilibj.Timer;
/**
 *
 * @author Derrick
 * 
 */
public class RollerClaw extends Thread {
    // Constants
    private static final double FORWARD = -1;
    private static final double REVERSE = 1;
    private static final double TURN_TIME_SECONDS = 0.1;

    // Modes
    private final int IDLE_MODE = 0;
    private final int INTAKE_MODE = 1;
    private final int TURN_MODE = 2;

    private int m_mode = IDLE_MODE;
    private final SpeedController m_topMotor;     //The two motors on the claw:
    private final SpeedController m_bottomMotor;  //the top and bottom
    private final DigitalInput m_tubeDetector;
    private final boolean m_tubeDetectorActiveState;
    private Runnable m_hasTubeHandler;
    private double m_turnStopTime;
    
    public RollerClaw(SpeedController top,
                      SpeedController bottom,
                      DigitalInput tubeDetector,
                      boolean tubeDetectorActiveState){
        m_topMotor = top;
        m_bottomMotor = bottom;
        m_tubeDetector = tubeDetector;
        m_tubeDetectorActiveState = tubeDetectorActiveState;
    }

    /**
     * This function is called to pull a tube into the roller claw.
     */
    public void pullIn(){
         m_topMotor.set(-REVERSE);
         m_bottomMotor.set(REVERSE);

         m_mode = INTAKE_MODE;
         
         synchronized(this) {
            this.notify();
        }
    }
    /**
     * This function is called to push the tube out of the roller claw.
     */
    public void pushOut(){
         m_topMotor.set(-FORWARD/2.5);
         m_bottomMotor.set(FORWARD/2.5);

         m_mode = IDLE_MODE;
    }
    /**
     * This function is called to turn the tube up in the roller claw.
     */
    public void turnUp(){
         m_turnStopTime = Timer.getFPGATimestamp() + TURN_TIME_SECONDS;

         m_topMotor.set(-REVERSE/2.5);
         m_bottomMotor.set(FORWARD/2.5);

         m_mode = TURN_MODE;

         synchronized(this) {
            this.notify();
        }
    }
    /**
     * This function is called to turn the tube down in the roller claw.
     */
    public void turnDown(){
         m_turnStopTime = Timer.getFPGATimestamp() + TURN_TIME_SECONDS;

         m_topMotor.set(-FORWARD/6);
         m_bottomMotor.set(REVERSE/6);

         m_mode = TURN_MODE;
         synchronized(this) {
            this.notify();
        }
    }

    public void stopRollers(){
        m_topMotor.set(0);
        m_bottomMotor.set(0);

        m_mode = IDLE_MODE;
    }

    public void setHasTubeHandler(Runnable r) {
        m_hasTubeHandler = r;
    }

    public void run() {
        while(true) {
            if(m_mode == INTAKE_MODE) {
                //System.out.println("Roller claw detector: " + m_tubeDetector.get());
                if(m_tubeDetector.get() == m_tubeDetectorActiveState) {
                    stopRollers();

                    if(m_hasTubeHandler != null)
                        m_hasTubeHandler.run();
                }
            } else if(m_mode == TURN_MODE) {
                if(Timer.getFPGATimestamp() > m_turnStopTime)
                    stopRollers();
            } else {
                // Wait for a mode change
                try {
                    synchronized (this) {
                        wait();
                    }
                } catch (InterruptedException ex) {
                }
            }
            Timer.delay(0.020);
        }
    }
}
