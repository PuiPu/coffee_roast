# MVVM arch.
## model
### BLE model
- interface
- function
	- (dis)connect to BLE server
		- bluetooth scanner
		- GATT server
			- on change
			- 
	- update temperature data (livedata)
### line chart model
- interface
- function
	- draw point & line in chart
	- update line chart with new data 
## view
- main activity
- out off range activity
- save file activity
- browse history activity

## view model
( --- same as model --- )
# module
```mathematica
ActivityMain/
├── /
│   ├── File1.txt
│   └── File2.txt
├── Folder2/
│   ├── Subfolder1/
│   │   └── File3.txt
│   └── File4.txt
└── File5.txt

ActivityStoreFile/
├── /
│   ├── File1.txt
│   └── File2.txt
├── Folder2/
│   ├── Subfolder1/
│   │   └── File3.txt
│   └── File4.txt
└── File5.txt
```
```mathematica
CLASS BLEClient/
├── Variable/
│   ├── DeviceName (ESP32)
│   ├── UUID SPP
│   ├── bluetoothAdapter
│   ├── bluetoothSocket
│   ├──
│   └── 
├── Function/
│   ├── Subfolder1/
│   │   └── File3.txt
│   └── File4.txt
└── File5.txt
CLASS LineChart/
├── Variable/
│   ├── lineChart
│   └── LineDataSet
│   └── LineData
│   └── Description
├── Constructor/
│   ├── LineChart()
├── Function/
│   ├── updateChart()/
│   │   └── 
│   └──  
└── 
```