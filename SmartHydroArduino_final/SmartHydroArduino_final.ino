#include <SD.h>
#include <SPI.h>
#include <WiFiEsp.h>
#include <WiFiEspClient.h>
#include <WiFiEspServer.h>
#include <DHT.h>
#include "DFRobot_PH.h"
#include "DFRobot_EC10.h"
#include <arduino-timer.h>
#include <Wire.h>
#include "PredictEC.h"
 
//HIGH IS LOW
//LOW IS HIGH
//WE DON'T KNOW EITHER
 
// WiFi network settings
char ssid[] = "AutoGrow";       // newtork SSID (name). 8 or more characters
char password[] = "Wormp33#";  // network password. 8 or more characters
String message = "";
 
#include <EEPROM.h>
#include "EC.h"
#include "pH.h"
#include "Humidity.h"
#include "Temperature.h"
Eloquent::ML::Port::RandomForestEC ForestEC;
Eloquent::ML::Port::RandomForestpH ForestPH;
Eloquent::ML::Port::RandomForestHumidity ForestHumidity;
Eloquent::ML::Port::RandomForestTemperature ForestTemperature;
Eloquent::ML::Port::LinearRegression model;
 
WiFiEspServer server(80);
RingBuffer buf(16);
 
#define FLOW_PIN 2
#define LIGHT_PIN A7
#define EC_PIN A8
#define PH_PIN A9
#define DHTTYPE DHT22
#define LED_PIN 4
#define FAN_PIN 5
#define PUMP_PIN 6
#define EXTRACTOR_PIN 7  
#define DHT_PIN 8
#define PH_UP_PIN 9
#define PH_DOWN_PIN 10
#define EC_UP_PIN 11
#define EC_DOWN_PIN 12
 
File myFile;
int pinCS = 53;
DFRobot_PH ph;
DHT dht = DHT(DHT_PIN, DHTTYPE);
 
const unsigned long SIXTEEN_HR = 57600000;
const unsigned long PUMP_INTERVAL = 5000;
const unsigned long EIGHT_HR = 28800000;
const unsigned long FOUR_HR = 14400000;
const unsigned long THREE_HR = 108000000;
 
DFRobot_EC10 ec;
auto timer = timer_create_default();
float temperature;
float humidity;
float ecLevel;
float phLevel;
float lightLevel;
float flowRate;
float voltage, phValue, ecValue, temp= 25;
volatile int pulseCount = 0;
unsigned long currentTime, cloopTime;
 
void setup() {
  Serial.begin(9600);
  Serial2.begin(115200);
  
  // Initialize WiFi module using Serial1 (This line remains)
  Serial1.begin(115200);  // Use Serial1 for communication with ESP8266
  WiFi.init(&Serial2);
 
  // You can comment out or remove the following check
  /*
  if (WiFi.status() == WL_NO_SHIELD) {
    Serial.println("WiFi module not detected. Please check wiring and power.");
    while (true)
      ;  // Don't proceed further
  }
  */
 
  // If the ESP-01 is not connected, this will still allow the rest of the setup to continue
  Serial.print("Attempting to start AP ");
  Serial.println(ssid);
 
  IPAddress localIp(192, 168, 8, 14);  // create an IP address
  WiFi.configAP(localIp);              // set the IP address of the AP
 
  // start access point
  WiFi.beginAP(ssid, 11, password, ENC_TYPE_WPA2_PSK);
 
  Serial.print("Access point started");
 
  // Start the server
  server.begin();
  ec.begin();
  dht.begin();
  ph.begin();
 
  pinMode(FLOW_PIN, INPUT);
  pinMode(pinCS, OUTPUT);
 
  attachInterrupt(0, incrementPulseCounter, RISING);
  sei();
 
  for (int i = 3; i < 13; i++) {
    if (i != 8) {
      pinMode(i, OUTPUT);
      togglePin(i);
    }
  }
 
  pinMode(LED_PIN, OUTPUT);
  pinMode(FAN_PIN, OUTPUT);
  pinMode(PUMP_PIN, OUTPUT);
  pinMode(EXTRACTOR_PIN, OUTPUT);
 
  // Initialize additional pins
  pinMode(FLOW_PIN, INPUT);
  pinMode(LIGHT_PIN, INPUT);
  pinMode(EC_PIN, INPUT);
  pinMode(PH_PIN, INPUT);
  pinMode(DHT_PIN, INPUT);
  pinMode(PH_UP_PIN, OUTPUT);
  pinMode(PH_DOWN_PIN, OUTPUT);
  pinMode(EC_UP_PIN, OUTPUT);
  pinMode(EC_DOWN_PIN, OUTPUT);
 
  // turning on equipment that should be on by default
  togglePin(LED_PIN);
  togglePin(FAN_PIN);
  togglePin(PUMP_PIN);
  togglePin(EXTRACTOR_PIN);
 
  Serial.println("Server started");
  timer.every(5000, estimateTemperature);
  timer.every(5000, estimateHumidity);
  timer.every(SIXTEEN_HR, estimateEC);
  timer.every(SIXTEEN_HR, estimatePH);
  timer.every(THREE_HR, SendToSD);
  timer.every(THREE_HR, ECModel);
 
  toggleLightOn();
  pHCalibration();
  ecCalibration();
 
 
}
 
void ECModel(){
    // Get the EC value (already implemented in your code)
  float ecValue = getEC();  // The EC value that will be used as input to predict the pump run time
 
  // Create a feature array with the EC value (assuming only one feature for simplicity)
  float features[1];  // Only one feature (EC value) in this example
  features[0] = ecValue;  // Assign the EC value to the first feature in the array
 
  // Predict the time duration for the pump to run using the LinearRegression model
  float predictedTime = model.predict(features);  // Using the LinearRegression model to predict time
 
  // Print the predicted time to the serial monitor
  Serial.print("Predicted pump run time (ms): ");
  Serial.println(predictedTime);
 
  // Activate the pump for the predicted time
  digitalWrite(PUMP_PIN, HIGH);  // Turn on the pump
  delay(predictedTime);  // Keep the pump running for the predicted time
  digitalWrite(PUMP_PIN, LOW);  // Turn off the pump
}
 
void loop() {
  WiFiEspClient client = server.available();
 
  // Read sensor data
  temperature = dht.readTemperature();
  humidity = dht.readHumidity();
  lightLevel = getLightLevel();
  ecLevel = getEC();
  phLevel = getPH();
  flowRate = getFlowRate();
 
  timer.tick();
 
 String data = "{" + String(phLevel) + "," + String(temperature) +  "," + String(humidity) + "," + String(lightLevel) + "," + String(ecLevel) + "," + String(flowRate) +  " }";
 Serial2.print(data);
 delay(2000); 
          /////////
  
  if (client) {
    handleClient(client);
    buf.init();
    message = "";
    while (client.connected()) {  
      if (client.available()) {   
        char c = client.read();
        buf.push(c);
 
        if (buf.endsWith("\r\n\r\n")) {
          message = "{\n  \"PH\": \"" + String(phLevel) + "\",\n \"Light\": \"" + String(lightLevel) +  "\",\n  \"EC\": \"" + String(ecLevel) + "\",\n  \"FlowRate\": \"" + String(flowRate) + "\",\n  \"Humidity\": \"" + String(humidity) + "\",\n  \"Temperature\": \"" + String(temperature) +  "\"\n }";
          ec.calibration(ecLevel, temperature);
 
          sendHttpResponse(client, message);
          break;
        }
 
        // Device toggling commands
        if (buf.endsWith("/light")) togglePin(LED_PIN);
        if (buf.endsWith("/fan")) togglePin(FAN_PIN);
        if (buf.endsWith("/extract")) togglePin(EXTRACTOR_PIN);
        if (buf.endsWith("/pump")) togglePin(PUMP_PIN);
        if (buf.endsWith("/phUp")) activatePHAdjustment(PH_UP_PIN);
        if (buf.endsWith("/phDown")) activatePHAdjustment(PH_DOWN_PIN);
        if (buf.endsWith("/ecUp")) activateECAdjustment(EC_UP_PIN);
        if (buf.endsWith("/ecDown")) activateECAdjustment(EC_DOWN_PIN);
      }
    }
    Serial.println("Client disconnected");
    client.stop();
  }
 
  if (Serial1.available() > 0) {
    String command = Serial1.readStringUntil('\n');
    command.trim();  // Remove any trailing newlines or spaces
 
    // Handle the received commands
    if (command == "LED_ON") {
      digitalWrite(LED_PIN, HIGH);
      Serial.println("Arduino: LED turned ON");
    } else if (command == "LED_OFF") {
      digitalWrite(LED_PIN, LOW);
      Serial.println("Arduino: LED turned OFF");
    } else if (command == "FAN_ON") {
      digitalWrite(FAN_PIN, HIGH);
      Serial.println("Arduino: Fan turned ON");
    } else if (command == "FAN_OFF") {
      digitalWrite(FAN_PIN, LOW);
      Serial.println("Arduino: Fan turned OFF");
    } else if (command == "PUMP_ON") {
      digitalWrite(PUMP_PIN, HIGH);
      Serial.println("Arduino: Pump turned ON");
    } else if (command == "PUMP_OFF") {
      digitalWrite(PUMP_PIN, LOW);
      Serial.println("Arduino: Pump turned OFF");
    } else if (command == "EXTRACTOR_ON") {
      digitalWrite(EXTRACTOR_PIN, HIGH);
      Serial.println("Arduino: Extractor turned ON");
    } else if (command == "EXTRACTOR_OFF") {
      digitalWrite(EXTRACTOR_PIN, LOW);
      Serial.println("Arduino: Extractor turned OFF");
    } else if (command == "PH_UP") {
      digitalWrite(PH_UP_PIN, HIGH);
      Serial.println("Arduino: pH Up activated");
    } else if (command == "PH_DOWN") {
      digitalWrite(PH_DOWN_PIN, HIGH);
      Serial.println("Arduino: pH Down activated");
    } else if (command == "EC_UP") {
      digitalWrite(EC_UP_PIN, HIGH);
      Serial.println("Arduino: EC Up activated");
    } else if (command == "EC_DOWN") {
      digitalWrite(EC_DOWN_PIN, HIGH);
      Serial.println("Arduino: EC Down activated");
    } else if (command == "GET_DATA") {
      // Simulate sensor readings
      int temp = analogRead(LIGHT_PIN);   // Replace with real sensor logic
      int flow = digitalRead(FLOW_PIN);
      int ec = analogRead(EC_PIN);
      int ph = analogRead(PH_PIN);
      
      // Send sensor data to ESP
      Serial1.print("TEMP:");
      Serial1.println(temp);
      Serial1.print("FLOW:");
      Serial1.println(flow);
      Serial1.print("EC:");
      Serial1.println(ec);
      Serial1.print("PH:");
      Serial1.println(ph);
    }
  }
}
 
void handleSerialCommands(String command) {
  command.trim();
  if (command == "LED_ON") togglePin(LED_PIN, HIGH);
  else if (command == "LED_OFF") togglePin(LED_PIN, LOW);
  else if (command == "FAN_ON") togglePin(FAN_PIN, HIGH);
  else if (command == "FAN_OFF") togglePin(FAN_PIN, LOW);
  else if (command == "PUMP_ON") togglePin(PUMP_PIN, HIGH);
  else if (command == "PUMP_OFF") togglePin(PUMP_PIN, LOW);
  else if (command == "EXTRACTOR_ON") togglePin(EXTRACTOR_PIN, HIGH);
  else if (command == "EXTRACTOR_OFF") togglePin(EXTRACTOR_PIN, LOW);
}
 
void activatePHAdjustment(int pin) {
  digitalWrite(pin, HIGH);
  timer.in(PUMP_INTERVAL, disablePH);
}
 
void activateECAdjustment(int pin) {
  digitalWrite(pin, HIGH);
  timer.in(PUMP_INTERVAL, disableEC);
}
 
  /**
  * Inverts the reading of a pin.
  */
void togglePin(int pin) {
  digitalWrite(pin, !(digitalRead(pin)));
}
 
void togglePin(int pin, int toggleValue) {
  digitalWrite(pin, toggleValue);
}
 
  /**
  * Sends a http response along with a message.
  */
void sendHttpResponse(WiFiEspClient client, String message) {
    client.print(
    "HTTP/1.1 200 OK\r\n"
    "Content-Type: application/json\r\n"
    "Connection: close\r\n");
 
  if (message.length() > 0) {
    client.print("Content-Length:" + String(message.length()) + "\r\n\r\n");
    Serial.print(message.length());
    client.print(message);
  }
}
 
float getLightLevel() {
  return analogRead(LIGHT_PIN);
}
 
float getEC() {
  float ecVoltage = (float)analogRead(EC_PIN)/1024.0*5000.0;
  return ec.readEC(ecVoltage, temperature);
}
 
float getPH() {
  float phVoltage = analogRead(PH_PIN)/1024.0*5000;
  return ph.readPH(phVoltage, temperature);
}
 
void setComponent(int result, int pin, int status){
    if (result == 0) { // Below Optimal
    if (status == 1){
    digitalWrite(pin, LOW);
    //Serial.println("FAN offfffff");
    }
  }
  else if (result == 1) { // Above Optimal
    if (status == 0){
      digitalWrite(pin, HIGH);
      //Serial.println("FAN ON");
    }
  }
  else {
     if (status == 0){ //Optimal
      Serial.println("Component: "+ digitalRead(pin));
      togglePin(pin);
      //Serial.println("COMPONENT OFF!!!!!!!");    
    }
  }
}
 
void handleClient(WiFiEspClient client) {
  String request = "";
  while (client.connected()) {
    if (client.available()) {
      char c = client.read();
      request += c;
 
      if (c == '\n') {
        if (request.startsWith("GET /temperature")) {
          sendResponse(client, "temperature", temperature);
        } else if (request.startsWith("GET /humidity")) {
          sendResponse(client, "humidity", humidity);
        } else if (request.startsWith("GET /ec")) {
          sendResponse(client, "ec", ecValue);
        } else {
          send404(client);
        }
        break;
      }
    }
  }
  client.stop();
}
 
void sendResponse(WiFiEspClient client, String key, float value) {
  String response = "{ \"" + key + "\": " + String(value) + " }";
  client.println("HTTP/1.1 200 OK");
  client.println("Content-Type: application/json");
  client.println("Connection: close");
  client.println("Content-Length: " + String(response.length()));
  client.println();
  client.println(response);
}
 
void send404(WiFiEspClient client) {
  String response = "{ \"error\": \"404 Not Found\" }";
  client.println("HTTP/1.1 404 Not Found");
  client.println("Content-Type: application/json");
  client.println("Connection: close");
  client.println("Content-Length: " + String(response.length()));
  client.println();
  client.println(response);
}
 
void setPump(int result, int pinUp, int pinDown, int statusUp,int statusDown){
    if (result == 0) { //Below Optimal
    if (statusUp == 1 || statusDown == 0){
    digitalWrite(pinUp, LOW);
    digitalWrite(pinDown, HIGH);
    Serial.println("pump up offfffff");
    }
  }
  else if (result == 1) { //Above Optimal
    if (statusUp == 0 || statusDown == 1){
      digitalWrite(pinUp, HIGH);
      digitalWrite(pinDown, LOW);
      Serial.println("pump down on");
    }
  }
  else {
     
      Serial.println("Component: " + digitalRead(pinUp));
      Serial.println("Component: " + digitalRead(pinDown));
      
      togglePin(pinUp, HIGH);
      togglePin(pinDown, HIGH);
      Serial.println("COMPONENTS OFF!");    
    
  }
}
 
void estimateTemperature() {
  if(temperature != NAN){
    int result = ForestTemperature.predict(&temperature);
    int fanStatus = digitalRead(FAN_PIN);
    int lightStatus = digitalRead(LIGHT_PIN);
    Serial.println(result);
 
    setComponent(result, FAN_PIN, fanStatus);
  }
  
}
 
void estimateHumidity() {
  if(humidity != NAN){
    int result = ForestHumidity.predict(&humidity);
    int extractorStatus = digitalRead(EXTRACTOR_PIN);
 
    setComponent(result, EXTRACTOR_PIN, extractorStatus);
  }
}
 
void estimatePH() {
  if(phLevel != NAN){
    int result = ForestPH.predict(&phLevel);
    int phUpStatus = digitalRead(PH_UP_PIN);
    int phDownStatus = digitalRead(PH_DOWN_PIN);
 
    setPump(result, PH_UP_PIN, PH_DOWN_PIN, phUpStatus, phDownStatus);
    timer.in(PUMP_INTERVAL, disablePH);
  }
}
 
void estimateEC() {
  if(ecLevel != NAN){
    int result = ForestEC.predict(&ecLevel);
    int ecUpStatus = digitalRead(EC_UP_PIN);
    int ecDownStatus = digitalRead(EC_DOWN_PIN);
 
    setPump(result, EC_UP_PIN, EC_DOWN_PIN, ecUpStatus, ecDownStatus);
    timer.in(PUMP_INTERVAL, disableEC);
  }
  
}
 
void estimateFactors() {
  estimatePH();
  estimateTemperature();
  estimateHumidity();
  estimateEC();
}
 
void disablePH() {
  digitalWrite(PH_UP_PIN, HIGH);
  digitalWrite(PH_DOWN_PIN, HIGH);
}
 
void disableEC() {
  digitalWrite(EC_UP_PIN, HIGH);
  digitalWrite(EC_DOWN_PIN, HIGH);
  Serial.println("EC HIT");
}
 
void incrementPulseCounter() {
  pulseCount++;
}
 
float getFlowRate() {
  currentTime = millis();
 
  if (currentTime >= (cloopTime + 1000)) {
    cloopTime = currentTime;
    float flowRatePerHr = (pulseCount * 60 / 7.5);
    pulseCount = 0;
    return flowRatePerHr;
  }
}
 
void toggleLightOn() {
  togglePin(LED_PIN, LOW);
  timer.in(EIGHT_HR, toggleLightOff);
}
 
void toggleLightOff() {
  togglePin(LED_PIN, HIGH);
  timer.in(FOUR_HR, toggleLightOn);
}
 
void pHCalibration(){
   static unsigned long timepoint = millis();
    if(millis()-timepoint>1000U){                  //time interval: 1s
        timepoint = millis();
        //temperature = readTemperature();         // read your temperature sensor to execute temperature compensation
        voltage = analogRead(PH_PIN)/1024.0*5000;  // read the voltage
        phValue = ph.readPH(voltage,temp);  // convert voltage to pH with temperature compensation
        Serial.print("temperature:");
        Serial.print(temp,1);
        Serial.print("^C  pH:");
        Serial.println(phValue,2);
    }
    ph.calibration(voltage,temp);
}
 
 
void ecCalibration(){
   static unsigned long timepoint = millis();
    if(millis()-timepoint>1000U)  //time interval: 1s
    {
      timepoint = millis();
      voltage = analogRead(EC_PIN)/1024.0*5000;   // read the voltage
      //temperature = readTemperature();          // read your temperature sensor to execute temperature compensation
      ecValue =  ec.readEC(voltage,temperature);  // convert voltage to EC with temperature compensation
      Serial.print("temperature:");
      Serial.print(temperature,1);
      Serial.print("^C  EC:");
      Serial.print(ecValue,2);
      Serial.println("ms/cm");
    }
    ec.calibration(voltage,temperature);
}
 
 
void SendToSD(){
  if (SD.begin()) {
    Serial.println("SD card is ready to use");
  } else {
    Serial.println("Init failed");
    return;
  }
 
String file = "Readings.txt";
  myFile = SD.open(file, FILE_WRITE);
 
  if (myFile) {
    Serial.println("Writing to file...");
              message = "{\n  \"PH\": \"" + String(phLevel) + "\",\n \"Light\": \"" + String(lightLevel) +  "\",\n  \"EC\": \"" + String(ecLevel) + "\",\n  \"FlowRate\": \"" + String(flowRate) + "\",\n  \"Humidity\": \"" + String(humidity) + "\",\n  \"Temperature\": \"" + String(temperature) +  "\"\n }";
 
    myFile.print(message);
    myFile.close();
    Serial.println("Done");
  } else {
    Serial.println("error opening file");
  }
 
  myFile = SD.open("Readings.txt");
  if (myFile) {
    Serial.println("Read:");
    while (myFile.available()) {
      Serial.write(myFile.read());
    }
 
    myFile.close();
  } else {
    Serial.println("Error opening file");
  }
}
 