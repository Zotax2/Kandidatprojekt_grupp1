#include <SPI.h>
#include <SoftwareSerial.h>
#include <Wire.h> 
#include <LiquidCrystal_I2C.h>
/************************** SPI Pins **************************
Pin 11: Master Out Slave In (MOSI) – the connection for the master device to send data to the slave device
Pin 12: Master In Slave Out (MISO) – the connection for the slave device to send data back to the master device
Pin 13: Serial Clock (SCLK) – the line that carries the clock pulse generated by the master device
Pin 10: Slave Select/Chip Select (SS/CS) – the connection used by the master device to inform the slave device 
that it will send or request data. The SS/CS pin should be set to LOW to inform the slave that the master 
will send or request data. Otherwise, it is always HIGH.
*******************************************************************/
const byte SPin = 10; // SPI Chip Select, Slave Select
/************************** LED Pins **************************
Pin 2: undefined command
pin 3: picked up sccesfull
pin 4: pick up failed
pin 5: pick up led
*******************************************************************/
const byte odef = 2; 
const byte succes = 3;
const byte error = 4;
/************************** Distance mesuermant Pins **************************
Pin 6: output to trigg the sesnor
pin 7: input/ microcontroller 
*******************************************************************/
const byte trig = 6;
const byte echo = 7;
float duration = 0;
float distance_ultra = 0;  
const byte ljus_sensor = 0;
const byte lamp = 5;

LiquidCrystal_I2C lcd(0x3f,20,4);

// connect Bluetooth RX/TX pins to Arduino device pins
const byte BTrxPin = 8;
const byte BTtxPin = 9;
SoftwareSerial Bluetooth(BTrxPin, BTtxPin);  // RX, TX
// connect Master/Slave RX/TX pins to Arduino pins, note that Naster RX shall be connected to Slave TX, and verse versa

volatile byte PC_Command; // This is the received byte from Bluetooth device
volatile bool acknowledge = false; // indicate if a PC command is acked
volatile bool picked = false; // this to indicate if picked or not
uint8_t motor_data = 0; // Data to be sent to Slave Arduino
uint8_t dummy_data; // this is used to capture dummy data from SPI Data Register
uint16_t effect_data_low = 0;  // the low power data and the high will be combined and give the power in total
uint16_t effect_data_high = 0;
// The following are used to store the X, Y, and distance info
int x_corr;
int y_corr;
uint8_t distance;
int ljus_value;  // this will store the value read from the photo transistor
int counter_try; // will count how many attempts to pick the box
int R ; // This will store the value we get from the slave that represents the direction
// Power data to be received from Slave Arduino
int power = 0;
// The compass
enum compass { North,
               South,
               East,
               West };
enum compass direction;
// Cross data counter is advanced each time a CROSS is detected by the sensors on the slave side
// When cross_counter++, means X++, Y++, X--, Y-- depending on the compass


// Internal functions
void handle_pc_commands();
bool led_blink();
void update_corr();
void read_power();
void read_distance();
void update_direction();
void pick_box();


void setup() {
  // Open serial port
  Serial.begin(9600);
  lcd.init();                 //Init the LCD
  lcd.backlight();
  lcd.clear();
  lcd.print("Effekt = ");  // this will program once and will not disapear
  // begin bluetooth serial port communication
  Bluetooth.begin(9600);
  Serial.println("\n");
  Serial.println("Bluetooth started at 9600 - pin 8 & 9\n");
  pinMode(SPin, OUTPUT);                // set the SS pin as an output
  // set all the Leds to output
  pinMode(error, OUTPUT);  
  pinMode(succes, OUTPUT);
  pinMode(odef, OUTPUT);
  pinMode(lamp, OUTPUT);
  pinMode(ljus_sensor, INPUT);
  SPI.begin();                          // initialize the SPI library
  SPI.setClockDivider(SPI_CLOCK_DIV8);  //Sets clock for SPI communication at 8 (16/8=2Mhz)
  digitalWrite(SPin, HIGH);             // slave select is default high
  pinMode(trig, OUTPUT);
  lcd.home();
  

  // default orientation
  direction = East;
  x_corr = 1;
  y_corr = 2;
  ljus_value = 0;
  counter_try = 0;
  
}

void loop() {
  // READ FROM PC COMMANDS
  if (Bluetooth.available() > 0) {
    PC_Command = Bluetooth.read();   
    if (PC_Command > 35) {
      handle_pc_commands();  // PC commands start from a
      Bluetooth.write(PC_Command);
      if (acknowledge)
        Bluetooth.write("A\n");
      else
        Bluetooth.write("U\n");
    }
  }
  read_power();       // update and display the power
  read_distance();    // read the distance
  update_corr();      // This function updates both the direction and the cooredinates
  // To send the status to Pc through bluetooth
  Bluetooth.write('X');
  Bluetooth.write(x_corr );
  Bluetooth.write('Y');
  Bluetooth.write(y_corr );
  Bluetooth.write("\n");
  Bluetooth.write('D');
  Bluetooth.write(distance_ultra);
  Bluetooth.write("\n");
  Bluetooth.write('P');
  Bluetooth.write(power);
  Bluetooth.write("\n");
  Bluetooth.write('R');
  if(direction == East){Bluetooth.write('E');}
  if(direction == West){Bluetooth.write('W');}
  if(direction == South){Bluetooth.write('S');}
  if(direction == North){Bluetooth.write('N');}
  Bluetooth.write("\n");

  delay(300);
}
void handle_pc_commands() {
  switch (PC_Command) 
  {
    case 'a' ... 'g':  // send motor command to the slave unit
      motor_data = PC_Command;
      digitalWrite(SPin, LOW);  // SS is pin 10
      dummy_data = SPI.transfer(motor_data);
      digitalWrite(SPin, HIGH);
      delay(100);
      acknowledge = true;
      break;
    case '0' ... '9':  // send motor speed factor
      motor_data = PC_Command;
      digitalWrite(SPin, LOW);  // SS is pin 10
      dummy_data = SPI.transfer(motor_data);
      digitalWrite(SPin, HIGH);
      delay(100);
      acknowledge = true;
      break;
    case 'l':  // Pick up command
      Bluetooth.write("A\n");     
      while (!picked && counter_try <=4)  // we should try to pick at least 5 times if the pick up was unsuccessful
      {
        counter_try++;                     
        pick_box();                       // the function to pich the box 
      }
      // this will put the AGV in better position to further commands
      dummy_data = SPI.transfer('a');     
      delay(880);
      dummy_data = SPI.transfer('e');
      delay(100);

      if (picked)   // if successful pick
      {
        digitalWrite(succes, HIGH);  // Set the LED high for half secund
        delay (500);
        digitalWrite(succes, LOW);
        Bluetooth.write('B');        // Send B = successful to PC
        Bluetooth.write("\n"); 
      }

      if (!picked)   // if unsuccessful pick  
      {
        digitalWrite(error, HIGH);  // Set the Led high for half secund
        delay (500);
        digitalWrite(error, LOW);
        Bluetooth.write('C');       // Send C = unsuccessful to PC
        Bluetooth.write("\n");        
      }
      // return to default value
      picked = false;               
      counter_try = 0;
      
      break;

    case 'j':
      
      y_corr = 6;      // Change the coordinates to (1,6)
      
      break;
  }
}

void update_corr()     
{
  int new_value;
  digitalWrite(SPin, LOW); 
  // request the pin code from the slave and store it in new_value
  dummy_data = SPI.transfer('M');
  delay(100);
  new_value = SPI.transfer('N');
  delay(100);
   // if the code is correct do the following
  if (new_value == 236) { 

    switch (direction) {      // update the x and the y coordinates depending on direction
      case North:
        y_corr = y_corr + 1;
        break;
      case South:
        y_corr = y_corr - 1 ;
        break;
      case East:
        x_corr = x_corr + 1;
        break;
      case West:
        x_corr = x_corr - 1;
        break;
      default:
        break;
    }

  }
  update_direction();        // we need to update the direction here 
  digitalWrite(SPin, HIGH);  
}

void read_power()  // Read power from slave
{
  digitalWrite(SPin, LOW);
  // request the data low and high for the power from slave 
  dummy_data = SPI.transfer('I');
  delay(100);
  effect_data_low = SPI.transfer('J');
  delay(100);
  effect_data_high = SPI.transfer('K');
  digitalWrite(SPin, HIGH);
  delay(100);
  power = (effect_data_high << 8) | effect_data_low;   // combining the data and stor in power
  set.cursor(8,0);
  lcd.print(power);                 // display the value on LCD 
}

void update_direction()       
{
  // rquest the direction value from the slave and store it R 
  dummy_data = SPI.transfer('L');
  delay(100);
  R = SPI.transfer('N');
  delay(100);
 // translating the R value to directions
  switch(R)
  {
   case 1 : 
   direction = East;
   break;

   case 2 : 
   direction = South;
   break;
   case 3 : 
   direction = West;
   break;

   case 4 : 
   direction = North;
   break;

   default:
   break;
  }

}

void read_distance()    // this function will read the distance 
{
  // this will send a puls signal to trigger the ultra sound sensor
	digitalWrite(trig, LOW);   
	delayMicroseconds(2);  
	digitalWrite(trig, HIGH);  
	delayMicroseconds(10);   // bandwidth 10 ms
	digitalWrite(trig, LOW);  
  // this code will calculate the distance s = v/t
  duration = pulseIn(echo, HIGH);     
  distance_ultra = (duration*.0343)/2; 
  delay(100); 

}

void pick_box()
{
  float i = 6;  
  // set the speed to lowest possible = 1  
  dummy_data = SPI.transfer('1');
  delay(100);
  // move forward 
  dummy_data = SPI.transfer('a');
  delay(100); 
  // read the distance 
  read_distance();
  while (distance_ultra > i)  // stop when distance is 6 ! in reality it will not stop immediatly som the it will stop about 3 cm 
  {
    read_distance();
  }
  dummy_data = SPI.transfer('e');  
  delay(100);
  digitalWrite(lamp, HIGH);   // avtivate led light for 3 s
  delay (3000);
  digitalWrite(lamp,LOW);
  ljus_value = analogRead(ljus_sensor);    // read the ligh sensor value
  if (ljus_value > 70)                     // if the value bigger than the threshold = 70
  {
    picked = true;                         // store true in the boolean picked
    
  }
  dummy_data = SPI.transfer('b');          // move backward 4s, more than enough to reach the previous crossing 
  delay(4000);
}