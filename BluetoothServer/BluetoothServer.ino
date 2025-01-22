#include <BLEDevice.h>
#include <BLEUtils.h>
#include <BLEServer.h>
#include <SPI.h>
#include "max6675.h" // MAX6675 驅動庫

// BLE UUID
#define SERVICE_UUID "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
#define CHARACTERISTIC_UUID "beb5483e-36e1-4688-b7f5-ea07361b26a8"

// MAX6675 引腳設定
int thermoDO = 27; // MISO (Data Out)
int thermoCS = 14;  // CS (Chip Select)
int thermoCLK = 12; // SCK (Clock)
MAX6675 thermocouple(thermoCLK, thermoCS, thermoDO);

BLECharacteristic *pCharacteristic;

void setup() {
  Serial.begin(115200);

  // 初始化 MAX6675
  Serial.println("Initializing MAX6675...");
  delay(500);

  // 初始化 BLE
  BLEDevice::init("ESP32_Temperature");
  BLEServer *pServer = BLEDevice::createServer();

  BLEService *pService = pServer->createService(SERVICE_UUID);

  pCharacteristic = pService->createCharacteristic(
    CHARACTERISTIC_UUID,
    BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_NOTIFY
  );

  pCharacteristic->setValue("No Data"); // 初始值
  pService->start();

  BLEAdvertising *pAdvertising = pServer->getAdvertising();
  pAdvertising->start();

  Serial.println("BLE Server is running. Connect to 'ESP32_Temperature'.");
}

void loop() {
  // 讀取溫度
  double temperature = thermocouple.readCelsius();

  // 打印到序列監視器
  Serial.print("Temperature: ");
  Serial.print(temperature);
  Serial.println(" °C");

  // 更新 BLE 特性值
  char tempStr[10];
  snprintf(tempStr, sizeof(tempStr), "%.2f", temperature); // 格式化溫度值
  pCharacteristic->setValue(tempStr); // 將溫度值寫入特性
  pCharacteristic->notify(); // 通知客戶端有新數據

  delay(1000); // 每 2 秒更新一次
}
