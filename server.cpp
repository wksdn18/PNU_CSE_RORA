#include <sys/types.h> 
#include <sys/stat.h> 
#include <sys/socket.h> 
#include <sys/un.h> 
#include <unistd.h> 
#include <stdio.h> 
#include <stdlib.h> 
#include <string.h> 
#include "arduPiLoRa.h"

int e;

#define MAXLINE 1024
#define  FILE_SERVER "/home/pi/cooking/examples/LoRa/server"

void setup()
{
  // Print a start message
  printf("SX1272 module and Raspberry Pi: send packets without ACK\n");
  
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
  e |= sx1272.setNodeAddress(3);
  printf("Setting Node address: state %d\n", e);
  
  // Print a success message
  if (e == 0)
    printf("SX1272 successfully configured\n");
  else
    printf("SX1272 initialization failed\n");

  delay(1000);
}

int main(int argc, char **argv)
{
	setup();
    int server_sockfd, client_sockfd;
    int state;
	socklen_t client_len;
    pid_t pid;

    FILE *fp;
    struct sockaddr_un clientaddr, serveraddr;

    char buf[MAXLINE];

    // internet 기반의 스트림 소켓을 만들도록 한다. 
    client_len = sizeof(clientaddr);
    if ((server_sockfd = socket(AF_UNIX, SOCK_STREAM, 0)) < 0)
    {
        perror("socket error : ");
        exit(0);
    }
    bzero(&serveraddr, sizeof(serveraddr));
    serveraddr.sun_family = AF_UNIX;
    strcpy(serveraddr.sun_path, FILE_SERVER);

    state = bind(server_sockfd , (struct sockaddr *)&serveraddr, 
            sizeof(serveraddr));
    if (state == -1)
    {
        perror("bind error : ");
        exit(0);
    }

    state = listen(server_sockfd, 5);
    if (state == -1)
    {
        perror("listen error : ");
        exit(0);
    }

    while(1)
    {
        client_sockfd = accept(server_sockfd, (struct sockaddr *)&clientaddr, &client_len);
        pid = fork();
        if (pid == 0)
        {
            if (client_sockfd == -1)
            {
                perror("Accept error : ");
                exit(0);
            }
            while(1)
            {
                memset(buf, 0x00, MAXLINE);
                if (read(client_sockfd, buf, MAXLINE) <= 0)
                {
                    close(client_sockfd);
                    exit(0);
                }
                write(client_sockfd, buf, strlen(buf));
   				e = sx1272.sendPacketTimeout(8, buf);
    			printf("Packet sent, state %d\n",e);
    			delay(4000);
            }
        }
    }
    close(client_sockfd);
}