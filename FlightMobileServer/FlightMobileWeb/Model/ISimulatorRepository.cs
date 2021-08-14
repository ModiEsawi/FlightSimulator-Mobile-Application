using FlightGearServer.Model.Entities;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace FlightGearServer.Model
{

    /*Manage simulator connection*/
    public interface ISimulatorRepository
    {
        //send a command to simulator , if success : return true , false otherwise.
        bool SendCommand(Command command);

        //return true if simulator connected , false otherwise.
        bool IsConnected();

        //connect to simulator , return true if success , false otherwise.
        bool TryConnect(string ip, int port);
    }
}
