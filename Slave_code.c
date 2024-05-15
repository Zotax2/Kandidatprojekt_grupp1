#include <SPI.h>
#include <math.h>
// pin to read the current and the voltage for power measurement
const byte current_pin = 0;
const byte voltage_pin = 1;
// Motor sensor pins
const byte right_sensor_pin = 2;
const byte middle_right_sensor_pin = 3;
const byte middle_left_sensor_pin = 4;// white
const byte left_sensor_pin = 5;
// to store the value from the sensor pins
int sensor_value;
volatile byte sensor_threshold = 100;

// Right Motor Control Pins
int PWM_right_motor_pin = 5;    // Must be one of 3, 5, 6, 9, 10, or 11
int right_motor_ctrl_pin1 = 2;  
int right_motor_ctrl_pin2 = 3;  
// Left Motor Control Pins
int PWM_left_motor_pin = 6;    // Must be one of 3, 5, 6, 9, 10, or 11
int left_motor_ctrl_pin1 = 4;  
int left_motor_ctrl_pin2 = 7;  

volatile boolean received = false;
volatile byte received_data = 0;
volatile byte motor_data;
volatile byte speed_data;
volatile byte sensor_data;
// to count the crosses passed
int cross_counter_A = 0;
int cross_counter_B = 0;

// Data to be send to Master Arduino
uint16_t power;
uint8_t effect_data_low = 0;
uint8_t effect_data_high = 0;

volatile byte speed_factor = 5;
volatile byte speed_index = 0;
volatile byte motor_speed[10] = { 0, 51, 76, 102, 127, 153, 178, 204, 229, 255 };
volatile byte global_motor_speed = 0;
volatile byte right_motor_speed = 0;
volatile byte left_motor_speed = 0;
volatile byte right_motor_offset = 0;
volatile byte left_motor_offset = 0;
int R = 1;   // initial value for R that represents direction
int k = 0;    //  Used for regulation speed

// Interrupt service routine to handle SPI from master
ISR(SPI_STC_vect)  //Inerrrput routine function
{
  received_data = SPDR;  // Get the received data from SPDR register
  received = true;       // Sets received as True
}

// Internal function declarations
void line_following();
void handle_motor_commands();
void rotate_right();
void rotate_left();
void stop_agv();
void move_forward();
void move_back();
void update_speed();
void generate_effect();
void increase_speed();
void decrease_speed();
void handle_interrupt();
void handle_sensor_data();
void break_agv();

// Setup with all defaul values
void setup() {
  Serial.begin(9600);

  pinMode(MISO, OUTPUT);  //Sets MISO as OUTPUT
  SPCR |= _BV(SPE);       //Turn on SPI in Slave Mode
  received = false;
  SPI.attachInterrupt();  //Activate SPI Interuupt

  pinMode(PWM_right_motor_pin, OUTPUT);
  pinMode(right_motor_ctrl_pin1, OUTPUT);
  pinMode(right_motor_ctrl_pin2, OUTPUT);
  pinMode(PWM_left_motor_pin, OUTPUT);
  pinMode(left_motor_ctrl_pin1, OUTPUT);
  pinMode(left_motor_ctrl_pin2, OUTPUT);
  // Start idle Motor State
  digitalWrite(right_motor_ctrl_pin1, LOW);
  digitalWrite(right_motor_ctrl_pin2, LOW);
  digitalWrite(left_motor_ctrl_pin1, LOW);
  digitalWrite(left_motor_ctrl_pin2, LOW);

  global_motor_speed = motor_speed[0];  // 1 ... 9 = 20% ... 100%
  right_motor_speed = global_motor_speed;
  left_motor_speed = global_motor_speed;
}

void loop() {
  // The PMW values will control the speed of the motors. This is repeatedly done on each loop
  analogWrite(PWM_right_motor_pin, right_motor_speed);
  analogWrite(PWM_left_motor_pin, left_motor_speed);
  // Read the motor sensors
  sensor_data = 0;
  sensor_value = analogRead(right_sensor_pin);
  if (sensor_value > sensor_threshold) {
    sensor_data = sensor_data | 0x1;  // Set bit 1 in sensor data
  }
  sensor_value = analogRead(middle_right_sensor_pin);
  if (sensor_value > sensor_threshold) {
    sensor_data = sensor_data | 0x2;  // Set bit 2 in sensor data
  }

  sensor_value = analogRead(middle_left_sensor_pin);
  if (sensor_value > sensor_threshold) {
    sensor_data = sensor_data | 0x4;  // Set bit 3 in sensor data
  }
  sensor_value = analogRead(left_sensor_pin);
  if (sensor_value > sensor_threshold) {
    sensor_data = sensor_data | 0x8;  // Set bit 4 in sensor data
  }
  // Handle sensor data. sensor data now could be 0, or 1, or 2. or 3
  handle_sensor_data();
  // Check if there is a Bluetooth command
  if (received) {  // The order of this code is synchronized with Master order, do not change the order
    handle_interrupt();
    received = false;
  }
  generate_effect(); // update the power consumption
  delay(30);
}
/************************************************************
HOW SENSORS ARE USED TO CONTROLL THE MOTION OF AGV
THERE ARE ONLY Four SENSORS AVAILABLE, THIS IS THE MINIM REQUIREMENT
TYPES OF CONTROL:
1.  LINE FOLLOWING: THIS IS VERY IMPORTANT TO KEEP THE AGV ON TRACK
    WHEN MOVING ON A STRAIGHT LINE, IF Middle to the RIGHT SENSOR IS DETECTED, 
    THEN TURN LEFT TO ADJUST TO THE LINE, THIS IS ACHIEVED BY
    THE RIGHT MOTOR IS STOPPED, THE LEFT CONTINUE TO DRIVE, THIS IS REPEATED
    UNTILL THE MIDDLE RIGHT SENSOR IS NO LONGER DETECTING THE LINE
    tHE SAME FOR THE MIDDLE LEFT SENSOR, IT CAUSE TO ADJUST TO LEFT
    SEE CASE 10 AND 2 BELOW
2. 
    WHEN BOTH SENSORS ARE DETECTING A CROSS LINE, THEN
    A)  STOP/BREAK IMMEDIATLY, BUT DUE TO THE PHYSICAL LAWS
        THE AGV WILL CONTINUE MOVING FORWARD, TO STOPP IMMEDIATLY
        REVERS THE MOTION (MOVE BACKWARD) FOR A SPECIFIC TIME
        THIS TIME IS DEPENDING ON THE SPEED OF THE AGV, SHORT
        TIME FOR LOW SPEED AND LONG TIME FOR HIGH SPEED
    B)  NOW THE AGV IS STOPPED ALMOST NEAR THE LINE. ADJUST 
        THE POSITION OF AGV TO BE IN THE CENTER OF THE CROSS
        BY MOVING FORWARD FOR A SPECIFIC OF TIME (EXPREMENTAL)
    C)  ROTATION: ROTATION IS DONE ON A SPECIFIC SPEED/POWER OF MOTOR
        TO ACHIEVE A PREDICTABLE RESULT
        TO ROTATE LEFT: START THE ROTATION UNTIL THE MIDDEL LEFT SENSOR
        DETECT THE LINE, THEN CONTINUE THE ROTATION UNTILL THE 
        MIDDEL LEFT SENSOR WILL NOT SEE THE LINE, THIS MEANS THE LINE 
        IS NOW BETWEEN THE MIDDEL LEFT AND MIDDEL RIGHT SENSOR, DO THE SAME
        WHEN ROTATING RIGHT
************************************************************/
void handle_sensor_data() {
  // store the speed value in K depending on what is the motorspeed 
  if (global_motor_speed == motor_speed[1])
  {
    k = 150;
  }
  else 
  {
    k = 100;
  }
  switch (sensor_data) {
    /***** DO NOTHING **********************/
    case 0:
    case 1:  
    case 5:
    case 6:
    case 8:
    case 9:
    case 10:  // Do nothing
      right_motor_speed = global_motor_speed;
      left_motor_speed = global_motor_speed;
      break;
    /***** Regulation to right *********************/
    case 2: 
      if (!to_back_1)  // when AGV not moving backward. regulation is activated just when AGV moving forward
      {
        analogWrite(PWM_right_motor_pin, 0);
        analogWrite(PWM_left_motor_pin, global_motor_speed + k );

      }
      delay(25); 
      break;
    /***** Regulation to  left *********************/
    case 4:
      if (!to_back_1) // when AGV not moving backward. regulation is activated just when AGV moving forward
      {
        analogWrite(PWM_left_motor_pin, 0);
        analogWrite(PWM_right_motor_pin, global_motor_speed + k);
      }
       delay(25); 
      break;
    /***** STOP AT CROSS *********************/
    case 3:    
    case 7:
    case 11:
    case 12:
    case 13:
    case 14:
    case 15:  
      cross_counter_A++;    // count one more cross
      unsigned long period;
      unsigned long time_now;
      if (to_back_1)        // if the Agv moving back 
      {
        period = 500;
        time_now = millis();
        cross_counter_A--;     // Should not count the cross when moving back 

      }
      else 
      {
        break_agv();       // break AGV
        period = 5;
        time_now = millis();
      }

      move_forward();
      while (millis() < time_now + period) {
        //wait approx. [period] ms to let the agv continue move forward after the horizontal line
      }
      stop_agv();     // stop the Agv and wait for further commands
      to_back_1 = false;
      
      break;
    default:  // Do nothing, this case should not occur
      right_motor_speed = global_motor_speed;
      left_motor_speed = global_motor_speed;
      break;
  }
}

void handle_interrupt() {

  switch (received_data) {
    case 'E':
     SPDR = sensor_data;

    case 'I': // Send the low data when J is recieved 
      SPDR = effect_data_low;
      break;
    case 'J':  // Send the high data when J is recieved 
      SPDR = effect_data_high;
      break;
    case 'K':
      SPDR = 0;  // this is used to clear the SPDR 
      break;
      
    case 'M':   
    if(cross_counter_A > cross_counter_B) // if the Agv passed a cross will send a pincode to master
    {
      SPDR = 236;
      cross_counter_B++;
    }
      
      break;
    case 'N':
      SPDR = 0; // this is used to clear the SPDR 
      break;
    case 'L': // when L recieved send the value R 
      SPDR = R;
      break;
    case '0' ... '9': // when a number between 0 to 9 recieved from the master save it speed_data and update the speed 
      speed_data = SPDR;
      update_speed();
      break;
    case 'a' ... 'g': // when a character between a and g store it in motor_data and handle it.
      motor_data = SPDR;
      handle_motor_commands();
      break;
    default:  
      break;
  }
}

void handle_motor_commands() {
  switch (motor_data) {
    case 'a':  // move the AGV forward
      move_forward();
      break;
    case 'b':  // move the AGV backward
      to_back_1 = true;
      to_back_2 = true;

      move_back();
      break;
    case 'c':  // move the AGV right
      rotate_right();
      //Serial.println("rotate right");
      break;
    case 'd':  // move the AGV left
      rotate_left();
      //Serial.println("rotate left");
      break;
    case 'e':  // stop
      stop_agv();
      break;
    case 'f':  // increase speed
      increase_speed();
      break;
    case 'g':  // decrese speed
      decrease_speed();
      break;
    default:
      Serial.println("unknown motor command");
      break;
  }
}

void rotate_right() {
  // Set the motor speed to 5 this is the best choise for rotation. Eperimentall
  analogWrite(PWM_right_motor_pin, motor_speed[5]  ); 
  analogWrite(PWM_left_motor_pin, motor_speed[5] );
  unsigned long period = 800;
  unsigned long time_now = millis();
  // rotate right for 0.8 s 
  digitalWrite(right_motor_ctrl_pin1, LOW);
  digitalWrite(right_motor_ctrl_pin2, HIGH);
  digitalWrite(left_motor_ctrl_pin1, LOW);
  digitalWrite(left_motor_ctrl_pin2, HIGH);   
  while (millis() < time_now + period) 
  {

  } 
  // read the middle right sensor value 
  sensor_value = analogRead(middle_right_sensor_pin);
   // Turn right until the middle right sensor is not detecting the band
  while (sensor_value < sensor_threshold) {
    digitalWrite(right_motor_ctrl_pin1, LOW);
    digitalWrite(right_motor_ctrl_pin2, HIGH);
    digitalWrite(left_motor_ctrl_pin1, LOW);
    digitalWrite(left_motor_ctrl_pin2, HIGH);
    sensor_value = analogRead(middle_right_sensor_pin);
  }
  // Turn right until the middle right sensor is detecting the band
  sensor_value = analogRead(middle_right_sensor_pin);
  while (sensor_value > sensor_threshold ) {
    digitalWrite(right_motor_ctrl_pin1, LOW);
    digitalWrite(right_motor_ctrl_pin2, HIGH);
    digitalWrite(left_motor_ctrl_pin1, LOW);
    digitalWrite(left_motor_ctrl_pin2, HIGH);
    sensor_value = analogRead(middle_right_sensor_pin);
  }
  // update the direction represented by R 
  R=R+1;
  if(R==5)
  {
    R=1;
  }
  stop_agv(); 
}

void rotate_left() 
{
// Set the motor speed to 5 this is the best choise for rotation. Eperimentall
  analogWrite(PWM_right_motor_pin, motor_speed[5]  );
  analogWrite(PWM_left_motor_pin, motor_speed[5]  );
  unsigned long period = 800;
  unsigned long time_now = millis();
  // rotate left for 0.8 s
  digitalWrite(right_motor_ctrl_pin1, HIGH);
  digitalWrite(right_motor_ctrl_pin2, LOW);
  digitalWrite(left_motor_ctrl_pin1, HIGH);
  digitalWrite(left_motor_ctrl_pin2, LOW); 
  while (millis() < time_now + period) 
  {

  } 
  // read the middle right sensor value
  sensor_value = analogRead(middle_left_sensor_pin);
   // Turn left  until the middle left sensor is not detecting the band
  while (sensor_value < sensor_threshold) {
    digitalWrite(right_motor_ctrl_pin1, HIGH);
    digitalWrite(right_motor_ctrl_pin2, LOW);
    digitalWrite(left_motor_ctrl_pin1, HIGH);
    digitalWrite(left_motor_ctrl_pin2, LOW);
    sensor_value = analogRead(middle_left_sensor_pin);
  }
  // Turn left until the middle left sensor is detecting the band
  sensor_value = analogRead(middle_left_sensor_pin);
  while (sensor_value > sensor_threshold ) {
    digitalWrite(right_motor_ctrl_pin1, HIGH);
    digitalWrite(right_motor_ctrl_pin2, LOW);
    digitalWrite(left_motor_ctrl_pin1, HIGH);
    digitalWrite(left_motor_ctrl_pin2, LOW);
    sensor_value = analogRead(middle_left_sensor_pin);
  }
  // update the direction represented by R 
  R=R-1;
  if(R==0)
  {
    R=4;
  }
  stop_agv();
 
}

void stop_agv() {

  digitalWrite(right_motor_ctrl_pin1, LOW);
  digitalWrite(right_motor_ctrl_pin2, LOW);
  digitalWrite(left_motor_ctrl_pin1, LOW);
  digitalWrite(left_motor_ctrl_pin2, LOW);
}



void move_forward() 
{
  digitalWrite(right_motor_ctrl_pin1, HIGH);
  digitalWrite(right_motor_ctrl_pin2, LOW);
  digitalWrite(left_motor_ctrl_pin1, LOW);
  digitalWrite(left_motor_ctrl_pin2, HIGH);
  delay(200);

}
void move_back() 
{

  digitalWrite(right_motor_ctrl_pin1, LOW);
  digitalWrite(right_motor_ctrl_pin2, HIGH);
  digitalWrite(left_motor_ctrl_pin1, HIGH);
  digitalWrite(left_motor_ctrl_pin2, LOW);
}

void update_speed() {
  speed_index = speed_data - '0';  // covert ASCII to integer
  global_motor_speed = motor_speed[speed_index];
  right_motor_speed = global_motor_speed;
  left_motor_speed = global_motor_speed;

}

void increase_speed() {
  // increase speed
  global_motor_speed = global_motor_speed + speed_factor;
  if (global_motor_speed > 255) global_motor_speed = 255;

}

void decrease_speed() {
  // decrese speed
  global_motor_speed = global_motor_speed - speed_factor;
  if (global_motor_speed < 0) global_motor_speed = 0;

}

//This function will read voltages from amplifier and convert them to power  
void generate_effect() {
  float voltage, current, power; //Defing the variables that will be used for power calculation
  int power_2; //power that will be transmitted to master PSU
  voltage = (5/1024) * analogRead(voltage_pin); //Reading the voltage level from voltage amplifier and converting them to voltage level
  current = (5/1024) * analogRead(current_pin); //Reading the voltage level from current amplifier and converting them to voltage level
  power = voltage * current * 1.5; //Calculating the power and scaling them with 1.5 to get the correct power level

  power_2 = (int) power; // Converting the power level to integer data type
  // ----------- will be sent in next second
  effect_data_low = power_2; //Splitting the transmition power to lower bits
  effect_data_high = (power_2>> 8) & 0xff; //Splitting the transmission power to higher bits
}

//This function breaks the AGV
void break_agv() 
{
  //Moving the AGV backward depending on speed
  unsigned long period = 50;
  unsigned long time_now = millis();

  if (global_motor_speed > motor_speed[4]) 
  {
    period = 250;
  }
  else if (global_motor_speed < motor_speed[4]) 
  {
    period = 30;
  } 
  // TRY TO BREAK, FOR A  SHORT TIME
  move_back();
  while (millis() < time_now + period) 
  {
    //wait approx. [period] ms to let the agv contunue move forward after the horizental line
  }
  stop_agv();
  
}