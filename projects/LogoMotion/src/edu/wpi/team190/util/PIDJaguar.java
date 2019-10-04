/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.wpi.team190.util;

import edu.wpi.first.wpilibj.CANJaguar.ControlMode;
import edu.wpi.first.wpilibj.CANJaguar.PositionReference;
import edu.wpi.first.wpilibj.can.CANTimeoutException;

/**
 *
 * @author Derrick
 */
public interface PIDJaguar {
    public void changeControlMode(ControlMode controlMode)
            throws CANTimeoutException;
    public ControlMode getControlMode()
            throws CANTimeoutException;
    public void setPositionReference(PositionReference ref)
            throws CANTimeoutException;
    public void configPotentiometerTurns(int turns)
            throws CANTimeoutException;
    public void setPID(double p, double i, double d)
            throws CANTimeoutException;
    public void enableControl()
            throws CANTimeoutException;
    public void disableControl()
            throws CANTimeoutException;
    public void setX(double x)
            throws CANTimeoutException;
    public double getPosition()
            throws CANTimeoutException;
    public boolean getPowerCycled()
            throws CANTimeoutException;
}
