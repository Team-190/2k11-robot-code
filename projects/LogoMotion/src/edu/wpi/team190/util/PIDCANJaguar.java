/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.wpi.team190.util;

import edu.wpi.first.wpilibj.CANJaguar;
import edu.wpi.first.wpilibj.can.CANTimeoutException;

/**
 *
 * @author Paul
 */
public class PIDCANJaguar extends CANJaguar implements PIDJaguar{
    public PIDCANJaguar(int port) throws CANTimeoutException{
        super(port);
    }
}
