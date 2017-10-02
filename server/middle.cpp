/*  
 *  LoRa 868 / 915MHz SX1272 LoRa module
 *  
 *  Copyright (C) Libelium Comunicaciones Distribuidas S.L. 
 *  http://www.libelium.com 
 *  
 *  This program is free software: you can redistribute it and/or modify 
 *  it under the terms of the GNU General Public License as published by 
 *  the Free Software Foundation, either version 3 of the License, or 
 *  (at your option) any later version. 
 *  
 *  This program is distributed in the hope that it will be useful, 
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of 
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License 
 *  along with this program.  If not, see http://www.gnu.org/licenses/. 
 *  
 *  Version:           1.2
 *  Design:            David Gascón 
 *  Implementation:    Covadonga Albiñana, Victor Boria, Ruben Martin
 */
 
// Include the SX1272 and SPI library: 
#include "arduPiLoRa.h"
#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <sys/types.h>
#include <signal.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <string.h>


#define MAXLINE 1024
char *excapechar = "exit\n";

int server_fd, client_fd;
socklen_t clilen;
int num;
char sendline[MAXLINE], rbuf[MAXLINE];
size_t size;
pid_t pid;
struct sockaddr_in client_addr, server_addr;
socklen_t len;
struct sockaddr_in fd_ser, fd_cli;


int e;
char my_packet[100];

void setup()
{
  // Print a start message
  printf("SX1272 module and Raspberry Pi: receive packets without ACK\n");
  
  // Power ON the module
  e = sx1272.ON();
  printf("Setting power ON: state %d\n", e);
  
  // Set transmission mode
  e |= sx1272.setMode(4);
  printf("Setting Mode: state %d\n", e);
  
  // Set header
  e |= sx1272.setHeaderON();
  printf("Setting Header ON: state %d\n", e);
  
  // Select frequency channel
  e |= sx1272.setChannel(CH_10_868);
  printf("Setting Channel: state %d\n", e);
  
  // Set CRC
  e |= sx1272.setCRC_ON();
  printf("Setting CRC ON: state %d\n", e);
  
  // Select output power (Max, High or Low)
  e |= sx1272.setPower('H');
  printf("Setting Power: state %d\n", e);
  
  // Set the node address
  e |= sx1272.setNodeAddress(8);
  printf("Setting Node address: state %d\n", e);
  
  // Print a success message
  if (e == 0)
    printf("SX1272 successfully configured\n");
  else
    printf("SX1272 initialization failed\n");

  delay(1000);
}

void loop(void)
{
  // Receive message
  e = sx1272.receivePacketTimeout(10000);
  if ( e == 0 )
  {
    printf("Receive packet, state %d\n",e);

    for (unsigned int i = 0; i < sx1272.packet_received.length; i++)
    {
      my_packet[i] = (char)sx1272.packet_received.data[i];
    }
    printf("Message: %s\n", my_packet);
  }
  else {
    printf("Receive packet, state %d\n",e);
  }
}


int main(int argc, char *argv[]){

	
	if(argc < 2)
	{
		printf("Usage : %s TCP_PORT\n",argv[0]);
		return -1;
	}

	if((server_fd = socket(PF_INET, SOCK_STREAM, 0)) < 0)
	{
		printf("Server : Can't open stream socket\n");
		return -1;
	}

	setup();

	printf("SOCKET = %d\n",server_fd);

	bzero((char *)&server_addr, sizeof(server_addr));
	bzero((char *)&fd_ser, sizeof(server_addr));
	bzero((char *)&fd_cli, sizeof(server_addr));

	server_addr.sin_family = AF_INET;
	server_addr.sin_addr.s_addr = htonl(INADDR_ANY);
	server_addr.sin_port = htons(atoi(argv[1]));

	if(bind(server_fd, (struct sockaddr *)&server_addr, sizeof(server_addr)) < 0)
	{
		printf("Server : Can't bind local address \n");
		return -1;
	}

	printf("Server started. \nWating for client...");

	listen(server_fd, 1);
	clilen = sizeof(client_addr);
	if((client_fd = accept(server_fd,(struct sockaddr *)&client_addr, &clilen)) != -1)
	{
		printf("Server : client connected\n");
		printf("Accept Socket = %d\n",client_fd);
		printf("Accept IP : %s, Port : %u\n", inet_ntoa(client_addr.sin_addr),(unsigned)ntohs(client_addr.sin_port));

		if(getpeername(server_fd,(struct sockaddr *)&fd_ser, &len) == 0)
		{
			printf("fd_ser IP : %s, Port : %u\n", inet_ntoa(fd_ser.sin_addr),(unsigned)ntohs(fd_ser.sin_port));
		}

		if(getpeername(client_fd,(struct sockaddr *)&fd_cli, &len) == 0)
		{	
			printf("fd_cli IP : %s, Port : %u\n", inet_ntoa(fd_cli.sin_addr),(unsigned)ntohs(fd_cli.sin_port));
		}
	}
		
	else
	{
		printf("Server : failed in accepting.\n");
		return -1;
	}

	if((pid = unistd::fork()) > 0)
	{
		while(1)
		{

			e = sx1272.receivePacketTimeout(10000);
			if ( e == 0 )
			{
			printf("Receive packet, state %d\n",e);
			
			for (unsigned int i = 0; i < sx1272.packet_received.length; i++){
			my_packet[i] = (char)sx1272.packet_received.data[i];
			}
			printf("Message: %s\n", my_packet);
			char exam[10] = "Hello\n";
			unistd::write(client_fd,exam,10);
			}
			else {
			printf("Receive packet, state %d\n",e);
			}
			

			size = strlen(my_packet);
			printf("%d\n",&size);
			printf("%d\n",unistd::write(client_fd, my_packet, size));
			my_packet[size] = '\0';
			if(unistd::write(client_fd, my_packet, size) != size)
			{
				printf("Server : fail in writing\n");
			}
			else{
				printf("%d\n",&size);
			}
			if(strncmp(sendline, excapechar, 4) == 0)
			{
				kill(pid,SIGQUIT);
				break;
			}
		}
	}

	else if(pid == 0)
	{
		while(1)
		{
			if((size = unistd::read(client_fd, rbuf, MAXLINE)) > 0)
			{
				rbuf[size] = '\0';

				if(strncmp(rbuf,excapechar, 4) == 0)
					break;

				printf("%s",rbuf);
			}
		}
	}

	unistd::close(server_fd);
	unistd::close(client_fd);
}
