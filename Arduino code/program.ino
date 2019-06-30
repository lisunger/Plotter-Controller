#import<SoftwareSerial.h>
#import<Stepper.h>
#import<Servo.h>

byte START_WORD[4] = {'w', 'o', 'r', 'd'};

#define COMMAND_UP 1
#define COMMAND_DOWN 2
#define COMMAND_LEFT 3
#define COMMAND_RIGHT 4
#define COMMAND_DOT 5
#define COMMAND_STOP 6
#define DELAY_NEMA 500

#define STEPS_NEMA 10
#define STEPS_BYJ 27

int STEPS_PER_REVOLUTION = 2048;
Stepper myStepper(STEPS_PER_REVOLUTION, 6, 8, 7, 9);
Servo pen;

// bluetooth connection
SoftwareSerial bluetooth(5, 4); // RX, TX

// Pins
int DIR = 11;
int STEP = 12;
int upStopper = A0;
int downStopper = A1;
int leftStopper = A2;
int rightStopper = A3;

// Bluetooth data
byte command;
unsigned int value;
unsigned long commandIndex;

void setup() {
  pinMode(DIR, OUTPUT);
  pinMode(STEP, OUTPUT);
  pinMode(upStopper, INPUT);
  pinMode(downStopper, INPUT);
  pinMode(leftStopper, INPUT);
  pinMode(rightStopper, INPUT);
  pinMode(3, OUTPUT);

  myStepper.setSpeed(16);
  Serial.begin(31250);
  bluetooth.begin(9600);
  pen.attach(3);
  pen.write(90);
}

void loop() {
  while (Serial.available()) Serial.read();
  readCommand();
  executeCommand();
}

void up(int steps) {
  steps *= STEPS_BYJ;
  for (int i = 0; i < steps / 20; i++) {
    if (digitalRead(upStopper) == HIGH) { // Stop triggered
      turnOffByj();
      return;
    } else {
      myStepper.step(-20);
    }
  }
  if (digitalRead(upStopper) == HIGH) { // Stop triggered
    turnOffByj();
    return;
  } else {
    myStepper.step(-(steps % 20));
    turnOffByj();
  }
}

void down(int steps) {
  steps *= STEPS_BYJ;
  for (int i = 0; i < steps / 20; i++) {
    if (digitalRead(downStopper) == HIGH) { // Stop triggered
      turnOffByj();
      return;
    } else {
      myStepper.step(20);
    }
  }
  if (digitalRead(downStopper) == HIGH) { // Stop triggered
    turnOffByj();
    return;
  } else {
    myStepper.step(steps % 20);
    turnOffByj();
  }
}

void turnOffByj() {
  digitalWrite(6, LOW);
  digitalWrite(7, LOW);
  digitalWrite(8, LOW);
  digitalWrite(9, LOW);
}

void right(int steps) {
  steps *= STEPS_NEMA;
  digitalWrite(DIR, HIGH);
  for (int i = 0; i < steps / 5; i++) {
    if (digitalRead(rightStopper) == HIGH) { // Stop triggered
      return;
    } else {
      for (int j = 0; j < 5; j++) {
        digitalWrite(STEP, HIGH);
        delayMicroseconds(DELAY_NEMA);
        digitalWrite(STEP, LOW);
        delayMicroseconds(DELAY_NEMA);
      }
    }
  }

  if (digitalRead(rightStopper) == HIGH) { // Stop triggered
    return;
  } else {
    for (int j = 0; j < steps % 5; j++) {
      digitalWrite(STEP, HIGH);
      delayMicroseconds(DELAY_NEMA);
      digitalWrite(STEP, LOW);
      delayMicroseconds(DELAY_NEMA);
    }
  }
}

void left(int steps) {
  steps *= STEPS_NEMA;
  digitalWrite(DIR, LOW);
  for (int i = 0; i < steps / 5; i++) {
    if (digitalRead(leftStopper) == HIGH) { // Stop triggered
      return;
    } else {
      for (int j = 0; j < 5; j++) {
        digitalWrite(STEP, HIGH);
        delayMicroseconds(DELAY_NEMA);
        digitalWrite(STEP, LOW);
        delayMicroseconds(DELAY_NEMA);
      }
    }
  }

  if (digitalRead(leftStopper) == HIGH) { // Stop triggered
    return;
  } else {
    for (int j = 0; j < steps % 5; j++) {
      digitalWrite(STEP, HIGH);
      delayMicroseconds(DELAY_NEMA);
      digitalWrite(STEP, LOW);
      delayMicroseconds(DELAY_NEMA);
    }
  }
}

void readCommand() {

  // read bytes until start code word is found (4 bytes START_WORD)
  byte start[4];
  for (; ;) {
    while (bluetooth.available() < 1) { }
    start[0] = bluetooth.read();
    if (check(start, 4)) break;
    shiftArray(start, 4);
  }

  // wait until full command available
  while (bluetooth.available() < 7) { };
  byte instruction[7];
  bluetooth.readBytes(instruction, 7);
  command = instruction[0];
  value = (instruction[1] << 8) | instruction[2];
  commandIndex = 0L;
  // cast to long, because otherwise the compiler treats it as 16-bit and loses the bits when shifting
  commandIndex |= (((long)instruction[3]) << 24);
  commandIndex |= (((long)instruction[4]) << 16);
  commandIndex |= (instruction[5] << 8);
  commandIndex |= instruction[6];
}

void writeConfirm() {
  Serial.println(commandIndex);
  bluetooth.write((commandIndex >> 24) & 0b11111111);
  bluetooth.write((commandIndex >> 16) & 0b11111111);
  bluetooth.write((commandIndex >> 8) & 0b11111111);
  bluetooth.write((commandIndex) & 0b11111111);
}

void dot() {
  int endStep = 35;
  pen.write(endStep);
  delay(250);
  pen.write(85);
  delay(200);
}

bool check(byte* data, int len) {
  return (data[3] == START_WORD[0]
        && data[2] == START_WORD[1]
        && data[1] == START_WORD[2]
        && data[0] == START_WORD[3]);
}

void executeCommand() {
  switch (command) {
    case COMMAND_UP : {
        up(value);
        writeConfirm();
        break;
      }
    case COMMAND_DOWN : {
        down(value);
        writeConfirm();
        break;
      }
    case COMMAND_LEFT : {
        left(value);
        writeConfirm();
        break;
      }
    case COMMAND_RIGHT : {
        right(value);
        writeConfirm();
        break;
      }
    case COMMAND_DOT : {
        dot();
        writeConfirm();
        break;
      }
    case COMMAND_STOP : {
        turnOffByj();
        writeConfirm();
        break;
      }
  }
}

void shiftArray(byte* data, int len) {
  byte temp = data[len - 1];
  for (int i = len; i > 0; i--) {
    data[i] = data[i - 1];
  }
  data[0] = temp;
}
