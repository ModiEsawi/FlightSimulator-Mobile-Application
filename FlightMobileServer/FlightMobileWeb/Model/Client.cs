using System;
using System.Net.Sockets;
using System.Text;

namespace FlightGearServer.Model
{
    /*
     * Client class, manage basic TCP Socket
     */
    public class Client
    {
        private TcpClient client = null;
        public NetworkStream stream = null;

        //connect to server , if success : return true , false otherwise . 
        public bool Connect(string ip, int port)
        {
            try
            {
                this.client = new TcpClient(ip, port);
                this.stream = client.GetStream();
                return true;
            }
            catch (Exception) { return false; }
        }

        //return true if server connected , false otherwise
        public bool IsConnected()
        {
            if (this.client != null)
            {
                return this.client.Connected;
            }
            return false;
        }

        //close the socket
        public void Disconnect()
        {
            this.client.Close();
            this.stream.Close();
        }

        //send a message to the server
        public bool SendMessage(string message)
        {
            byte[] bytesToSend = ASCIIEncoding.ASCII.GetBytes(message);
            if (!IsConnected())
                return false;
            try
            {
                stream.Write(bytesToSend, 0, bytesToSend.Length);
            }
            catch (Exception)
            {
                return false;
            }
            return true;
        }

        //send a message to the server, get response .
        public string SendMessageGetResponse(string message)
        {
            SendMessage(message);
            Byte[] data = new Byte[1024];
            Int32 bytes = this.stream.Read(data, 0, data.Length); ;
            return System.Text.Encoding.ASCII.GetString(data, 0, bytes);
        }
    }
}
