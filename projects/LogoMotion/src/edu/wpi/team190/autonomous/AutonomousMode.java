package edu.wpi.team190.autonomous;

/**
 *
 * @author pmalmsten
 */
public abstract class AutonomousMode extends Thread {
    /**
     * Passes control of the robot to this AutonomousMode.
     */
    public abstract void run();
}
