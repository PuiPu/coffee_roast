#include <BLEDevice.h>
#include <BLEUtils.h>
#include <BLEServer.h>
#include <BLE2902.h>
#include <SPI.h>
#include "max6675.h" // MAX6675 驅動庫

// BLE UUID
#define SERVICE_UUID        "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
#define CHARACTERISTIC_UUID "beb5483e-36e1-4688-b7f5-ea07361b26a8"

// MAX6675 引腳設定
int thermoDO = 27; // MISO (Data Out)
int thermoCS = 14; // CS (Chip Select)
int thermoCLK = 12; // SCK (Clock)
MAX6675 thermocouple(thermoCLK, thermoCS, thermoDO);

BLECharacteristic *pCharacteristic;
BLEServer *pServer;
BLEAdvertising *pAdvertising;

bool deviceConnected = false;
bool oldDeviceConnected = false;

// BLE 伺服器回調函數
class MyServerCallbacks : public BLEServerCallbacks {
    void onConnect(BLEServer* pServer) override {
        Serial.println("BLE Client Connected!");
        deviceConnected = true;
    }

    void onDisconnect(BLEServer* pServer) override {
        Serial.println("BLE Client Disconnected!");
        deviceConnected = false;
        pAdvertising->start();  // BLE 斷開後自動廣播
    }
};

void setup() {
    Serial.begin(115200);

    // 初始化 MAX6675
    Serial.println("Initializing MAX6675...");
    delay(500);

    // 初始化 BLE
    BLEDevice::init("ESP32_Temperature");
    pServer = BLEDevice::createServer();
    pServer->setCallbacks(new MyServerCallbacks()); // 設置回調

    BLEService *pService = pServer->createService(SERVICE_UUID);

    pCharacteristic = pService->createCharacteristic(
        CHARACTERISTIC_UUID,
        BLECharacteristic::PROPERTY_NOTIFY  |
        BLECharacteristic::PROPERTY_INDICATE |
        BLECharacteristic::PROPERTY_READ | 
        BLECharacteristic::PROPERTY_WRITE
    );

    // 設置 Notify 屬性 (重要)
    pCharacteristic->addDescriptor(new BLE2902());

    // 啟動服務
    pService->start();

    // 設置 BLE 廣播
    pAdvertising = pServer->getAdvertising();
    pAdvertising->addServiceUUID(SERVICE_UUID);
    pAdvertising->setScanResponse(true);
    pAdvertising->setMinPreferred(0x06);  // 設定低功耗模式
    pAdvertising->setMinPreferred(0x12);  // 確保相容 Android
    pAdvertising->start();

    Serial.println("BLE Server is running. Connect to 'ESP32_Temperature'.");
}

void loop() {
    if (deviceConnected) {  // 只有當設備已連線時才更新數據
        // 讀取溫度
        float temperature = thermocouple.readCelsius();

        // 打印到序列監視器
        Serial.print("Temperature: ");
        Serial.print(temperature);
        Serial.println(" °C");

        // 更新 BLE 特性值
        char tempStr[10];
        snprintf(tempStr, sizeof(tempStr), "%.2f", temperature);
        pCharacteristic->setValue(tempStr);
        pCharacteristic->notify(); // 通知客戶端有新數據
    }

    // 監控連線狀態
    if (!deviceConnected && oldDeviceConnected) {
        delay(500);  // 等待設備完全斷開
        pServer->startAdvertising(); // 重新開始廣播
        Serial.println("Start advertising...");
        oldDeviceConnected = deviceConnected;
    }

    if (deviceConnected && !oldDeviceConnected) {
        Serial.println("Device connected!");
        oldDeviceConnected = deviceConnected;
    }

    delay(1000); // 每秒更新一次數據
}
