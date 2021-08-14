using FlightGearServer.Model.Entities;
using Microsoft.AspNetCore.Http;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;

namespace FlightGearServer.Model
{
    /*Manage simulator connection*/
    public class SimulatorRepository : ISimulatorRepository
    {
        readonly Client client = null; 
        readonly Dictionary<string, string> varMap;
        private static Mutex mutex;
        private readonly string getMessage; //to optimize

        public SimulatorRepository()
        {
            client = new Client();
            varMap = new Dictionary<string, string>();
            varMap["aileron"] = "/controls/flight/aileron";
            varMap["rudder"] = "/controls/flight/rudder";
            varMap["elevator"] = "/controls/flight/elevator";
            varMap["throttle"] = "/controls/engines/current-engine/throttle";
            getMessage = "get " + varMap["aileron"] + "\r\n"; 
            getMessage += "get " + varMap["elevator"] + "\r\n";
            getMessage += "get " + varMap["rudder"] + "\r\n";
            getMessage += "get " + varMap["throttle"] + "\r\n";
        }

        //send a command to simulator , if success : return true , false otherwise.
        public bool SendCommand(Command command)
        {
            if (!IsConnected())
                return false;
            try
            {
                mutex.WaitOne();
                //set message , for all 4 variables
                string message = "set " + varMap["aileron"] + " " + command.Aileron.ToString() + "\r\n";
                message += "set " + varMap["elevator"] + " " + command.Elevator.ToString() + "\r\n";
                message += "set " + varMap["rudder"] + " " + command.Rudder.ToString() + "\r\n";
                message += "set " + varMap["throttle"] + " " + command.Throttle.ToString() + "\r\n";
                client.SendMessage(message); 
                // send "get" message
                string response = client.SendMessageGetResponse(getMessage);
                char[] operators = { '\n', '\r' };
                var values = new List<string>(); 
                foreach (string v in response.Split(operators).ToList())
                {
                    if (v != "") //split result 
                        values.Add(v);
                }
                //validiation : variables updated in simulator
                if (Double.Parse(values[0]) == command.Aileron
                    && Double.Parse(values[1]) == command.Elevator
                    && Double.Parse(values[2]) == command.Rudder
                    && Double.Parse(values[3]) == command.Throttle)
                {
                    mutex.ReleaseMutex();
                    return true;
                }
                else
                {
                    mutex.ReleaseMutex();
                    return false;
                }
            }
            catch (Exception) //in parsing the values 
            {
                mutex.ReleaseMutex();
                return false;
            }
        }

        //return true if simulator connected , false otherwise.
        public bool IsConnected()
        {
            return client.IsConnected();
        }

        //connect to simulator , return true if success , false otherwise.
        public bool TryConnect(string ip, int port)
        {
            bool connected =  client.Connect(ip, port);
            if (!connected)
                return false;
            mutex = new Mutex();
            client.SendMessage("data\r\n"); //opening message 
            return true;
        }

        //destructor
        ~SimulatorRepository()
        {
            if (client != null)
                client.Disconnect();
        }
    }
}
