#! /usr/bin/python

import threading
import signal
import sys
import os
import traceback
from time import sleep
from importlib import import_module

class RemoteControllableRapp:

	objects_dict = {}

	work_thread = None

	PYTHON_DIRECT_TOKEN = '[Crispico Direct Message]'

	PYTHON_EVENT_TOKEN = '[Crispico Event Message]'
	
	PYTHON_OPTIONAL_TOKEN = '[Crispico Optional Message]'
	
	PYTHON_EXCEPTION_TOKEN = '[Crispico Exception Message]'

	SLEEP_TIME = 1
	
	lock = threading.Lock()
	
	def __init__(self, name):
		self.name = name

	@staticmethod
	def serialize(event):
		return event.toString()

	@staticmethod
	def sendEvent(objectName, eventHandlerField, serializedEvent):
		RemoteControllableRapp.lock.acquire()
		sys.stdout.write(RemoteControllableRapp.PYTHON_EVENT_TOKEN + objectName + "|" + eventHandlerField + "|" + serializedEvent + "\n")
		sys.stdout.flush()
		RemoteControllableRapp.lock.release()

	def create(self, *arguments):  # objectName, CanonicClassPath, arguments
		if len(arguments) < 2:
			raise Exception("argument count must be greater or equal to 2")

		listPath = arguments[1].rsplit('.', 1)
		if len(listPath) != 1:
			moduleName = import_module(listPath[0])
			className = listPath[1]
		else:
			moduleName = sys.modules[__name__]
			className = arguments[1]
		if len(arguments) == 2:
			self.objects_dict[arguments[0]] = getattr(moduleName, className)()
		else:
			typeAndValueArgs = arguments[2:]
			valueArgs = []
			if len(typeAndValueArgs) % 2 == 1:
				raise Exception("argument count must be divisible by 2")
			for i in range(len(typeAndValueArgs) / 2):
				value = typeAndValueArgs[2 * i + 1]
				typeOptions = {	'str': str(value),
								'int' : int(value),
								'bool': value == 'True' or value == 'true',
								'long': long(value),
								'float': float(value)
				}
				if not typeAndValueArgs[2 * i] in typeOptions:
					raise Exception(typeAndValueArgs[2 * i] + " does not exist as a primitive; try str, int, bool, long or float")
				valueArgs.append(typeOptions[typeAndValueArgs[2 * i]])
			self.objects_dict[arguments[0]] = getattr(moduleName, className)(*valueArgs)
		return RemoteControllableRapp.PYTHON_OPTIONAL_TOKEN + arguments[0] + ' of type ' + arguments[1] + ' was created'

	def addEventListener(self, *arguments):  # objectName, eventHandlerField
		if len(arguments) != 2:
			raise Exception("argument count must be equal to 2")
		obj = self.objects_dict[arguments[0]]
		if not hasattr(obj, arguments[1]):
			raise Exception("this object has no field called " + arguments[1])
		def handler(event):
			serializedEvent = self.serialize(event)
			self.sendEvent(arguments[0], arguments[1], serializedEvent)
		setattr(obj, arguments[1], handler)
		return RemoteControllableRapp.PYTHON_OPTIONAL_TOKEN + 'Event listener added for ' + arguments[0] + ' for field ' + arguments[1]
	
	def beginLoop(self):
		if self.work_thread is None:
			self.work_thread = threading.Thread(target=RemoteControllableRapp.process_events_loop, args=(self,))
			self.work_thread.start()
			return RemoteControllableRapp.PYTHON_OPTIONAL_TOKEN + 'Python event loop started'
		return RemoteControllableRapp.PYTHON_OPTIONAL_TOKEN + 'Python event loop was already started, will not start a new one'
	
	def signal_handler(self, signal, frame):
		if self.work_thread is not None:
			self.work_thread.do_run = False
			self.work_thread.join()
		sys.exit(0)

	def process_input_loop(self):
		while True:
			s = sys.stdin.readline().strip()
			self.process_input(s)

	def process_input(self, linein):
		commandLine = linein.split("|")
		if len(commandLine) < 2 or len(commandLine) > 3:
			RemoteControllableRapp.lock.acquire()
			sys.stdout.write(RemoteControllableRapp.PYTHON_DIRECT_TOKEN + \
				'format: myClass|myFunc|arg1,arg2,arg3,...,argn or myClass|myFunc or myClass|myFunc|\n')
			sys.stdout.flush()
			RemoteControllableRapp.lock.release()
			return

		argumentsLine = []

		if len(commandLine) == 3:
			commandLineSize = len(commandLine[2])
			argument = ""
			isEscaped = False

			for pos, letter in enumerate(commandLine[2]):
				if (pos < commandLineSize - 1) and (letter == '\\') and (commandLine[2][pos + 1] == ','):
					isEscaped = True
					continue

				if letter == ',':
					if isEscaped:
						isEscaped = False
						argument = argument + letter
					else:
						argumentsLine.append(argument)
						argument = ""
				else:
					argument = argument + letter
			# happens for last argument
			if argument != "":
				argumentsLine.append(argument)

		if commandLine[0] == self.name:
			result = getattr(self, commandLine[1])(*argumentsLine)
		else:
			valueArgs = []
			if len(argumentsLine) % 2 == 1:
				raise Exception("argument count must be divisible by 2")
			for i in range(len(argumentsLine) / 2):
				value = argumentsLine[2 * i + 1]
				typeOptions = {	'str': str(value),
								'int' : int(value),
								'bool': value == 'True' or value == 'true',
								'long': long(value),
								'float': float(value)
				}
				if not argumentsLine[2 * i] in typeOptions:
					raise Exception(typeAndValueArgs[2 * i] + " does not exist as a primitive; try str, int, bool, long or float")
				valueArgs.append(typeOptions[argumentsLine[2 * i]])
			result = getattr(self.objects_dict[commandLine[0]], commandLine[1])(*valueArgs)
		RemoteControllableRapp.lock.acquire()
		if result is not None:
			if str(result).startswith(RemoteControllableRapp.PYTHON_OPTIONAL_TOKEN):
				sys.stdout.write(RemoteControllableRapp.PYTHON_DIRECT_TOKEN + str(result).replace("\n", "\\n") + '\n')
			else:
				sys.stdout.write(RemoteControllableRapp.PYTHON_DIRECT_TOKEN + commandLine[0] + '|' + commandLine[1] + '|' + str(result).replace("\n", "\\n") + '\n')
		else:
			sys.stdout.write(RemoteControllableRapp.PYTHON_DIRECT_TOKEN + commandLine[0] + '|' + commandLine[1] + '|' + 'Empty message returned\n')
		sys.stdout.flush()
		RemoteControllableRapp.lock.release()
	
	@staticmethod
	def process_events_loop(remoteObject):
		try:
			for value in remoteObject.objects_dict.itervalues():
				if 'setup' in dir(value):
					value.setup()
			loopDict = {}
			for value in remoteObject.objects_dict.itervalues():
				if 'loop' in dir(value):
					loopDict[value] = True
			while getattr(threading.currentThread(), 'do_run', True):
				for value in remoteObject.objects_dict.itervalues():
					if value in loopDict:
						value.loop()
			 	sleep(remoteObject.SLEEP_TIME)
		except Exception as e:
			escapedException = traceback.format_exc().replace("\n", "\\n")
			RemoteControllableRapp.lock.acquire()
			sys.stdout.write(RemoteControllableRapp.PYTHON_EXCEPTION_TOKEN + escapedException + "\n")
			sys.stdout.flush()
			RemoteControllableRapp.lock.release()

if __name__ == '__main__':
	remoteControllableRapp = RemoteControllableRapp('remoteControllableRapp')
	signal.signal(signal.SIGINT, remoteControllableRapp.signal_handler)
	try:
		remoteControllableRapp.process_input_loop()
	except Exception as e:
		escapedException = traceback.format_exc().replace("\n", "\\n")
		RemoteControllableRapp.lock.acquire()
		sys.stdout.write(RemoteControllableRapp.PYTHON_EXCEPTION_TOKEN + escapedException + "\n")
		sys.stdout.flush()
		RemoteControllableRapp.lock.release()
