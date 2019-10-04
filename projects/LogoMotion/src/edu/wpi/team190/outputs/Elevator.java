package edu.wpi.team190.outputs;

import edu.wpi.first.wpilibj.CANJaguar;
import edu.wpi.first.wpilibj.can.CANTimeoutException;
import edu.wpi.team190.cntrl.operator.ErrorTracker;
import edu.wpi.team190.util.PIDJaguar;
import edu.wpi.team190.util.Util;

/**
 *
 * @author Derrick
 */
public class Elevator {
    public static final String ELEVATOR_MECHANISM_NAME = "Elevator";

    public static final int POT_TURNS = 10;
    public static final double UPPER_BOUND = 1.9;
    public static final double LOWER_BOUND = 9.8;  // was 9.7
    
    public static final double kP = 1.0;
    public static final double kI = 0.0;
    public static final double kD = 0.5;

    private final PIDJaguar  m_motor1;
    private double m_currentSetpoint = 0;
    private boolean m_JaguarIsInitialized = false;

    public static class Position {
        public static final double BASE_LINE = 9.70;
        public static final double BOTTOM_OFFSET = -0.34;
        public static final double MIDDLE_OFFSET = -3.29;
        public static final double TOP_OFFSET = -6.59;

        public static final double MIDDLE_COLUMN_OFFSET = -0.61;

        public static final Position BASELINE = new Position(BASE_LINE);
        public static final Position BOTTOM = new Position(BASE_LINE + BOTTOM_OFFSET);
        public static final Position MIDDLE = new Position(BASE_LINE + MIDDLE_OFFSET);
        public static final Position TOP = new Position(BASE_LINE + TOP_OFFSET);

        private double m_position = 0.0;

        public Position(double position) {
            m_position = position;
        }

        public double getValue() {
            return m_position;
        }
    }

    public Elevator(PIDJaguar motor1) {
        m_motor1 = motor1;
        m_currentSetpoint = Util.constrainToRange(Position.BASELINE.getValue(), UPPER_BOUND, LOWER_BOUND);

        try {
            m_motor1.setPositionReference(CANJaguar.PositionReference.kPotentiometer);
            m_motor1.configPotentiometerTurns(POT_TURNS);
            m_motor1.changeControlMode(CANJaguar.ControlMode.kPosition);
            m_motor1.setPID(kP, kI, kD);
            m_motor1.enableControl();
            m_motor1.setX(m_currentSetpoint);
            m_JaguarIsInitialized = true;
        } catch (CANTimeoutException ex) {
            ex.printStackTrace();
            ErrorTracker.getInstance().errorOccurred(ErrorTracker.ErrorTypes.CAN_TIMEOUT, ELEVATOR_MECHANISM_NAME);
        } catch (NullPointerException ex) {
        }
    }
    /**
     * This function is called to move the elevator to a predetermined height.
     *
     * Takes height level and middle column determiner. The height level is an
     * integer between 1 and 3 inclusive that sets the target height to one of 3
     * preset values.  The middle determiner offsets the height when the target
     * row is in the middle due to a difference in height.
     *
     * @param heightLevel
     * @param middleColumn
     */
    public void setPosition(Position position, boolean middleColumn){
        if (!m_JaguarIsInitialized)
            return;
        double newSetpoint = position.getValue() + ((middleColumn) ? Position.MIDDLE_COLUMN_OFFSET : 0);
        
        if(newSetpoint == m_currentSetpoint)
            return;

        m_currentSetpoint = Util.constrainToRange(newSetpoint, UPPER_BOUND, LOWER_BOUND);
        setJaguarSetpoint(m_currentSetpoint);
    }
    /**
     * This function is called to manually move the elevator up.
     */
    public void jogUp(){
        if (!m_JaguarIsInitialized)
            return;
        m_currentSetpoint = Util.constrainToRange(m_currentSetpoint -= 0.1, UPPER_BOUND, LOWER_BOUND);
        setJaguarSetpoint(m_currentSetpoint);
    }
    /**
     * This function is called to manually move the elevator down.
     */
    public void jogDown(){
        if (!m_JaguarIsInitialized)
            return;
        m_currentSetpoint = Util.constrainToRange(m_currentSetpoint += 0.1, UPPER_BOUND, LOWER_BOUND);
        setJaguarSetpoint(m_currentSetpoint);
    }

    public void dropForScoring() {
        if (!m_JaguarIsInitialized)
            return;
        m_currentSetpoint = Util.constrainToRange(m_currentSetpoint += 0.6, UPPER_BOUND, LOWER_BOUND);
        setJaguarSetpoint(m_currentSetpoint);
    }
    
    private void setJaguarSetpoint(double sp) {
        if (!m_JaguarIsInitialized)
            return;
        try {
            m_motor1.setX(sp);
        } catch (CANTimeoutException ex) {
            ex.printStackTrace();
            ErrorTracker.getInstance().errorOccurred(ErrorTracker.ErrorTypes.CAN_TIMEOUT, ELEVATOR_MECHANISM_NAME);
        }
    }

    public double getCurrentPosition() {
        try {
            return m_motor1.getPosition();
        } catch (CANTimeoutException ex) {
            ex.printStackTrace();
            ErrorTracker.getInstance().errorOccurred(ErrorTracker.ErrorTypes.CAN_TIMEOUT, ELEVATOR_MECHANISM_NAME);
        }
        return 0;
    }

    public boolean isPositionWithinTolerance(double tolerance) {
        return Math.abs(getCurrentPosition() - m_currentSetpoint) <= tolerance;
    }
}
