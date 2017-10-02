#!/usr/bin/python
# -*- coding: utf-8 -*-

import RPi.GPIO as GPIO
import smbus
import math
import time
import sys
import threading
import socket
import os, os.path
from gps import *
from subprocess import call



call(['sudo','gpsd','/dev/ttyS0','-F','/var/run/gpsd.sock'])
# Power management registers
power_mgmt_1 = 0x6b
power_mgmt_2 = 0x6c
 
x_value = 0
y_value = 0
z_value = 0
max_value = 0
gas_value = 0
pir_value = 0
mpu_flag = 0

thread_lock = 0
 
gpsd = None #seting the global variable
 

object_image = ""
gender = ""



def read_byte(adr):
    return bus.read_byte_data(address, adr)
 
def read_word(adr):
    high = bus.read_byte_data(address, adr)
    low = bus.read_byte_data(address, adr+1)
    val = (high << 8) + low
    return val
 
def read_word_2c(adr):
    val = read_word(adr)
    if (val >= 0x8000):
        return -((65535 - val) + 1)
    else:
        return val

bus = smbus.SMBus(1) # or bus = smbus.SMBus(1) for Revision 2 boards
address = 0x68       # This is the address value read via the i2cdetect command

GPIO.setmode(GPIO.BOARD)
GPIO.setup(7, GPIO.IN)
GPIO.setup(40,GPIO.IN)
 
gas = 7 
pir = 26                                          #Associate pin 26 to pir
GPIO.setup(pir, GPIO.IN)                          #Set pin as GPIO in 
 
class pir_thread(threading.Thread):
	def __init__(self, threadID, name, counter):
        	threading.Thread.__init__(self)
        	self.threadID = threadID
        	self.name = name
        	self.counter = counter
	def run(self):
        	print "Starting " + self.name
		while True:				
			global thread_lock
			global gender
			if GPIO.input(40)!=1:                            #Check whether pir is HIGH
				global pir_value			
                                print GPIO.input(40)
				print '!'
				aa = call(['sudo','python','face.py'])
				f = open('t.txt','r')
				face_image = f.read()
				if face_image[0] == 'm' :
                                    gender = "0,"+ face_image[1]
                                else :
                                    gender = "1,"+ face_image[1]
                                print gender
				f.close()
				pir_value = 1
				time.sleep(5)
				print '='
class gas_thread(threading.Thread):
     def __init__(self, threadID, name, counter):
        threading.Thread.__init__(self)
        self.threadID = threadID
        self.name = name
        self.counter = counter
     def run(self):
     	print "Starting " + self.name
	while True:
		if GPIO.input(7)!=1:                            #Check whether pir is HIGH
			global gas_value
			global thread_lock
			gas_value = 1
			time.sleep(0.5)
class mpu_thread(threading.Thread):
     def __init__(self, threadID, name, counter):
        threading.Thread.__init__(self)
        self.threadID = threadID
        self.name = name
        self.counter = counter
     def run(self):
		bus.write_byte_data(address, 0x6b, 0)
        #print "Starting " + self.name
		global mpu_flag
		global x_value
		global y_value
		global z_value
		global max_value
		flag = 0
		while True:
			gyro_xout = read_word_2c(0x43)
			gyro_yout = read_word_2c(0x45)
			gyro_zout = read_word_2c(0x47)
			
			
			if gyro_xout > 15000 : 
				x_value = 1
				flag = 1
			if gyro_xout < -15000 :
				x_value = -1
				flag = 1
			if gyro_yout > 15000 :
				y_value = 1
				flag = 1
			if gyro_yout < -15000:
				y_value = -1
				flag = 1
			if gyro_zout > 15000 :
				z_value = 1
				flag = 1
			if gyro_zout < -15000 :
				z_value = -1
				flag = 1	
			if flag :
				if gyro_xout < 0 :
					gyro_xout = -1 * gyro_xout
				if gyro_yout < 0 :
					gyro_yout = -1 * gyro_yout
				if gyro_zout < 0 :
					gyro_zout = -1 * gyro_zout
				if gyro_xout > gyro_yout :
					if gyro_xout > gyro_zout :
						max_value = gyro_xout
					else :
						max_value = gyro_zout
				else:
					if gyro_yout > gyro_zout :
						max_value = gyro_yout
					else :
						max_value = gyro_zout
								

				print x_value
				print y_value
				print z_value
				global object_image
				print '!'
				aa = call(['sudo','python','object.py'])
				f = open('object.txt','r')
				object_image = f.read()
				print '='
				print object_image
				mpu_flag = 1
				flag = 0
				f.close()
				time.sleep(5)
class GpsPoller(threading.Thread):
  def __init__(self):
    threading.Thread.__init__(self)
    global gpsd #bring it in scope
    gpsd = gps(mode=WATCH_ENABLE) #starting the stream of info
    self.current_value = None
    self.running = True #setting the thread running to true
 
  def run(self):
    global gpsd
    while gpsp.running:
      gpsd.next() #this will continue to loop and grab EACH set of gpsd info to clear the buffer
 				
thread1 = pir_thread(1,"th1",1)
thread2 = gas_thread(2,"th2",2)
thread3 = mpu_thread(3,"th3",3)

thread1.start()
thread2.start()
thread3.start()

client = socket.socket( socket.AF_UNIX, socket.SOCK_STREAM)
client.connect( "/home/pi/cooking/examples/LoRa/server" )
print "Ready."
print "Ctrl-C to quit."
print "Sending 'DONE' shuts down the server and quits."
gpsp = GpsPoller() # create the thread


try:
	gpsp.start() # start it up
	while True:
		accel_data=""
		gas_data=""
		pir_data=""
		lat = int(gpsd.fix.latitude) 
		lat_ = int(( gpsd.fix.latitude - lat ) * 10000)
		lon = int(gpsd.fix.longitude)
		lon_ = int((gpsd.fix.longitude - lon ) * 10000)
		

		if mpu_flag:
			#accel_data = "1234567890123456789012345678901234567890123456789012345678901234567890" + '\n'
			accel_data = "ACC," + str(x_value) + "," + str(y_value) + "," + str(z_value) + "," + str(max_value) + "," + str(lat) + "," + str(lat_) + "," + str(lon) + "," + str(lon_) + "," + object_image  + '\n'
			print accel_data
			client.send(accel_data)
			x_value = 0
			y_value = 0
			z_value = 0
			max_value = 0
			mpu_flag = 0
		if gas_value :
			gas_data = "GAS,"+ str(lat) + "," + str(lat_) + "," + str(lon) + "," + str(lon_) +  '\n'
			print gas_data
			client.send(gas_data)
			gas_value = 0
		if pir_value :
			pir_data = "PIR"+"," + str(lat) + "," + str(lat_) + "," + str(lon) + "," + str(lon_) + ","  + gender +'\n'
			print pir_data
			client.send(pir_data)
			pir_value = 0
			



		
except KeyboardInterrupt:
	GPIO.cleanup()