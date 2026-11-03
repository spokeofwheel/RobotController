package org.firstinspires.ftc.teamcode;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.CRServo;
import static org.firstinspires.ftc.teamcode.RobotConfig.*;

@TeleOp(name = "Simple Drive", group = "TeleOp")
public class SimpleDrive extends OpMode {
    private DcMotor leftDrive;
    private DcMotor rightDrive;
    private DcMotorEx launchMotor;
    private DcMotor centralIntake;
	private CRServo rightIntake;
	private CRServo leftIntake;
    private CRServo feederServo;

    ToggleAction launchToggle;
	ToggleAction intakeToggle;

    @Override
    public void init() {
        leftDrive = hardwareMap.get(DcMotor.class, "left_drive");
        rightDrive = hardwareMap.get(DcMotor.class, "right_drive");
        launchMotor = hardwareMap.get(DcMotorEx.class, "launch");
        centralIntake = hardwareMap.get(DcMotor.class, "intake");
        rightIntake = hardwareMap.get(CRServo.class, "right_intake");
        leftIntake = hardwareMap.get(CRServo.class, "left_intake");
        feederServo = hardwareMap.get(CRServo.class, "feeder");

        // set rotation directions
        leftDrive.setDirection(DcMotor.Direction.FORWARD);
        rightDrive.setDirection(DcMotor.Direction.REVERSE);
        launchMotor.setDirection(DcMotor.Direction.FORWARD);
        centralIntake.setDirection(DcMotor.Direction.FORWARD);
        rightIntake.setDirection(DcMotor.Direction.REVERSE);
        leftIntake.setDirection(DcMotor.Direction.FORWARD);
        feederServo.setDirection(CRServo.Direction.REVERSE);

        // set zero power behaviour
        leftDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        launchMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        centralIntake.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

		launchToggle = new ToggleAction();
		intakeToggle = new ToggleAction();

		telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());
        telemetry.addData("Status", "Initialized.");
        telemetry.update();
    }

    @Override
    public void loop() {
        launchToggle.update(gamepad1.b);
		intakeToggle.update(gamepad1.a);

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

        if (launchToggle.isActive()) {
            launchMotor.setPower(launchSpeed);
            // allow time for launch motor to get up to speed
            if (launchToggle.getStateDuration() >= 2.5) feederServo.setPower(1.0);
        } else {
            launchMotor.setPower(0.0);
            feederServo.setPower(0.0);
        }

		if(intakeToggle.isActive()) {
			centralIntake.setPower(1.0);
			rightIntake.setPower(1.0);
			leftIntake.setPower(1.0);
		} else {
			centralIntake.setPower(0.0);
			rightIntake.setPower(0.0);
			leftIntake.setPower(0.0);
		}

        // Display telemetry data
        double launchRpm = (launchMotor.getVelocity() / 28) * 60.0;
        telemetry.addData("Drive", "Left (%.0f%%), Right (%.0f%%)", leftPower * 100, rightPower * 100);
        telemetry.addData("Intake", "(%s), Left (%.0f%%), Central (%.0f%%), Right (%.0f%%)", intakeToggle.isActive() ? "Active" : "Inactive", leftIntake.getPower() * 100, centralIntake.getPower() * 100, rightIntake.getPower() * 100);
        telemetry.addData("Launch Mechanism", "(%s), Launch (%.0f rpm), Feed (%.0f%%)", launchToggle.isActive() ? "Active" : "Inactive", launchRpm, feederServo.getPower() * 100);
        telemetry.update();
    }
}
