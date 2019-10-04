package edu.wpi.team190.outputs.drive;

/*
 * Note: the package-level scoping below is intentional!
 */

/**
 * DriveMode represents a mode of operation for the drivetrain which can
 * be swapped in (entered) and swapped out (exited). 
 *
 * @author pmalmsten
 */
abstract class DriveMode {
    abstract void swapOutOn(SixWheelDrive drive);
    abstract void swapInOn(SixWheelDrive drive);
    abstract void drive(SixWheelDrive drive, double left, double right);
}
