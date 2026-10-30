package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

@TeleOp(name = "Simple Drive", group = "TeleOp")
public class SimpleDrive extends OpMode {
    private DcMotor leftDrive;
    private DcMotor rightDrive;
    private DcMotorEx launchMotor;
    private final double launchTpr = 28;
    private DcMotor intakeMotor;
    private CRServo feederServo;

    double driveSpeedMultiplier = 1;
    ToggleAction launchToggle;
	ToggleAction intakeDirToggle;

    @Override
    public void init() {
        leftDrive = hardwareMap.get(DcMotor.class, "left_drive");
        rightDrive = hardwareMap.get(DcMotor.class, "right_drive");
        launchMotor = hardwareMap.get(DcMotorEx.class, "launch");
        intakeMotor = hardwareMap.get(DcMotor.class, "intake");
        feederServo = hardwareMap.get(CRServo.class, "feeder");

        // set rotation directions
        leftDrive.setDirection(DcMotor.Direction.FORWARD);
        rightDrive.setDirection(DcMotor.Direction.REVERSE);
        launchMotor.setDirection(DcMotor.Direction.FORWARD);
        intakeMotor.setDirection(DcMotor.Direction.FORWARD);
        feederServo.setDirection(CRServo.Direction.REVERSE);

        // set zero power behaviour
        leftDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        launchMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        intakeMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        launchToggle = new ToggleAction();

        telemetry.addData("Status", "Initialized. Ready to drive!");
        telemetry.update();
    }

    @Override
    public void loop() {
        launchToggle.update(gamepad1.b);

        double drive = -gamepad1.left_stick_y;
        double turn = gamepad1.right_stick_x;

        // Calculate power for each side (Arcade Drive math)
        double leftPower = drive + turn;
        double rightPower = drive - turn;

        // Normalise powers if they exceed 1.0 to keep steering responsive
        double max = Math.max(Math.abs(leftPower), Math.abs(rightPower));
        if (max > 1.0) {
            leftPower /= max;
            rightPower /= max;
        }

        // Apply power to the motors
        leftDrive.setPower(leftPower * driveSpeedMultiplier);
        rightDrive.setPower(rightPower * driveSpeedMultiplier);
        intakeMotor.setPower(1.0);

        if (launchToggle.isActive()) {
            launchMotor.setPower(1.0);
            // allow time for launch motor to get up to speed
            if (launchToggle.getStateDuration() >= 2.5) feederServo.setPower(1.0);
        } else {
            launchMotor.setPower(0.0);
            feederServo.setPower(0.0);
        }

        // Display telemetry data
        double launchRpm = (launchMotor.getVelocity() / launchTpr) * 60.0;
        telemetry.addData("Drive", "Left (%.0f%%), Right (%.0f%%)", leftPower * 100, rightPower * 100);
        telemetry.addData("Intake", "Motor (%.0f%%)", intakeMotor.getPower() * 100);
        telemetry.addData("Launch Mechanism", "Active (%s), Launch (%.0f rpm), Feed (%.0f%%)", launchToggle.isActive(), launchRpm, feederServo.getPower() * 100);
        telemetry.update();
    }
}
