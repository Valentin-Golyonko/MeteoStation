# MeteoStation
The application reads data over bluetooth from a home weather station built on arduino nano v3.

<p><b>UPD</b> (08.05.18): Project closed or better to say splitted into 2 different (<a href="https://github.com/Valentin-Golyonko/RGBStripControl_Arduino">1</a> and <a href="https://github.com/Valentin-Golyonko/esp8266_inHome_weather/">2</a>) because of high power consuming (around 260mA, arduino nano Imax = 200mA) and so work limitation for chip (ex: 1 strange bug on pin 9 for RGB-Strip).</p>

<p>Arduino firmware file:
<a href="https://github.com/Valentin-Golyonko/MeteoStation/blob/master/meteo_station/meteo_station.ino">meteo_station.ino</a></p>

<p>All components before soldering:</p>
<img src="https://github.com/Valentin-Golyonko/MeteoStation/blob/master/meteo_station/before_soldering.jpg" alt="befor_soldering">

<p>Assembled</p>
<img src="https://github.com/Valentin-Golyonko/MeteoStation/blob/master/meteo_station/weather_station_front.jpg" alt="befor_soldering">
<img src="https://github.com/Valentin-Golyonko/MeteoStation/blob/master/meteo_station/weather_station_back.jpg" alt="befor_soldering">

<p>The project uses the following components:</p>
<p>&nbsp&nbsp  Arduino Nano v3, 2.90$</p>
<p>&nbsp&nbsp  DHT-22 - digital temperature and humidity sensor, 2.52$</p>
<p>&nbsp&nbsp  MQ-135 - air quality and hazardous gas detection sensor, 0.80$</p>
<p>&nbsp&nbsp  128x64 OLED LCD 0.96" (i2c), 2.33$</p>
<p>&nbsp&nbsp  BMP280 - digital barometric pressure altitude sensor (i2c), 0.81$</p>
<p>&nbsp&nbsp  DS3231 AT24C32 - clock memory module (i2c), 0.89$</p>
<p>&nbsp&nbsp  1-channel 5V relay, 0.52$</p>
<p>&nbsp&nbsp  RGB LED Strip 5050 DC12V 5m, 7.81$</p>
<p>&nbsp&nbsp  HC-06 Bluetooth (v2.0), 2.78$</p>
<p>&nbsp&nbsp  HC-SR501 - infrared PIR motion sensor, 0.80$</p>
<p>&nbsp&nbsp  PCB 5x7cm double-side, 0.30$</p>

<p>&nbsp&nbsp&nbsp    Summ = 22.84$ (or 15.03$ without Led Strip)</p>
