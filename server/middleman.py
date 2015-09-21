#!/usr/bin/env python

import socket
import sys

HOST = ""
PORT = 5555

s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
print("Socket Created")

try:
    s.bind((HOST, PORT))
except socket.error as msg:
    print("Bind failed. Error Code: " + str(msg[0]) + " Message: " + msg[1])
    sys.exit()

print("Socket Opened");
s.listen(10)
ip = socket.gethostbyname(socket.gethostname())
print("Listening on " + str(ip) + ":" + str(PORT))
while True:
    conn, addr = s.accept()
    print("Connected with " + str(addr[0]) + " : " + str(addr[1]))
    data = conn.recv(1024)
    if not data: 
        break
    conn.send(data)
    print("Closing connection with " + str(addr[0]) + " : " + str(addr[1]))
    conn.close()

s.close()