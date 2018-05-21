// hint! => delete Serial.begin(9600) and connected declarations after debugging
//          to reduce dynamic memory by 193 bytes (9%)
//
// ! you need next libraries:
//    MQ135 (from github), Adafruit_BMP280_Library, DHT_sensor_library,
//    RTClib, Adafruit_SSD1306, Adafruit_GFX_Library, Adafruit_Sensor (from github).

#include <SoftwareSerial.h>
#include <DHT.h>
#include <DHT_U.h>
#include <MQ135.h>
#include "RTClib.h"
#include <Adafruit_SSD1306.h>
#include <Adafruit_BMP280.h>

#define OLED_RESET 4
Adafruit_SSD1306 display(OLED_RESET);

#define NUMFLAKES 10
#define XPOS 0
#define YPOS 1
#define DELTAY 2

#define LOGO16_GLCD_HEIGHT 16
#define LOGO16_GLCD_WIDTH  16

// i'm using 126x64 display.
// but coz of high use of dynamic memory in 126x64 library mode
// here i choose x32 height,
// so now it 58% of dynamic memory instead of 83% and not working sketch!
#if (SSD1306_LCDHEIGHT != 32)
#error("Height incorrect, please fix Adafruit_SSD1306.h!");
#endif

Adafruit_BMP280 bmp; // I2C

SoftwareSerial SerialBLE(7, 6); // RX,TX on arduino board

#define DHTPIN A0
#define DHTTYPE DHT22
DHT dht(DHTPIN, DHTTYPE);

#define MQ135_PIN A2
MQ135 gasSensor = MQ135(MQ135_PIN);

int relayPin = 3;
bool light_always = false;
#define BlueLedPin 12   // power on indicator
#define GreenLedPin 8   // bluetooth connected indicator

int pirInputPin = 4;  // choose the input pin (for PIR sensor)
int pir = LOW;        // we start, assuming no motion detected

RTC_DS3231 rtc;

#define REDPIN 10 // RGB Strip pins
#define GREENPIN 11
#define BLUEPIN 9
int r_in = 100, g_in = 100, b_in = 100; // RGB color define
bool done = false;

long previousMillis_1 = 0; // will store last time status was updated
long previousMillis_2 = 0;
long previousMillis_3 = 0;
long previousMillis_4 = 0;
long previousMillis_5 = 0;
long previousMillis_6 = 0;
int period = 5000;

float h = -1, t2 = -1, a = -1, d = -1, t1 = -1;
int r = -1, p = -1;
int flag = 1; // update display
bool blt = false;
char ch_data[4]; // store incoming date from BLT

// temperature correction after 10 min work, because of self heating !
bool correction_t = true;
bool correction_delta = true;
float t1_zero, t2_zero;
float delta_t1 = 0;
float delta_t2 = 0;

void setup() {

  // actions with display
  display.begin(SSD1306_SWITCHCAPVCC, 0x3C);  // initialize with the I2C addr 0x3C (for the 128x64)
  display.display();
  //delay(10);
  display.clearDisplay(); // Clear the buffer.
  display.setTextSize(1);
  display.setTextColor(WHITE);
  display.setCursor(0, 0);
  display.println("Hello, world!");
  display.setTextColor(BLACK, WHITE); // 'inverted' text
  display.println(3.141592);
  display.setTextSize(2);
  display.setTextColor(WHITE);
  display.print("0x");
  display.println(0xDEADBEEF, HEX);
  display.display();

  rtc.begin();  // start clock
  bmp.begin(0x76);  // start BMP280 with 0x76 i2c address
  //if (! rtc.begin()) {
  //  Serial.println("Couldn't find RTC");
  //  while (1);
  //}

  // declare digital pins
  pinMode(relayPin, OUTPUT);
  digitalWrite(relayPin , HIGH);    // turn OFF relay !!!
  // it depends on connection to relay - green led mast be OFF
  // in that case relay is OFF -> no power consuming
  pinMode(pirInputPin, INPUT);      // declare rip-sensor as input
  digitalWrite(pirInputPin , LOW);
  pinMode(BlueLedPin, OUTPUT); // Blue Led
  digitalWrite(BlueLedPin, HIGH);
  pinMode(GreenLedPin, OUTPUT); // green led

  // UART speed
  //Serial.begin(9600);
  SerialBLE.begin(9600);
}

void loop() {

  // if the data came from SerialBLE
  ListenBlt();

  unsigned long currentMillis = millis();

  if (currentMillis - previousMillis_1 >= period) { // period from android app
    previousMillis_1 = currentMillis;

    if (blt) {
      // transfer date to android app over bluetooth
      Transmit(t1, t2, h, a, d, p, r);
    }
  }

  if (currentMillis - previousMillis_2 >= 1000) { // timer = 1sec
    previousMillis_2 = currentMillis;

    // update PIR sensor status
    PIR(p);

    // set the color in Rgb Strip
    if (light_always) {
      if (done) {
        RGBStrip(r_in, g_in, b_in);
        done = false;
      }
    }

    // update the text on the display
    switch (flag) {
      case 1: RTC(); break;
      case 2: Display_th(t1, h); break;
      case 3: Display_ap(a, d); break;
    }
  }

  // if no motion detected in last 30s -> turn LED OFF
  if (currentMillis - previousMillis_3 >= 30000) { // timer = 30sec
    previousMillis_3 = currentMillis;
    if (!light_always) {
      if (pir == LOW) {
        RGBStrip(0, 0, 0);
        digitalWrite(relayPin, HIGH);  // turn LED OFF
      }
    }
  }

  // update text on the display every 5s (by setting flag)
  if (currentMillis - previousMillis_4 >= 5000) { // timer 5 sec for every action
    previousMillis_4 = currentMillis;

    switch (flag) {
      case 1: flag = 2; break;
      case 2: flag = 3; break;
      case 3: flag = 1; break;
    }
  }

  if (currentMillis - previousMillis_5 >= period) {
    previousMillis_5 = currentMillis;

    // update sensors status
    PinStatus();

    t1 = t1 - delta_t1;
    t2 = t2 - delta_t2;

    // temperature correction: get start number (room/PCB temperature)
    if (correction_t) {
      t1_zero = t1;
      t2_zero = t2;
      correction_t = false;
    }
  }

  // temperature correction: get offset
  if (correction_delta) {
    if (currentMillis - previousMillis_6 >= 600000) { // timer = 10 min
      delta_t1 = t1 - t1_zero;
      delta_t2 = t2 - t1_zero;
      correction_delta = false;
    }
  }
}

void ListenBlt() {
  if (SerialBLE.available() > 0) {
    int count = 0;
    for (int i = 0; i < 4; i++) {
      // read input bytes
      ch_data[i] = SerialBLE.read();
      delay(10);    // magic! for stable receiving
      //Serial.println("-----------");
      //Serial.println(ch_data[i], DEC);
      count++;
      // i use only 2 bytes (2^16-1), ex-pl:  0010 0011 1101 1100  = 9180 (int)
      if (count == 4) {
        // convert byte to int ;  may be:  int i = atoi(intBuffer) ?
        int a = (long)(unsigned char)(ch_data[0]) << 24 |
                (long)(unsigned char)(ch_data[1]) << 16 |
                (int)(unsigned char)(ch_data[2]) << 8 |
                (int)(unsigned char)(ch_data[3]);

        //Serial.println(a, DEC);
        GetCommand(a);
        count = 0;
      }
    }
  }
}

void GetCommand(int in) {
  // We obtain the pin number by integer division (we find 1 number == pin)
  // and the action we need by obtaining the remainder of the division by 1000.
  switch (in / 1000) {
    case 1: // start data transfer
      Transmit(t1, t2, h, a, d, p, r);
      blt = true;
      period = (in % 1000) * 1000;  // in % 1000 = 5 -> 5sec = 5000ms
      digitalWrite(GreenLedPin, HIGH);
      //Serial.println(blt);
      //Serial.println(period);
      break;
    case 2: // stop date transfer
      blt = false;
      digitalWrite(GreenLedPin, LOW);
      //Serial.println(blt);
      break;
    case 3:
      // 3002 <= in <= 3000, relayPin + ON always / OFF
      switch (in % 1000) {
        case 2:
          digitalWrite(relayPin , LOW);
          light_always = true;
          RGBStrip(r_in, g_in, b_in);
          break;
        case 0:
          RGBStrip(0, 0, 0);
          digitalWrite(relayPin , HIGH);
          light_always = false;
          break;
      }
      break;
    case 10:  // 10xxx - red pin 10 to value xxx
      r_in = in % 1000;
      done = false; // continue reading \ read 2 more param.
      break;
    case 11:  // 11xxx - green pin 11 to value xxx
      g_in = in % 1000;
      done = false;
      break;
    case 13:  // 13 = code for blue pin 9, coz 90255 does't get in to 2 bytes :(
      b_in = in % 1000;
      done = true;  // allow to light ON RGBStrip
      break;
  }
}

void Display_th(float t, float h) { // show Temperature and Humidity
  display.clearDisplay();
  display.setTextSize(2);
  display.setCursor(0, 0);
  display.print(t);
  display.println(" *C");
  display.print(h);
  display.println(" %");
  display.display();
}

void Display_ap(float a, float d) { // show Air and Pressure
  display.clearDisplay();
  display.setTextSize(2);
  display.setCursor(0, 0);
  display.print(a);
  display.println(" air");
  display.print(d);
  display.println(" mm");
  display.display();
}

void PinStatus() {
  t1 = dht.readTemperature(); // deviation from standard value
  // or dht.readTemperature(true) for Fahrenheit
  h = dht.readHumidity();     // deviation from standard value
  //a = gasSensor.getCorrectedPPMZero(t1, h); // my 'hardCode'
  a = gasSensor.getCorrectedPPM(t1, h);
  r = digitalRead(relayPin);  // relayState
  p = digitalRead(pirInputPin); // pirState
  t2 = bmp.readTemperature(); // deviation from standard value
  d = bmp.readPressure() * 0.0075006; // to mm.rs.

  //  Serial.println(
  //    "t" + (String)t1 + "z" +
  //    "j" + (String)t2 + "q" +
  //    "h" + (String)h + "y" +
  //    "a" + (String)a + "x" +
  //    "d" + (String)d + "s" +
  //    "p" + (String)p + "u" +
  //    "l" + (String)r + "w" +
  //    "E");
}

void Transmit(float t1, float t2, float h, float a, float d, int p, int r) {
  SerialBLE.println(
    "t" + (String)t1 + "z" +
    "j" + (String)t2 + "q" +
    "h" + (String)h + "y" +
    "a" + (String)a + "x" +
    "d" + (String)d + "s" +
    "p" + (String)p + "u" +
    "l" + (String)r + "w" +
    "E"); // end of the line
}

void PIR(int val) {
  if (val == HIGH) {  // check if the input is HIGH
    digitalWrite(relayPin, LOW);  // turn LED ON
    RGBStrip(100, 100, 100); // Color = Red
    if (pir == LOW) {
      //Serial.println("Motion detected!");
      pir = HIGH;
    }
  } else {
    if (pir == HIGH) {
      //Serial.println("Motion ended!");
      pir = LOW;
    }
  }
}

void RTC() {
  DateTime now = rtc.now();

  display.clearDisplay();
  display.setTextSize(2);
  display.setCursor(0, 0);
  display.print(now.hour());
  display.print(":");
  display.print(now.minute());
  display.print(":");
  display.println(now.second());
  display.print(now.day());
  display.print(".");
  display.print(now.month());
  display.print(".");
  display.print(now.year());
  display.display();
}

void RGBStrip(int r, int g, int b) {
  //r_in = r; // save last state
  //g_in = g;
  //b_in = b;

  analogWrite(REDPIN , r);
  analogWrite(GREENPIN , g);
  analogWrite(BLUEPIN , b);
  //Serial.println("RGB: " + (String)r + "." + (String)g + "." + (String)b);
}
