package valentin8dev.by.MeteoStation;

/**
 * Defines constants
 */
public interface Constants {

    // Message types sent from the BluetoothService Handler
    int MESSAGE_STATE_CHANGE = 1;
    int MESSAGE_READ = 2;
    int MESSAGE_WRITE = 3;
    int MESSAGE_DEVICE_NAME = 4;
    int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothService Handler
    String DEVICE_NAME = "device_name";
    String TOAST = "toast";

    String turnOnLight_ru = "Включить свет";       // send 3002 (pin 3, high),  code '2', coz i torning On RGBStrip forcibly!
    String turnOffLight_ru = "Выключить свет";       // send 3000 (pin 3, low)
    String turnOnLight_eng = "Turn on the Lights";       // send 3002 (pin 3, high),  code '2', coz i torning On RGBStrip forcibly!
    String turnOffLight_eng = "Turn Off the Lights";       // send 3000 (pin 3, low)
}