// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;
import com.revrobotics.jni.CANSparkMaxJNI;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.Servo;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * The VM is configured to automatically run this class, and to call the functions corresponding to
 * each mode, as described in the TimedRobot documentation. If you change the name of this class or
 * the package after creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {
  public static int leftMotor = 18;
  public static int rightMotor = 19;
  public static int CamHorizontal = 8;
  public static int driveJoystick = 0;
  public static double m_cameraAngle = 0.5;
  public static int objX = 250;
  public static double deltaX = 0,deltaY = 0;
  



  private static final String kDefaultAuto = "Default";
  private static final String AIAuto = "AI Auto";
  private String m_autoSelected;
  private final SendableChooser<String> m_chooser = new SendableChooser<>();



  private final CANSparkMax m_leftMotor = new CANSparkMax(3,MotorType.kBrushless);
  private final CANSparkMax m_rightMotor = new CANSparkMax(2,MotorType.kBrushless);
  private Servo camera_horizontalServo = new Servo(CamHorizontal);


  XboxController m_driveController = new XboxController(driveJoystick);



  NetworkTable vision;

  public static NetworkTableEntry xVision;
  public NetworkTableEntry xMinVision;
  public NetworkTableEntry xMaxVision;
  public NetworkTableEntry yVision;
  public NetworkTableEntry yMinVision;
  public NetworkTableEntry yMaxVision;
  public NetworkTableEntry nameVision;
  public NetworkTableEntry test;
  public static NetworkTableEntry cameraAngle;

  /**
   * This function is run when the robot is first started up and should be used for any
   * initialization code.
   */
  @Override
  public void robotInit() {
    m_chooser.setDefaultOption("Default Auto", kDefaultAuto);
    m_chooser.addOption("AI Auto", AIAuto);
    SmartDashboard.putData("Auto choices", m_chooser);


    NetworkTableInstance inst = NetworkTableInstance.getDefault();

    vision = inst.getTable("vision");

 

    xVision = vision.getEntry("x");
    xMinVision = vision.getEntry("xmin");
    xMaxVision = vision.getEntry("xmax");
    yVision = vision.getEntry("y");
    yMinVision = vision.getEntry("ymin");
    yMaxVision = vision.getEntry("ymax");
    nameVision = vision.getEntry("name");
    cameraAngle = vision.getEntry("cameraAngle");
    test = vision.getEntry("test");

    xVision.setNumber(0);
    xMinVision.setNumber(0);
    xMaxVision.setNumber(0);
    yVision.setNumber(0);
    yMinVision.setNumber(0);
    yMaxVision.setNumber(0);
    nameVision.setString("Name");
    cameraAngle.setNumber(.5);
    test.setNumber(1);
    
  }

  /**
   * This function is called every robot packet, no matter the mode. Use this for items like
   * diagnostics that you want ran during disabled, autonomous, teleoperated and test.
   *
   * <p>This runs after the mode specific periodic functions, but before LiveWindow and
   * SmartDashboard integrated updating.
   */
  @Override
  public void robotPeriodic() {
    SmartDashboard.putNumber("Left Stick", m_driveController.getRawAxis(1));
    SmartDashboard.putNumber("Right Stick", m_driveController.getRawAxis(5));
    SmartDashboard.putNumber("Left Motor Amps", m_leftMotor.getOutputCurrent());
    SmartDashboard.putNumber("Left Motor Temp.", m_leftMotor.getMotorTemperature());

   objX = xVision.getNumber(250).intValue();
  }

  /**
   * This autonomous (along with the chooser code above) shows how to select between different
   * autonomous modes using the dashboard. The sendable chooser code works with the Java
   * SmartDashboard. If you prefer the LabVIEW Dashboard, remove all of the chooser code and
   * uncomment the getString line to get the auto name from the text box below the Gyro
   *
   * <p>You can add additional auto modes by adding additional comparisons to the switch structure
   * below with additional strings. If using the SendableChooser make sure to add them to the
   * chooser code above as well.
   */
  @Override
  public void autonomousInit() {
    m_autoSelected = m_chooser.getSelected();
    // m_autoSelected = SmartDashboard.getString("Auto Selector", kDefaultAuto);
    System.out.println("Auto selected: " + m_autoSelected);

    while(camera_horizontalServo.get() > 0){
      camera_horizontalServo.set(0);
    }
    camera_horizontalServo.set(0.5);
  }

  /** This function is called periodically during autonomous. */
  @Override
  public void autonomousPeriodic() {
    deltaX = xMaxVision.getDouble(0) - xMinVision.getDouble(0);
    deltaY = yMaxVision.getDouble(100) - yMinVision.getDouble(0);
    CANSparkMaxJNI.c_SparkMax_SetEnable(true); // Periodically ensure motor controller outputs are enabled
    //cameraAngle.setNumber(m_cameraAngle);
    switch (m_autoSelected) {
      case AIAuto:

        //If the obj is to the left of center
        if (objX < 100){
            m_rightMotor.set(.20);
            m_leftMotor.set(.20);
        }
        //If the obj is in the center of the screen
        else if(objX < 400 && objX > 100){
          m_rightMotor.set(0);
            m_leftMotor.set(0);
            if(deltaY < 100){
              m_rightMotor.set(-.15);
              m_leftMotor.set(.15);
            }
            if(deltaY > 200){
              m_rightMotor.set(.15);
            m_leftMotor.set(-.15);
            }
        }
        //If the objis to the right of center
        else if(objX > 400){
          m_rightMotor.set(-.20);
          m_leftMotor.set(-.20);
        }
        //If the object cannot be found, do nothing.
        else{
          System.out.println("Object Not Found");
        }
        break;
      case kDefaultAuto:
      default:
        // Put default auto code here
        break;
    }
  }

  /** This function is called once when teleop is enabled. */
  @Override
  public void teleopInit() {
    camera_horizontalServo.set(0.5);
  }

  /** This function is called periodically during operator control. */
  @Override
  public void teleopPeriodic() {
    CANSparkMaxJNI.c_SparkMax_SetEnable(true); // Periodically ensure motor controller outputs are enabled

    m_leftMotor.set(-m_driveController.getRawAxis(1));
    m_rightMotor.set(m_driveController.getRawAxis(5));

     //If the obj is to the left of center
     if (objX < 161){

    }
    //If the obj is in the center of the screen
    else if(objX < 340 && objX > 160){
      if(objX < 220){
        m_cameraAngle = m_cameraAngle - 0.01;
      }
      else if(objX > 280){
        m_cameraAngle = m_cameraAngle + 0.01;
      }
      camera_horizontalServo.set(m_cameraAngle);
    }
    //If the objis to the right of center
    else if(objX > 339){

    }
    //If the object cannot be found, do nothing.
    else{
      System.out.println("Object Not Found");
    }
  }

  /** This function is called once when the robot is disabled. */
  @Override
  public void disabledInit() {
    m_leftMotor.set(0);
    m_rightMotor.set(0);
  }

  /** This function is called periodically when disabled. */
  @Override
  public void disabledPeriodic() {}

  /** This function is called once when test mode is enabled. */
  @Override
  public void testInit() {}

  /** This function is called periodically during test mode. */
  @Override
  public void testPeriodic() {}
}
