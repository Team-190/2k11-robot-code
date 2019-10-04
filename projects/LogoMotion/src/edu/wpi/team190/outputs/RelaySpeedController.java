/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.wpi.team190.outputs;

import edu.wpi.first.wpilibj.Relay;

import edu.wpi.first.wpilibj.SpeedController;

/**
 *
 * @author Derrick
 */
public class RelaySpeedController implements SpeedController{
    private double m_setPoint = 0;
    private int relayPort = 1;

    private final Relay m_alignmentRelay = new Relay(relayPort);

    public RelaySpeedController(int portNumber) {
        
    }

    public double get() {
        return m_setPoint;
    }

    public void set(double speed, byte syncGroup) {
        set(speed);
    }

    public void set(double speed) {
        m_setPoint = speed;
        if (speed > 0) {
            m_alignmentRelay.set(Relay.Value.kForward);
        }
        else if (speed < 0) {
            m_alignmentRelay.set(Relay.Value.kReverse);
        }
        else {
            m_alignmentRelay.set(Relay.Value.kOff);
        }
    }

    public void disable() {
    }

    public void pidWrite(double output) {
        set(output);
    }
}
