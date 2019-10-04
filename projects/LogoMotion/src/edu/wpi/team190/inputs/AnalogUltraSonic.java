package edu.wpi.team190.inputs;

import edu.wpi.first.wpilibj.AnalogChannel;
import edu.wpi.first.wpilibj.PIDSource;
import edu.wpi.first.wpilibj.SensorBase;

/**
 *
 * @author Greg
 */
public class AnalogUltraSonic extends SensorBase implements PIDSource {
    public static final double DEG_OFF_FROM_STRAIGHT = 0;
    public static final double MV_PER_INCH = 9.8;

    AnalogChannel sensor;

    public AnalogUltraSonic(int port){
        sensor = new AnalogChannel(port);
    }

    public double getInches(){
        double rawInches = (sensor.getVoltage() * 1000) / MV_PER_INCH;

        return rawInches * Math.cos(DEG_OFF_FROM_STRAIGHT * (Math.PI / 180));
    }

    public double getCM(){
        return getInches() * 2.54;
    }

    public double pidGet() {
        return getInches();
    }
}
