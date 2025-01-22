// this example is public domain. enjoy!
// https://learn.adafruit.com/thermocouple/

#include "max6675.h"

// 定義 MAX6675 的引腳
// (GND, VCC, SCK, CS, SO) = (5.5v, 0v, pin12, pin14, pin27)
#define SCK 12 // Serial Clock (SCK) pin
#define CS 14   // Chip Select (CS) pin
#define SO 27  // Serial Out (SO) pin

MAX6675 thermocouple(SCK, CS, SO);

void setup() {
  Serial.begin(9600);

  Serial.println("MAX6675 test");
  // wait for MAX chip to stabilize
  delay(500);
}

void loop() {
  // print time
  unsigned long currentMillis = millis();
  Serial.print("Time since start (ms): ");
  Serial.print(currentMillis);
  Serial.print("=> ");
  // print temperature
  Serial.print("C = "); 
  Serial.println(thermocouple.readCelsius());
  // Serial.print("F = ");
  // Serial.println(thermocouple.readFahrenheit());
 
  // For the MAX6675 to update, you must delay AT LEAST 250ms between reads!
  delay(1000);
}
